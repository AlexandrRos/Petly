package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.domain.repository.UserRepository
import kotlin.Result.Companion.failure


class RegisterUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(email: String, password: String, name: String): Result<User> {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            return failure(IllegalArgumentException("Все поля обязательны"))
        }
        if (password.length < 6) {
            return failure(IllegalArgumentException("Пароль должен быть не менее 6 символов"))
        }
        return repository.register(email, password, name)
    }
}