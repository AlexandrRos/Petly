package ru.alexandrros.petly.presentation.profile

import ru.alexandrros.petly.domain.model.User

data class ProfileUiState(
    val isLoading: Boolean = true,
    val currentUser: User? = null,
    val isUpdatingPhoto: Boolean = false
)