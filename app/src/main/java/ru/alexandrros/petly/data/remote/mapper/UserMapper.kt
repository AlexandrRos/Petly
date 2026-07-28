package ru.alexandrros.petly.data.remote.mapper

import ru.alexandrros.petly.data.remote.model.UserDto
import ru.alexandrros.petly.domain.model.User


fun UserDto.toDomain(): User = User(
    uid = uid,
    email = email,
    name = name,
    specialist = specialist
)

fun User.toDto(): UserDto = UserDto(
    uid = uid,
    email = email,
    name = name,
    specialist = specialist
)