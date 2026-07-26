package ru.alexandrros.petly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import ru.alexandrros.petly.data.repository.FakePetRepository
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.repository.PetRepository

class PetListViewModel(
    private val repository: PetRepository = FakePetRepository()
) : ViewModel() {
    val pets: StateFlow<List<Pet>> = repository.getAllPets()

    fun getPetById(id: Int): Pet? = repository.getPetById(id)

    fun addPet(pet: Pet) {
        repository.addPet(pet)
    }

    fun updatePet(updatedPet: Pet) {
        repository.updatePet(updatedPet)
    }
}