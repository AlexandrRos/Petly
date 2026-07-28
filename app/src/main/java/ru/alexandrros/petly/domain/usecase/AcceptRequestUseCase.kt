package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.repository.RequestRepository


class AcceptRequestUseCase(private val requestRepository: RequestRepository) {
    suspend operator fun invoke(requestId: String, specialistUserId: String): Result<Unit> {
        return requestRepository.acceptRequest(requestId, specialistUserId)
    }
}