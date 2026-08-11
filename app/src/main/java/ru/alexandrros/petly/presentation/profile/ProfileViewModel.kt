package ru.alexandrros.petly.presentation.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.alexandrros.petly.domain.usecase.LogoutUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase
import ru.alexandrros.petly.domain.usecase.UpdateSpecialistUseCase
import ru.alexandrros.petly.domain.usecase.UpdateUserPhotoUseCase
import ru.alexandrros.petly.presentation.common.readAndCompressImage

class ProfileViewModel(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val updateSpecialist: UpdateSpecialistUseCase,
    private val updateUserPhoto: UpdateUserPhotoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    init {
        viewModelScope.launch {
            observeCurrentUser().collect { user ->
                _uiState.update { it.copy(isLoading = false, currentUser = user) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    fun setSpecialist(isSpecialist: Boolean) {
        viewModelScope.launch {
            val user = _uiState.value.currentUser ?: return@launch
            val newSpecialist = if (isSpecialist) "Vet" else "None"
            updateSpecialist(user.uid, newSpecialist)
                .onFailure { e ->
                    Log.e("ProfileViewModel", "Failed to update specialist", e)
                }
        }
    }

    fun updateProfilePhoto(imageUri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingPhoto = true) }
            try {
                val bytes = withContext(Dispatchers.IO) {
                    readAndCompressImage(context, imageUri, maxSizeBytes = 100 * 1024)
                }
                if (bytes == null || bytes.isEmpty()) {
                    _snackbarEvent.emit("Не удалось обработать изображение")
                    return@launch
                }

                val user = _uiState.value.currentUser ?: run {
                    _snackbarEvent.emit("Пользователь не авторизован")
                    return@launch
                }

                updateUserPhoto(user.uid, bytes)
                    .onSuccess {
                        _snackbarEvent.emit("Фото обновлено")
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit("Ошибка: ${e.localizedMessage}")
                        Log.e("ProfileViewModel", "Photo upload failed", e)
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Ошибка: ${e.localizedMessage}")
                Log.e("ProfileViewModel", "Error updating photo", e)
            } finally {
                _uiState.update { it.copy(isUpdatingPhoto = false) }
            }
        }
    }

    class Factory(
        private val observeCurrentUser: ObserveCurrentUserUseCase,
        private val logoutUseCase: LogoutUseCase,
        private val updateSpecialist: UpdateSpecialistUseCase,
        private val updateUserPhoto: UpdateUserPhotoUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(
                    observeCurrentUser, logoutUseCase, updateSpecialist, updateUserPhoto
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}