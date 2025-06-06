package com.example.travelsharingapp.data.repository

import com.example.travelsharingapp.data.model.ApplicationStatus
import com.example.travelsharingapp.data.model.TravelApplication
import com.example.travelsharingapp.data.model.TravelProposal
import com.example.travelsharingapp.utils.toApplicationStatusOrNull
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TravelApplicationRepository() {
    private val db = FirebaseFirestore.getInstance()
    private val applicationsCollection = db.collection("travel_applications")
    private val proposalsCollection = db.collection("travelProposals")
    private val usersCollection = db.collection("users")

    suspend fun addApplication(application: TravelApplication) {
        db.runTransaction { transaction ->
            val applicationRef = applicationsCollection.document()
            val proposalRef = proposalsCollection.document(application.proposalId)
            val userRef = usersCollection.document(application.userId)

            val travelProposalSnapshot = transaction.get(proposalRef)
            val travelProposal = travelProposalSnapshot.toObject(TravelProposal::class.java)
                ?: throw IllegalStateException("Travel proposal not found.")

            transaction.set(applicationRef, application.copy(applicationId = applicationRef.id))
            transaction.update(proposalRef, "applicationIds", FieldValue.arrayUnion(applicationRef.id))
            transaction.update(userRef, "applicationIds", FieldValue.arrayUnion(applicationRef.id))

            val newPendingApplicationsCount = travelProposal.pendingApplicationsCount + 1
            transaction.update(proposalRef, "pendingApplicationsCount", newPendingApplicationsCount)

        }.await()
    }

    suspend fun withdrawApplication(application: TravelApplication) {
        db.runTransaction { transaction ->
            val applicationRef = applicationsCollection.document(application.applicationId)
            val proposalRef = proposalsCollection.document(application.proposalId)
            val userRef = usersCollection.document(application.userId)

            val travelProposalSnapshot = transaction.get(proposalRef)
            val travelProposal = travelProposalSnapshot.toObject(TravelProposal::class.java)
                ?: throw IllegalStateException("Travel proposal not found.")

            transaction.delete(applicationRef)
            transaction.update(proposalRef, "applicationIds", FieldValue.arrayRemove(application.applicationId))
            transaction.update(userRef, "applicationIds", FieldValue.arrayRemove(application.applicationId))

            if (application.status == ApplicationStatus.Accepted.name) {
                val newParticipantsCount = travelProposal.participantsCount - (1 + application.accompanyingGuests.size)
                transaction.update(proposalRef, "participantsCount", newParticipantsCount)
                if (newParticipantsCount < travelProposal.maxParticipants) {
                    transaction.update(proposalRef, "status", "Published")
                }
            } else if (application.status == ApplicationStatus.Pending.name) {
                val newPendingCount = travelProposal.pendingApplicationsCount - 1
                transaction.update(proposalRef, "pendingApplicationsCount", newPendingCount)
            }
        }.await()
    }

    suspend fun acceptApplication(application: TravelApplication) {
        if (application.status.toApplicationStatusOrNull() == ApplicationStatus.Accepted) return

        db.runTransaction { transaction ->
            val applicationRef = applicationsCollection.document(application.applicationId)
            val proposalRef = proposalsCollection.document(application.proposalId)

            val travelProposalSnapshot = transaction.get(proposalRef)
            val travelProposal = travelProposalSnapshot.toObject(TravelProposal::class.java)
                ?: throw IllegalStateException("Travel proposal not found.")

            transaction.update(applicationRef, "status", ApplicationStatus.Accepted.name)

            val newParticipantsCount = travelProposal.participantsCount + 1 + application.accompanyingGuests.size
            val newPendingCount = if (application.status.toApplicationStatusOrNull() == ApplicationStatus.Pending) {
                travelProposal.pendingApplicationsCount - 1
            } else {
                travelProposal.pendingApplicationsCount
            }

            transaction.update(proposalRef, "participantsCount", newParticipantsCount)
            transaction.update(proposalRef, "pendingApplicationsCount", newPendingCount)

            if (newParticipantsCount >= travelProposal.maxParticipants) {
                transaction.update(proposalRef, "status", "Full")
            }
        }.await()
    }

    suspend fun rejectApplication(application: TravelApplication) {
        if (application.status.toApplicationStatusOrNull() == ApplicationStatus.Rejected) return

        db.runTransaction { transaction ->
            val applicationRef = applicationsCollection.document(application.applicationId)
            val proposalRef = proposalsCollection.document(application.proposalId)

            val travelProposalSnapshot = transaction.get(proposalRef)
            val travelProposal = travelProposalSnapshot.toObject(TravelProposal::class.java)
                ?: throw IllegalStateException("Travel proposal not found.")

            val originalStatus = application.status.toApplicationStatusOrNull()

            transaction.update(applicationRef, "status", ApplicationStatus.Rejected.name)

            if (originalStatus == ApplicationStatus.Accepted) {
                val newParticipantsCount = travelProposal.participantsCount - (1 + application.accompanyingGuests.size)
                transaction.update(proposalRef, "participantsCount", newParticipantsCount)
                if (newParticipantsCount < travelProposal.maxParticipants) {
                    transaction.update(proposalRef, "status", "Published")
                }
            } else if (originalStatus == ApplicationStatus.Pending) {
                val newPendingCount = travelProposal.pendingApplicationsCount - 1
                transaction.update(proposalRef, "pendingApplicationsCount", newPendingCount)
            }
        }.await()
    }

    fun observeApplicationsForUser(userId: String): Flow<List<TravelApplication>> = callbackFlow {
        val registration: ListenerRegistration = applicationsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    cancel("Snapshot error", error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(TravelApplication::class.java) ?: emptyList()
                trySend(list)
            }

        awaitClose { registration.remove() }
    }

    fun observeApplicationsForProposal(proposalId: String): Flow<List<TravelApplication>> = callbackFlow {
        val registration: ListenerRegistration = applicationsCollection
            .whereEqualTo("proposalId", proposalId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    cancel("Snapshot error", error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(TravelApplication::class.java) ?: emptyList()
                trySend(list)
            }

        awaitClose { registration.remove() }
    }
}