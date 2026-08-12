package ru.alexandrros.petly.data.remote.model

import com.google.firebase.firestore.DocumentId

data class RequestDto(
    @DocumentId val documentId: String = "",
    val creatorUserId: String = "",
    val petId: String = "",
    val petName: String = "",
    val species: String = "",
    val specialistUserId: String? = null,
    val createdAt: Long = 0L
)