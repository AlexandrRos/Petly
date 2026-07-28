package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.repository.UserRepository

class LogoutUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke() = userRepository.logout()
}