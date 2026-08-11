package ru.alexandrros.petly.presentation.requests

import ru.alexandrros.petly.domain.model.Request


data class RequestsUiState(
    val requests: List<Request> = emptyList(),
    val showMyRequests: Boolean = true,
    val photoCache: Map<String, ByteArray?> = emptyMap(),
    //Can be used in the future for request filters
    val currentUserSpecialist: String? = null
) {
    val isSpecialist: Boolean
        get() = currentUserSpecialist != null && currentUserSpecialist != "None"
}