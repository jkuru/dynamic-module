package com.example.sample.feature.animals

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

data class Animal(
    val id: Int,
    val name: String,
    val description: String,
    val imageUrl: String
)

@Composable
fun AnimalsScreen() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "animals_list") {
        composable("animals_list") {
            AnimalsListScreen(navController)
        }
        composable("animal_detail/{animalId}") { backStackEntry ->
            val animalId = backStackEntry.arguments?.getString("animalId")?.toIntOrNull()
            if (animalId != null) {
                AnimalDetailScreen(animalId, navController)
            }
        }
    }
}

@Composable
fun AnimalsListScreen(navController: NavController) {
    val animals = remember {
        listOf(
            Animal(1, "Lion", "The king of the jungle", "lion_url"),
            Animal(2, "Elephant", "The largest land animal", "elephant_url"),
            Animal(3, "Giraffe", "The tallest animal", "giraffe_url")
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(animals) { animal ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                onClick = { navController.navigate("animal_detail/${animal.id}") }
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = animal.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = animal.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun AnimalDetailScreen(animalId: Int, navController: NavController) {
    val animal = remember {
        when (animalId) {
            1 -> Animal(1, "Lion", "The king of the jungle", "lion_url")
            2 -> Animal(2, "Elephant", "The largest land animal", "elephant_url")
            3 -> Animal(3, "Giraffe", "The tallest animal", "giraffe_url")
            else -> null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (animal != null) {
            Text(
                text = animal.name,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = animal.description,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text("Animal not found")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigateUp() }) {
            Text("Back to List")
        }
    }
} 