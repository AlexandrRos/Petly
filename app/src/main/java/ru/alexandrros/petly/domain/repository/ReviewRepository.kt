package ru.alexandrros.petly.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alexandrros.petly.domain.model.Review
import ru.alexandrros.petly.domain.model.ReviewType
import ru.alexandrros.petly.domain.model.UserRating

interface ReviewRepository {
    suspend fun saveReview(review: Review): Result<String>
    suspend fun updateReview(reviewId: String, review: Review): Result<Unit>
    suspend fun deleteReview(reviewId: String): Result<Unit>
    fun observeReviewsForUser(uid: String, type: ReviewType? = null): Flow<List<Review>>
    fun observeUserRating(uid: String): Flow<UserRating?>
}