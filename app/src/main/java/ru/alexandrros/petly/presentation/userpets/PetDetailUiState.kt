package ru.alexandrros.petly.presentation.userpets

import ru.alexandrros.petly.domain.model.Pet

data class PetDetailUiState(
    val isLoading: Boolean = true,
    val pet: Pet? = null,
    val isCreating: Boolean = false,
    val errorMessage: String? = null
)