package ru.alexandrros.petly.presentation.requests

import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.model.UserRating

enum class RequestListViewMode {
    MY_REQUESTS,
    AVAILABLE,
    ACCEPTED_BY_ME
}

data class RequestsUiState(
    val requests: List<Request> = emptyList(),
    val viewMode: RequestListViewMode = RequestListViewMode.MY_REQUESTS,
    val photoCache: Map<String, ByteArray?> = emptyMap(),
    //Can be used in the future for request filters
    val currentUserSpecialist: String? = null,
    // Ratings cache for request creators
    val ratings: Map<String, UserRating?> = emptyMap(),
    // Filtering fields
    val isFilterDialogVisible: Boolean = false,
    val cityFilter: String = "",
    val minCost: String = "",
    val maxCost: String = "",
    val minRating: Float = 0f
) {
    val isSpecialist: Boolean
        get() = currentUserSpecialist != null && currentUserSpecialist != "None"
}