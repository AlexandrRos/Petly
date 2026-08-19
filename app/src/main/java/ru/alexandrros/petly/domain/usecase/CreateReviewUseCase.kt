package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.model.Review
import ru.alexandrros.petly.domain.repository.ReviewRepository

class CreateReviewUseCase(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(review: Review): Result<String> {
        return reviewRepository.saveReview(review)
    }
}