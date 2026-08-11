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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.usecase.CheckExistingRequestUseCase
import ru.alexandrros.petly.domain.usecase.CreateRequestUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase
import ru.alexandrros.petly.domain.usecase.ObservePetByUserUseCase

class PetDetailViewModel(
    private val petId: String,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val observePetByUser: ObservePetByUserUseCase,
    private val checkExistingRequest: CheckExistingRequestUseCase,
    private val createRequest: CreateRequestUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(PetDetailUiState())
    val uiState: StateFlow<PetDetailUiState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    private val currentUserId: StateFlow<String?> = observeCurrentUser()
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val petFlow: StateFlow<Pet?> = currentUserId
        .flatMapLatest { uid ->
            if (uid != null) observePetByUser(uid, petId)
            else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            petFlow.collect { pet ->
                _uiState.update { state ->
                    state.copy(isLoading = false, pet = pet)
                }
            }
        }
    }

    fun createRequest(petId: String, petName: String, species: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            val uid = currentUserId.first { it != null } ?: run {
                _snackbarEvent.emit("Ошибка: не удалось получить пользователя")
                _uiState.update { it.copy(isCreating = false) }
                return@launch
            }
            val alreadyExists = checkExistingRequest(uid, petId)
            if (alreadyExists) {
                _snackbarEvent.emit("Заявка для этого питомца уже создана")
                _uiState.update { it.copy(isCreating = false) }
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
            _uiState.update { it.copy(isCreating = false) }
        }
    }

    class Factory(
        private val petId: String,
        private val observeCurrentUser: ObserveCurrentUserUseCase,
        private val observePetByUserUseCase: ObservePetByUserUseCase,
        private val checkExistingRequest: CheckExistingRequestUseCase,
        private val createRequest: CreateRequestUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PetDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PetDetailViewModel(
                    petId, observeCurrentUser, observePetByUserUseCase,
                    checkExistingRequest, createRequest
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}