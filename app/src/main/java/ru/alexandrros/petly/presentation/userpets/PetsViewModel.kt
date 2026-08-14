package ru.alexandrros.petly.presentation.userpets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.usecase.ObserveUserPetsUseCase

class PetsViewModel(
    private val observeUserPets: ObserveUserPetsUseCase
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
}