package ru.alexandrros.petly.presentation.specialists

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.usecase.GetAllSpecialistsUseCase

class SpecialistListViewModel(
    private val getAllSpecialists: GetAllSpecialistsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpecialistListUiState())
    val uiState: StateFlow<SpecialistListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = SpecialistListUiState(isLoading = true)
            getAllSpecialists()
                .catch { e ->
                    Log.e("SpecialistListVM", "Error loading specialists", e)
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.localizedMessage)
                    }
                }
                .collect { list ->
                    _uiState.update {
                        it.copy(isLoading = false, specialists = list, errorMessage = null)
                    }
                }
        }
    }
}