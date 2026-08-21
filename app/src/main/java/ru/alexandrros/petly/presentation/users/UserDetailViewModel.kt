package ru.alexandrros.petly.presentation.users

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alexandrros.petly.domain.model.Review
import ru.alexandrros.petly.domain.model.ReviewType
import ru.alexandrros.petly.domain.usecase.CreateReviewUseCase
import ru.alexandrros.petly.domain.usecase.DeleteReviewUseCase
import ru.alexandrros.petly.domain.usecase.GetUserByIdUseCase
import ru.alexandrros.petly.domain.usecase.GetUserRatingUseCase
import ru.alexandrros.petly.domain.usecase.GetUserReviewsUseCase
import ru.alexandrros.petly.domain.usecase.ObserveCurrentUserUseCase
import ru.alexandrros.petly.domain.usecase.UpdateReviewUseCase
import ru.alexandrros.petly.presentation.common.isSpecialistValue

class UserDetailViewModel(
    private val userId: String,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val getUserRatingUseCase: GetUserRatingUseCase,
    private val getUserReviewsUseCase: GetUserReviewsUseCase,
    private val createReviewUseCase: CreateReviewUseCase,
    private val updateReviewUseCase: UpdateReviewUseCase,
    private val deleteReviewUseCase: DeleteReviewUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserDetailUiState())
    val uiState: StateFlow<UserDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase()
                .catch { }
                .collect { currentUser ->
                    _uiState.update {
                        it.copy(
                            currentUserId = currentUser?.uid ?: "",
                            currentUserName = currentUser?.name ?: ""
                        )
                    }
                }
        }

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
                        val isSpecialist = user.specialist.isSpecialistValue()
                        val newSelectedType = if (isSpecialist) {
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
        }

        viewModelScope.launch {
            getUserRatingUseCase(userId)
                .catch { }
                .collect { rating ->
                    _uiState.update { it.copy(rating = rating) }
                }
        }

        viewModelScope.launch {
            // Potential race: reviews may arrive before current user, flags may be stale
            combine(
                observeCurrentUserUseCase(),
                getUserReviewsUseCase(userId)
            ) { currentUser, reviews ->
                val currentUserId = currentUser?.uid ?: ""
                Triple(currentUserId, reviews, currentUser?.name ?: "")
            }.catch { }
                .collect { (currentUserId, reviews, currentUserName) ->
                    _uiState.update { state ->
                        state.copy(
                            currentUserId = currentUserId,
                            currentUserName = currentUserName,
                            reviews = reviews,
                            hasReviewedAsOwner = reviews.any { it.reviewerId == currentUserId && it.type == ReviewType.AS_OWNER },
                            hasReviewedAsSpecialist = reviews.any { it.reviewerId == currentUserId && it.type == ReviewType.AS_SPECIALIST }
                        )
                    }
                }
        }
    }

    fun setReviewType(type: ReviewType) {
        _uiState.update { it.copy(selectedReviewType = type) }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            deleteReviewUseCase(reviewId)
        }
    }

    fun showAddReviewForm() {
        val state = _uiState.value
        val isSpecialist = state.user?.specialist.isSpecialistValue()
        val selectedType = if (isSpecialist) state.selectedReviewType else ReviewType.AS_OWNER
        _uiState.update {
            it.copy(
                isReviewFormVisible = true,
                editingReviewId = null,
                editingReviewOriginalType = null,
                reviewFormRating = 0,
                reviewFormComment = "",
                reviewFormType = selectedType
            )
        }
    }

    fun showEditReviewForm(review: Review) {
        val state = _uiState.value
        val isSpecialist = state.user?.specialist.isSpecialistValue()
        _uiState.update {
            it.copy(
                isReviewFormVisible = true,
                editingReviewId = review.id,
                editingReviewOriginalType = review.type,
                reviewFormRating = review.rating,
                reviewFormComment = review.comment,
                reviewFormType = if (isSpecialist) review.type else ReviewType.AS_OWNER
            )
        }
    }

    fun hideReviewForm() {
        _uiState.update { it.copy(isReviewFormVisible = false, editingReviewId = null, editingReviewOriginalType = null) }
    }

    fun updateReviewFormRating(rating: Int) {
        _uiState.update { it.copy(reviewFormRating = rating) }
    }

    fun updateReviewFormComment(comment: String) {
        _uiState.update { it.copy(reviewFormComment = comment) }
    }

    fun updateReviewFormType(type: ReviewType) {
        _uiState.update { it.copy(reviewFormType = type) }
    }

    fun submitReview() {
        val state = _uiState.value
        val rating = state.reviewFormRating
        val comment = state.reviewFormComment.trim()

        if (rating == 0 || comment.isEmpty() || state.currentUserId.isEmpty()) return

        val isReviewedUserSpecialist = state.user?.specialist.isSpecialistValue()

        // Force type to AS_OWNER if the reviewed user is not a specialist
        val safeReviewType = if (!isReviewedUserSpecialist && state.reviewFormType == ReviewType.AS_SPECIALIST) {
            ReviewType.AS_OWNER
        } else {
            state.reviewFormType
        }

        // Duplicate prevention (client side)
        if (state.editingReviewId == null) {
            // Adding new review
            if ((safeReviewType == ReviewType.AS_OWNER && state.hasReviewedAsOwner) ||
                (safeReviewType == ReviewType.AS_SPECIALIST && state.hasReviewedAsSpecialist)
            ) {
                Log.d("UserDetailViewModel", "Attempted to add duplicate review of type $safeReviewType")
                return
            }
        } else {
            // Editing existing review
            val originalType = state.editingReviewOriginalType
            if (originalType != null && safeReviewType != originalType) {
                // Type changed, check if new type already has a review
                if ((safeReviewType == ReviewType.AS_OWNER && state.hasReviewedAsOwner) ||
                    (safeReviewType == ReviewType.AS_SPECIALIST && state.hasReviewedAsSpecialist)
                ) {
                    Log.d("UserDetailViewModel", "Attempted to change review type to duplicate type $safeReviewType")
                    return
                }
            }
        }

        viewModelScope.launch {
            val review = Review(
                reviewerId = state.currentUserId,
                reviewerName = state.currentUserName.ifEmpty { "Пользователь" },
                reviewedUserId = userId,
                rating = rating,
                comment = comment,
                type = safeReviewType,
                timestamp = System.currentTimeMillis()
            )

            val result = if (state.editingReviewId == null) {
                createReviewUseCase(review)
            } else {
                updateReviewUseCase(state.editingReviewId, review)
            }

            if (result.isSuccess) {
                _uiState.update { it.copy(isReviewFormVisible = false, editingReviewId = null, editingReviewOriginalType = null) }
            } else {
                Log.d("UserDetailViewModel", "Unable to save review: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}