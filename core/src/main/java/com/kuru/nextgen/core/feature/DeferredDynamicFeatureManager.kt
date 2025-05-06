package com.kuru.nextgen.core.feature

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


/**
 * Manages dynamic feature modules using deferred installs.
 */
class DeferredDynamicFeatureManager(
    application: Application
) {
    private var prefs: SharedPreferences? = null

    init {
        prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    // Coroutine scope for background checks
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // Manages dynamic feature module installations
    private val splitInstallManager: SplitInstallManager =
        SplitInstallManagerFactory.create(application)

    /**
     * Checks if a module is already installed.
     */
    private fun isModuleInstalled(moduleName: String): Boolean {
        return splitInstallManager.installedModules.contains(moduleName)
    }

    /**
     * Schedules a deferred installation of the module.
     */
    fun installModule(moduleName: String) {
        Log.d("DeferredDynamicFeatureManager", "loadModule started")
        if (isModuleInstalled(moduleName)) {
            return
        }
        scope.launch {
            // Schedule deferred installation
            splitInstallManager.deferredInstall(listOf(moduleName)).addOnSuccessListener {
                Log.d("DeferredInstall", "Scheduled $moduleName")
                monitorModuleInstallation(moduleName)
            }.addOnFailureListener { e ->
                Log.e("DeferredInstall", "Error: $e")
            }
        }
    }

    /**
     * Retries scheduling a deferred installation if needed.
     */
    fun retryModuleLoad(moduleName: String) {
        installModule(moduleName)
    }

    /**
     * Critical Piece to initialize the component
     */
    private fun loadPlantsFeature() {
        try {
            val clazz = Class.forName("com.kuru.nextgen.plants.PlantsFeature")
            // Accessing the class will trigger the init block
            val instance =
                clazz.getDeclaredField("INSTANCE").get(null) // If PlantsFeature is an object
            Log.d(TAG, "PlantsFeature loadPlantsFeature successfully")
            prefs?.edit()?.putBoolean("isFeatureInitialized", true)?.apply()
        } catch (e: Exception) {
            Log.d(TAG, "Failed to load PlantsFeature: ${e.message}", e)
        }

        FeatureRegistry.initializeAll()

    }

    private fun monitorModuleInstallation(moduleName: String) {
        // Start periodic checking
        val job = checkModulePeriodically(
            moduleName = moduleName,
            intervalMs = 5000L, // Check every 5 seconds
            maxAttempts = 12,   // Stop after 1 minute (12 * 5s)
            onComplete = { isInstalled ->
                if (isInstalled) {
                    Log.d("Monitor", "Module $moduleName is installed, proceeding with feature.")
                    loadPlantsFeature()
                } else {
                    Log.d("Monitor", "Module $moduleName failed to install within time limit.")
                    // Handle failure (e.g., retry or notify user)
                }
            }
        )
    }

    /**
     * Periodically checks if the module is installed using a coroutine.
     * Calls the onComplete callback when the module is installed or if an error occurs.
     *
     * @param moduleName The name of the module to check.
     * @param intervalMs The interval between checks in milliseconds (default: 5 seconds).
     * @param maxAttempts The maximum number of checks before giving up (default: 12, ~1 minute).
     * @param onComplete Callback with the result (true if installed, false if timed out).
     * @return Job that can be used to cancel the periodic check.
     */
    private fun checkModulePeriodically(
        moduleName: String,
        intervalMs: Long = 5000L, // 5 seconds
        maxAttempts: Int = 12,    // ~1 minute total
        onComplete: (Boolean) -> Unit
    ): Job {
        return scope.launch {
            var attempts = 0
            while (isActive && attempts < maxAttempts) {
                if (isModuleInstalled(moduleName)) {
                    Log.d(TAG, "Module $moduleName is installed after $attempts attempts")
                    onComplete(true)
                    return@launch
                }
                attempts++
                Log.d(TAG, "Check $attempts/$maxAttempts: $moduleName not installed yet")
                delay(intervalMs)
            }
            Log.d(TAG, "Max attempts ($maxAttempts) reached, $moduleName not installed")
            onComplete(false)
        }
    }

    companion object {
        private const val TAG = "DeferredDynamicFeatureManager"

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
