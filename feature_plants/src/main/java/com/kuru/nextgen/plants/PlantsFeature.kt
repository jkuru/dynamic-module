package com.kuru.nextgen.plants


import android.util.Log
import com.kuru.nextgen.core.feature.FeatureInitializer
import com.kuru.nextgen.core.feature.FeatureRegistry
import com.kuru.nextgen.core.util.FeatureScreenRegistry


// In :plants (e.g., com.kuru.nextgen.plants)
object PlantsFeatureInitializer : FeatureInitializer {
    override fun initialize() {
        Log.d("loadPlantsFeature", "PlantsFeatureInitializer initialize  successfully")
        FeatureScreenRegistry.register("plants") { navController ->
            PlantsScreen(navController) // Your screen composable
        }
    }
}

// In :plants (e.g., com.kuru.nextgen.plants)
object PlantsFeature {
    init {
        FeatureRegistry.registerInitializer(PlantsFeatureInitializer)
        Log.d("loadPlantsFeature", "PlantsFeature Object  successfully")
    }
}