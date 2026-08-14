package ru.alexandrros.petly.presentation.requests

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.usecase.GetAllRequestsUseCase
import ru.alexandrros.petly.domain.usecase.GetPetByUserUseCase
import ru.alexandrros.petly.domain.usecase.GetRequestsByCreatorUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase


@OptIn(ExperimentalCoroutinesApi::class)
class RequestsViewModel(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val getRequestsByCreator: GetRequestsByCreatorUseCase,
    private val getAllRequests: GetAllRequestsUseCase,
    private val getPetByUser: GetPetByUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestsUiState())
    val uiState: StateFlow<RequestsUiState> = _uiState.asStateFlow()

    // Prevent duplicate photo fetches for the same request
    private val loadingPhotoIds = mutableSetOf<String>()

    init {
        // Reactively load requests
        viewModelScope.launch {
            // Combine current user (for both uid and specialist status)
            // with toggle flags from the UI state
            combine(
                observeCurrentUser().map { it?.uid to it?.specialist },
                _uiState.map { it.showMyRequests }.distinctUntilChanged()
            ) { (uid, specialist), showMyRequests ->
                Triple(uid, specialist, showMyRequests)
            }
                .flatMapLatest { (uid, specialist, showMyRequests) ->
                    _uiState.update { it.copy(currentUserSpecialist = specialist) }

                    if (uid == null) {
                        flowOf(emptyList())
                    } else {
                        val isSpec = specialist != null && specialist != "None"
                        if (!isSpec) {
                            getRequestsByCreator(uid)
                        } else {
                            if (showMyRequests) {
                                getRequestsByCreator(uid)
                            } else {
                                getAllRequests()
                            }
                        }
                    }
                }
                .catch { e ->
                    Log.e("RequestsViewModel", "Request flow error", e)
                    emit(_uiState.value.requests)   // fall back to last known list
                }
                .collect { requestList ->
                    _uiState.update { it.copy(requests = requestList) }

                    // Fetch missing photos
                    requestList.forEach { req ->
                        if (req.id !in _uiState.value.photoCache && req.id !in loadingPhotoIds) {
                            loadingPhotoIds.add(req.id)
                            viewModelScope.launch {
                                try {
                                    val pet = getPetByUser(req.creatorUserId, req.petId).first()
                                    _uiState.update { state ->
                                        state.copy(photoCache = state.photoCache + (req.id to pet?.photoBytes))
                                    }
                                } catch (e: Exception) {
                                    _uiState.update { state ->
                                        state.copy(photoCache = state.photoCache + (req.id to null))
                                    }
                                } finally {
                                    loadingPhotoIds.remove(req.id)
                                }
                            }
                        }
                    }
                }
        }
    }

    fun toggleView() {
        _uiState.update { it.copy(showMyRequests = !it.showMyRequests) }
    }
}