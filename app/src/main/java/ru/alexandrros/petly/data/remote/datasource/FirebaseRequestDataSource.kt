package ru.alexandrros.petly.data.remote.datasource

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import ru.alexandrros.petly.data.remote.model.RequestDto
import kotlin.collections.mapNotNull
import kotlin.coroutines.cancellation.CancellationException

class FirebaseRequestDataSource {

    private val firestore = FirebaseFirestore.getInstance()
    private val requestsCollection = firestore.collection("requests")

    suspend fun createRequest(request: RequestDto): String {
        val docRef = requestsCollection.add(request).await()
        return docRef.id
    }

    suspend fun acceptRequest(requestId: String, specialistUserId: String) {
        requestsCollection.document(requestId)
            .update("specialistUserId", specialistUserId)
            .await()
    }

    suspend fun deleteRequest(requestId: String) {
        requestsCollection.document(requestId).delete().await()
    }

    suspend fun getRequestById(requestId: String): RequestDto? {
        return try {
            val doc = requestsCollection.document(requestId).get().await()
            if (doc.exists()) {
                doc.toObject<RequestDto>()?.copy(documentId = doc.id)
            } else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirebaseRequestDS", "getRequestById error", e)
            null
        }
    }

    suspend fun checkExistingRequest(userId: String, petId: String): Boolean {
        return try {
            val querySnapshot = requestsCollection
                .whereEqualTo("creatorUserId", userId)
                .whereEqualTo("petId", petId)
                .get()
                .await()
            !querySnapshot.isEmpty
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirebaseRequestDS", "checkExistingRequest error", e)
            false
        }
    }

    fun getRequestsByCreator(userId: String): Flow<List<RequestDto>> {
        return requestsCollection
            .whereEqualTo("creatorUserId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject<RequestDto>()?.copy(documentId = doc.id)
                }
            }
    }

    fun getRequestsBySpecialist(userId: String): Flow<List<RequestDto>> {
        return requestsCollection
            .whereEqualTo("specialistUserId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject<RequestDto>()?.copy(documentId = doc.id)
                }
            }
    }

    fun getAllRequests(): Flow<List<RequestDto>> {
        return requestsCollection
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject<RequestDto>()?.copy(documentId = doc.id)
                }
            }
    }
}