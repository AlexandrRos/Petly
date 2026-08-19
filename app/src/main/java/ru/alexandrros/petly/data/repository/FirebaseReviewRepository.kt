package ru.alexandrros.petly.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.alexandrros.petly.data.remote.datasource.FirebaseReviewDataSource
import ru.alexandrros.petly.data.remote.mapper.toDomain
import ru.alexandrros.petly.data.remote.mapper.toDto
import ru.alexandrros.petly.domain.model.Review
import ru.alexandrros.petly.domain.model.ReviewType
import ru.alexandrros.petly.domain.model.UserRating
import ru.alexandrros.petly.domain.repository.ReviewRepository
import kotlin.coroutines.cancellation.CancellationException

class FirebaseReviewRepository(
    private val dataSource: FirebaseReviewDataSource = FirebaseReviewDataSource()
) : ReviewRepository {

    override suspend fun saveReview(review: Review): Result<String> {
        return try {
            val dto = review.toDto()
            val docId = dataSource.saveReview(dto)
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirebaseReviewRepo", "Save review error", e)
            Result.failure(e)
        }
    }

    override suspend fun updateReview(reviewId: String, review: Review): Result<Unit> {
        return try {
            val dto = review.toDto()
            dataSource.updateReview(reviewId, dto)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirebaseReviewRepo", "Update review error", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteReview(reviewId: String): Result<Unit> {
        return try {
            dataSource.deleteReview(reviewId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirebaseReviewRepo", "Delete review error", e)
            Result.failure(e)
        }
    }

    override fun observeReviewsForUser(uid: String, type: ReviewType?): Flow<List<Review>> {
        return dataSource.observeReviewsForUser(uid, type)
            .map { dtos -> dtos.map { it.toDomain() } }
    }

    override fun observeUserRating(uid: String): Flow<UserRating?> {
        return dataSource.observeUserRating(uid)
    }
}