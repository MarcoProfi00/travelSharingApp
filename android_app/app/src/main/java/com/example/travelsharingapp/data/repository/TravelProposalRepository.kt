package com.example.travelsharingapp.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.travelsharingapp.data.model.TravelProposal
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class TravelProposalRepository(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val collection = db.collection("travelProposals")
    private val applicationsCollection = db.collection("travel_applications")

    fun observeAllProposals(): Flow<List<TravelProposal>> = callbackFlow {
        val registration: ListenerRegistration = collection.addSnapshotListener { snapshot, error  ->
            if (error != null) {
                cancel("Snapshot error", error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects(TravelProposal::class.java) ?: emptyList())
        }
        awaitClose { registration.remove() }
    }

    fun observeProposalsByOrganizer(organizerId: String): Flow<List<TravelProposal>> = callbackFlow {
        val registration: ListenerRegistration = collection
            .whereEqualTo("organizerId", organizerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    cancel("Snapshot error", error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(TravelProposal::class.java) ?: emptyList()
                trySend(list)
            }

        awaitClose { registration.remove() }
    }

    suspend fun getProposalById(id: String): TravelProposal? {
        val snapshot = collection.document(id).get().await()
        return snapshot.toObject(TravelProposal::class.java)
    }

    private suspend fun uploadProposalImagesToFirebase(
        proposalId: String,
        imageUrisToUpload: List<Uri>
    ): List<String> {
        val downloadUrls = mutableListOf<String>()
        val contentResolver = context.contentResolver
        val storageRef = storage.reference.child("travel_images")

        for (uri in imageUrisToUpload) {
            try {
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val extension = when (mimeType) {
                    "image/png" -> "png"
                    "image/webp" -> "webp"
                    "image/jpg", "image/jpeg" -> "jpg"
                    else -> "jpg"
                }

                val uniqueName = "${proposalId}_${System.currentTimeMillis()}_${UUID.randomUUID()}.$extension"
                val imageRef = storageRef.child(uniqueName)

                val inputStream = contentResolver.openInputStream(uri) ?: continue

                inputStream.use {
                    imageRef.putStream(it).await()
                }
                val downloadUrl = imageRef.downloadUrl.await()
                downloadUrls.add(downloadUrl.toString())
            } catch (e: Exception) {
                Log.e("FirebaseUpload", "Failed to upload image: $uri", e)
            }
        }

        return downloadUrls
    }

    suspend fun addProposal(proposal: TravelProposal, urisToUpload: List<Uri>) {
        val docRef = collection.document()
        val proposalId = docRef.id
        val newImageUrls = uploadProposalImagesToFirebase(proposalId, urisToUpload)
        val finalProposal = proposal.copy(
            proposalId = proposalId,
            images = newImageUrls
        )

        docRef.set(finalProposal).await()

    }

    suspend fun updateProposal(proposal: TravelProposal, urisToUpload: List<Uri>) {
        require(proposal.proposalId.isNotBlank()) { "Proposal ID cannot be blank for update." }

        val newImageUrls = uploadProposalImagesToFirebase(proposal.proposalId, urisToUpload)
        val allImages = proposal.images + newImageUrls
        val finalProposal = proposal.copy(images = allImages)

        collection.document(finalProposal.proposalId)
            .set(finalProposal)
            .await()
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun removeProposalById(proposalId: String) {
        val snapshot = collection.document(proposalId).get().await()
        val applicationIds = snapshot.get("applicationIds") as? List<String> ?: emptyList()

        applicationIds.filter { it.isNotBlank() }.forEach { appId ->
            applicationsCollection.document(appId).delete().await()
        }

        collection.document(proposalId).delete().await()
    }
}