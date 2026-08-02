package ru.alexandrros.petly.domain.usecase;

import ru.alexandrros.petly.domain.repository.UserRepository;

class UpdateUserProfileUseCase(private val userRepository:UserRepository) {
    suspend operator fun invoke(uid: String, name: String): Result<Unit> {
        return userRepository.updateUserName(uid, name)
    }
}
