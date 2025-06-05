package com.example.travelsharingapp.data.repository

import com.example.travelsharingapp.data.model.UserReview
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class UserReviewRepository() {
    private val db = FirebaseFirestore.getInstance()
    private val reviewsCollection = db.collection("user_reviews")

    suspend fun getReviewsForUser(userId: String): List<UserReview> {
        return try {
            val snapshot = reviewsCollection.whereEqualTo("reviewedUserId", userId).get().await()
            snapshot.documents.mapNotNull {
                it.toObject(UserReview::class.java)?.apply {
                    reviewId = it.id
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun observeReviewsForProposal(
        proposalId: String,
        onDataChange: (List<UserReview>) -> Unit
    ): ListenerRegistration {
        return FirebaseFirestore.getInstance()
            .collection("user_reviews")
            .whereEqualTo("proposalId", proposalId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val reviews = snapshot.documents.mapNotNull { it.toObject(UserReview::class.java) }
                onDataChange(reviews)
            }
    }

    suspend fun addReview(review: UserReview): Boolean {
        return try {
            val docRef = reviewsCollection.document()
            review.reviewId = docRef.id
            docRef.set(review).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun updateReview(review: UserReview): Boolean {
        return try {
            if (review.reviewId.isBlank()) return false
            reviewsCollection.document(review.reviewId).set(review).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun deleteReview(reviewId: String): Boolean {
        return try {
            if (reviewId.isBlank()) return false
            reviewsCollection.document(reviewId).delete().await()
            true
        } catch (_: Exception) {
            false
        }
    }
}