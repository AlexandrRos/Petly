package ru.alexandrros.petly.data.remote.mapper

import ru.alexandrros.petly.data.remote.model.RequestDto
import ru.alexandrros.petly.domain.model.Request



fun RequestDto.toDomain(): Request = Request(
    id = documentId,
    creatorUserId = creatorUserId,
    petId = petId,
    petName = petName,
    species = species,
    specialistUserId = specialistUserId,
    createdAt = createdAt
)

fun Request.toDto(): RequestDto = RequestDto(
    documentId = id,
    creatorUserId = creatorUserId,
    petId = petId,
    petName = petName,
    species = species,
    specialistUserId = specialistUserId,
    createdAt = createdAt
)