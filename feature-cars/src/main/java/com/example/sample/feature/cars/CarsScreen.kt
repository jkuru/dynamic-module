package com.example.sample.feature.cars

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

data class Car(
    val id: Int,
    val name: String,
    val brand: String,
    val year: Int,
    val description: String
)

@Composable
fun CarsScreen() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "cars_list") {
        composable("cars_list") {
            CarsListScreen(navController)
        }
        composable("car_detail/{carId}") { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId")?.toIntOrNull()
            if (carId != null) {
                CarDetailScreen(carId, navController)
            }
        }
    }
}

@Composable
fun CarsListScreen(navController: NavController) {
    val cars = remember {
        listOf(
            Car(1, "Model S", "Tesla", 2024, "Electric luxury sedan"),
            Car(2, "911", "Porsche", 2024, "Iconic sports car"),
            Car(3, "S-Class", "Mercedes-Benz", 2024, "Luxury flagship sedan")
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(cars) { car ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                onClick = { navController.navigate("car_detail/${car.id}") }
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = car.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = car.brand,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Year: ${car.year}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = car.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun CarDetailScreen(carId: Int, navController: NavController) {
    val car = remember {
        when (carId) {
            1 -> Car(1, "Model S", "Tesla", 2024, "Electric luxury sedan")
            2 -> Car(2, "911", "Porsche", 2024, "Iconic sports car")
            3 -> Car(3, "S-Class", "Mercedes-Benz", 2024, "Luxury flagship sedan")
            else -> null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (car != null) {
            Text(
                text = car.name,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = car.brand,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Year: ${car.year}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = car.description,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text("Car not found")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigateUp() }) {
            Text("Back to List")
        }
    }
} 