package ru.alexandrros.petly.data.remote.mapper

import ru.alexandrros.petly.data.remote.model.ReviewDto
import ru.alexandrros.petly.domain.model.Review
import ru.alexandrros.petly.domain.model.ReviewType

fun ReviewDto.toDomain(): Review = Review(
    id = documentId,
    reviewerId = reviewerId,
    reviewerName = reviewerName,
    reviewedUserId = reviewedUserId,
    rating = rating,
    comment = comment,
    type = ReviewType.valueOf(type),
    timestamp = timestamp
)

fun Review.toDto(): ReviewDto = ReviewDto(
    documentId = id,
    reviewerId = reviewerId,
    reviewerName = reviewerName,
    reviewedUserId = reviewedUserId,
    rating = rating,
    comment = comment,
    type = type.name,
    timestamp = timestamp
)