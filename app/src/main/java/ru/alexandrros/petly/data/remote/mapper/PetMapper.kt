package ru.alexandrros.petly.data.remote.mapper

import ru.alexandrros.petly.data.remote.model.PetDto
import ru.alexandrros.petly.domain.model.Pet

fun PetDto.toDomain(): Pet = Pet(
    id = id,
    userId = userId,
    name = name,
    species = species,
    breed = breed,
    age = age,
    weight = weight,
    isMale = isMale,
    sterilizationStatus = sterilizationStatus,
    vaccinations = vaccinations,
    chronicDiseases = chronicDiseases,
    allergies = allergies,
    personalityTraits = personalityTraits,
    feedingSchedule = feedingSchedule,
    walkingSchedule = walkingSchedule,
    medications = medications,
    photoRes = photoRes
)

fun Pet.toDto(): PetDto = PetDto(
    id = id,
    userId = userId,
    name = name,
    species = species,
    breed = breed,
    age = age,
    weight = weight,
    isMale = isMale,
    sterilizationStatus = sterilizationStatus,
    vaccinations = vaccinations,
    chronicDiseases = chronicDiseases,
    allergies = allergies,
    personalityTraits = personalityTraits,
    feedingSchedule = feedingSchedule,
    walkingSchedule = walkingSchedule,
    medications = medications,
    photoRes = photoRes
)