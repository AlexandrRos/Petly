package ru.alexandrros.petly.presentation.userpets

import ru.alexandrros.petly.domain.model.Pet

data class PetsUiState(
    val isLoading: Boolean = true,
    val pets: List<Pet> = emptyList(),
    val errorMessage: String? = null
)