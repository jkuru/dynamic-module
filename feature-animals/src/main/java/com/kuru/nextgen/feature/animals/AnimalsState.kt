package com.kuru.nextgen.feature.animals

sealed class AnimalsState {
    object Loading : AnimalsState()
    data class Success(
        val animals: List<Animal>,
        val selectedAnimal: Animal? = null
    ) : AnimalsState()
    data class Error(val message: String) : AnimalsState()
}

data class Animal(
    val id: Int,
    val name: String,
    val description: String
) 