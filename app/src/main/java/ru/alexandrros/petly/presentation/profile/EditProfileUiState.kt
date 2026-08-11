package ru.alexandrros.petly.presentation.profile

import ru.alexandrros.petly.domain.model.User

data class EditProfileUiState(
        val isLoading: Boolean = true,
        val currentUser: User? = null,
        val name: String = "",
        val isSaving: Boolean = false
)
