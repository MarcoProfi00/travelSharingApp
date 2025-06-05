package com.example.travelsharingapp.data.repository

import com.example.travelsharingapp.data.model.TravelProposalReview
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TravelReviewRepository() {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("travel_reviews")

    fun observeReviewsByProposalId(proposalId: String): Flow<List<TravelProposalReview>> = callbackFlow {
        val registration: ListenerRegistration = collection
            .whereEqualTo("proposalId", proposalId)
            .addSnapshotListener { snapshot, error  ->
            if (error != null) {
                cancel("Snapshot error", error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects(TravelProposalReview::class.java) ?: emptyList())
        }
        awaitClose { registration.remove() }
    }


    suspend fun addReview(proposalId: String, review: TravelProposalReview): String? {
        return try {
            val docRef = collection.document()
            val reviewWithId = review.copy(reviewId = docRef.id, proposalId = proposalId)
            docRef.set(reviewWithId).await()
            docRef.id
        } catch (_: Exception) {
            null
        }
    }

    suspend fun updateReview(proposalId: String, review: TravelProposalReview) {
        try {
            collection.document(review.reviewId).update(
                mapOf(
                    "reviewerId" to review.reviewerId,
                    "reviewerFirstName" to review.reviewerFirstName,
                    "reviewerLastName" to review.reviewerLastName,
                    "images" to review.images,
                    "tips" to review.tips,
                    "rating" to review.rating,
                    "comment" to review.comment,
                    "proposalId" to proposalId
                )
            ).await()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun deleteReview(reviewId: String) {
        try {
            collection.document(reviewId).delete().await()
        } catch (_: Exception) {
            null
        }
    }
}