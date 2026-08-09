package ru.alexandrros.petly.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.repository.PetRepository

//for PetDetailScreen update after editing
class ObservePetByUserUseCase(private val petRepository: PetRepository) {
    operator fun invoke(userId: String, petId: String): Flow<Pet?> =
        petRepository.observePetById(userId, petId)
}