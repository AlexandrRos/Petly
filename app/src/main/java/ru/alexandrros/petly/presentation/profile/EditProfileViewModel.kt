package ru.alexandrros.petly.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase
import ru.alexandrros.petly.domain.usecase.UpdateUserProfileUseCase

class EditProfileViewModel(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val updateUserProfile: UpdateUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    init {
        viewModelScope.launch {
            observeCurrentUser().collect { user ->
                _uiState.update { state ->
                    val newName = if (state.name.isBlank() && user?.name?.isNotBlank() == true) {
                        user.name
                    } else {
                        state.name
                    }
                    state.copy(
                        isLoading = false,
                        currentUser = user,
                        name = newName
                    )
                }
            }
        }
    }

    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun saveProfile() {
        val currentState = _uiState.value
        val user = currentState.currentUser ?: return
        val newName = currentState.name.trim()

        if (newName.isBlank() || newName == user.name) {
            viewModelScope.launch {
                _snackbarEvent.emit("Имя не изменилось или пустое")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            updateUserProfile(user.uid, newName)
                .onSuccess {
                    _snackbarEvent.emit("Профиль обновлён")
                }
                .onFailure { e ->
                    _snackbarEvent.emit("Ошибка: ${e.localizedMessage}")
                }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}