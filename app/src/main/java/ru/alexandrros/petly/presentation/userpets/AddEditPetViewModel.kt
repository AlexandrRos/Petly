package ru.alexandrros.petly.presentation.userpets

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.usecase.AddPetUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase
import ru.alexandrros.petly.domain.usecase.UpdatePetUseCase
import ru.alexandrros.petly.presentation.common.readAndCompressImage

class AddEditPetViewModel(
    val isNew: Boolean,
    private val initialPet: Pet?,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val addPetUseCase: AddPetUseCase,
    private val updatePetUseCase: UpdatePetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        if (initialPet != null) AddEditPetUiState(
            name = initialPet.name,
            species = initialPet.species,
            breed = initialPet.breed ?: "",
            ageText = initialPet.age?.toString() ?: "",
            weightText = initialPet.weight?.toString() ?: "",
            isMale = initialPet.isMale,
            sterilizationStatus = initialPet.sterilizationStatus ?: false,
            vaccinationsText = initialPet.vaccinations.joinToString(", "),
            chronicDiseasesText = initialPet.chronicDiseases.joinToString(", "),
            allergiesText = initialPet.allergies.joinToString(", "),
            personalityTraitsText = initialPet.personalityTraits.joinToString(", "),
            medicationsText = initialPet.medications.joinToString(", "),
            feedingSchedule = initialPet.feedingSchedule ?: "",
            walkingSchedule = initialPet.walkingSchedule ?: "",
            photoBytes = initialPet.photoBytes
        ) else AddEditPetUiState()
    )
    val uiState: StateFlow<AddEditPetUiState> = _uiState.asStateFlow()

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent

    fun updateState(transform: (AddEditPetUiState) -> AddEditPetUiState) {
        _uiState.update(transform)
    }

    fun updatePhoto(uri: Uri, context: Context) {
        val bytes = readAndCompressImage(context, uri, maxSizeBytes = 100 * 1024)
        updateState { it.copy(photoBytes = bytes) }
    }

    fun savePet() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val state = _uiState.value

            val userId = initialPet?.userId ?: observeCurrentUser()
                .map { it?.uid ?: "" }
                .first { it.isNotEmpty() }

            val pet = Pet(
                id = initialPet?.id ?: "",
                userId = userId,
                name = state.name,
                species = state.species,
                breed = state.breed.ifBlank { null },
                age = state.ageText.toIntOrNull(),
                weight = state.weightText.toDoubleOrNull(),
                isMale = state.isMale,
                sterilizationStatus = state.sterilizationStatus,
                vaccinations = state.vaccinationsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                chronicDiseases = state.chronicDiseasesText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                allergies = state.allergiesText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                personalityTraits = state.personalityTraitsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                feedingSchedule = state.feedingSchedule.ifBlank { null },
                walkingSchedule = state.walkingSchedule.ifBlank { null },
                medications = state.medicationsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                photoBytes = state.photoBytes
            )

            try {
                if (isNew) addPetUseCase(pet)
                else updatePetUseCase(pet)
                _saveSuccessEvent.emit(Unit)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.localizedMessage) }
            }
        }
    }

    class Factory(
        private val initialPet: Pet?,
        private val observeCurrentUser: ObserveCurrentUserUseCase,
        private val addPetUseCase: AddPetUseCase,
        private val updatePetUseCase: UpdatePetUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddEditPetViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AddEditPetViewModel(
                    isNew = initialPet == null,
                    initialPet = initialPet,
                    observeCurrentUser = observeCurrentUser,
                    addPetUseCase = addPetUseCase,
                    updatePetUseCase = updatePetUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}