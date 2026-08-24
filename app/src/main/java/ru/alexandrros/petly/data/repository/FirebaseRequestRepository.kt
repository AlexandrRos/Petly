package ru.alexandrros.petly.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.alexandrros.petly.data.remote.datasource.FirebaseRequestDataSource
import ru.alexandrros.petly.data.remote.mapper.toDomain
import ru.alexandrros.petly.data.remote.mapper.toDto
import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.repository.RequestRepository
import kotlin.coroutines.cancellation.CancellationException


class FirebaseRequestRepository(
    private val dataSource: FirebaseRequestDataSource = FirebaseRequestDataSource()
) : RequestRepository {

    override suspend fun createRequest(request: Request): Result<String> {
        return try {
            val dto = request.toDto()
            val docId = dataSource.createRequest(dto)
            Result.success(docId)
        } catch (e: Exception) {
            Log.e("FirebaseRequestRepo", "Create request error", e)
            Result.failure(e)
        }
    }

    override suspend fun acceptRequest(requestId: String, specialistUserId: String): Result<Unit> {
        return try {
            dataSource.acceptRequest(requestId, specialistUserId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRequestRepo", "Accept request error", e)
            Result.failure(e)
        }
    }

    override suspend fun confirmRequest(requestId: String): Result<Unit> {
        return try {
            dataSource.confirmRequest(requestId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRequestRepo", "Confirm request error", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteRequest(requestId: String): Result<Unit> {
        return try {
            dataSource.deleteRequest(requestId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRequestRepo", "Delete request error", e)
            Result.failure(e)
        }
    }

    override suspend fun dismissRequest(requestId: String): Result<Unit> {
        return try {
            dataSource.dismissRequest(requestId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRequestRepo", "Dismiss request error", e)
            Result.failure(e)
        }
    }

    override suspend fun getRequestById(requestId: String): Request? {
        return try {
            dataSource.getRequestById(requestId)?.toDomain()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirebaseRequestRepo", "Get request by id error", e)
            null
        }
    }

    override suspend fun checkExistingRequest(userId: String, petId: String): Boolean {
        return try {
            dataSource.checkExistingRequest(userId, petId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirebaseRequestRepo", "Check existing request error", e)
            false
        }
    }

    override fun getRequestsByCreator(userId: String): Flow<List<Request>> {
        return dataSource.getRequestsByCreator(userId)
            .map { dtos -> dtos.map { it.toDomain() } }
    }

    override fun getRequestsBySpecialist(userId: String): Flow<List<Request>> {
        return dataSource.getRequestsBySpecialist(userId)
            .map { dtos -> dtos.map { it.toDomain() } }
    }

    override fun getAllRequests(): Flow<List<Request>> {
        return dataSource.getAllRequests()
            .map { dtos -> dtos.map { it.toDomain() } }
    }
}