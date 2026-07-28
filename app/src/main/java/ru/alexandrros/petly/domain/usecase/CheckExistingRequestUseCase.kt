package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.repository.RequestRepository

class CheckExistingRequestUseCase(private val requestRepository: RequestRepository) {
    suspend operator fun invoke(userId: String, petId: String): Boolean =
        requestRepository.checkExistingRequest(userId, petId)
}