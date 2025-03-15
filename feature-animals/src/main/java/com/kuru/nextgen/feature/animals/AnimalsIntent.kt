package com.kuru.nextgen.feature.animals

sealed class AnimalsIntent {
    object LoadAnimals : AnimalsIntent()
    data class SelectAnimal(val animalId: Int) : AnimalsIntent()
    object NavigateBack : AnimalsIntent()
    data class ShowError(val message: String) : AnimalsIntent()
} 