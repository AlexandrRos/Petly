package ru.alexandrros.petly.presentation.requests

import androidx.lifecycle.ViewModel
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
import ru.alexandrros.petly.domain.usecase.ConfirmRequestUseCase
import ru.alexandrros.petly.domain.usecase.DeleteRequestUseCase
import ru.alexandrros.petly.domain.usecase.DismissRequestUseCase
import ru.alexandrros.petly.domain.usecase.GetPetByUserUseCase
import ru.alexandrros.petly.domain.usecase.GetRequestByIdUseCase
import ru.alexandrros.petly.domain.usecase.GetUserByIdUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase

class RequestDetailViewModel(
    private val requestId: String,
    private val getRequestByIdUseCase: GetRequestByIdUseCase,
    private val acceptRequestUseCase: AcceptRequestUseCase,
    private val confirmRequestUseCase: ConfirmRequestUseCase,
    private val dismissRequestUseCase: DismissRequestUseCase,
    private val deleteRequestUseCase: DeleteRequestUseCase,
    private val getPetByUserUseCase: GetPetByUserUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestDetailUiState())
    val uiState: StateFlow<RequestDetailUiState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase()
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

            val req = getRequestByIdUseCase(requestId)
            _uiState.update { it.copy(request = req, isLoading = false) }

            if (req != null) {
                getPetByUserUseCase(req.creatorUserId, req.petId)
                    .catch { e ->
                        if (e !is CancellationException) {
                            _snackbarEvent.emit("Ошибка загрузки питомца: ${e.localizedMessage}")
                        }
                    }
                    .collect { pet ->
                        _uiState.update { it.copy(pet = pet) }
                    }

                req.specialistUserId?.let { specialistId ->
                    getUserByIdUseCase(specialistId)
                        .catch { e ->
                            if (e !is CancellationException) {
                                _snackbarEvent.emit("Ошибка загрузки специалиста: ${e.localizedMessage}")
                            }
                        }
                        .collect { user ->
                            _uiState.update { it.copy(specialistUser = user) }
                        }
                }

                getUserByIdUseCase(req.creatorUserId)
                    .catch { e ->
                        if (e !is CancellationException) {
                            _snackbarEvent.emit("Ошибка загрузки владельца: ${e.localizedMessage}")
                        }
                    }
                    .collect { user ->
                        _uiState.update { it.copy(ownerUser = user) }
                    }
            }
        }
    }

    fun deleteRequest() {
        viewModelScope.launch {
            val req = _uiState.value.request ?: return@launch
            if (_uiState.value.isOwner) {
                _uiState.update { it.copy(deletionState = DeletionState.DELETING) }
                deleteRequestUseCase(req.id)
                    .onSuccess {
                        _uiState.update { it.copy(deletionState = DeletionState.SUCCESS) }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(deletionState = DeletionState.IDLE) }
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

    fun confirmRequest() {
        viewModelScope.launch {
            val reqId = _uiState.value.request?.id ?: return@launch
            confirmRequestUseCase(reqId)
                .onSuccess {
                    _snackbarEvent.emit("Заявка подтверждена")
                    loadRequest()
                }
                .onFailure { e ->
                    _snackbarEvent.emit("Ошибка: ${e.localizedMessage}")
                }
        }
    }

    fun dismissRequest() {
        viewModelScope.launch {
            val reqId = _uiState.value.request?.id ?: return@launch
            dismissRequestUseCase(reqId)
                .onSuccess {
                    _snackbarEvent.emit("Специалист отклонён")
                    loadRequest()
                }
                .onFailure { e ->
                    _snackbarEvent.emit("Ошибка: ${e.localizedMessage}")
                }
        }
    }
}