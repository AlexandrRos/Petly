package ru.alexandrros.petly.presentation.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.domain.usecase.AcceptRequestUseCase
import ru.alexandrros.petly.domain.usecase.DeleteRequestUseCase
import ru.alexandrros.petly.domain.usecase.GetPetByUserUseCase
import ru.alexandrros.petly.domain.usecase.GetRequestByIdUseCase
import ru.alexandrros.petly.domain.usecase.GetUserByIdUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase

class RequestDetailViewModel(
    private val requestId: String,
    private val getRequestById: GetRequestByIdUseCase,
    private val acceptRequestUseCase: AcceptRequestUseCase,
    private val deleteRequestUseCase: DeleteRequestUseCase,
    private val getPetByUser: GetPetByUserUseCase,
    private val getUserById: GetUserByIdUseCase,
    private val observeCurrentUser: ObserveCurrentUserUseCase
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

    private val currentUser: StateFlow<User?> = observeCurrentUser()
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
            val req = getRequestById(requestId)
            _request.value = req
            _isLoading.value = false

            if (req != null) {
                getPetByUser(req.creatorUserId, req.petId)
                    .catch { e ->
                        if (e !is CancellationException) {
                            _snackbarEvent.emit("Ошибка загрузки питомца: ${e.localizedMessage}")
                        }
                    }
                    .collect { pet ->
                        _pet.value = pet
                    }

                req.specialistUserId?.let { specialistId ->
                    getUserById(specialistId)
                        .catch { e ->
                            if (e !is CancellationException) {
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
                deleteRequestUseCase(req.id)   // <-- renamed
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
            acceptRequestUseCase(reqId, uid)   // <-- renamed
                .onSuccess {
                    _snackbarEvent.emit("Заявка принята")
                    loadRequest()
                }
                .onFailure { e -> _snackbarEvent.emit("Ошибка: ${e.localizedMessage}") }
        }
    }

    class Factory(
        private val requestId: String,
        private val getRequestById: GetRequestByIdUseCase,
        private val acceptRequestUseCase: AcceptRequestUseCase,
        private val deleteRequestUseCase: DeleteRequestUseCase,
        private val getPetByUser: GetPetByUserUseCase,
        private val getUserById: GetUserByIdUseCase,
        private val observeCurrentUser: ObserveCurrentUserUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RequestDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RequestDetailViewModel(
                    requestId,
                    getRequestById,
                    acceptRequestUseCase,
                    deleteRequestUseCase,
                    getPetByUser,
                    getUserById,
                    observeCurrentUser
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}