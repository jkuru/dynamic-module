package com.kuru.nextgen.plants

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kuru.nextgen.core.util.LogDisposableEffect
import com.kuru.nextgen.core.util.LogEffect


@Composable
fun PlantsScreen(navController: NavController) {
    val viewModel = remember { PlantsViewModel() }
    val state by viewModel.state.collectAsState()
    val innerNavController = rememberNavController()

    LogDisposableEffect("PlantsScreen") {
        // Any cleanup needed
    }

    // Add navigation destination change listener
    DisposableEffect(innerNavController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, arguments ->
            Log.d("Navigation", "Navigated to: ${destination.route}")
            when (destination.route) {
                "plants_list" -> {
                    viewModel.handleIntent(PlantsIntent.NavigateBack)
                }

                "plant_detail/{plantId}" -> {
                    arguments?.getString("plantId")?.toIntOrNull()?.let { id ->
                        viewModel.handleIntent(PlantsIntent.SelectPlant(id))
                    }
                }
            }
        }

        innerNavController.addOnDestinationChangedListener(listener)

        onDispose {
            innerNavController.removeOnDestinationChangedListener(listener)
        }
    }

    NavHost(navController = innerNavController, startDestination = "plants_list") {
        composable("plants_list") {
            PlantsListScreen(
                state = state,
                onIntent = viewModel::handleIntent,
                onNavigateToDetail = { id ->
                    innerNavController.navigate("plant_detail/$id")
                }
            )
        }
        composable("plant_detail/{plantId}") { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId")?.toIntOrNull()
            if (plantId != null) {
                PlantDetailScreen(
                    state = state,
                    onIntent = viewModel::handleIntent,
                    onNavigateBack = { 
                        if (innerNavController.previousBackStackEntry != null) {
                            innerNavController.navigateUp()
                        } else {
                            navController.navigateUp()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PlantsListScreen(
    state: PlantsState,
    onIntent: (PlantsIntent) -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    LaunchedEffect(Unit) {
        Log.d("PlantsListScreen", "Loading plants")
        onIntent(PlantsIntent.LoadPlants)
    }
    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = state.error!!)
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(state.plants) { plant ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        onClick = { onNavigateToDetail(plant.id) }
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = plant.name,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = plant.species,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = plant.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlantDetailScreen(
    state: PlantsState,
    onIntent: (PlantsIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        Log.d("PlantDetailScreen", "Loading Detail")

    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        state.selectedPlant?.let { plant ->
            Text(
                text = plant.name,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = plant.species,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = plant.description,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Care Instructions",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = plant.careInstructions,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Watering Frequency",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = plant.wateringFrequency,
                style = MaterialTheme.typography.bodyLarge
            )
        } ?: run {
            Text("Plant not found")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateBack) {
            Text("Back to List")
        }
    }
}