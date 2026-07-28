package ru.alexandrros.petly.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.repository.PetRepository


class GetPetByUserUseCase(private val petRepository: PetRepository) {
    operator fun invoke(userId: String, petId: String): Flow<Pet?> =
        petRepository.getPetById(userId, petId)
}