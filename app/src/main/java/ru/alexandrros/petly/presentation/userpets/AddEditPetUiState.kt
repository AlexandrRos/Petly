package ru.alexandrros.petly.presentation.userpets

data class AddEditPetUiState(
    //Form fields
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val ageText: String = "",
    val weightText: String = "",
    val isMale: Boolean = true,
    val sterilizationStatus: Boolean = false,
    val vaccinationsText: String = "",
    val chronicDiseasesText: String = "",
    val allergiesText: String = "",
    val personalityTraitsText: String = "",
    val medicationsText: String = "",
    val feedingSchedule: String = "",
    val walkingSchedule: String = "",
    val photoBytes: ByteArray? = null,
    //State
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AddEditPetUiState) return false

        if (name != other.name) return false
        if (species != other.species) return false
        if (breed != other.breed) return false
        if (ageText != other.ageText) return false
        if (weightText != other.weightText) return false
        if (isMale != other.isMale) return false
        if (sterilizationStatus != other.sterilizationStatus) return false
        if (vaccinationsText != other.vaccinationsText) return false
        if (chronicDiseasesText != other.chronicDiseasesText) return false
        if (allergiesText != other.allergiesText) return false
        if (personalityTraitsText != other.personalityTraitsText) return false
        if (medicationsText != other.medicationsText) return false
        if (feedingSchedule != other.feedingSchedule) return false
        if (walkingSchedule != other.walkingSchedule) return false
        if (isSaving != other.isSaving) return false
        if (errorMessage != other.errorMessage) return false
        if (photoBytes != null) {
            if (other.photoBytes == null || !photoBytes.contentEquals(other.photoBytes)) return false
        } else if (other.photoBytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + species.hashCode()
        result = 31 * result + breed.hashCode()
        result = 31 * result + ageText.hashCode()
        result = 31 * result + weightText.hashCode()
        result = 31 * result + isMale.hashCode()
        result = 31 * result + sterilizationStatus.hashCode()
        result = 31 * result + vaccinationsText.hashCode()
        result = 31 * result + chronicDiseasesText.hashCode()
        result = 31 * result + allergiesText.hashCode()
        result = 31 * result + personalityTraitsText.hashCode()
        result = 31 * result + medicationsText.hashCode()
        result = 31 * result + feedingSchedule.hashCode()
        result = 31 * result + walkingSchedule.hashCode()
        result = 31 * result + isSaving.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + (photoBytes?.contentHashCode() ?: 0)
        return result
    }
}