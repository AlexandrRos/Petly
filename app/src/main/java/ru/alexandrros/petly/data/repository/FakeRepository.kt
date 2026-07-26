package ru.alexandrros.petly.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.repository.PetRepository

class FakePetRepository : PetRepository {
    private val _pets = MutableStateFlow(
        listOf(
            Pet(
                id = 1,
                name = "Котяра", species = "Кот", breed = "Беспородный",
                age = 3, weight = 4.5, isMale = true,
                sterilizationStatus = true, vaccinations = listOf("Бешенство"),
                allergies = listOf("Курица"), personalityTraits = listOf("Игривый", "Боится пылесоса"),
                feedingSchedule = "2 раза в день (утро/вечер)"
            ),
            Pet(
                id = 2,
                name = "Вася", species = "Сиамский кот", breed = "Сиамская",
                age = 5, weight = 5.0, isMale = true,
                sterilizationStatus = false, vaccinations = listOf("Бешенство", "Комплексная"),
                chronicDiseases = listOf("Нет"), personalityTraits = listOf("Ласковый", "Агрессия к собакам"),
                feedingSchedule = "Специальный корм 2 раза в день",
                walkingSchedule = "Прогулки на поводке утром и вечером",
                medications = listOf("Инсулин")
            ),
            Pet(
                id = 3,
                name = "Белла", species = "Хомяк", breed = "Сирийский",
                age = 1, weight = 0.15, isMale = false,
                sterilizationStatus = false, personalityTraits = listOf("Активная", "Боится громких звуков"),
                feedingSchedule = "Зерносмесь утром и вечером", walkingSchedule = "Колесо в клетке"
            )
        )
    )

    override fun getAllPets(): StateFlow<List<Pet>> = _pets

    override fun getPetById(id: Int): Pet? = _pets.value.find { it.id == id }

    override fun addPet(pet: Pet) {
        val newId = (_pets.value.maxOfOrNull { it.id } ?: 0) + 1
        _pets.update { currentList -> currentList + pet.copy(id = newId) }
    }

    override fun updatePet(updatedPet: Pet) {
        _pets.update { currentList ->
            currentList.map { if (it.id == updatedPet.id) updatedPet else it }
        }
    }
}