package ru.alexandrros.petly.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alexandrros.petly.domain.model.Review
import ru.alexandrros.petly.domain.model.UserRating


interface UserRatingRepository {
    fun getRatingByUserId(userId: String): Flow<UserRating>
    fun getReviewsByUserId(userId: String): Flow<List<Review>>
}