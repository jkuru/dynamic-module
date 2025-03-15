package com.kuru.nextgen.feature.cars

sealed class CarsIntent {
    object LoadCars : CarsIntent()
    data class SelectCar(val carId: Int) : CarsIntent()
    object NavigateBack : CarsIntent()
    data class ShowError(val message: String) : CarsIntent()
} 