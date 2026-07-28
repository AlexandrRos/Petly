package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.repository.RequestRepository

class CreateRequestUseCase(private val requestRepository: RequestRepository) {
    suspend operator fun invoke(request: Request): Result<String> =
        requestRepository.createRequest(request)
}