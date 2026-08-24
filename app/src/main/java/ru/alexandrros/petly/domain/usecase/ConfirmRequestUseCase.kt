package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.repository.RequestRepository

class ConfirmRequestUseCase(private val requestRepository: RequestRepository) {
    suspend operator fun invoke(requestId: String): Result<Unit> =
        requestRepository.confirmRequest(requestId)
}