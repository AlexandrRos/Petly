package ru.alexandrros.petly.data.repository

import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.repository.PetRepository

class FakePetRepository : PetRepository {
    private val pets = listOf(
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
            chronicDiseases = listOf("Диабет"), personalityTraits = listOf("Ласковый", "Агрессия к собакам"),
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

    override fun getAllPets() = pets
    override fun getPetById(id: Int) = pets.firstOrNull { it.id == id }
}