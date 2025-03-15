package com.kuru.nextgen

import android.os.Bundle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kuru.nextgen.core.util.FeatureScreenRegistry
import com.kuru.nextgen.feature.animals.AnimalsScreen
import com.kuru.nextgen.feature.cars.CarsScreen

class MainActivity : ComponentActivity() {
    private lateinit var dynamicFeatureManager: DynamicFeatureManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dynamicFeatureManager = DynamicFeatureManager(this.applicationContext)

        setContent {
            MaterialTheme {
                MainScreen(dynamicFeatureManager)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Clean up the listener to prevent memory leaks
        dynamicFeatureManager.unregisterListener()
    }
}


@Composable
fun MainScreen(dynamicFeatureManager: DynamicFeatureManager) {
    val navController = rememberNavController()
    val installationState by dynamicFeatureManager.installationState.collectAsState()

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
                        // Always navigate to "plants"; installation is handled in the composable
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
                when (val state = installationState) {
                    is InstallationState.NotInstalled -> {
                        // Trigger installation when the composable is first composed
                        LaunchedEffect(Unit) {
                            dynamicFeatureManager.startInstall("plants")
                        }
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Installing Plants feature...")
                        }
                    }
                    is InstallationState.Downloading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Downloading Plants feature...")
                            }
                        }
                    }
                    is InstallationState.Installed -> {
                        FeatureScreenRegistry.getScreen("plants")?.invoke(navController) ?: run {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Plants feature is installed but screen not found")
                            }
                        }
                    }
                    is InstallationState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(state.message)
                        }
                    }
                }
            }
        }
    }
}