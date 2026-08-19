package ru.alexandrros.petly.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.alexandrros.petly.domain.model.Review
import ru.alexandrros.petly.domain.model.ReviewType
import ru.alexandrros.petly.domain.repository.ReviewRepository

class GetUserReviewsUseCase(
    private val reviewRepository: ReviewRepository
) {
    operator fun invoke(uid: String, type: ReviewType? = null): Flow<List<Review>> {
        return reviewRepository.observeReviewsForUser(uid, type)
    }
}