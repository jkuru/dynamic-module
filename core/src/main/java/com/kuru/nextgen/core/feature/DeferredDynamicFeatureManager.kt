package com.kuru.nextgen.core.feature

import android.app.Application
import android.util.Log
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory

/**
 * Manages dynamic feature modules using deferred installs.
 */
class DeferredDynamicFeatureManager private constructor(
    private val application: Application
) {

    // Manages dynamic feature module installations
    private val splitInstallManager: SplitInstallManager =
        SplitInstallManagerFactory.create(application)

    /**
     * Checks if a module is already installed.
     */
    fun isModuleInstalled(moduleName: String): Boolean {
        return splitInstallManager.installedModules.contains(moduleName)
    }

    /**
     * Schedules a deferred installation of the module.
     */
    fun loadModule(moduleName: String) {
        Log.d("DeferredDynamicFeatureManager", "loadModule started")
        if (isModuleInstalled(moduleName)) {
            return
        }

        // Schedule deferred installation
        splitInstallManager.deferredInstall(listOf(moduleName)).addOnSuccessListener {
            Log.d("DeferredInstall", "Scheduled $moduleName")
        }
            .addOnFailureListener { e ->
                Log.e("DeferredInstall", "Error: $e")
            }
        // Note: Installation happens in the background with no real-time updates
    }

    /**
     * Retries scheduling a deferred installation if needed.
     */
    fun retryModuleLoad(moduleName: String) {
        loadModule(moduleName)
    }

    companion object {
        @Volatile
        private var instance: DeferredDynamicFeatureManager? = null

        /**
         * Provides the singleton instance of DeferredDynamicFeatureManager.
         * Initializes the instance if it doesn't exist yet.
         */
        fun getInstance(application: Application): DeferredDynamicFeatureManager {
            return instance ?: synchronized(this) {
                Log.d("DeferredDynamicFeatureManager", "New Instance")
                instance ?: DeferredDynamicFeatureManager(application).also { instance = it }
            }
        }
    }
}

/**
 * Represents the state of a dynamic feature module for deferred installs.
 */
sealed class DeferredModuleState {
    object NotLoaded : DeferredModuleState()
    object Installed : DeferredModuleState()
}