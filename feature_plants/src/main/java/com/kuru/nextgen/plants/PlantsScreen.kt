package com.kuru.nextgen.plants

import android.app.Activity
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kuru.nextgen.core.util.LogDisposableEffect
import com.kuru.nextgen.core.util.LogEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantsFeatureScreen(
    outerNavController: NavController, // Main app's NavController
    params: List<String> // Parameters from DFComponentActivity
) {
    val TAG = "PlantsFeatureScreen"
    val innerNavController = rememberNavController()
    val viewModel = remember { PlantsViewModel() }
    var isEffectTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isEffectTriggered) {
            Log.d(TAG, "Effect triggered")
            isEffectTriggered = true
            Log.d(TAG, "PlantsFeatureScreen Composable launched successfully")
        }
    }

    LogDisposableEffect(TAG) {
        Log.d(TAG, "PlantsFeatureScreen Composable disposed")
    }


    // Determine current route for TopAppBar title
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentRoute) {
                            "plant_detail/{plantId}" -> "Plant Detail"
                            else -> "Plants List"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (innerNavController.popBackStack()) {
                            Log.d(TAG, "Navigated up within inner NavController")
                        } else {
                            Log.d(TAG, "Calling main NavController destination")
                            (innerNavController.context as? Activity)?.finish()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = innerNavController,
            startDestination = "plants_list",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("plants_list") {
                PlantsListScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { id ->
                        Log.d(TAG, "Navigating to detail for plant ID: $id")
                        innerNavController.navigate("plant_detail/$id")
                    }
                )
            }
            composable("plant_detail/{plantId}") { backStackEntry ->
                PlantDetailScreen(
                    viewModel = viewModel,
                    plantId = backStackEntry.arguments?.getString("plantId")?.toIntOrNull(),
                    onNavigateBack = {
                        if (innerNavController.popBackStack()) {
                            Log.d(TAG, "Navigated up from PlantDetailScreen")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PlantsListScreen(
    viewModel: PlantsViewModel,
    onNavigateToDetail: (Int) -> Unit,
) {
    val TAG = "PlantsListScreen"
    val state by viewModel.state.collectAsState()

    LogEffect(TAG, "LaunchedEffect triggered")
    LaunchedEffect(Unit) {
        viewModel.handleIntent(PlantsIntent.LoadPlants)
        Log.d(TAG, "LoadPlants Intent sent")
    }

    when {
        state.isLoading -> {
            Log.d(TAG, "Displaying Loading indicator")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        state.error != null -> {
            Log.e(TAG, "Displaying error: ${state.error}")
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            }
        }
        else -> {
            Log.d(TAG, "Displaying plants list with ${state.plants.size} items")
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(state.plants, key = { it.id }) { plant ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = {
                            Log.d(TAG, "Navigating to detail for plant ID: ${plant.id}")
                            onNavigateToDetail(plant.id)
                        }
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
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = plant.species,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = plant.description,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3
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
    viewModel: PlantsViewModel,
    plantId: Int?,
    onNavigateBack: () -> Unit
) {
    val TAG = "PlantDetailScreen"
    val state by viewModel.state.collectAsState()

    LaunchedEffect(plantId) {
        if (plantId != null) {
            Log.d(TAG, "Selecting plant with ID: $plantId")
            viewModel.handleIntent(PlantsIntent.SelectPlant(plantId))
        } else {
            Log.w(TAG, "Plant ID is null, cannot select plant.")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "Disposing PlantDetailScreen, clearing selection in ViewModel")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        state.selectedPlant?.let { plant ->
            if (plant.id != plantId && plantId != null) {
                Log.d(TAG, "Waiting for correct plant data (ID: $plantId)")
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Log.d(TAG, "Displaying details for plant: ${plant.name} (ID: ${plant.id})")
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = plant.species,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = plant.description,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Care Instructions",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = plant.careInstructions,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Watering Frequency",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = plant.wateringFrequency,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } ?: run {
            if (plantId != null && !state.isLoading) {
                Log.w(TAG, "Plant with ID $plantId not found in state.")
                Text("Plant not found")
            } else if (!state.isLoading) {
                Log.w(TAG, "Selected plant is null, and no ID specified.")
                Text("No plant selected")
            } else {
                Log.d(TAG, "Selected plant is null, likely loading.")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back to List")
        }
    }
}