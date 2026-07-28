package ru.alexandrros.petly.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.repository.RequestRepository


class GetAllRequestsUseCase(private val requestRepository: RequestRepository) {
    operator fun invoke(): Flow<List<Request>> =
        requestRepository.getAllRequests()
}