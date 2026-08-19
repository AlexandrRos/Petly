package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.repository.ReviewRepository

class DeleteReviewUseCase(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(reviewId: String): Result<Unit> {
        return reviewRepository.deleteReview(reviewId)
    }
}