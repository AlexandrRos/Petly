package ru.alexandrros.petly.data.remote.datasource

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import ru.alexandrros.petly.data.remote.model.ReviewDto
import ru.alexandrros.petly.domain.model.ReviewType
import ru.alexandrros.petly.domain.model.UserRating

class FirebaseReviewDataSource {

    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")
    private val usersCollection = firestore.collection("users")

    suspend fun saveReview(review: ReviewDto): String {
        validateReviewType(review)
        ensureUniqueReview(review.reviewerId, review.reviewedUserId, review.type, excludeDocId = null)
        val docRef = reviewsCollection.add(review).await()
        return docRef.id
    }

    suspend fun updateReview(reviewId: String, review: ReviewDto) {
        validateReviewType(review)
        ensureUniqueReview(review.reviewerId, review.reviewedUserId, review.type, excludeDocId = reviewId)
        reviewsCollection.document(reviewId)
            .set(review, SetOptions.merge())
            .await()
    }

    suspend fun deleteReview(reviewId: String) {
        reviewsCollection.document(reviewId).delete().await()
    }

    fun observeReviewsForUser(uid: String, type: ReviewType? = null): Flow<List<ReviewDto>> {
        var query = reviewsCollection.whereEqualTo("reviewedUserId", uid)
        if (type != null) {
            query = query.whereEqualTo("type", type.name)
        }
        return query.snapshots()
            .map { snapshot: QuerySnapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject<ReviewDto>()?.copy(documentId = doc.id)
                }
            }
    }

    fun observeUserRating(uid: String): Flow<UserRating?> = callbackFlow {
        val query = reviewsCollection.whereEqualTo("reviewedUserId", uid)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirebaseReviewDS", "Rating listen error", error)
                return@addSnapshotListener
            }
            val reviews = snapshot?.documents ?: emptyList()
            if (reviews.isEmpty()) {
                trySend(null)
            } else {
                val total = reviews.size
                val sum = reviews.sumOf { it.getLong("rating")?.toInt() ?: 0 }
                val average = sum.toDouble() / total
                trySend(UserRating(average = average, totalCount = total))
            }
        }
        awaitClose { listener.remove() }
    }

    private suspend fun isUserSpecialist(uid: String): Boolean {
        return try {
            val userDoc = usersCollection.document(uid).get().await()
            val specialist = userDoc.getString("specialist")
            !specialist.isNullOrBlank() && !specialist.equals("None", ignoreCase = true)
        } catch (e: Exception) {
            Log.e("FirebaseReviewDS", "Error checking specialist status", e)
            false
        }
    }

    /**
     * Validates that the review type is allowed for the given user.
     *
     * @throws IllegalArgumentException if the review type is AS_SPECIALIST
     *         but the reviewed user is not a specialist.
     */
    private suspend fun validateReviewType(review: ReviewDto) {
        // Only reviews of type AS_SPECIALIST need validation
        if (review.type != ReviewType.AS_SPECIALIST.name) {
            return
        }

        val isSpecialist = isUserSpecialist(review.reviewedUserId)
        if (!isSpecialist) {
            throw IllegalArgumentException(
                "User ${review.reviewedUserId} is not a specialist; cannot save review as AS_SPECIALIST"
            )
        }
    }

    /**
     * Ensures that the current user does not already have a review of the given type
     * for the target user.
     *
     * @throws IllegalStateException if a duplicate review exists.
     */
    private suspend fun ensureUniqueReview(
        reviewerId: String,
        reviewedUserId: String,
        type: String,
        excludeDocId: String?
    ) {
        val query = reviewsCollection
            .whereEqualTo("reviewerId", reviewerId)
            .whereEqualTo("reviewedUserId", reviewedUserId)
            .whereEqualTo("type", type)

        val snapshot = query.get().await()
        val duplicateExists = snapshot.documents.any { it.id != excludeDocId }
        if (duplicateExists) {
            throw IllegalStateException("User $reviewerId already has a review of type $type for user $reviewedUserId")
        }
    }
}