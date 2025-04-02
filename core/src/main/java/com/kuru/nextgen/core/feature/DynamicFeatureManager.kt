package com.kuru.nextgen.core.feature


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
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

class DynamicFeatureManager private constructor(
    application: Application
) : ModuleManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val manager = SplitInstallManagerFactory.create(application)
    private val states = mutableMapOf<String, MutableStateFlow<ModuleState>>()
    private val mutex = Mutex()
    private var currentActivity: Activity? = null
    private val prefs: SharedPreferences? =
        currentActivity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val listener = SplitInstallStateUpdatedListener { state ->
        val moduleName =
            state.moduleNames().firstOrNull() ?: return@SplitInstallStateUpdatedListener
        val stateFlow = states[moduleName] ?: return@SplitInstallStateUpdatedListener

        stateFlow.value = when (state.status()) {
            SplitInstallSessionStatus.DOWNLOADING -> {
                Log.d(TAG, "📱  SplitInstallSessionStatus.DOWNLOADING ")
                val progress = state.bytesDownloaded().toFloat() / state.totalBytesToDownload()
                ModuleState.Downloading(progress)
            }

            SplitInstallSessionStatus.INSTALLED -> {
                Log.d(TAG, "📱  SplitInstallSessionStatus.INSTALLED ")
                loadPlantsFeature()
                prefs?.edit()?.putBoolean("isFeatureInitialized", true)?.apply()
                ModuleState.Installed
            }

            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                Log.d(TAG, "📱  SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION ")
                ModuleState.ConfirmationRequired {
                    currentActivity?.startIntentSender(
                        state.resolutionIntent()?.intentSender,
                        null, 0, 0, 0
                    )
                }
            }

            SplitInstallSessionStatus.FAILED -> {
                Log.e(TAG, "📱  SplitInstallSessionStatus.FAILED ")
                ModuleState.Error("Installation failed")
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

    override fun getModuleStateV1(moduleName: String): Flow<ModuleState> {
        Log.e(TAG, "📱  getModuleStateV1 moduleName = $moduleName ")
        return states.getOrPut(moduleName) {
            MutableStateFlow(
                if (isModuleInstalled(moduleName)) ModuleState.Installed
                else ModuleState.NotInstalled
            )
        }
    }

    override suspend fun loadModule(moduleName: String) {
        Log.d(TAG, "📱  loadModule moduleName = $moduleName ")
        mutex.withLock {
            if (isModuleInstalled(moduleName)) return

            val stateFlow = states.getOrPut(moduleName) {
                MutableStateFlow(ModuleState.NotInstalled)
            }

            stateFlow.value = ModuleState.Loading
            Log.d(TAG, "📱  loadModule isModuleInstalled = false ")
            try {
                val request = SplitInstallRequest.newBuilder()
                    .addModule(moduleName)
                    .build()
                Log.d(TAG, "📱  loadModule manager.startInstall(request)")
                manager.startInstall(request)
            } catch (e: Exception) {
                Log.e(TAG, "📱  loadModule Exception $e")
                stateFlow.value = ModuleState.Error(e.message ?: "Installation failed")
            }
        }
    }

    override fun retryModuleLoad(moduleName: String) {
        Log.d(TAG, "📱  retryModuleLoad =  $moduleName")
        scope.launch { loadModule(moduleName) }
    }

    override fun cleanup() {
        Log.d(TAG, "📱  cleanup unregisterListener ")
        scope.cancel()
        manager.unregisterListener(listener)
    }

    override fun setActivity(activity: Activity?) {
        currentActivity = activity
    }

    private fun loadPlantsFeature() {
        try {
            val clazz = Class.forName("com.kuru.nextgen.plants.PlantsFeature")
            // Accessing the class will trigger the init block
            val instance =
                clazz.getDeclaredField("INSTANCE").get(null) // If PlantsFeature is an object
            Log.d(TAG, "PlantsFeature loadPlantsFeature successfully")
        } catch (e: Exception) {
            Log.d(TAG, "Failed to load PlantsFeature: ${e.message}", e)
        }

        FeatureRegistry.initializeAll()

    }

    companion object {
        private const val TAG = "DynamicFeatureManager"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: DynamicFeatureManager? = null

        fun getInstance(application: Application): DynamicFeatureManager {
            Log.d(TAG, "📱 Getting DynamicFeatureManager instance")
            return instance ?: synchronized(this) {
                instance ?: DynamicFeatureManager(application).also { instance = it }
            }
        }
    }
}

interface ModuleManager {
    fun isModuleInstalled(moduleName: String): Boolean
    fun getModuleStateV1(moduleName: String): Flow<ModuleState>
    suspend fun loadModule(moduleName: String)
    fun retryModuleLoad(moduleName: String)
    fun cleanup()
    fun setActivity(activity: Activity?)
}

sealed class ModuleState {
    data object NotInstalled : ModuleState()
    data object Loading : ModuleState()
    data class Downloading(val progress: Float) : ModuleState()
    data object Installed : ModuleState()
    data class ConfirmationRequired(val confirm: () -> Unit) : ModuleState()
    data class Error(val message: String) : ModuleState()
}

