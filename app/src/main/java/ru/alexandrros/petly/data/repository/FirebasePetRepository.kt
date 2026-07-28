package ru.alexandrros.petly.data.repository;


import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.repository.PetRepository


class FirebasePetRepository(private val userId: String) : PetRepository {

    private val petsCollection = FirebaseFirestore.getInstance()
        .collection("users").document(userId).collection("pets")

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    override fun getAllPets(): StateFlow<List<Pet>> = _pets

    init {
        petsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val pets = snapshot?.documents?.mapNotNull { doc ->
                doc.toPet(userId)
            } ?: emptyList()
            _pets.value = pets
        }
    }

    override fun getPetById(id: String): Pet? {
        return _pets.value.find { it.id == id }
    }

    override fun getPetById(userId: String, petId: String): Flow<Pet?> = flow {
        val docRef = FirebaseFirestore.getInstance()
            .collection("users").document(userId)
            .collection("pets").document(petId)
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            emit(snapshot.toPet(userId))
        } else {
            emit(null)
        }
    }.catch { e ->
        if (e is kotlinx.coroutines.CancellationException) throw e
        Log.e("FirebasePetRepo", "Error fetching pet by id", e)
        emit(null)
    }

    override suspend fun addPet(pet: Pet) {
        val docRef = petsCollection.document()
        val petWithId = pet.copy(id = docRef.id)
        docRef.set(petWithId.toMap()).await()
    }

    override suspend fun updatePet(updatedPet: Pet) {
        petsCollection.document(updatedPet.id).set(updatedPet.toMap()).await()
    }

    override suspend fun deletePet(petId: String) {
        petsCollection.document(petId).delete().await()
    }
}

// Extension functions
fun Pet.toMap(): Map<String, Any?> {
    return mapOf(
        "userId" to userId,
        "name" to name,
        "species" to species,
        "breed" to breed,
        "age" to age,
        "weight" to weight,
        "isMale" to isMale,
        "sterilizationStatus" to sterilizationStatus,
        "vaccinations" to vaccinations,
        "chronicDiseases" to chronicDiseases,
        "allergies" to allergies,
        "personalityTraits" to personalityTraits,
        "feedingSchedule" to feedingSchedule,
        "walkingSchedule" to walkingSchedule,
        "medications" to medications,
        "photoRes" to photoRes
    )
}

fun DocumentSnapshot.toPet(userId: String): Pet? {
    return try {
        Pet(
            id = id,
            userId = userId,
            name = getString("name") ?: "",
            species = getString("species") ?: "",
            breed = getString("breed"),
            age = getLong("age")?.toInt(),
            weight = getDouble("weight"),
            isMale = getBoolean("isMale") ?: true,
            sterilizationStatus = getBoolean("sterilizationStatus"),
            vaccinations = get("vaccinations") as? List<String> ?: emptyList(),
            chronicDiseases = get("chronicDiseases") as? List<String> ?: emptyList(),
            allergies = get("allergies") as? List<String> ?: emptyList(),
            personalityTraits = get("personalityTraits") as? List<String> ?: emptyList(),
            feedingSchedule = getString("feedingSchedule"),
            walkingSchedule = getString("walkingSchedule"),
            medications = get("medications") as? List<String> ?: emptyList(),
            photoRes = getLong("photoRes")?.toInt()
        )
    } catch (e: Exception) { null }
}