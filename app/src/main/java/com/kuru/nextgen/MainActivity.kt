package com.kuru.nextgen

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kuru.nextgen.core.feature.ModuleManager
import com.kuru.nextgen.core.feature.ModuleManagerProvider
import com.kuru.nextgen.core.feature.ModuleState
import com.kuru.nextgen.core.util.FeatureScreenRegistry
import com.kuru.nextgen.feature.animals.AnimalsScreen
import com.kuru.nextgen.feature.cars.CarsScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var moduleManager: ModuleManager
    private lateinit var confirmationLauncher: ActivityResultLauncher<IntentSenderRequest>

    companion object {
        const val PLANTS_MODULE = "plants"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        moduleManager = ModuleManagerProvider.getInstance(application)
        moduleManager.setActivity(this)
        
        confirmationLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                moduleManager.handleUserConfirmation(PLANTS_MODULE)
            }
        }
        
        moduleManager.setConfirmationLauncher(confirmationLauncher)

        setContent {
            MaterialTheme {
                MainScreen(
                    moduleManager = moduleManager
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        moduleManager.setActivity(null)
        moduleManager.cleanup()
    }
}

@Composable
fun MainScreen(
    moduleManager: ModuleManager
) {
    val navController = rememberNavController()
    val moduleState by moduleManager.getModuleState(MainActivity.PLANTS_MODULE)
        .collectAsState(initial = ModuleState.NotLoaded)
    val scope = rememberCoroutineScope()

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
                        scope.launch {
                            moduleManager.loadModule(MainActivity.PLANTS_MODULE)
                        }
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
                when (val state = moduleState) {
                    ModuleState.NotLoaded -> {
                        LaunchedEffect(Unit) {
                            moduleManager.loadModule(MainActivity.PLANTS_MODULE)
                        }
                        LoadingScreen("Preparing to load Plants feature...")
                    }
                    ModuleState.Loading -> {
                        LoadingScreen("Loading Plants feature...")
                    }
                    is ModuleState.LoadingProgress -> {
                        LoadingScreen(
                            message = "Downloading Plants feature...",
                            progress = state.progress
                        )
                    }
                    ModuleState.Loaded -> {
                        FeatureScreenRegistry.getScreen("plants")?.invoke(navController) ?: run {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Plants feature is installed but screen not found")
                            }
                        }
                    }
                    is ModuleState.Error -> {
                        ErrorScreen(
                            message = state.message,
                            onRetry = { moduleManager.retryModuleLoad(MainActivity.PLANTS_MODULE) }
                        )
                    }
                    is ModuleState.NeedsConfirmation -> {
                        ConfirmationScreen(onConfirm = state.confirmationCallback)
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
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                when {
                    message.contains("error code: -2") || message.contains("NETWORK_ERROR") ->
                        "No internet connection. Please check your network and try again."
                    else -> message
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun ConfirmationScreen(onConfirm: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Additional storage space is required")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onConfirm) {
                Text("Continue Installation")
            }
        }
    }
}