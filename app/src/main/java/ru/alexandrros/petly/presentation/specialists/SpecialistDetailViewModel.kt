package ru.alexandrros.petly.presentation.specialists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.usecase.GetUserByIdUseCase

class SpecialistDetailViewModel(
    private val specialistId: String,
    private val getUserById: GetUserByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpecialistDetailUiState())
    val uiState: StateFlow<SpecialistDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = SpecialistDetailUiState(isLoading = true)
            getUserById(specialistId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
                }
                .collect { user ->
                    _uiState.update { it.copy(isLoading = false, user = user, errorMessage = null) }
                }
        }
    }

    class Factory(
        private val specialistId: String,
        private val getUserById: GetUserByIdUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SpecialistDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SpecialistDetailViewModel(specialistId, getUserById) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}