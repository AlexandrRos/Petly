package ru.alexandrros.petly.presentation.viewmodel

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
import ru.alexandrros.petly.domain.repository.UserRepository

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    val currentUser: StateFlow<User?> = userRepository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                return UserViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    fun setSpecialist(isSpecialist: Boolean) {
        viewModelScope.launch {
            val user = currentUser.first() ?: return@launch
            val newSpecialist = if (isSpecialist) "Vet" else "None"
            userRepository.updateSpecialist(user.uid, newSpecialist)
                .onFailure { e ->
                    Log.e("UserViewModel", "Failed to update specialist", e)
                }
        }
    }
}