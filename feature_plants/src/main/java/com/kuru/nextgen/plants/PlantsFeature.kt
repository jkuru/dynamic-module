package com.kuru.nextgen.plants


import com.kuru.nextgen.core.feature.FeatureInitializer
import com.kuru.nextgen.core.feature.FeatureRegistry
import com.kuru.nextgen.core.util.FeatureScreenRegistry


// In :plants (e.g., com.kuru.nextgen.plants)
object PlantsFeatureInitializer : FeatureInitializer {
    override fun initialize() {
        // Example: Register a screen or perform initialization
        FeatureScreenRegistry.register("plants") { navController ->
            PlantsScreen(navController) // Your screen composable
        }
    }
}

// In :plants (e.g., com.kuru.nextgen.plants)
object PlantsFeature {
    init {
        FeatureRegistry.registerInitializer(PlantsFeatureInitializer)
    }
}