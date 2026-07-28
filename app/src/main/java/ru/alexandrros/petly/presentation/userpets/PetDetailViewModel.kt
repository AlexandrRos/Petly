package ru.alexandrros.petly.presentation.userpets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.usecase.CheckExistingRequestUseCase
import ru.alexandrros.petly.domain.usecase.CreateRequestUseCase
import ru.alexandrros.petly.domain.usecase.GetPetByUserUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase

class PetDetailViewModel(
    private val petId: String,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val getPetByUser: GetPetByUserUseCase,
    private val checkExistingRequest: CheckExistingRequestUseCase,
    private val createRequest: CreateRequestUseCase
) : ViewModel() {

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    private val _isCreatingRequest = MutableStateFlow(false)
    val isCreatingRequest: StateFlow<Boolean> = _isCreatingRequest

    private val currentUserId: StateFlow<String?> = observeCurrentUser()
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pet: StateFlow<Pet?> = currentUserId
        .flatMapLatest { uid ->
            if (uid != null) getPetByUser(uid, petId)
            else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun createRequest(petId: String, petName: String, species: String) {
        viewModelScope.launch {
            _isCreatingRequest.value = true
            val uid = currentUserId.first { it != null } ?: run {
                _snackbarEvent.emit("Ошибка: не удалось получить пользователя")
                _isCreatingRequest.value = false
                return@launch
            }
            val alreadyExists = checkExistingRequest(uid, petId)
            if (alreadyExists) {
                _snackbarEvent.emit("Заявка для этого питомца уже создана")
                _isCreatingRequest.value = false
                return@launch
            }
            createRequest(
                Request(
                    creatorUserId = uid,
                    petId = petId,
                    petName = petName,
                    species = species
                )
            ).onSuccess {
                _snackbarEvent.emit("Заявка успешно создана")
            }.onFailure { e ->
                _snackbarEvent.emit("Ошибка при создании заявки: ${e.localizedMessage}")
            }
            _isCreatingRequest.value = false
        }
    }

    class Factory(
        private val petId: String,
        private val observeCurrentUser: ObserveCurrentUserUseCase,
        private val getPetByUser: GetPetByUserUseCase,
        private val checkExistingRequest: CheckExistingRequestUseCase,
        private val createRequest: CreateRequestUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PetDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PetDetailViewModel(
                    petId, observeCurrentUser, getPetByUser,
                    checkExistingRequest, createRequest
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}