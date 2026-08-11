package ru.alexandrros.petly.presentation.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    private val _uiState = MutableStateFlow(RequestDetailUiState())
    val uiState: StateFlow<RequestDetailUiState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    init {
        viewModelScope.launch {
            observeCurrentUser()
                .catch {}
                .collect { user ->
                    _uiState.update { state ->
                        state.copy(
                            currentUserId = user?.uid,
                            currentUserSpecialist = user?.specialist
                        )
                    }
                }
        }
        loadRequest()
    }

    private fun loadRequest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val req = getRequestById(requestId)
            _uiState.update { it.copy(request = req, isLoading = false) }

            if (req != null) {
                getPetByUser(req.creatorUserId, req.petId)
                    .catch { e ->
                        if (e !is CancellationException) {
                            _snackbarEvent.emit("Ошибка загрузки питомца: ${e.localizedMessage}")
                        }
                    }
                    .collect { pet ->
                        _uiState.update { it.copy(pet = pet) }
                    }

                req.specialistUserId?.let { specialistId ->
                    getUserById(specialistId)
                        .catch { e ->
                            if (e !is CancellationException) {
                                _snackbarEvent.emit("Ошибка загрузки специалиста: ${e.localizedMessage}")
                            }
                        }
                        .collect { user ->
                            _uiState.update { it.copy(specialistUser = user) }
                        }
                }
            }
        }
    }

    fun deleteRequest() {
        viewModelScope.launch {
            val req = _uiState.value.request ?: return@launch
            if (_uiState.value.isOwner) {
                deleteRequestUseCase(req.id)
                    .onSuccess {
                        _snackbarEvent.emit("Заявка удалена")
                        _uiState.update { it.copy(request = null) }
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit("Ошибка: ${e.localizedMessage}")
                    }
            }
        }
    }

    fun acceptRequest() {
        viewModelScope.launch {
            val uid = _uiState.value.currentUserId ?: return@launch
            val reqId = _uiState.value.request?.id ?: return@launch
            acceptRequestUseCase(reqId, uid)
                .onSuccess {
                    _snackbarEvent.emit("Заявка принята")
                    loadRequest()
                }
                .onFailure { e ->
                    _snackbarEvent.emit("Ошибка: ${e.localizedMessage}")
                }
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