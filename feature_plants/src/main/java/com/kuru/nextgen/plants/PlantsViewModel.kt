package com.kuru.nextgen.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlantsViewModel : ViewModel() {
    private val _state = MutableStateFlow(PlantsState())
    val state: StateFlow<PlantsState> = _state.asStateFlow()

    init {
        loadPlants()
    }

    private fun loadPlants() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                // Simulate network call
                val plants = listOf(
                    Plant(
                        id = 1,
                        name = "Snake Plant",
                        species = "Sansevieria trifasciata",
                        description = "A hardy, low-maintenance plant known for its air-purifying qualities.",
                        careInstructions = "Place in indirect light, water every 2-3 weeks.",
                        wateringFrequency = "Every 2-3 weeks"
                    ),
                    Plant(
                        id = 2,
                        name = "Peace Lily",
                        species = "Spathiphyllum",
                        description = "Beautiful flowering plant that thrives in low light conditions.",
                        careInstructions = "Keep soil moist but not soggy, place in low to medium light.",
                        wateringFrequency = "Weekly"
                    ),
                    Plant(
                        id = 3,
                        name = "Spider Plant",
                        species = "Chlorophytum comosum",
                        description = "Easy to grow plant that produces baby plants on long stems.",
                        careInstructions = "Water when top soil is dry, bright indirect light.",
                        wateringFrequency = "Every 1-2 weeks"
                    )
                )
                _state.value = _state.value.copy(
                    plants = plants,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Failed to load plants",
                    isLoading = false
                )
            }
        }
    }

    fun handleIntent(intent: PlantsIntent) {
        when (intent) {
            is PlantsIntent.LoadPlants -> loadPlants()
            is PlantsIntent.SelectPlant -> {
                _state.value = _state.value.copy(
                    selectedPlant = _state.value.plants.find { it.id == intent.id }
                )
            }
            is PlantsIntent.NavigateBack -> {
                _state.value = _state.value.copy(selectedPlant = null)
            }
            is PlantsIntent.ShowError -> {
                _state.value = _state.value.copy(error = intent.message)
            }
        }
    }
} 