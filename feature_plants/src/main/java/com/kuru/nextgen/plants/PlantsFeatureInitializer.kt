package com.kuru.nextgen.plants

import com.kuru.nextgen.core.util.FeatureScreenRegistry


object PlantsFeatureInitializer {
    init {
        FeatureScreenRegistry.register("plants") { navController ->
            PlantsScreen(navController)
        }
    }
}