package com.kuru.nextgen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kuru.featureflow.component.ui.DFComponentActivity
import com.kuru.nextgen.feature.animals.AnimalsScreen
import com.kuru.nextgen.feature.cars.CarsScreen


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }

}

@Composable
fun MainScreen(

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
                val intent: Intent = Intent(
                    navController.context,
                    DFComponentActivity::class.java
                )
                intent.putExtra("uri", "/chase/df/route/feature_plants")
                navController.context.startActivity(intent)
            }
        }
    }
}



