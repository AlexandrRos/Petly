package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.repository.UserRepository

class UpdateUserPhotoUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(uid: String, photoBytes: ByteArray): Result<Unit> {
        return userRepository.updateUserPhoto(uid, photoBytes)
    }
}