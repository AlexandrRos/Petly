package ru.alexandrros.petly.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.alexandrros.petly.domain.model.Pet

interface PetRepository {
    fun getAllPets(): StateFlow<List<Pet>>
    fun getPetById(id: String): Pet?
    suspend fun addPet(pet: Pet)
    suspend fun updatePet(updatedPet: Pet)
    suspend fun deletePet(petId: String)
    fun getPetById(userId: String, petId: String): Flow<Pet?>
}