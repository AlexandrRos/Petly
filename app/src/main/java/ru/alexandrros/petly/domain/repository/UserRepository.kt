package ru.alexandrros.petly.domain.repository

import ru.alexandrros.petly.domain.model.User

interface UserRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, name: String): Result<User>
}