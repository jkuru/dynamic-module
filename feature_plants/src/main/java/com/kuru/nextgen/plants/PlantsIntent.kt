package com.kuru.nextgen.plants

sealed class PlantsIntent {
    object LoadPlants : PlantsIntent()
    data class SelectPlant(val id: Int) : PlantsIntent()
    object NavigateBack : PlantsIntent()
    data class ShowError(val message: String) : PlantsIntent()
} 