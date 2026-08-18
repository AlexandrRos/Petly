package ru.alexandrros.petly.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.alexandrros.petly.domain.model.UserRating
import ru.alexandrros.petly.domain.repository.UserRatingRepository

class GetUserRatingUseCase(
    private val repository: UserRatingRepository
) {
    operator fun invoke(userId: String): Flow<UserRating> =
        repository.getRatingByUserId(userId)
}