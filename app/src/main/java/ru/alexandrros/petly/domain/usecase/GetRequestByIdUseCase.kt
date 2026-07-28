package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.repository.RequestRepository


class GetRequestByIdUseCase(private val requestRepository: RequestRepository) {
    suspend operator fun invoke(requestId: String): Request? =
        requestRepository.getRequestById(requestId)
}