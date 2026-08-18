package ru.alexandrros.petly.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.alexandrros.petly.domain.model.Review
import ru.alexandrros.petly.domain.model.ReviewType
import ru.alexandrros.petly.domain.model.UserRating
import ru.alexandrros.petly.domain.repository.UserRatingRepository

class MockUserRatingRepository : UserRatingRepository {

    private val reviews = listOf(
        Review(
            id = "r1",
            targetUserId = "user1",
            reviewerId = "user2",
            reviewerName = "Иван Петров",
            rating = 5,
            comment = "Отличный специалист! Очень заботливо отнёсся к моему питомцу.",
            type = ReviewType.AS_SPECIALIST,
            createdAt = System.currentTimeMillis() - 3600_000
        ),
        Review(
            id = "r2",
            targetUserId = "user1",
            reviewerId = "user3",
            reviewerName = "Мария Сидорова",
            rating = 4,
            comment = "Хорошо знает своё дело, рекомендую.",
            type = ReviewType.AS_SPECIALIST,
            createdAt = System.currentTimeMillis() - 7200_000
        ),
        Review(
            id = "r3",
            targetUserId = "user1",
            reviewerId = "user4",
            reviewerName = "Алексей Иванов",
            rating = 5,
            comment = "Как владелец очень ответственно подходит к уходу за питомцем.",
            type = ReviewType.AS_OWNER,
            createdAt = System.currentTimeMillis() - 10800_000
        )
    )

    override fun getRatingByUserId(userId: String): Flow<UserRating> = flow {
        val userReviews = reviews
        if (userReviews.isEmpty()) {
            emit(UserRating(average = 0.0, totalCount = 0))
        } else {
            val avg = userReviews.map { it.rating }.average()
            emit(UserRating(average = avg, totalCount = userReviews.size))
        }
    }

    override fun getReviewsByUserId(userId: String): Flow<List<Review>> = flow {
        emit(reviews)
    }
}