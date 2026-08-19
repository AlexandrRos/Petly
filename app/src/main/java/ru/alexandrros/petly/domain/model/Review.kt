package ru.alexandrros.petly.domain.model

data class Review(
    val id: String = "",
    val reviewerId: String,
    val reviewerName: String,
    val reviewedUserId: String,
    val rating: Int,
    val comment: String,
    val type: ReviewType,
    val timestamp: Long = System.currentTimeMillis()
)