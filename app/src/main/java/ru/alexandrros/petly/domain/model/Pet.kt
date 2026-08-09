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
    val photoBytes: ByteArray? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Pet) return false

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (name != other.name) return false
        if (species != other.species) return false
        if (breed != other.breed) return false
        if (age != other.age) return false
        if (weight != other.weight) return false
        if (isMale != other.isMale) return false
        if (sterilizationStatus != other.sterilizationStatus) return false
        if (vaccinations != other.vaccinations) return false
        if (chronicDiseases != other.chronicDiseases) return false
        if (allergies != other.allergies) return false
        if (personalityTraits != other.personalityTraits) return false
        if (feedingSchedule != other.feedingSchedule) return false
        if (walkingSchedule != other.walkingSchedule) return false
        if (medications != other.medications) return false
        if (photoBytes != null) {
            if (other.photoBytes == null) return false
            if (!photoBytes.contentEquals(other.photoBytes)) return false
        } else if (other.photoBytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + species.hashCode()
        result = 31 * result + (breed?.hashCode() ?: 0)
        result = 31 * result + (age ?: 0)
        result = 31 * result + (weight?.hashCode() ?: 0)
        result = 31 * result + isMale.hashCode()
        result = 31 * result + (sterilizationStatus?.hashCode() ?: 0)
        result = 31 * result + vaccinations.hashCode()
        result = 31 * result + chronicDiseases.hashCode()
        result = 31 * result + allergies.hashCode()
        result = 31 * result + personalityTraits.hashCode()
        result = 31 * result + (feedingSchedule?.hashCode() ?: 0)
        result = 31 * result + (walkingSchedule?.hashCode() ?: 0)
        result = 31 * result + medications.hashCode()
        result = 31 * result + (photoBytes?.contentHashCode() ?: 0)
        return result
    }
}