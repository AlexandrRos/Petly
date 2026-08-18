package ru.alexandrros.petly.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.model.ReviewType
import ru.alexandrros.petly.domain.usecase.GetUserByIdUseCase
import ru.alexandrros.petly.domain.usecase.GetUserRatingUseCase
import ru.alexandrros.petly.domain.usecase.GetUserReviewsUseCase

class UserDetailViewModel(
    private val userId: String,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val getUserRatingUseCase: GetUserRatingUseCase,
    private val getUserReviewsUseCase: GetUserReviewsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserDetailUiState())
    val uiState: StateFlow<UserDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = UserDetailUiState(isLoading = true)

            getUserByIdUseCase(userId)
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.localizedMessage)
                    }
                }
                .collect { user ->
                    if (user == null) {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = "Пользователь не найден")
                        }
                    } else {
                        val isSpecialist = user.specialist != null
                        val newSelectedType = if (isSpecialist) {
                            // keep current if still valid, otherwise default to AS_SPECIALIST
                            if (_uiState.value.selectedReviewType == ReviewType.AS_OWNER ||
                                _uiState.value.selectedReviewType == ReviewType.AS_SPECIALIST
                            ) {
                                _uiState.value.selectedReviewType
                            } else {
                                ReviewType.AS_SPECIALIST
                            }
                        } else {
                            ReviewType.AS_OWNER
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                user = user,
                                errorMessage = null,
                                selectedReviewType = newSelectedType
                            )
                        }
                    }
                }

            viewModelScope.launch {
                getUserRatingUseCase(userId)
                    .catch { }
                    .collect { rating ->
                        _uiState.update { it.copy(rating = rating) }
                    }
            }

            viewModelScope.launch {
                getUserReviewsUseCase(userId)
                    .catch { }
                    .collect { reviews ->
                        _uiState.update { it.copy(reviews = reviews) }
                    }
            }
        }
    }

    fun setReviewType(type: ReviewType) {
        _uiState.update { it.copy(selectedReviewType = type) }
    }
}