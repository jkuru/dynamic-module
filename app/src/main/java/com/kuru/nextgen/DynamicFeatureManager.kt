package com.kuru.nextgen

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DynamicFeatureManager(private val context: Context) {
    private val splitInstallManager: SplitInstallManager = SplitInstallManagerFactory.create(context.applicationContext)
    private val _installationState = MutableStateFlow<InstallationState>(InstallationState.NotInstalled)
    val installationState: StateFlow<InstallationState> = _installationState.asStateFlow()

    private val listener = SplitInstallStateUpdatedListener { state ->
        // Update state only for the relevant feature if needed; for now, assuming one feature at a time
        when (state.status()) {
            SplitInstallSessionStatus.DOWNLOADING -> {
                _installationState.value = InstallationState.Downloading(
                    progress = state.bytesDownloaded().toFloat() / state.totalBytesToDownload()
                )
            }
            SplitInstallSessionStatus.INSTALLED -> {
                _installationState.value = InstallationState.Installed
            }
            SplitInstallSessionStatus.FAILED -> {
                _installationState.value = InstallationState.Error(
                    message = "Installation failed with error code: ${state.errorCode()}"
                )
            }
            // Add other statuses as needed (e.g., PENDING, INSTALLING)
            else -> {
                // Optionally handle other states
            }
        }
    }

    init {
        splitInstallManager.registerListener(listener)
    }

    fun isFeatureInstalled(featureName: String): Boolean {
        return splitInstallManager.installedModules.contains(featureName)
    }

    fun startInstall(featureName: String): Task<Int> {
        val request = SplitInstallRequest.newBuilder()
            .addModule(featureName)
            .build()

        return splitInstallManager.startInstall(request)
    }

    fun setError(message: String) {
        _installationState.value = InstallationState.Error(message)
    }

    fun unregisterListener() {
        splitInstallManager.unregisterListener(listener)
    }
}

sealed class InstallationState {
    object NotInstalled : InstallationState()
    data class Downloading(val progress: Float) : InstallationState()
    object Installed : InstallationState()
    data class Error(val message: String) : InstallationState()
}