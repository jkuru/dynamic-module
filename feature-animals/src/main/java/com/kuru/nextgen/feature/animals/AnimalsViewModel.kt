package com.kuru.nextgen.feature.animals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnimalsViewModel : ViewModel() {
    private val _state = MutableStateFlow<AnimalsState>(AnimalsState.Loading)
    val state: StateFlow<AnimalsState> = _state.asStateFlow()

    fun handleIntent(intent: AnimalsIntent) {
        when (intent) {
            is AnimalsIntent.LoadAnimals -> loadAnimals()
            is AnimalsIntent.SelectAnimal -> selectAnimal(intent.animalId)
            is AnimalsIntent.NavigateBack -> navigateBack()
            is AnimalsIntent.ShowError -> _state.value = AnimalsState.Error(intent.message)
        }
    }

    private fun loadAnimals() {
        viewModelScope.launch {
            try {
                // TODO: Replace with actual data loading
                val animals = listOf(
                    Animal(1, "Lion", "King of the jungle"),
                    Animal(2, "Elephant", "Largest land animal"),
                    Animal(3, "Giraffe", "Tallest land animal")
                )
                _state.value = AnimalsState.Success(animals = animals)
            } catch (e: Exception) {
                _state.value = AnimalsState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun selectAnimal(animalId: Int) {
        val currentState = _state.value
        if (currentState is AnimalsState.Success) {
            val selectedAnimal = currentState.animals.find { it.id == animalId }
            _state.value = currentState.copy(selectedAnimal = selectedAnimal)
        }
    }

    private fun navigateBack() {
        val currentState = _state.value
        if (currentState is AnimalsState.Success) {
            _state.value = currentState.copy(selectedAnimal = null)
        }
    }
} 