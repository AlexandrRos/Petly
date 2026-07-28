package ru.alexandrros.petly.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.repository.RequestRepository
import ru.alexandrros.petly.domain.repository.UserRepository


class RequestViewModel(
    private val userRepository: UserRepository,
    private val requestRepository: RequestRepository

) : ViewModel() {
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent


    // Track ongoing request creation
    private val _isCreatingRequest = MutableStateFlow(false)
    val isCreatingRequest: StateFlow<Boolean> = _isCreatingRequest

    private val currentUserId: StateFlow<String?> = userRepository.getCurrentUser()
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _showMyRequests = MutableStateFlow(true)
    val showMyRequests: StateFlow<Boolean> = _showMyRequests
    private val isSpecialist = MutableStateFlow(false)
    fun setSpecialist(isSpecialist: Boolean) {
        this.isSpecialist.value = isSpecialist
    }

    val requests: StateFlow<List<Request>> =
        combine(currentUserId, _showMyRequests, isSpecialist) { userId, myRequests, specialist ->
            if (userId == null) {
                flowOf(emptyList())
            } else {
                if (!specialist) {
                    // Non‑specialist always sees own requests
                    requestRepository.getRequestsByCreator(userId)
                } else {
                    if (myRequests) {
                        requestRepository.getRequestsByCreator(userId)
                    } else {
                        requestRepository.getAllRequests()   // all requests in the system
                    }
                }
            }
        }
            .flatMapLatest { it }
            .catch { e ->
                Log.e("RequestViewModel", "Request flow error", e)
                val lastKnown = requests.value
                emit(if (lastKnown.isNotEmpty()) lastKnown else emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleView() {
        _showMyRequests.value = !_showMyRequests.value
    }

    fun createRequest(petId: String, petName: String, species: String) {
        viewModelScope.launch {
            _isCreatingRequest.value = true
            val uid = currentUserId.first { it != null } ?: run {
                _snackbarEvent.emit("Ошибка: не удалось получить пользователя")
                _isCreatingRequest.value = false
                return@launch
            }
            val alreadyExists = requestRepository.checkExistingRequest(uid, petId)
            if (alreadyExists) {
                _snackbarEvent.emit("Заявка для этого питомца уже создана")
                _isCreatingRequest.value = false
                return@launch
            }
            requestRepository.createRequest(
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
        private val userRepository: UserRepository,
        private val requestRepository: RequestRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return RequestViewModel(userRepository, requestRepository) as T
        }
    }
}