package ru.alexandrros.petly.users

import ru.alexandrros.petly.domain.model.Review
import ru.alexandrros.petly.domain.model.ReviewType
import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.domain.model.UserRating

data class UserDetailUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val errorMessage: String? = null,
    val rating: UserRating? = null,
    val reviews: List<Review> = emptyList(),
    val selectedReviewType: ReviewType = ReviewType.AS_SPECIALIST,
    val currentUserId: String = "",
    val currentUserName: String = "",
    val isReviewFormVisible: Boolean = false,
    val editingReviewId: String? = null,
    val reviewFormRating: Int = 0,
    val reviewFormComment: String = "",
    val reviewFormType: ReviewType = ReviewType.AS_SPECIALIST
)