package ru.alexandrros.petly.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.domain.repository.UserRepository


class ObserveCurrentUserUseCase(private val userRepository: UserRepository) {
    operator fun invoke(): Flow<User?> = userRepository.getCurrentUser()
}