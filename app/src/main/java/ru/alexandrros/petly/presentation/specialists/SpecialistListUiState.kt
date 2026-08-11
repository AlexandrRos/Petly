package ru.alexandrros.petly.presentation.specialists

import ru.alexandrros.petly.domain.model.User

data class SpecialistListUiState(
    val isLoading: Boolean = true,
    val specialists: List<User> = emptyList(),
    val errorMessage: String? = null
)