package com.kuru.nextgen.core.feature

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.splitinstall.SplitInstallManager
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


internal class DynamicFeatureManager private constructor(
    private val application: Application
) : ModuleManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val splitInstallManager: SplitInstallManager = SplitInstallManagerFactory.create(application)
    private val moduleStates = mutableMapOf<String, MutableStateFlow<InstallationState>>()
    private val mutex = Mutex()
    private val sessionMap = mutableMapOf<Int, String>()
    private var currentActivity: Activity? = null
    private var confirmationLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    private val listener = SplitInstallStateUpdatedListener { state ->
        Log.d(TAG, "🔄 Installation state updated: status=${state.status()}, sessionId=${state.sessionId()}")
        Log.d(TAG, "📍 Current session map: $sessionMap")

        val sessionId = state.sessionId()
        val moduleName = sessionMap[sessionId]

        if (moduleName == null) {
            Log.w(TAG, "⚠️ Received update for unknown session $sessionId")
            return@SplitInstallStateUpdatedListener
        }

        val stateFlow = moduleStates[moduleName]
        if (stateFlow == null) {
            Log.w(TAG, "⚠️ No state flow found for module $moduleName")
            return@SplitInstallStateUpdatedListener
        }

        Log.d(TAG, "📱 Processing state update for module: $moduleName")
        when (state.status()) {
            SplitInstallSessionStatus.DOWNLOADING -> {
                val progress = state.bytesDownloaded().toFloat() / state.totalBytesToDownload()
                Log.d(TAG, "⬇️ Downloading module $moduleName: ${(progress * 100).toInt()}%")
                stateFlow.value = InstallationState.Downloading(progress)
            }
            SplitInstallSessionStatus.INSTALLED -> {
                Log.d(TAG, "✅ Module $moduleName installed successfully")
                stateFlow.value = InstallationState.Installed
                cleanupSession(sessionId)
            }
            SplitInstallSessionStatus.FAILED -> {
                Log.e(TAG, "❌ Installation failed for module $moduleName with error code: ${state.errorCode()}")
                stateFlow.value = InstallationState.Error(
                    message = "Installation failed with error code: ${state.errorCode()}"
                )
                cleanupSession(sessionId)
            }
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                Log.d(TAG, "👤 Module $moduleName requires user confirmation")
                stateFlow.value = InstallationState.RequiresUserConfirmation(state)
            }
            SplitInstallSessionStatus.PENDING -> {
                Log.d(TAG, "⏳ Module $moduleName installation pending")
                stateFlow.value = InstallationState.Pending
            }
            SplitInstallSessionStatus.INSTALLING -> {
                Log.d(TAG, "🔧 Installing module $moduleName")
                stateFlow.value = InstallationState.Installing
            }
            SplitInstallSessionStatus.CANCELED -> {
                Log.w(TAG, "🚫 Installation canceled for module $moduleName")
                stateFlow.value = InstallationState.Error("Installation was canceled")
                cleanupSession(sessionId)
            }
            else -> {
                Log.d(TAG, "ℹ️ Unhandled status: ${state.status()} for module $moduleName")
            }
        }
    }

    private fun cleanupSession(sessionId: Int) {
        Log.d(TAG, "🧹 Cleaning up session $sessionId")
        sessionMap.remove(sessionId)
        Log.d(TAG, "📍 Session map after cleanup: $sessionMap")
    }

    init {
        Log.d(TAG, "🎬 Initializing DynamicFeatureManager")
        splitInstallManager.registerListener(listener)
    }

    override fun isModuleInstalled(moduleName: String): Boolean {
        val isInstalled = splitInstallManager.installedModules.contains(moduleName)
        Log.d(TAG, "📦 Checking if module $moduleName is installed: $isInstalled")
        return isInstalled
    }

    override fun getModuleState(moduleName: String): Flow<ModuleState> {
        Log.d(TAG, "🔍 Getting state flow for module: $moduleName")
        return moduleStates.getOrPut(moduleName) {
            Log.d(TAG, "📝 Creating new state flow for module: $moduleName")
            MutableStateFlow(
                if (isModuleInstalled(moduleName)) InstallationState.Installed
                else InstallationState.NotInstalled
            )
        }.map { installationState ->
            Log.d(TAG, "🔄 Mapping installation state for $moduleName: $installationState")
            when (installationState) {
                is InstallationState.NotInstalled -> ModuleState.NotLoaded
                is InstallationState.Pending -> ModuleState.Loading
                is InstallationState.Installing -> ModuleState.Loading
                is InstallationState.Downloading -> ModuleState.LoadingProgress(installationState.progress)
                is InstallationState.Installed -> ModuleState.Loaded
                is InstallationState.RequiresUserConfirmation -> {
                    Log.d(TAG, "👤 Preparing confirmation dialog for module: $moduleName")
                    ModuleState.NeedsConfirmation {
                        installationState.state.resolutionIntent()?.let {
                            Log.d(TAG, "🚀 Launching confirmation dialog for module: $moduleName")
                            confirmationLauncher?.launch(
                                IntentSenderRequest.Builder(it.intentSender).build()
                            )
                        }
                    }
                }
                is InstallationState.Error -> ModuleState.Error(installationState.message)
            }
        }
    }

    private fun isModuleCompatible(moduleName: String): Boolean {
        Log.d(TAG, "🔍 Checking compatibility for module: $moduleName")
        
        // Check if we're on the UI thread - we should be for Play Core operations
        val onUiThread = android.os.Looper.getMainLooper() == android.os.Looper.myLooper()
        if (!onUiThread) {
            Log.w(TAG, "⚠️ isModuleCompatible called from background thread, this may cause issues with Play Core API")
            // Consider switching to UI thread if needed
        }
        
        try {
            // If module is already installed, no need to check compatibility
            if (splitInstallManager.installedModules.contains(moduleName)) {
                Log.d(TAG, "✅ Module $moduleName is already installed")
                return true
            }

            // Validate module exists in the app bundle
            val request = try {
                SplitInstallRequest.newBuilder()
                    .addModule(moduleName)
                    .build()
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "❌ Module $moduleName not found in app bundle", e)
                return false
            }

            // Check device API level compatibility
            val moduleInfo = splitInstallManager.installedModules.find { it == moduleName }
            if (moduleInfo == null) {
                // Module not found in installed modules, check if it can be installed
                try {
                    Log.d(TAG, "✅ Module $moduleName is available for installation")
                    return true
                } catch (e: com.google.android.play.core.splitinstall.SplitInstallException) {
                    when (e.errorCode) {
                        -5 -> { // INCOMPATIBLE_WITH_EXISTING_SESSION
                            Log.w(TAG, "⚠️ Module $moduleName has an existing installation session")
                            return true // Still compatible, just has an ongoing installation
                        }
                        -6 -> { // NETWORK_ERROR
                            Log.w(TAG, "⚠️ Network error while checking module $moduleName")
                            return true // Network error shouldn't determine compatibility
                        }
                        -3 -> { // API_NOT_AVAILABLE
                            Log.e(TAG, "❌ Module $moduleName API not available on this device", e)
                            return false
                        }
                        -4 -> { // INVALID_REQUEST
                            Log.e(TAG, "❌ Module $moduleName installation request invalid", e)
                            return false
                        }
                        else -> {
                            Log.e(TAG, "❌ Unknown error checking module $moduleName compatibility: ${e.errorCode}", e)
                            return false
                        }
                    }
                }
            }

            Log.d(TAG, "✅ Module $moduleName passed all compatibility checks")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error checking module $moduleName compatibility", e)
            return false
        }
    }


    override suspend fun loadModule(moduleName: String) {
        Log.d(TAG, "📥 Starting load process for module: $moduleName")
        mutex.withLock {
            try {
                if (isModuleInstalled(moduleName)) {
                    Log.d(TAG, "✅ Module $moduleName is already installed")
                    moduleStates[moduleName]?.value = InstallationState.Installed
                    return
                }

                val stateFlow = moduleStates.getOrPut(moduleName) {
                    Log.d(TAG, "📝 Creating new state flow for module: $moduleName")
                    MutableStateFlow(InstallationState.NotInstalled)
                }

                if (stateFlow.value is InstallationState.Downloading ||
                    stateFlow.value is InstallationState.Installing ||
                    stateFlow.value is InstallationState.Pending
                ) {
                    Log.d(TAG, "⏳ Installation already in progress for $moduleName")
                    return
                }

                val activity = currentActivity
                if (activity == null || activity.isFinishing || activity.isDestroyed) {
                    Log.e(TAG, "❌ App is not in foreground for module: $moduleName")
                    stateFlow.value = InstallationState.Error("App must be in the foreground to install this module. Please try again.")
                    return
                }

                if (!isModuleCompatible(moduleName)) {
                    Log.e(TAG, "❌ Module $moduleName is not compatible with this device")
                    val errorMessage = "This module is not compatible with your device. Please ensure you have the latest app version."
                    stateFlow.value = InstallationState.Error(errorMessage)
                    return
                }


//                val connectivityManager = application.getSystemService(ConnectivityManager::class.java)
//                val network = connectivityManager.activeNetwork
//                val capabilities = connectivityManager.getNetworkCapabilities(network)
//                val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
//
//                if (!isConnected) {
//                    Log.e(TAG, "❌ No internet connection available for module: $moduleName")
//                    stateFlow.value = InstallationState.Error("No internet connection available")
//                    return
//                }

                val request = SplitInstallRequest.newBuilder()
                    .addModule(moduleName)
                    .build()

                Log.d(TAG, "🚀 Initiating installation request for module: $moduleName")
                stateFlow.value = InstallationState.Pending

                val deferredTask = splitInstallManager.startInstall(request)
                try {
                    Log.d(TAG, "⏳ Waiting for Play Store service binding...")
                    val sessionId = deferredTask.result
                    Log.d(TAG, "📍 Mapping sessionId $sessionId to module $moduleName")
                    sessionMap[sessionId] = moduleName
                    Log.d(TAG, "✨ Started installation with sessionId: $sessionId for module: $moduleName")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to bind to Play Store service for module: $moduleName", e)
                    val errorMessage = when {
                        e is com.google.android.play.core.splitinstall.SplitInstallException && e.errorCode == -7 -> {
                            splitInstallManager.deferredInstall(listOf(moduleName))
                                .addOnSuccessListener {
                                    Log.d(TAG, "✅ Deferred installation queued for module: $moduleName")
                                }
                                .addOnFailureListener { error ->
                                    Log.e(TAG, "❌ Failed to queue deferred installation", error)
                                }
                            "Installation queued. Please reopen the app to complete."
                        }
                        else -> "Failed to bind to Play Store service: ${e.message}"
                    }
                    stateFlow.value = InstallationState.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Installation failed for module: $moduleName", e)
                moduleStates[moduleName]?.value = InstallationState.Error("Installation failed: ${e.message}")
            }
        }
    }

    override fun retryModuleLoad(moduleName: String) {
        Log.d(TAG, "🔄 Retrying module load for: $moduleName")
        scope.launch {
            loadModule(moduleName)
        }
    }

    override fun handleUserConfirmation(moduleName: String) {
        Log.d(TAG, "👍 Handling user confirmation for module: $moduleName")
        retryModuleLoad(moduleName)
    }

    override fun cleanup() {
        Log.d(TAG, "🧹 Cleaning up DynamicFeatureManager")
        scope.cancel()
        splitInstallManager.unregisterListener(listener)
        sessionMap.clear()
        moduleStates.clear()
        currentActivity = null
        confirmationLauncher = null
    }

    override fun setConfirmationLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        Log.d(TAG, "🎯 Setting confirmation launcher")
        confirmationLauncher = launcher
    }

    override fun setActivity(activity: Activity?) {
        Log.d(TAG, "🎬 Setting activity: ${activity?.javaClass?.simpleName}")
        currentActivity = activity
    }

    companion object {
        private const val TAG = "DynamicFeatureManager"
        @SuppressLint("StaticFieldLeak")
        private var instance: DynamicFeatureManager? = null

        @Synchronized
        fun getInstance(application: Application): DynamicFeatureManager {
            Log.d(TAG, "📱 Getting DynamicFeatureManager instance")
            return instance ?: DynamicFeatureManager(application).also {
                instance = it
                Log.d(TAG, "✨ Created new DynamicFeatureManager instance")
            }
        }
    }

    private sealed class InstallationState {
        object NotInstalled : InstallationState()
        object Pending : InstallationState()
        object Installing : InstallationState()
        data class Downloading(val progress: Float) : InstallationState()
        object Installed : InstallationState()
        data class RequiresUserConfirmation(val state: com.google.android.play.core.splitinstall.SplitInstallSessionState) : InstallationState()
        data class Error(val message: String) : InstallationState()

        override fun toString(): String {
            return when (this) {
                is NotInstalled -> "NotInstalled"
                is Pending -> "Pending"
                is Installing -> "Installing"
                is Downloading -> "Downloading(${(progress * 100).toInt()}%)"
                is Installed -> "Installed"
                is RequiresUserConfirmation -> "RequiresUserConfirmation"
                is Error -> "Error($message)"
            }
        }
    }
}