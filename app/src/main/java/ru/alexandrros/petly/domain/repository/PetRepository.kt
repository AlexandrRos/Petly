package ru.alexandrros.petly.domain.repository

import ru.alexandrros.petly.domain.model.Pet

interface PetRepository {
    fun getAllPets(): List<Pet>
    fun getPetById(id: Int): Pet?
}