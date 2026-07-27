package ru.alexandrros.petly.data.repository

import kotlinx.coroutines.delay
import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.domain.repository.UserRepository
import kotlin.time.Duration.Companion.milliseconds

class FakeUserRepository : UserRepository {

    private val users = listOf(
        UserCredentials("user@example.com", "password123", "Иван Петров"),
        UserCredentials("admin@test.com", "admin", "Администратор")
    )

    override suspend fun login(email: String, password: String): Result<User> {
        delay(1_500.milliseconds)
        val match = users.find { it.email == email && it.password == password }
        return if (match != null) {
            Result.success(User(email = match.email, name = match.name))
        } else {
            Result.failure(Exception("Неверный email или пароль"))
        }
    }

    private data class UserCredentials(
        val email: String,
        val password: String,
        val name: String
    )
}