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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kuru.nextgen.core.util.LogDisposableEffect
import com.kuru.nextgen.core.util.LogEffect

// This is the main entry point Composable provided to DFComponentActivity's dynamicScreenLambda
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantsFeatureScreen(
    // Although DFComponentActivity provides a NavController,
    // we don't strictly need it here anymore for back navigation.
    // Keep it if the feature *needs* to navigate *outside* itself, otherwise it can be removed.
    outerNavController: NavController, // Renamed for clarity
    params: List<String> // Parameters passed from the hosting Activity
) {
    val TAG = "PlantsFeatureScreen"
    val innerNavController = rememberNavController() // Controller for internal navigation (list <-> detail)
    val viewModel = remember { PlantsViewModel() } // Instantiate ViewModel

    // Get the current Activity context to finish it on back press from the top level
    val context = LocalContext.current
    val activity = context as? Activity // Safely cast to Activity

    LogEffect(TAG, "PlantsFeatureScreen Composable launched successfully")
    LogDisposableEffect(TAG) {
        Log.d(TAG, "PlantsFeatureScreen Composable disposed")
    }

    // Determine the current route within the inner NavHost to potentially change TopAppBar title
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Dynamically set title based on the inner route
                    Text(
                        when (currentRoute) {
                            "plant_detail/{plantId}" -> "Plant Detail"
                            else -> "Plants List" // Default title for "plants_list"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Check if we can navigate back *within* the feature first
                        if (innerNavController.previousBackStackEntry != null) {
                            innerNavController.navigateUp()
                        } else {
                            // Otherwise, finish the hosting Activity to go back to the main app
                            Log.d(TAG, "No inner back stack, finishing Activity")
                            activity?.finish()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
                // Optional: Add actions or customize colors
            )
        }
    ) { paddingValues -> // Scaffold provides padding for edge-to-edge
        // NavHost for navigating between the list and detail screens *within* this feature
        NavHost(
            navController = innerNavController,
            startDestination = "plants_list",
            // Apply padding from Scaffold to the NavHost container
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("plants_list") {
                // Pass the ViewModel and navigation callback
                PlantsListScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { id ->
                        innerNavController.navigate("plant_detail/$id")
                    }
                )
            }
            composable("plant_detail/{plantId}") { backStackEntry ->
                // Extract ID and show detail screen
                // Note: The back navigation is now handled by the TopAppBar's navigationIcon
                PlantDetailScreen(
                    viewModel = viewModel,
                    plantId = backStackEntry.arguments?.getString("plantId")?.toIntOrNull(),
                    // Provide a lambda to navigate back *within* the feature (handled by TopAppBar now)
                    onNavigateBack = { innerNavController.navigateUp() }
                )
            }
        }
    }
}


// --- Sub-Screens (List and Detail) ---

@Composable
fun PlantsListScreen(
    viewModel: PlantsViewModel, // Receive ViewModel
    onNavigateToDetail: (Int) -> Unit
) {
    val TAG = "PlantsListScreen"
    val state by viewModel.state.collectAsState() // Observe state

    LogEffect(TAG, "LaunchedEffect triggered")
    LaunchedEffect(Unit) { // Load data when the list screen appears
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
                modifier = Modifier.fillMaxSize().padding(16.dp), // Add padding for error text
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            }
        }

        else -> {
            Log.d(TAG, "Displaying plants list with ${state.plants.size} items")
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp) // Padding for the list content
            ) {
                items(state.plants, key = { it.id }) { plant -> // Add key for performance
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp), // Reduced vertical padding
                        onClick = {
                            Log.d(TAG, "Navigating to detail for plant ID: ${plant.id}")
                            onNavigateToDetail(plant.id)
                            // Send intent *after* triggering navigation if needed,
                            // or handle selection in ViewModel based on navigation event
                            // viewModel.handleIntent(PlantsIntent.SelectPlant(plant.id))
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
                            Spacer(modifier = Modifier.height(4.dp)) // Reduced spacer
                            Text(
                                text = plant.species,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant // Softer color
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = plant.description,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3 // Limit description lines in list
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
    viewModel: PlantsViewModel, // Receive ViewModel
    plantId: Int?, // Receive ID from NavHost argument
    onNavigateBack: () -> Unit // Lambda to navigate up within the inner NavHost
) {
    val TAG = "PlantDetailScreen"
    val state by viewModel.state.collectAsState() // Observe state

    // Select the plant when the screen is composed or plantId changes
    LaunchedEffect(plantId) {
        if (plantId != null) {
            Log.d(TAG, "Selecting plant with ID: $plantId")
            viewModel.handleIntent(PlantsIntent.SelectPlant(plantId))
        } else {
            Log.w(TAG, "Plant ID is null, cannot select plant.")
            // Optionally navigate back if ID is unexpectedly null
            // onNavigateBack()
        }
    }

    // Use DisposableEffect for cleanup if needed when leaving the detail screen
    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "Disposing PlantDetailScreen, clearing selection in ViewModel")
            // Deselect plant when navigating away from detail? Optional.
            // viewModel.handleIntent(PlantsIntent.DeselectPlant)
        }
    }

    // Main content column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // Padding for the detail content
    ) {
        state.selectedPlant?.let { plant ->
            if (plant.id != plantId && plantId != null) {
                // State might briefly hold the wrong plant if selection is async
                // Show loading or placeholder while the correct plant loads
                Log.d(TAG, "Waiting for correct plant data (ID: $plantId)")
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Plant data is ready and matches the requested ID
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
            // Handle case where selectedPlant is null (e.g., after error or initial load)
            if (plantId != null && !state.isLoading) { // Avoid showing "not found" during load
                Log.w(TAG, "Plant with ID $plantId not found in state.")
                Text("Plant not found")
            } else if (!state.isLoading) {
                Log.w(TAG, "Selected plant is null, and no ID specified.")
                Text("No plant selected")
            } else {
                Log.d(TAG, "Selected plant is null, likely loading.")
                // Optionally show loading indicator here too
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Push button to bottom

        // Button is less critical now as TopAppBar handles back navigation
        // Keep it for explicit action if desired, or remove it.
        Button(
            onClick = onNavigateBack, // Navigates up within the inner NavHost
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back to List")
        }
    }
}
