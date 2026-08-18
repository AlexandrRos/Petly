package ru.alexandrros.petly.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.alexandrros.petly.domain.model.Review
import ru.alexandrros.petly.domain.repository.UserRatingRepository

class GetUserReviewsUseCase(
    private val repository: UserRatingRepository
) {
    operator fun invoke(userId: String): Flow<List<Review>> =
        repository.getReviewsByUserId(userId)
}