package com.kuru.nextgen.core.util

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

object FeatureScreenRegistry {
    private val screens = mutableMapOf<String, @Composable (NavController) -> Unit>()

    fun register(route: String, screen: @Composable (NavController) -> Unit) {
        screens[route] = screen
    }

    fun getScreen(route: String): (@Composable (NavController) -> Unit)? = screens[route]
}