package ru.alexandrros.petly.presentation.requests

import ru.alexandrros.petly.domain.model.Request


data class RequestsUiState(
    val requests: List<Request> = emptyList(),
    val showMyRequests: Boolean = true,
    val isSpecialist: Boolean = false,
    val photoCache: Map<String, ByteArray?> = emptyMap(),
)