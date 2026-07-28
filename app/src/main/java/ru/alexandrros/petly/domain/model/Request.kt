package ru.alexandrros.petly.domain.model

//parameterless constructor needed for firestore
data class Request @JvmOverloads constructor(
    val id: String = "",
    val creatorUserId: String = "",
    val petId: String = "",
    val petName: String = "",
    val species: String = "",
    val specialistUserId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)