package com.kuru.nextgen.core.feature


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.util.Log
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DynamicFeatureManagerV1 private constructor(
    private val application: Application
) : ModuleManagerV1 {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val manager = SplitInstallManagerFactory.create(application)
    private val states = mutableMapOf<String, MutableStateFlow<ModuleStateV1>>()
    private val mutex = Mutex()
    private var currentActivity: Activity? = null

    private val listener = SplitInstallStateUpdatedListener { state ->
        val moduleName =
            state.moduleNames().firstOrNull() ?: return@SplitInstallStateUpdatedListener
        val stateFlow = states[moduleName] ?: return@SplitInstallStateUpdatedListener

        stateFlow.value = when (state.status()) {
            SplitInstallSessionStatus.DOWNLOADING -> {
                Log.e(TAG, "📱  SplitInstallSessionStatus.DOWNLOADING ")
                val progress = state.bytesDownloaded().toFloat() / state.totalBytesToDownload()
                ModuleStateV1.Downloading(progress)
            }

            SplitInstallSessionStatus.INSTALLED -> ModuleStateV1.Installed
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                Log.e(TAG, "📱  SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION ")
                ModuleStateV1.ConfirmationRequired {
                    currentActivity?.startIntentSender(
                        state.resolutionIntent()?.intentSender,
                        null, 0, 0, 0
                    )
                }
            }

            SplitInstallSessionStatus.FAILED -> {
                Log.e(TAG, "📱  SplitInstallSessionStatus.FAILED ")
                ModuleStateV1.Error("Installation failed")
            }

            else -> stateFlow.value // Keep current state for other statuses
        }
    }

    init {
        manager.registerListener(listener)
    }

    override fun isModuleInstalled(moduleName: String): Boolean {
        return manager.installedModules.contains(moduleName)
    }

    override fun getModuleStateV1(moduleName: String): Flow<ModuleStateV1> {
        Log.e(TAG, "📱  getModuleStateV1 moduleName = $moduleName ")
        return states.getOrPut(moduleName) {
            MutableStateFlow(
                if (isModuleInstalled(moduleName)) ModuleStateV1.Installed
                else ModuleStateV1.NotInstalled
            )
        }
    }

    override suspend fun loadModule(moduleName: String) {
        Log.e(TAG, "📱  loadModule moduleName = $moduleName ")
        mutex.withLock {
            if (isModuleInstalled(moduleName)) return

            val stateFlow = states.getOrPut(moduleName) {
                MutableStateFlow(ModuleStateV1.NotInstalled)
            }

            stateFlow.value = ModuleStateV1.Loading
            Log.e(TAG, "📱  loadModule isModuleInstalled = false ")
            try {
                val request = SplitInstallRequest.newBuilder()
                    .addModule(moduleName)
                    .build()
                Log.e(TAG, "📱  loadModule manager.startInstall(request)")
                manager.startInstall(request)
            } catch (e: Exception) {
                Log.e(TAG, "📱  loadModule Exception $e")
                stateFlow.value = ModuleStateV1.Error(e.message ?: "Installation failed")
            }
        }
    }

    override fun retryModuleLoad(moduleName: String) {
        Log.e(TAG, "📱  retryModuleLoad ")
        scope.launch { loadModule(moduleName) }
    }

    override fun cleanup() {
        Log.e(TAG, "📱  cleanup ")
        scope.cancel()
        manager.unregisterListener(listener)
    }

    override fun setActivity(activity: Activity?) {
        currentActivity = activity
    }

    companion object {
        private const val TAG = "DynamicFeatureManagerV1"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: DynamicFeatureManagerV1? = null

        fun getInstance(application: Application): DynamicFeatureManagerV1 {
            Log.e(TAG, "📱 Getting DynamicFeatureManager instance")
            return instance ?: synchronized(this) {
                instance ?: DynamicFeatureManagerV1(application).also { instance = it }
            }
        }
    }
}

interface ModuleManagerV1 {
    fun isModuleInstalled(moduleName: String): Boolean
    fun getModuleStateV1(moduleName: String): Flow<ModuleStateV1>
    suspend fun loadModule(moduleName: String)
    fun retryModuleLoad(moduleName: String)
    fun cleanup()
    fun setActivity(activity: Activity?)
}

sealed class ModuleStateV1 {
    data object NotInstalled : ModuleStateV1()
    data object Loading : ModuleStateV1()
    data class Downloading(val progress: Float) : ModuleStateV1()
    data object Installed : ModuleStateV1()
    data class ConfirmationRequired(val confirm: () -> Unit) : ModuleStateV1()
    data class Error(val message: String) : ModuleStateV1()
}