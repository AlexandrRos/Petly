package ru.alexandrros.petly.data.remote.model

data class UserDto(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val specialist: String? = null
)