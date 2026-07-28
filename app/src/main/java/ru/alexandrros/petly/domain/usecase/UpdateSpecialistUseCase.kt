package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.repository.UserRepository

class UpdateSpecialistUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(uid: String, specialist: String): Result<Unit> =
        userRepository.updateSpecialist(uid, specialist)
}