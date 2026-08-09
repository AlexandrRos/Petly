package ru.alexandrros.petly.data.remote.datasource

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import ru.alexandrros.petly.data.remote.model.PetDto
import kotlin.collections.mapNotNull
import kotlin.coroutines.cancellation.CancellationException

class FirebasePetDataSource {

    private fun petsCollection(userId: String) =
        FirebaseFirestore.getInstance()
            .collection("users").document(userId)
            .collection("pets")

    fun getPetsByUser(userId: String): Flow<List<PetDto>> = callbackFlow {
        val listener = petsCollection(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val pets = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject<PetDto>()?.copy(id = doc.id)
            } ?: emptyList()
            trySend(pets)
        }
        awaitClose { listener.remove() }
    }

    suspend fun getPetById(userId: String, petId: String): PetDto? {
        val docRef = petsCollection(userId).document(petId)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) snapshot.toObject<PetDto>()?.copy(id = snapshot.id)
            else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirebasePetDS", "getPetById error", e)
            null
        }
    }

    //for PetDetailScreen update after editing
    fun observePetById(userId: String, petId: String): Flow<PetDto?> = callbackFlow {
        val docRef = petsCollection(userId).document(petId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val dto = snapshot.toObject<PetDto>()?.copy(id = snapshot.id)
                trySend(dto)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun getPetByIdCachedFirst(userId: String, petId: String): PetDto? {
        val cacheDoc = try {
            petsCollection(userId).document(petId)
                .get(Source.CACHE).await()
        } catch (e: Exception) {
            null
        }
        if (cacheDoc != null && cacheDoc.exists()) {
            return cacheDoc.toObject<PetDto>()?.copy(id = petId)
        }
        return getPetById(userId, petId)
    }

    suspend fun addPet(userId: String, pet: PetDto): String {
        val docRef = petsCollection(userId).document()
        val petWithId = pet.copy(id = docRef.id)
        docRef.set(petWithId).await()
        return docRef.id
    }

    suspend fun updatePet(userId: String, pet: PetDto) {
        petsCollection(userId).document(pet.id).set(pet).await()
    }

    suspend fun deletePet(userId: String, petId: String) {
        petsCollection(userId).document(petId).delete().await()
    }
}