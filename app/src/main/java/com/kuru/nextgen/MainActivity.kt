package com.kuru.nextgen

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kuru.nextgen.NextGenApplication.Companion.PLANTS_MODULE
import com.kuru.nextgen.NextGenApplication.Companion.TAG
import com.kuru.nextgen.core.feature.DynamicFeatureManager
import com.kuru.nextgen.core.feature.FeatureRegistry
import com.kuru.nextgen.core.util.FeatureScreenRegistry
import com.kuru.nextgen.feature.animals.AnimalsScreen
import com.kuru.nextgen.feature.cars.CarsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val prefs: SharedPreferences by lazy { getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    override fun onCreate(savedInstanceState: Bundle?) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        super.onCreate(savedInstanceState)
        val isFeatureInitialized = prefs.getBoolean("isFeatureInitialized", false)
        val featureManager = DynamicFeatureManager.getInstance(application)
        featureManager.setActivity(this)
        setContent {
            MaterialTheme {
                MainScreen(featureManager)
            }
        }

        scope.launch {
            Log.d(TAG, "DynamicFeatureManager started")
            if (!featureManager.isModuleInstalled(PLANTS_MODULE)) {
                // Module isn’t installed: load it and initialize the feature
                Log.d(TAG, "Loading plants module")
                prefs.edit().putBoolean("isFeatureInitialized",false).apply()
                featureManager.loadModule(PLANTS_MODULE)
            } else if (!isFeatureInitialized) {
                // Module is installed, but feature isn’t initialized: initialize it
                Log.d(TAG, "Module already installed, initializing failed")
            } else {
                // Module is installed and feature is initialized: do nothing
                Log.d(TAG, "Module already installed and feature initialized")
            }
        }
    }
}

@Composable
fun MainScreen(
    featureManager: DynamicFeatureManager
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Pets, contentDescription = "Animals") },
                    label = { Text("Animals") },
                    selected = navController.currentDestination?.route == "animals",
                    onClick = { navController.navigate("animals") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Cars") },
                    label = { Text("Cars") },
                    selected = navController.currentDestination?.route == "cars",
                    onClick = { navController.navigate("cars") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LocalFlorist, contentDescription = "Plants") },
                    label = { Text("Plants") },
                    selected = navController.currentDestination?.route == "plants",
                    onClick = {
                        navController.navigate("plants")
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "animals",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("animals") {
                AnimalsScreen(navController)
            }
            composable("cars") {
                CarsScreen(navController)
            }
            composable("plants") {
                if (featureManager.isModuleInstalled(PLANTS_MODULE)) {
                    LoadingScreen("Module is installed!...")
                    FeatureScreenRegistry.getScreen("plants")?.invoke(navController) ?: run {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Plants feature is installed but screen not found")
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Plants feature is being prepared and will be available soon")
                    }
                }

            }
        }
    }
}

@Composable
private fun LoadingScreen(message: String, progress: Float? = null) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            if (progress != null) {
                Text("${(progress * 100).toInt()}%")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(message)
        }
    }
}

@Composable
private fun ErrorScreen(messageVal: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(messageVal)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


