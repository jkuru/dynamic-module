package com.kuru.nextgen.feature.cars

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
fun CarsScreen(
    navController: NavController,
    viewModel: CarsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(CarsIntent.LoadCars)
    }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, arguments ->
            Log.d("Navigation", "Navigated to: ${destination.route}")
            when (destination.route) {
                "cars_list" -> {
                    viewModel.handleIntent(CarsIntent.NavigateBack)
                }
                "car_detail/{carId}" -> {
                    val carId = arguments?.getString("carId")?.toIntOrNull()
                    if (carId != null) {
                        viewModel.handleIntent(CarsIntent.SelectCar(carId))
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
        is CarsState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is CarsState.Success -> {
            val successState = state as CarsState.Success
            if (successState.selectedCar != null) {
                CarDetailScreen(
                    car = successState.selectedCar,
                    onBackClick = {
                        viewModel.handleIntent(CarsIntent.NavigateBack)
                    }
                )
            } else {
                CarsListScreen(
                    cars = successState.cars,
                    onCarClick = { carId ->
                        viewModel.handleIntent(CarsIntent.SelectCar(carId))
                    }
                )
            }
        }
        is CarsState.Error -> {
            val errorState = state as CarsState.Error
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
private fun CarsListScreen(
    cars: List<Car>,
    onCarClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(cars) { car ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                onClick = { onCarClick(car.id) }
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = car.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = car.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarDetailScreen(
    car: Car,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(car.name) },
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
                text = car.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
} 