package ru.alexandrros.petly.domain.model

data class Review(
    val id: String,
    val targetUserId: String,
    val reviewerId: String,
    val reviewerName: String,
    val rating: Int,
    val comment: String,
    val type: ReviewType,
    val createdAt: Long
)