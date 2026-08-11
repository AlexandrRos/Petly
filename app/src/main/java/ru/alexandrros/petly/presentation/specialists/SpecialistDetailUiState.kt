package ru.alexandrros.petly.presentation.specialists

import ru.alexandrros.petly.domain.model.User

data class SpecialistDetailUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val errorMessage: String? = null
)