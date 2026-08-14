package ru.alexandrros.petly.presentation.userpets

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
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
import ru.alexandrros.petly.domain.usecase.GetPetByIdUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase
import ru.alexandrros.petly.domain.usecase.UpdatePetUseCase
import ru.alexandrros.petly.presentation.common.readAndCompressImage

class AddEditPetViewModel(
    private val petId: String?,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getPetByIdUseCase: GetPetByIdUseCase,
    private val addPetUseCase: AddPetUseCase,
    private val updatePetUseCase: UpdatePetUseCase
) : ViewModel() {

    val isNew: Boolean = petId == null

    private var initialPet: Pet? = null

    private val _uiState = MutableStateFlow(AddEditPetUiState())
    val uiState: StateFlow<AddEditPetUiState> = _uiState.asStateFlow()

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent

    init {
        if (petId != null) {
            viewModelScope.launch {
                val pet = getPetByIdUseCase(petId)
                initialPet = pet
                if (pet != null) {
                    _uiState.value = AddEditPetUiState(
                        name = pet.name,
                        species = pet.species,
                        breed = pet.breed ?: "",
                        ageText = pet.age?.toString() ?: "",
                        weightText = pet.weight?.toString() ?: "",
                        isMale = pet.isMale,
                        sterilizationStatus = pet.sterilizationStatus ?: false,
                        vaccinationsText = pet.vaccinations.joinToString(", "),
                        chronicDiseasesText = pet.chronicDiseases.joinToString(", "),
                        allergiesText = pet.allergies.joinToString(", "),
                        personalityTraitsText = pet.personalityTraits.joinToString(", "),
                        medicationsText = pet.medications.joinToString(", "),
                        feedingSchedule = pet.feedingSchedule ?: "",
                        walkingSchedule = pet.walkingSchedule ?: "",
                        photoBytes = pet.photoBytes
                    )
                }
            }
        }
    }

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

            val userId = initialPet?.userId ?: observeCurrentUserUseCase()
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
}