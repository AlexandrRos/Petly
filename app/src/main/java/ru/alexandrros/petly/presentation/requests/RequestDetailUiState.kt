package ru.alexandrros.petly.presentation.requests

import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.model.User

enum class DeletionState { IDLE, DELETING, SUCCESS, ERROR }

data class RequestDetailUiState(
    val isLoading: Boolean = true,
    val request: Request? = null,
    val pet: Pet? = null,
    val specialistUser: User? = null,
    val ownerUser: User? = null,
    val currentUserId: String? = null,
    val currentUserSpecialist: String? = null,
    val deletionState: DeletionState = DeletionState.IDLE
) {
    val canAccept: Boolean
        get() = currentUserId != null &&
                request != null &&
                request.specialistUserId == null &&
                currentUserSpecialist != null &&
                currentUserSpecialist != "None"

    val canConfirm: Boolean
        get() = currentUserId != null &&
                request != null &&
                request.creatorUserId == currentUserId &&
                request.status == Request.STATUS_ACCEPTED

    val canDismiss: Boolean
        get() = currentUserId != null &&
                request != null &&
                request.creatorUserId == currentUserId &&
                request.status == Request.STATUS_ACCEPTED

    val isOwner: Boolean
        get() = currentUserId != null &&
                request != null &&
                currentUserId == request.creatorUserId
}