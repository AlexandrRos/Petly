package ru.alexandrros.petly.presentation.specialists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.domain.usecase.GetUserByIdUseCase

class SpecialistDetailViewModel(
    specialistId: String,
    private val getUserById: GetUserByIdUseCase
) : ViewModel() {

    private val _specialist = MutableStateFlow<User?>(null)
    val specialist: StateFlow<User?> = _specialist

    init {
        viewModelScope.launch {
            getUserById(specialistId)
                .catch { _specialist.value = null }
                .collect { _specialist.value = it }
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