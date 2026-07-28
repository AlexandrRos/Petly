package ru.alexandrros.petly.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alexandrros.petly.domain.model.Request


interface RequestRepository {
    suspend fun createRequest(request: Request): Result<String>
    suspend fun acceptRequest(requestId: String, specialistUserId: String): Result<Unit>
    suspend fun deleteRequest(requestId: String): Result<Unit>
    fun getRequestsByCreator(userId: String): Flow<List<Request>>
    fun getRequestsBySpecialist(userId: String): Flow<List<Request>>
    fun getAllRequests(): Flow<List<Request>>
    suspend fun getRequestById(requestId: String): Request?
    suspend fun checkExistingRequest(userId: String, petId: String): Boolean
}