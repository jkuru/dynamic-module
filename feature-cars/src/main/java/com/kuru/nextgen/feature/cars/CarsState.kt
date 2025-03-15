package com.kuru.nextgen.feature.cars

sealed class CarsState {
    object Loading : CarsState()
    data class Success(
        val cars: List<Car>,
        val selectedCar: Car? = null
    ) : CarsState()
    data class Error(val message: String) : CarsState()
}

data class Car(
    val id: Int,
    val name: String,
    val description: String
) 