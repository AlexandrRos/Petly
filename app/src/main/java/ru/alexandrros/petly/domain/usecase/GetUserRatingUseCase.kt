package ru.alexandrros.petly.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.alexandrros.petly.domain.model.UserRating
import ru.alexandrros.petly.domain.repository.ReviewRepository

class GetUserRatingUseCase(
    private val reviewRepository: ReviewRepository
) {
    operator fun invoke(uid: String): Flow<UserRating?> {
        return reviewRepository.observeUserRating(uid)
    }
}