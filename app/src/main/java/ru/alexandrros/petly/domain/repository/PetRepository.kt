package ru.alexandrros.petly.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ru.alexandrros.petly.domain.model.Pet

interface PetRepository {
    fun getAllPets(): StateFlow<List<Pet>>
    fun getPetById(id: Int): Pet?
    fun addPet(pet: Pet)
    fun updatePet(updatedPet: Pet)
}