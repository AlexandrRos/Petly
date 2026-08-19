package ru.alexandrros.petly.data.remote.model

import com.google.firebase.firestore.DocumentId
import ru.alexandrros.petly.domain.model.ReviewType

data class ReviewDto(
    @DocumentId val documentId: String = "",
    val reviewerId: String = "",
    val reviewerName: String = "",
    val reviewedUserId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val type: String = ReviewType.AS_SPECIALIST.name,
    val timestamp: Long = 0L
)