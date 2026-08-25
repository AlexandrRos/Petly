package ru.alexandrros.petly.presentation.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.usecase.CheckExistingRequestUseCase
import ru.alexandrros.petly.domain.usecase.CreateRequestUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase

class RequestCreationViewModel(
    private val petId: String,
    private val petName: String,
    private val species: String,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val checkExistingRequestUseCase: CheckExistingRequestUseCase,
    private val createRequestUseCase: CreateRequestUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestCreationUiState())
    val uiState: StateFlow<RequestCreationUiState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    private val currentUserId: StateFlow<String?> = observeCurrentUserUseCase()
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onCityChange(city: String) {
        _uiState.update { it.copy(city = city, cityError = false) }
    }

    fun onCostChange(cost: String) {
        _uiState.update { it.copy(cost = cost) }
    }

    fun onStartDateChange(dateMillis: Long) {
        _uiState.update { it.copy(startDate = dateMillis, startDateError = false) }
    }

    fun onEndDateChange(dateMillis: Long) {
        _uiState.update { it.copy(endDate = dateMillis, endDateError = false) }
    }

    fun createRequest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val uid = currentUserId.first { it != null }
            if (uid == null) {
                _snackbarEvent.emit("Ошибка: не удалось получить пользователя")
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val city = _uiState.value.city.trim()
            if (city.isEmpty()) {
                _snackbarEvent.emit("Введите город")
                _uiState.update { it.copy(isLoading = false, cityError = true) }
                return@launch
            }

            val cost = _uiState.value.cost.toDoubleOrNull()
            if (cost != null && cost < 0) {
                _snackbarEvent.emit("Стоимость не может быть отрицательной")
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val startDate = _uiState.value.startDate
            val endDate = _uiState.value.endDate
            if (startDate == null || endDate == null) {
                _snackbarEvent.emit("Выберите даты начала и окончания")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        startDateError = startDate == null,
                        endDateError = endDate == null
                    )
                }
                return@launch
            }
            if (endDate < startDate) {
                _snackbarEvent.emit("Дата окончания должна быть после даты начала")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        startDateError = true,
                        endDateError = true
                    )
                }
                return@launch
            }

            val alreadyExists = checkExistingRequestUseCase(uid, petId)
            if (alreadyExists) {
                _snackbarEvent.emit("Заявка для этого питомца уже создана")
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val request = Request(
                creatorUserId = uid,
                petId = petId,
                petName = petName,
                species = species,
                city = city,
                cost = cost,
                startDate = startDate,
                endDate = endDate,
                status = Request.STATUS_PENDING
            )

            createRequestUseCase(request)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    _snackbarEvent.emit("Ошибка при создании заявки: ${e.localizedMessage}")
                }
        }
    }
}