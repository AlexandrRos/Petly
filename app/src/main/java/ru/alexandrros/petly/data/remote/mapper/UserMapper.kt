package ru.alexandrros.petly.data.remote.mapper

import ru.alexandrros.petly.data.remote.model.UserDto
import ru.alexandrros.petly.domain.model.User


// data/mapper/UserMapper.kt
import com.google.firebase.firestore.Blob

fun UserDto.toDomain(): User = User(
    uid = uid,
    email = email,
    name = name,
    specialist = specialist,
    photoBytes = photoBytes
)

fun User.toDto(): UserDto = UserDto(
    uid = uid,
    email = email,
    name = name,
    specialist = specialist,
    photoBytes = photoBytes
)

// Helper to convert Firestore Blob to ByteArray
fun Blob?.toBytes(): ByteArray? = this?.toBytes()

// Helper to convert ByteArray to Blob
fun ByteArray?.toBlob(): Blob? = this?.let { Blob.fromBytes(it) }