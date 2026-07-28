package ru.alexandrros.petly.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.repository.PetRepository

class ObserveUserPetsUseCase(private val petRepository: PetRepository) {
    operator fun invoke(): StateFlow<List<Pet>> = petRepository.getAllPets()
}