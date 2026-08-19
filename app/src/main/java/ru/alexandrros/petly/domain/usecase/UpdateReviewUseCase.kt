package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.model.Review
import ru.alexandrros.petly.domain.repository.ReviewRepository

class UpdateReviewUseCase(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(reviewId: String, review: Review): Result<Unit> {
        return reviewRepository.updateReview(reviewId, review)
    }
}