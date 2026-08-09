package ru.alexandrros.petly.presentation.requests

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.usecase.GetAllRequestsUseCase
import ru.alexandrros.petly.domain.usecase.GetPetByUserUseCase
import ru.alexandrros.petly.domain.usecase.GetRequestsByCreatorUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase

class RequestViewModel(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val getRequestsByCreator: GetRequestsByCreatorUseCase,
    private val getAllRequests: GetAllRequestsUseCase,
    private val getPetByUser: GetPetByUserUseCase
) : ViewModel() {

    private val currentUserId: StateFlow<String?> = observeCurrentUser()
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _showMyRequests = MutableStateFlow(true)
    val showMyRequests: StateFlow<Boolean> = _showMyRequests

    private val isSpecialist = MutableStateFlow(false)
    fun setSpecialist(isSpecialist: Boolean) {
        this.isSpecialist.value = isSpecialist
    }

    private val _photoCache = MutableStateFlow<Map<String, ByteArray?>>(emptyMap())
    val photoCache: StateFlow<Map<String, ByteArray?>> = _photoCache

    // To prevent fetching the same request twice
    private val loadingRequestIds = mutableSetOf<String>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val requests: StateFlow<List<Request>> = combine(
        currentUserId, _showMyRequests, isSpecialist
    ) { userId, myRequests, specialist ->
        if (userId == null) {
            flowOf(emptyList())
        } else {
            if (!specialist) {
                getRequestsByCreator(userId)
            } else {
                if (myRequests) {
                    getRequestsByCreator(userId)
                } else {
                    getAllRequests()
                }
            }
        }
    }
        .flatMapLatest { it }
        .onEach { requestList ->
            requestList.forEach { req ->
                val reqId = req.id
                if (reqId !in _photoCache.value && reqId !in loadingRequestIds) {
                    loadingRequestIds.add(reqId)
                    viewModelScope.launch {
                        try {
                            val pet = getPetByUser(req.creatorUserId, req.petId).first()
                            _photoCache.update { it + (reqId to pet?.photoBytes) }
                        } catch (e: Exception) {
                            _photoCache.update { it + (reqId to null) }
                        } finally {
                            loadingRequestIds.remove(reqId)
                        }
                    }
                }
            }
        }
        .catch { e ->
            Log.e("RequestViewModel", "Request flow error", e)
            val lastKnown = requests.value
            emit(if (lastKnown.isNotEmpty()) lastKnown else emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleView() {
        _showMyRequests.value = !_showMyRequests.value
    }

    class Factory(
        private val observeCurrentUser: ObserveCurrentUserUseCase,
        private val getRequestsByCreator: GetRequestsByCreatorUseCase,
        private val getAllRequests: GetAllRequestsUseCase,
        private val getPetByUser: GetPetByUserUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RequestViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RequestViewModel(
                    observeCurrentUser,
                    getRequestsByCreator,
                    getAllRequests,
                    getPetByUser
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}