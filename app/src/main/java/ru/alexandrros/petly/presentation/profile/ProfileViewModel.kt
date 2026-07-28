package ru.alexandrros.petly.presentation.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.domain.usecase.LogoutUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase
import ru.alexandrros.petly.domain.usecase.UpdateSpecialistUseCase

class ProfileViewModel(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val updateSpecialist: UpdateSpecialistUseCase
) : ViewModel() {

    val currentUser: StateFlow<User?> = observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
                    Log.e("UserViewModel", "Failed to update specialist", e)
                }
        }
    }

    class Factory(
        private val observeCurrentUser: ObserveCurrentUserUseCase,
        private val logoutUseCase: LogoutUseCase,
        private val updateSpecialist: UpdateSpecialistUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(
                    observeCurrentUser,
                    logoutUseCase,
                    updateSpecialist
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}