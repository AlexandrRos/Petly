package ru.alexandrros.petly.domain.usecase

import ru.alexandrros.petly.domain.repository.PetRepository


class DeletePetUseCase(private val petRepository: PetRepository) {
    suspend operator fun invoke(petId: String): Result<Unit> {
        return try {
            petRepository.deletePet(petId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}