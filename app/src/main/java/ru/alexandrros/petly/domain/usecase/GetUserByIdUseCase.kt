package ru.alexandrros.petly.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.domain.repository.UserRepository


class GetUserByIdUseCase(private val userRepository: UserRepository) {
    operator fun invoke(uid: String): Flow<User?> = userRepository.getUserById(uid)
}