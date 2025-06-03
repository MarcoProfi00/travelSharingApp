package com.example.travelsharingapp.data.repository

import android.util.Log
import com.example.travelsharingapp.data.model.ApplicationStatus
import com.example.travelsharingapp.data.model.TravelApplication
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class TravelApplicationRepository() {

    private val db = FirebaseFirestore.getInstance()
    private val applicationsRef = db.collection("travel_applications")

    suspend fun getAllApplications(): List<TravelApplication> {
        return try {
            val snapshot = applicationsRef.get().await()
            snapshot.documents.mapNotNull { it.toObject(TravelApplication::class.java) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getApplicationsByProposalId(proposalId: String): List<TravelApplication> {
        return try {
            val snapshot = applicationsRef.whereEqualTo("proposalId", proposalId).get().await()
            snapshot.documents.mapNotNull { it.toObject(TravelApplication::class.java) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getApplicationsByUserId(userId: String): List<TravelApplication> {
        return try {
            val snapshot = applicationsRef.whereEqualTo("userId", userId).get().await()
            snapshot.documents.mapNotNull { it.toObject(TravelApplication::class.java) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun addApplication(application: TravelApplication): TravelApplication {
        val docRef = if (application.applicationId.isNotBlank()) {
            applicationsRef.document(application.applicationId)
        } else {
            applicationsRef.document()
        }

        val applicationWithId = application.copy(applicationId = docRef.id)
        docRef.set(applicationWithId).await()
        return applicationWithId
    }

    suspend fun removeApplication(applicationId: String) {
        applicationsRef.document(applicationId).delete().await()
    }

    suspend fun updateApplicationStatus(applicationId: String, newStatus: ApplicationStatus) {
        applicationsRef.document(applicationId).update("status", newStatus.name).await()
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun addApplicationIdToProposal(proposalId: String, applicationId: String) {
        val proposalRef = FirebaseFirestore.getInstance().collection("travelProposals").document(proposalId)
        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(proposalRef)
            val currentIds = (snapshot.get("applicationIds") as? List<String>)?.toMutableList() ?: mutableListOf()
            if (!currentIds.contains(applicationId)) {
                val index = currentIds.indexOfFirst { it.isBlank() }
                if (index != -1) currentIds[index] = applicationId else currentIds.add(applicationId)
                transaction.update(proposalRef, "applicationIds", currentIds)
            }
        }.await()
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun addApplicationIdToUser(userId: String, applicationId: String) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentIds = (snapshot.get("applicationIds") as? List<String>)?.toMutableList() ?: mutableListOf()
            if (!currentIds.contains(applicationId)) {
                val index = currentIds.indexOfFirst { it.isBlank() }
                if (index != -1) currentIds[index] = applicationId else currentIds.add(applicationId)
                transaction.update(userRef, "applicationIds", currentIds)
            }
        }.await()
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun removeApplicationIdFromProposal(proposalId: String, applicationId: String) {
        val proposalRef = FirebaseFirestore.getInstance().collection("travelProposals").document(proposalId)
        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(proposalRef)
            Log.d("FirestoreDebug", "Trying to update proposalId=$proposalId with applicationId=$applicationId, exists=${snapshot.exists()}")
            val currentIds = (snapshot.get("applicationIds") as? List<String>)?.toMutableList() ?: mutableListOf()
            if (currentIds.contains(applicationId)) {
                val index = currentIds.indexOf(applicationId)
                if (index != -1) currentIds[index] = ""
                transaction.update(proposalRef, "applicationIds", currentIds)
            }
        }.await()
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun removeApplicationIdFromUser(userId: String, applicationId: String) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            Log.d("FirestoreDebug", "Trying to update userId=$userId with applicationId=$applicationId, exists=${snapshot.exists()}")
            val currentIds = (snapshot.get("applicationIds") as? List<String>)?.toMutableList() ?: mutableListOf()
            if (currentIds.contains(applicationId)) {
                val index = currentIds.indexOf(applicationId)
                if (index != -1) currentIds[index] = ""
                transaction.update(userRef, "applicationIds", currentIds)
            }
        }.await()
    }

    fun listenApplicationsByUserId(
        userId: String,
        onUpdate: (List<TravelApplication>) -> Unit
    ): ListenerRegistration {
        return applicationsRef
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(TravelApplication::class.java) }
                    onUpdate(list)
                }
            }
    }
}