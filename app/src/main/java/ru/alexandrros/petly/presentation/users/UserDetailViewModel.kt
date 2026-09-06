package ru.alexandrros.petly.presentation.users

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
                .catch {
                    _uiState.update { it.copy(isCurrentUserLoading = false) }
                }
                .collect { currentUser ->
                    _uiState.update {
                        it.copy(
                            currentUserId = currentUser?.uid ?: "",
                            currentUserName = currentUser?.name ?: "",
                            isCurrentUserLoading = false
                        )
                    }
                }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

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
            getUserReviewsUseCase(userId)
                .catch { }
                .collect { reviews ->
                    _uiState.update { it.copy(reviews = reviews) }
                }
        }
    }

    fun setReviewType(type: ReviewType) {
        _uiState.update {
            it.copy(
                selectedReviewType = type,
                isReviewFormVisible = false,
                editingReviewId = null
            )
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            // Mark as deleting
            _uiState.update { state ->
                state.copy(deletingReviewIds = state.deletingReviewIds + reviewId)
            }

            val result = deleteReviewUseCase(reviewId)

            // Remove from deleting set regardless of result
            _uiState.update { state ->
                state.copy(deletingReviewIds = state.deletingReviewIds - reviewId)
            }

            if (result.isFailure) {
                Log.d("UserDetailViewModel", "Failed to delete review: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun showAddReviewForm() {
        val state = _uiState.value
        if (userId == state.currentUserId) {
            Log.d("UserDetailViewModel", "Cannot review yourself")
            return
        }
        _uiState.update {
            it.copy(
                isReviewFormVisible = true,
                editingReviewId = null,
                reviewFormRating = 0,
                reviewFormComment = ""
            )
        }
    }

    fun showEditReviewForm(review: Review) {
        val state = _uiState.value
        if (userId == state.currentUserId) {
            return
        }
        _uiState.update {
            it.copy(
                isReviewFormVisible = true,
                editingReviewId = review.id,
                reviewFormRating = review.rating,
                reviewFormComment = review.comment
            )
        }
    }

    fun hideReviewForm() {
        _uiState.update {
            it.copy(
                isReviewFormVisible = false,
                editingReviewId = null
            )
        }
    }

    fun updateReviewFormRating(rating: Int) {
        _uiState.update { it.copy(reviewFormRating = rating) }
    }

    fun updateReviewFormComment(comment: String) {
        _uiState.update { it.copy(reviewFormComment = comment) }
    }

    fun showReviewFilterDialog() {
        _uiState.update { it.copy(isReviewFilterDialogVisible = true) }
    }

    fun hideReviewFilterDialog() {
        _uiState.update { it.copy(isReviewFilterDialogVisible = false) }
    }

    fun setReviewSortOption(option: ReviewSortOption) {
        _uiState.update { it.copy(reviewSortOption = option) }
    }

    fun setRatingFilterMin(min: Float) {
        _uiState.update { state ->
            state.copy(
                ratingFilterMin = min.coerceAtMost(state.ratingFilterMax)
            )
        }
    }

    fun setRatingFilterMax(max: Float) {
        _uiState.update { state ->
            state.copy(
                ratingFilterMax = max.coerceAtLeast(state.ratingFilterMin)
            )
        }
    }

    fun resetReviewFilters() {
        _uiState.update {
            it.copy(
                reviewSortOption = ReviewSortOption.LATEST,
                ratingFilterMin = 0f,
                ratingFilterMax = 5f
            )
        }
    }

    fun submitReview() {
        val state = _uiState.value
        if (userId == state.currentUserId) {
            Log.d("UserDetailViewModel", "Cannot submit self-review")
            return
        }

        val rating = state.reviewFormRating
        val comment = state.reviewFormComment.trim()

        if (rating == 0 || comment.isEmpty() || state.currentUserId.isEmpty()) return

        val isReviewedUserSpecialist = state.user?.specialist.isSpecialistValue()

        val safeReviewType = if (!isReviewedUserSpecialist && state.selectedReviewType == ReviewType.AS_SPECIALIST) {
            ReviewType.AS_OWNER
        } else {
            state.selectedReviewType
        }

        val hasReviewedAsOwner = state.reviews.any {
            it.reviewerId == state.currentUserId && it.type == ReviewType.AS_OWNER
        }
        val hasReviewedAsSpecialist = state.reviews.any {
            it.reviewerId == state.currentUserId && it.type == ReviewType.AS_SPECIALIST
        }

        if (state.editingReviewId == null) {
            if ((safeReviewType == ReviewType.AS_OWNER && hasReviewedAsOwner) ||
                (safeReviewType == ReviewType.AS_SPECIALIST && hasReviewedAsSpecialist)
            ) {
                Log.d("UserDetailViewModel", "Attempted to add duplicate review of type $safeReviewType")
                return
            }
        }

        _uiState.update { it.copy(isSubmittingReview = true) }

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
                _uiState.update {
                    it.copy(
                        isSubmittingReview = false,
                        isReviewFormVisible = false,
                        editingReviewId = null
                    )
                }
            } else {
                Log.d("UserDetailViewModel", "Unable to save review: ${result.exceptionOrNull()?.message}")
                _uiState.update { it.copy(isSubmittingReview = false) }
            }
        }
    }
}