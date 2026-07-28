package ru.alexandrros.petly.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.domain.repository.PetRepository
import ru.alexandrros.petly.domain.repository.RequestRepository
import ru.alexandrros.petly.domain.repository.UserRepository


class RequestDetailViewModel(
    private val requestId: String,
    private val requestRepository: RequestRepository,
    private val userRepository: UserRepository,
    private val petRepositoryProvider: (userId: String) -> PetRepository
) : ViewModel() {

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _request = MutableStateFlow<Request?>(null)
    val request: StateFlow<Request?> = _request

    private val _pet = MutableStateFlow<Pet?>(null)
    val pet: StateFlow<Pet?> = _pet

    private val _specialistUser = MutableStateFlow<User?>(null)
    val specialistUser: StateFlow<User?> = _specialistUser
    private val currentUser: StateFlow<User?> = userRepository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val currentUserId: StateFlow<String?> = currentUser
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val canAccept: StateFlow<Boolean> = combine(
        currentUserId, _request, currentUser
    ) { uid, req, user ->
        uid != null &&
                req != null &&
                req.specialistUserId == null &&
                user?.specialist != null &&
                user.specialist != "None"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadRequest()
    }

    private fun loadRequest() {
        viewModelScope.launch {
            _isLoading.value = true
            val req = requestRepository.getRequestById(requestId)
            _request.value = req
            _isLoading.value = false

            if (req != null) {
                val petRepo = petRepositoryProvider(req.creatorUserId)
                petRepo.getPetById(req.creatorUserId, req.petId)
                    .catch { e ->
                        if (e !is kotlinx.coroutines.CancellationException) {
                            _snackbarEvent.emit("Ошибка загрузки питомца: ${e.localizedMessage}")
                        }
                    }
                    .collect { pet ->
                        _pet.value = pet
                    }

                req.specialistUserId?.let { specialistId ->
                    userRepository.getUserById(specialistId)
                        .catch { e ->
                            if (e !is kotlinx.coroutines.CancellationException) {
                                _snackbarEvent.emit("Ошибка загрузки специалиста: ${e.localizedMessage}")
                            }
                        }
                        .collect { user ->
                            _specialistUser.value = user
                        }
                }
            }
        }
    }

    fun isOwner(): Boolean {
        val uid = currentUserId.value
        val req = _request.value
        return uid != null && req != null && uid == req.creatorUserId
    }

    fun deleteRequest() {
        viewModelScope.launch {
            val req = _request.value ?: return@launch
            if (isOwner()) {
                requestRepository.deleteRequest(req.id)
                    .onSuccess {
                        _snackbarEvent.emit("Заявка удалена")
                        _request.value = null
                    }
                    .onFailure { e -> _snackbarEvent.emit("Ошибка: ${e.localizedMessage}") }
            }
        }
    }

    fun acceptRequest() {
        viewModelScope.launch {
            val uid = currentUserId.first { it != null } ?: return@launch
            val reqId = _request.value?.id ?: return@launch
            requestRepository.acceptRequest(reqId, uid)
                .onSuccess {
                    _snackbarEvent.emit("Заявка принята")
                    loadRequest()   // refresh
                }
                .onFailure { e -> _snackbarEvent.emit("Ошибка: ${e.localizedMessage}") }
        }
    }

    class Factory(
        private val requestId: String,
        private val requestRepository: RequestRepository,
        private val userRepository: UserRepository,
        private val petRepositoryProvider: (userId: String) -> PetRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return RequestDetailViewModel(
                requestId, requestRepository, userRepository, petRepositoryProvider
            ) as T
        }
    }
}