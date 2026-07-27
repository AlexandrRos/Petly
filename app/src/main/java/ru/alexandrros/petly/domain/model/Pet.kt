package ru.alexandrros.petly.domain.model

data class Pet(
    val id: String = "",              // Firestore document ID
    val userId: String,               // owner’s UID
    val name: String,
    val species: String,
    val breed: String? = null,
    val age: Int? = null,
    val weight: Double? = null,
    val isMale: Boolean,
    val sterilizationStatus: Boolean? = null,
    val vaccinations: List<String> = emptyList(),
    val chronicDiseases: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val personalityTraits: List<String> = emptyList(),
    val feedingSchedule: String? = null,
    val walkingSchedule: String? = null,
    val medications: List<String> = emptyList(),
    val photoRes: Int? = null
)