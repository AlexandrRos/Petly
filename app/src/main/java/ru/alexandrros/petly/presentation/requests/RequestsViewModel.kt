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
import ru.alexandrros.petly.domain.model.Request
import ru.alexandrros.petly.domain.model.UserRating
import ru.alexandrros.petly.domain.usecase.GetAllRequestsUseCase
import ru.alexandrros.petly.domain.usecase.GetPetByUserUseCase
import ru.alexandrros.petly.domain.usecase.GetRequestsByCreatorUseCase
import ru.alexandrros.petly.domain.usecase.GetUserRatingUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase


@OptIn(ExperimentalCoroutinesApi::class)
class RequestsViewModel(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val getRequestsByCreator: GetRequestsByCreatorUseCase,
    private val getAllRequests: GetAllRequestsUseCase,
    private val getPetByUser: GetPetByUserUseCase,
    private val getUserRating: GetUserRatingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestsUiState())
    val uiState: StateFlow<RequestsUiState> = _uiState.asStateFlow()

    private val rawRequests = MutableStateFlow<List<Request>>(emptyList())
    private val ratingsCache = MutableStateFlow<Map<String, UserRating?>>(emptyMap())
    private val currentUserId = MutableStateFlow("")

    private val loadingPhotoIds = mutableSetOf<String>()
    private val loadingRatingUserIds = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            observeCurrentUser()
                .map { it?.uid ?: "" }
                .distinctUntilChanged()
                .collect { uid -> currentUserId.value = uid }
        }

        // Load raw requests based on view mode and current user
        viewModelScope.launch {
            combine(
                observeCurrentUser().map { it?.uid to it?.specialist },
                _uiState.map { it.viewMode }.distinctUntilChanged()
            ) { (uid, specialist), viewMode ->
                Triple(uid, specialist, viewMode)
            }
                .flatMapLatest { (uid, specialist, viewMode) ->
                    _uiState.update { it.copy(currentUserSpecialist = specialist) }

                    if (uid == null) {
                        flowOf(emptyList())
                    } else {
                        val isSpec = specialist != null && specialist != "None"
                        when {
                            !isSpec -> getRequestsByCreator(uid)
                            viewMode == RequestListViewMode.MY_REQUESTS -> getRequestsByCreator(uid)
                            viewMode == RequestListViewMode.ACCEPTED_BY_ME -> getAllRequests()
                            else -> getAllRequests() // AVAILABLE
                        }
                    }
                }
                .catch { e ->
                    Log.e("RequestsViewModel", "Request flow error", e)
                    emit(rawRequests.value)
                }
                .collect { requestList ->
                    rawRequests.value = requestList
                    loadPhotosAndRatings(requestList)
                }
        }

        // Combine raw requests, filters, ratings, and user to produce displayed list
        viewModelScope.launch {
            combine(
                rawRequests,
                _uiState,
                ratingsCache,
                currentUserId
            ) { rawList, uiState, ratings, uid ->
                val isSpecialist = uiState.isSpecialist
                val viewMode = uiState.viewMode

                var filtered = rawList
                if (isSpecialist) {
                    when (viewMode) {
                        RequestListViewMode.MY_REQUESTS -> {
                            // Already filtered by creator
                        }
                        RequestListViewMode.ACCEPTED_BY_ME -> {
                            filtered = filtered.filter { it.specialistUserId == uid }
                        }
                        RequestListViewMode.AVAILABLE -> {
                            // Exclude own requests and requests already assigned to other specialists
                            filtered = filtered.filter { req ->
                                req.creatorUserId != uid &&
                                        (req.specialistUserId == null || req.specialistUserId == uid)
                            }
                            // Apply additional filters
                            val city = uiState.cityFilter.trim()
                            if (city.isNotEmpty()) {
                                filtered = filtered.filter { it.city.contains(city, ignoreCase = true) }
                            }
                            uiState.minCost.toDoubleOrNull()?.let { min ->
                                filtered = filtered.filter { (it.cost ?: 0.0) >= min }
                            }
                            uiState.maxCost.toDoubleOrNull()?.let { max ->
                                filtered = filtered.filter { (it.cost ?: 0.0) <= max }
                            }
                            if (uiState.minRating > 0f) {
                                filtered = filtered.filter { req ->
                                    val rating = ratings[req.creatorUserId]
                                    rating != null && rating.totalCount > 0 && rating.average >= uiState.minRating
                                }
                            }
                        }
                    }
                }
                // Update ratings in UI state
                val updatedUiState = uiState.copy(requests = filtered, ratings = ratings)
                updatedUiState
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun loadPhotosAndRatings(requestList: List<Request>) {
        // Load missing pet photos
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
                        Log.d("RequestsViewModel", "Error loading photos: $e")
                        _uiState.update { state ->
                            state.copy(photoCache = state.photoCache + (req.id to null))
                        }
                    } finally {
                        loadingPhotoIds.remove(req.id)
                    }
                }
            }
        }

        // Load user ratings if current user is specialist (needed for card display and filtering)
        val currentUi = _uiState.value
        if (currentUi.isSpecialist) {
            val uniqueUserIds = requestList.map { it.creatorUserId }.toSet()
            uniqueUserIds.forEach { userId ->
                if (userId !in ratingsCache.value && userId !in loadingRatingUserIds) {
                    loadingRatingUserIds.add(userId)
                    viewModelScope.launch {
                        try {
                            val rating = getUserRating(userId).first()
                            ratingsCache.update { it + (userId to rating) }
                        } catch (e: Exception) {
                            Log.d("RequestsViewModel", "Error loading ratings: $e")
                            ratingsCache.update { it + (userId to null) }
                        } finally {
                            loadingRatingUserIds.remove(userId)
                        }
                    }
                }
            }
        }
    }

    fun cycleViewMode() {
        val current = _uiState.value.viewMode
        val next = when (current) {
            RequestListViewMode.MY_REQUESTS -> RequestListViewMode.AVAILABLE
            RequestListViewMode.AVAILABLE -> RequestListViewMode.ACCEPTED_BY_ME
            RequestListViewMode.ACCEPTED_BY_ME -> RequestListViewMode.MY_REQUESTS
        }
        _uiState.update { it.copy(viewMode = next, isFilterDialogVisible = false) }
    }

    // Filter management functions
    fun showFilterDialog() {
        _uiState.update { it.copy(isFilterDialogVisible = true) }
    }

    fun hideFilterDialog() {
        _uiState.update { it.copy(isFilterDialogVisible = false) }
    }

    fun updateCityFilter(city: String) {
        _uiState.update { it.copy(cityFilter = city) }
    }

    fun updateMinCost(cost: String) {
        _uiState.update { it.copy(minCost = cost) }
    }

    fun updateMaxCost(cost: String) {
        _uiState.update { it.copy(maxCost = cost) }
    }

    fun updateMinRating(rating: Float) {
        _uiState.update { it.copy(minRating = rating) }
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                cityFilter = "",
                minCost = "",
                maxCost = "",
                minRating = 0f
            )
        }
    }
}