package ru.alexandrros.petly.presentation.requests

data class RequestCreationUiState(
    val isLoading: Boolean = false,
    val city: String = "",
    val cost: String = "",
    val startDate: Long? = null,
    val endDate: Long? = null,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)