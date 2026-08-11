package ru.alexandrros.petly.presentation.userpets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.usecase.AddPetUseCase
import ru.alexandrros.petly.domain.usecase.DeletePetUseCase
import ru.alexandrros.petly.domain.usecase.GetPetByIdUseCase
import ru.alexandrros.petly.domain.usecase.ObserveUserPetsUseCase
import ru.alexandrros.petly.domain.usecase.UpdatePetUseCase

class PetsViewModel(
    private val observeUserPets: ObserveUserPetsUseCase,
    private val getPetByIdUseCase: GetPetByIdUseCase,
    private val addPetUseCase: AddPetUseCase,
    private val updatePetUseCase: UpdatePetUseCase,
    private val deletePetUseCase: DeletePetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetsUiState())
    val uiState: StateFlow<PetsUiState> = _uiState.asStateFlow()

    init {
        // observeUserPets() is a hot StateFlow/SharedFlow – it never throws errors here, no need for catch block
        viewModelScope.launch {
            _uiState.value = PetsUiState(isLoading = true)
            observeUserPets().collect { pets ->
                _uiState.update {
                    it.copy(isLoading = false, pets = pets, errorMessage = null)
                }
            }
        }
    }

    fun getPetById(id: String): Pet? = getPetByIdUseCase(id)

    fun addPet(pet: Pet) {
        viewModelScope.launch { addPetUseCase(pet) }
    }

    fun updatePet(updatedPet: Pet) {
        viewModelScope.launch { updatePetUseCase(updatedPet) }
    }

    fun deletePet(petId: String) {
        viewModelScope.launch { deletePetUseCase(petId) }
    }

    class Factory(
        private val observeUserPets: ObserveUserPetsUseCase,
        private val getPetByIdUseCase: GetPetByIdUseCase,
        private val addPetUseCase: AddPetUseCase,
        private val updatePetUseCase: UpdatePetUseCase,
        private val deletePetUseCase: DeletePetUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PetsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PetsViewModel(
                    observeUserPets,
                    getPetByIdUseCase,
                    addPetUseCase,
                    updatePetUseCase,
                    deletePetUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}