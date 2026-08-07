package ru.alexandrros.petly.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.domain.repository.UserRepository

class GetAllSpecialistsUseCase(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<List<User>> = repository.getAllSpecialists()
}