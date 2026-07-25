package ru.alexandrros.petly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import ru.alexandrros.petly.data.repository.FakePetRepository
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.repository.PetRepository

class PetListViewModel(
    private val repository: PetRepository = FakePetRepository()
) : ViewModel() {
    val pets: List<Pet> = repository.getAllPets()

    fun getPetById(id: Int): Pet? = repository.getPetById(id)
}