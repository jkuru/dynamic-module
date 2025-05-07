package com.kuru.nextgen.plants

import android.content.Context
import android.util.Log
import com.kuru.featureflow.component.domain.DFFeatureRegistryUseCase
import com.kuru.featureflow.component.register.DFFeatureConfig
import com.kuru.featureflow.component.register.DFFeatureInterceptor
import com.kuru.featureflow.component.register.DFRegistryComponentEntry
import com.kuru.featureflow.component.register.DFRegistryEntryPoint
import dagger.hilt.android.EntryPointAccessors

class PlantEntry : DFRegistryComponentEntry {

    companion object {
        private const val TAG = "PlantEntry"
    }

    constructor() {
        // Public no-arg constructor for ServiceLoader
    }

    override fun initialize(context: Context) { // Method from DFComponentEntry interface
        Log.d(TAG, "Get EntryPointAccessors from Context ")
        // 1. Get the Entry Point accessor
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            DFRegistryEntryPoint::class.java // Use the specific Entry Point interface
        )

        Log.d(TAG, "EntryPointAccessors from Context is =  $entryPoint")
        Log.d(TAG, "Get DFFeatureRegistryUseCase from ComponentRegistry ")
        // 2. Get the DFComponentRegistry via the Entry Point
        val registry: DFFeatureRegistryUseCase = entryPoint.getComponentRegistry()
        Log.d(TAG, "DFFeatureRegistryUseCase from ComponentRegistry  is $registry")
        // 3. Now use the registry
        val config = plantConfig() // Your feature-specific config
        registry.register(config) { navController, params ->
            Log.d(TAG, "Navigating to PlantsFeatureScreen ")
            PlantsFeatureScreen(navController, params) // Your feature's screen
        }
        Log.d(TAG, "Plant feature initialized and registered!")
    }

    // Helper function to create config
    private fun plantConfig(): DFFeatureConfig {
        return DFFeatureConfig.create(
            route = "feature_plants",
            // Post-install interceptor
            DFFeatureInterceptor(preInstall = false) {
                try {
                    Log.d(TAG, "Analytics for plants feature")
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed  $e")
                    false
                }
            }
        )
    }

}