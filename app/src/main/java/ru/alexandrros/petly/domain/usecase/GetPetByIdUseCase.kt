package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.repository.PetRepository


class GetPetByIdUseCase(private val petRepository: PetRepository) {
    operator fun invoke(id: String): Pet? = petRepository.getPetById(id)
}