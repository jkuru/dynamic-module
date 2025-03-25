package com.kuru.nextgen.core.feature

import android.app.Application

object ModuleManagerProvider {


    private fun getInstance(application: Application): ModuleManager {
        return DynamicFeatureManager.getInstance(application)
    }

    /**
     * Get both regular and deferred managers in a convenient data class
     */
    fun getFeatureManagers(application: Application): FeatureManagers {
        return FeatureManagers(
            regular = getInstance(application)
        )
    }

    /**
     * Clean up both managers
     */
    fun cleanup(application: Application) {
        DynamicFeatureManager.getInstance(application).cleanup()
    }
}

/**
 * Data class holding both types of feature managers
 */
data class FeatureManagers(
    val regular: ModuleManager
) 