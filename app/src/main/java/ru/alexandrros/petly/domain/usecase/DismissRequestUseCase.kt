package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.repository.RequestRepository

class DismissRequestUseCase(private val requestRepository: RequestRepository) {
    suspend operator fun invoke(requestId: String): Result<Unit> =
        requestRepository.dismissRequest(requestId)
}