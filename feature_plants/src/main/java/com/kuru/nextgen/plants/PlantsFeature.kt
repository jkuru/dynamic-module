package com.kuru.nextgen.plants


import android.util.Log
import com.kuru.nextgen.core.feature.FeatureInitializer
import com.kuru.nextgen.core.feature.FeatureRegistry
import com.kuru.nextgen.core.util.FeatureScreenRegistry


private const val TAG = "DynamicFeatureManager"
// In :plants (e.g., com.kuru.nextgen.plants)
object PlantsFeatureInitializer : FeatureInitializer {
    /**
     * Step One :
     * The actual NavHostController instance is created and managed within the main application's UI,
     * specifically inside the MainScreen composable function:
     *
     * Step Two :
     *  In your :plants module, inside PlantsFeatureInitializer,
     *  you register a lambda function with the FeatureScreenRegistry:
     *
     * At this registration stage, you are not providing a specific NavController instance.
     * You are providing a function (a lambda) that takes a NavController as a parameter and,
     * when invoked, will call PlantsScreen with whatever NavController it receives.
     * The navController inside { navController -> ... } is just the parameter name for the lambda.
     *
     * Step Three:
     *  Invoking the Registered Screen (Main Module - During Navigation):
     *
     * When the user navigates to the "plants" route (e.g., by clicking the bottom navigation item, which calls navController.navigate("plants")),
     * the NavHost finds the corresponding composable block defined in the Main Activity
     *
     */
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