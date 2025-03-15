package com.kuru.nextgen.feature.cars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CarsViewModel : ViewModel() {
    private val _state = MutableStateFlow<CarsState>(CarsState.Loading)
    val state: StateFlow<CarsState> = _state.asStateFlow()

    fun handleIntent(intent: CarsIntent) {
        when (intent) {
            is CarsIntent.LoadCars -> loadCars()
            is CarsIntent.SelectCar -> selectCar(intent.carId)
            is CarsIntent.NavigateBack -> navigateBack()
            is CarsIntent.ShowError -> _state.value = CarsState.Error(intent.message)
        }
    }

    private fun loadCars() {
        viewModelScope.launch {
            try {
                // TODO: Replace with actual data loading
                val cars = listOf(
                    Car(1, "Tesla Model 3", "Electric sedan"),
                    Car(2, "BMW M3", "Sports sedan"),
                    Car(3, "Mercedes C-Class", "Luxury sedan")
                )
                _state.value = CarsState.Success(cars = cars)
            } catch (e: Exception) {
                _state.value = CarsState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun selectCar(carId: Int) {
        val currentState = _state.value
        if (currentState is CarsState.Success) {
            val selectedCar = currentState.cars.find { it.id == carId }
            _state.value = currentState.copy(selectedCar = selectedCar)
        }
    }

    private fun navigateBack() {
        val currentState = _state.value
        if (currentState is CarsState.Success) {
            _state.value = currentState.copy(selectedCar = null)
        }
    }
} 