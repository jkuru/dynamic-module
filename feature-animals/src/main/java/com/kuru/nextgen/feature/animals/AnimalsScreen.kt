package com.kuru.nextgen.feature.animals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.util.Log

@Composable
fun AnimalsScreen(
    navController: NavController,
    viewModel: AnimalsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(AnimalsIntent.LoadAnimals)
    }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, arguments ->
            Log.d("Navigation", "Navigated to: ${destination.route}")
            when (destination.route) {
                "animals_list" -> {
                    viewModel.handleIntent(AnimalsIntent.NavigateBack)
                }
                "animal_detail/{animalId}" -> {
                    val animalId = arguments?.getString("animalId")?.toIntOrNull()
                    if (animalId != null) {
                        viewModel.handleIntent(AnimalsIntent.SelectAnimal(animalId))
                    }
                }
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    when (state) {
        is AnimalsState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is AnimalsState.Success -> {
            val successState = state as AnimalsState.Success
            if (successState.selectedAnimal != null) {
                AnimalDetailScreen(
                    animal = successState.selectedAnimal,
                    onBackClick = {
                        viewModel.handleIntent(AnimalsIntent.NavigateBack)
                    }
                )
            } else {
                AnimalsListScreen(
                    animals = successState.animals,
                    onAnimalClick = { animalId ->
                        viewModel.handleIntent(AnimalsIntent.SelectAnimal(animalId))
                    }
                )
            }
        }
        is AnimalsState.Error -> {
            val errorState = state as AnimalsState.Error
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = errorState.message)
            }
        }
    }
}

@Composable
private fun AnimalsListScreen(
    animals: List<Animal>,
    onAnimalClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(animals) { animal ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                onClick = { onAnimalClick(animal.id) }
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = animal.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = animal.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimalDetailScreen(
    animal: Animal,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(animal.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = animal.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
} 