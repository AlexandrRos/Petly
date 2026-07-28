package ru.alexandrros.petly.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.repository.RequestRepository

import kotlin.Result


class FirebaseRequestRepository : RequestRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val requestsCollection = firestore.collection("requests")

    override suspend fun createRequest(request: Request): Result<String> {
        return try {
            val docRef = requestsCollection.add(request).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("FirebaseRequestRepo", "Create request error", e)
            Result.failure(e)
        }
    }

    override suspend fun acceptRequest(requestId: String, specialistUserId: String): Result<Unit> {
        return try {
            requestsCollection.document(requestId)
                .update("specialistUserId", specialistUserId)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRequestRepo", "Accept request error", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteRequest(requestId: String): Result<Unit> {
        return try {
            requestsCollection.document(requestId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRequestRepo", "Delete request error", e)
            Result.failure(e)
        }
    }

    override suspend fun getRequestById(requestId: String): Request? {
        return try {
            val doc = requestsCollection.document(requestId).get().await()
            if (doc.exists()) {
                doc.toObject(Request::class.java)?.copy(id = doc.id)
            } else null
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirebaseRequestRepo", "Get request by id error", e)
            null
        }
    }

    override suspend fun checkExistingRequest(userId: String, petId: String): Boolean {
        return try {
            val querySnapshot = requestsCollection
                .whereEqualTo("creatorUserId", userId)
                .whereEqualTo("petId", petId)
                .get()
                .await()
            !querySnapshot.isEmpty
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirebaseRequestRepo", "Check existing request error", e)
            false
        }
    }

    override fun getRequestsByCreator(userId: String): Flow<List<Request>> {
        return requestsCollection
            .whereEqualTo("creatorUserId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Request::class.java)?.copy(id = doc.id)
                }
            }
    }

    override fun getRequestsBySpecialist(userId: String): Flow<List<Request>> {
        return requestsCollection
            .whereEqualTo("specialistUserId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Request::class.java)?.copy(id = doc.id)
                }
            }
    }

    override fun getAllRequests(): Flow<List<Request>> {
        return requestsCollection
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Request::class.java)?.copy(id = doc.id)
                }
            }
    }
}