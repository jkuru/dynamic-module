package com.kuru.nextgen.plants


import android.util.Log
import com.kuru.nextgen.core.feature.FeatureInitializer
import com.kuru.nextgen.core.feature.FeatureRegistry
import com.kuru.nextgen.core.util.FeatureScreenRegistry


private const val TAG = "DynamicFeatureManager"
// In :plants (e.g., com.kuru.nextgen.plants)
object PlantsFeatureInitializer : FeatureInitializer {

    override fun initialize() {
        Log.d(TAG, "PlantsFeatureInitializer initialize ")
        FeatureScreenRegistry.register("plants") { navController ->
            PlantsScreen(navController) // Your screen composable
        }
    }
}

// In :plants (e.g., com.kuru.nextgen.plants)
object PlantsFeature {
    init {
        Log.d(TAG, "PlantsFeature Module entry successfully")
        FeatureRegistry.registerInitializer(PlantsFeatureInitializer)
    }
}