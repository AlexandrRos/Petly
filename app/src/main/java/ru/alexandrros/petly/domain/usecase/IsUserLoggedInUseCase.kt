package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.repository.UserRepository

class IsUserLoggedInUseCase(private val repository: UserRepository) {
    operator fun invoke(): Boolean = repository.isUserLoggedIn()
}