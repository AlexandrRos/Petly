package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.repository.RequestRepository


class DeleteRequestUseCase(private val requestRepository: RequestRepository) {
    suspend operator fun invoke(requestId: String): Result<Unit> {
        return requestRepository.deleteRequest(requestId)
    }
}