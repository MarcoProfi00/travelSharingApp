package com.example.travelsharingapp.data.repository

import com.example.travelsharingapp.data.model.UserProfile
import com.example.travelsharingapp.data.model.UserReview
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserReviewRepository() {
    private val db = FirebaseFirestore.getInstance()
    private val reviewsCollection = db.collection("user_reviews")
    private val usersCollection = db.collection("users")

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

    fun observeReviewsByProposalId(proposalId: String): Flow<List<UserReview>> = callbackFlow {
        val registration: ListenerRegistration = reviewsCollection
            .whereEqualTo("proposalId", proposalId)
            .addSnapshotListener { snapshot, error  ->
                if (error != null) {
                    cancel("Snapshot error", error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(UserReview::class.java) ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    suspend fun addReview(review: UserReview): Result<Unit> = runCatching {
        db.runTransaction { transaction ->
            val userProfileRef = usersCollection.document(review.reviewedUserId)
            val userProfileSnapshot = transaction.get(userProfileRef)
            val userProfile = userProfileSnapshot.toObject(UserProfile::class.java)
                ?: throw IllegalStateException("User profile not found.")

            val newNumberOfReviews = userProfile.numberOfReviews + 1
            val newAverageRating = ((userProfile.rating * userProfile.numberOfReviews) + review.rating) / newNumberOfReviews

            val updatedProfile = userProfile.copy(
                rating = newAverageRating,
                numberOfReviews = newNumberOfReviews
            )

            val newReviewRef = reviewsCollection.document()
            review.reviewId = newReviewRef.id

            transaction.set(newReviewRef, review)
            transaction.set(userProfileRef, updatedProfile)
        }.await()
    }

    suspend fun updateReview(updatedReview: UserReview, oldRating: Float): Result<Unit> = runCatching {
        db.runTransaction { transaction ->
            val userProfileRef = usersCollection.document(updatedReview.reviewedUserId)
            val userProfileSnapshot = transaction.get(userProfileRef)
            val userProfile = userProfileSnapshot.toObject(UserProfile::class.java)
                ?: throw IllegalStateException("User profile not found.")

            if (userProfile.numberOfReviews > 0) {
                val currentTotalRating = userProfile.rating * userProfile.numberOfReviews
                val newTotalRating = currentTotalRating - oldRating + updatedReview.rating
                val newAverageRating = newTotalRating / userProfile.numberOfReviews

                val updatedProfile = userProfile.copy(rating = newAverageRating)
                transaction.set(userProfileRef, updatedProfile)
            }

            val reviewRef = reviewsCollection.document(updatedReview.reviewId)
            transaction.set(reviewRef, updatedReview)
        }.await()
    }

    suspend fun deleteReview(reviewToDelete: UserReview): Result<Unit> = runCatching {
        db.runTransaction { transaction ->
            val userProfileRef = usersCollection.document(reviewToDelete.reviewedUserId)
            val userProfileSnapshot = transaction.get(userProfileRef)
            val userProfile = userProfileSnapshot.toObject(UserProfile::class.java)
                ?: throw IllegalStateException("User profile not found.")

            val newNumberOfReviews = userProfile.numberOfReviews - 1
            val newAverageRating = if (newNumberOfReviews <= 0) {
                0.0f
            } else {
                ((userProfile.rating * userProfile.numberOfReviews) - reviewToDelete.rating) / newNumberOfReviews
            }

            val updatedProfile = userProfile.copy(
                rating = newAverageRating,
                numberOfReviews = if (newNumberOfReviews < 0) 0 else newNumberOfReviews
            )

            val reviewRef = reviewsCollection.document(reviewToDelete.reviewId)
            transaction.delete(reviewRef)
            transaction.set(userProfileRef, updatedProfile)
        }.await()
    }
}