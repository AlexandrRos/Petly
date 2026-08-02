package ru.alexandrros.petly.presentation.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.domain.usecase.LogoutUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase
import ru.alexandrros.petly.domain.usecase.UpdateSpecialistUseCase
import ru.alexandrros.petly.domain.usecase.UpdateUserPhotoUseCase
import java.io.ByteArrayOutputStream

class ProfileViewModel(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val updateSpecialist: UpdateSpecialistUseCase,
    private val updateUserPhoto: UpdateUserPhotoUseCase
) : ViewModel() {

    val currentUser: StateFlow<User?> = observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isUpdatingPhoto = MutableStateFlow(false)
    val isUpdatingPhoto: StateFlow<Boolean> = _isUpdatingPhoto

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    fun setSpecialist(isSpecialist: Boolean) {
        viewModelScope.launch {
            val user = currentUser.first() ?: return@launch
            val newSpecialist = if (isSpecialist) "Vet" else "None"
            updateSpecialist(user.uid, newSpecialist)
                .onFailure { e ->
                    Log.e("ProfileViewModel", "Failed to update specialist", e)
                }
        }
    }

    fun updateProfilePhoto(imageUri: Uri, context: Context) {
        viewModelScope.launch {
            _isUpdatingPhoto.value = true
            try {
                //Read and compress image on background thread
                val bytes = withContext(Dispatchers.IO) {
                    readBytesFromUri(context, imageUri)?.let {
                        compressImage(it, maxSizeBytes = 100 * 1024)   // target ~100 KB
                    }
                }
                if (bytes == null || bytes.isEmpty()) {
                    _snackbarEvent.emit("Не удалось обработать изображение")
                    return@launch
                }

                val user = currentUser.first() ?: run {
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
                _isUpdatingPhoto.value = false
            }
        }
    }

    private fun readBytesFromUri(context: Context, uri: Uri): ByteArray? {
        return try {
            // Attempt to read raw bytes first
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val rawBytes = stream.readBytes()
                if (rawBytes.isNotEmpty()) return@use rawBytes
            }

            // If raw bytes failed, decode as bitmap and compress
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                    val compressed = baos.toByteArray()
                    bitmap.recycle()
                    return compressed
                }
            }
            null
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Failed to read image from URI", e)
            null
        }
    }


    private fun compressImage(bytes: ByteArray, maxSizeBytes: Int): ByteArray {
        if (bytes.size <= maxSizeBytes) return bytes
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
        val quality = 50 // aggressive compression
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        bitmap.recycle()
        return baos.toByteArray()
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