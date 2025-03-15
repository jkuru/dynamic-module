package com.kuru.nextgen.plants

data class PlantsState(
    val plants: List<Plant> = emptyList(),
    val selectedPlant: Plant? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class Plant(
    val id: Int,
    val name: String,
    val species: String,
    val description: String,
    val careInstructions: String,
    val wateringFrequency: String
) 