package ru.alexandrros.petly.data.remote.model

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId

data class PetDto(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val species: String = "",
    val breed: String? = null,
    val age: Int? = null,
    val weight: Double? = null,
    val isMale: Boolean = true,
    val sterilizationStatus: Boolean? = null,
    val vaccinations: List<String> = emptyList(),
    val chronicDiseases: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val personalityTraits: List<String> = emptyList(),
    val feedingSchedule: String? = null,
    val walkingSchedule: String? = null,
    val medications: List<String> = emptyList(),
    val photoBlob: Blob? = null
)