package ru.alexandrros.petly.domain.repository

import ru.alexandrros.petly.domain.model.User

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, name: String): Result<User>
    fun getCurrentUser(): Flow<User?>
    suspend fun logout()   // new
}