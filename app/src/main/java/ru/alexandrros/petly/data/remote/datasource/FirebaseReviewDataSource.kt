package ru.alexandrros.petly.data.remote.datasource

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
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
import kotlin.collections.mapNotNull

class FirebaseReviewDataSource {

    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")

    suspend fun saveReview(review: ReviewDto): String {
        val docRef = reviewsCollection.add(review).await()
        return docRef.id
    }

    suspend fun updateReview(reviewId: String, review: ReviewDto) {
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
                snapshot.documents.mapNotNull { doc: DocumentSnapshot ->
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
}