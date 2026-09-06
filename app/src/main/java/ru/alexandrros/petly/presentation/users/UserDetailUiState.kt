package ru.alexandrros.petly.presentation.users

import ru.alexandrros.petly.domain.model.Review
import ru.alexandrros.petly.domain.model.ReviewType
import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.domain.model.UserRating

enum class ReviewSortOption {
    LATEST,
    OLDEST,
    HIGHEST_RATING,
    LOWEST_RATING
}

data class UserDetailUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val errorMessage: String? = null,
    val rating: UserRating? = null,
    val reviews: List<Review> = emptyList(),
    val selectedReviewType: ReviewType = ReviewType.AS_SPECIALIST,
    val currentUserId: String = "",
    val currentUserName: String = "",
    val isCurrentUserLoading: Boolean = true,
    val isReviewFormVisible: Boolean = false,
    val editingReviewId: String? = null,
    val isSubmittingReview: Boolean = false,
    val deletingReviewIds: Set<String> = emptySet(),
    val reviewFormRating: Int = 0,
    val reviewFormComment: String = "",
    val reviewSortOption: ReviewSortOption = ReviewSortOption.LATEST,
    val ratingFilterMin: Float = 0f,
    val ratingFilterMax: Float = 5f,
    val isReviewFilterDialogVisible: Boolean = false
)