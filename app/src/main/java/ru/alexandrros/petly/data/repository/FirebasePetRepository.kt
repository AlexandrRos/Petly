package ru.alexandrros.petly.data.repository;

import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.repository.PetRepository


import com.google.firebase.firestore.FirebaseFirestore;
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class FirebasePetRepository(private val userId: String) : PetRepository {

    private val petsCollection = FirebaseFirestore.getInstance()
        .collection("users").document(userId).collection("pets")

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    override fun getAllPets(): StateFlow<List<Pet>> = _pets

    init {
        // Listen for real-time updates
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

    override suspend fun addPet(pet: Pet) {
        // Add a new document, let Firestore generate the ID
        val docRef = petsCollection.document()   // auto-id
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

// Extension functions to convert between Pet and Map
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