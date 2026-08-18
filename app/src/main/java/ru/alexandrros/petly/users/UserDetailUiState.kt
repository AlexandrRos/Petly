package ru.alexandrros.petly.users

import ru.alexandrros.petly.domain.model.User

data class UserDetailUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val errorMessage: String? = null
)