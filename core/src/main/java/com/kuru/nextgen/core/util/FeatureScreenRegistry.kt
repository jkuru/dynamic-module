package com.kuru.nextgen.core.util

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

object FeatureScreenRegistry {
    private val screens = mutableMapOf<String, @Composable (NavController) -> Unit>()
    private const val TAG = "DynamicFeatureManager"

    fun register(route: String, screen: @Composable (NavController) -> Unit) {
        Log.d(TAG,"FeatureScreenRegistry  register $route")
        screens[route] = screen
    }

    fun getScreen(route: String): (@Composable (NavController) -> Unit)? = screens[route]
}