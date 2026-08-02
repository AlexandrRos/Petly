package ru.alexandrros.petly.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase
import ru.alexandrros.petly.domain.usecase.UpdateUserProfileUseCase

class EditProfileViewModel(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val updateUserProfile: UpdateUserProfileUseCase
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    init {
        viewModelScope.launch {
            observeCurrentUser().collect { user ->
                _currentUser.value = user
                user?.let {
                    if (_name.value.isBlank() && it.name.isNotBlank()) {
                        _name.value = it.name
                    }
                }
            }
        }
    }

    fun onNameChanged(newName: String) {
        _name.value = newName
    }

    fun saveProfile() {
        val user = _currentUser.value ?: return
        val newName = _name.value.trim()
        if (newName.isBlank() || newName == user.name) {
            viewModelScope.launch {
                _snackbarEvent.emit("Имя не изменилось или пустое")
            }
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            updateUserProfile(user.uid, newName)
                .onSuccess {
                    _snackbarEvent.emit("Профиль обновлён")
                }
                .onFailure { e ->
                    _snackbarEvent.emit("Ошибка: ${e.localizedMessage}")
                }
            _isSaving.value = false
        }
    }

    class Factory(
        private val observeCurrentUser: ObserveCurrentUserUseCase,
        private val updateUserProfile: UpdateUserProfileUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EditProfileViewModel(observeCurrentUser, updateUserProfile) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}