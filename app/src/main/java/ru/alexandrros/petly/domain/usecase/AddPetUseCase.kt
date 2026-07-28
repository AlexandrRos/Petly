package ru.alexandrros.petly.domain.usecase


import kotlin.Result
import kotlin.Unit
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.repository.PetRepository

class AddPetUseCase(private val petRepository:PetRepository) {
    suspend operator fun invoke(pet:Pet):Result<Unit>

    {
        return try {
            petRepository.addPet(pet)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
