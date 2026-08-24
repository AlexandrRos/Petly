package ru.alexandrros.petly.data.remote.model

import com.google.firebase.firestore.DocumentId

data class RequestDto(
    @DocumentId val documentId: String = "",
    val creatorUserId: String = "",
    val petId: String = "",
    val petName: String = "",
    val species: String = "",
    val specialistUserId: String? = null,
    val city: String = "",
    val cost: Double? = null,
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val status: String = STATUS_PENDING,
    val createdAt: Long = 0L
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_ACCEPTED = "accepted"
        const val STATUS_CONFIRMED = "confirmed"
    }
}