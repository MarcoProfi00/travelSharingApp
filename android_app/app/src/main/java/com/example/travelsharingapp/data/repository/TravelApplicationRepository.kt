package com.example.travelsharingapp.data.repository

import com.example.travelsharingapp.data.model.ApplicationStatus
import com.example.travelsharingapp.data.model.TravelApplication
import com.example.travelsharingapp.data.model.TravelProposal
import com.example.travelsharingapp.utils.toApplicationStatusOrNull
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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

        val proposalRef = proposalsCollection.document(application.proposalId)

        val travelProposal = proposalRef.get().await().toObject(TravelProposal::class.java)
            ?: throw IllegalStateException("Travel proposal with ID ${application.proposalId} not found.")

        val allPendingApplications = applicationsCollection
            .whereEqualTo("proposalId", application.proposalId)
            .whereEqualTo("status", ApplicationStatus.Pending.name)
            .get()
            .await()
            .toObjects(TravelApplication::class.java)

        val newParticipantsCount = travelProposal.participantsCount + 1 + application.accompanyingGuests.size
        val isNowFull = newParticipantsCount >= travelProposal.maxParticipants

        db.runTransaction { transaction ->
            val freshProposalSnapshot = transaction.get(proposalRef)
            val freshTravelProposal = freshProposalSnapshot.toObject(TravelProposal::class.java)
                ?: throw FirebaseFirestoreException(
                    "Proposal not found during transaction.",
                    FirebaseFirestoreException.Code.ABORTED
                )

            val expectedParticipantsCount = freshTravelProposal.participantsCount + 1 + application.accompanyingGuests.size
            if (expectedParticipantsCount > freshTravelProposal.maxParticipants) {
                throw FirebaseFirestoreException(
                    "Proposal state changed, participant count would exceed max.",
                    FirebaseFirestoreException.Code.ABORTED
                )
            }

            val applicationRef = applicationsCollection.document(application.applicationId)
            transaction.update(applicationRef, "status", ApplicationStatus.Accepted.name)
            transaction.update(proposalRef, "participantsCount", newParticipantsCount)

            if (isNowFull) {
                transaction.update(proposalRef, "status", "Full")
                transaction.update(proposalRef, "pendingApplicationsCount", 0)

                allPendingApplications.forEach { appToCancel ->
                    if (appToCancel.applicationId != application.applicationId) {
                        val appToCancelRef = applicationsCollection.document(appToCancel.applicationId)
                        transaction.update(appToCancelRef, "status", ApplicationStatus.Cancelled.name)
                    }
                }
            } else {
                val newPendingCount = (freshTravelProposal.pendingApplicationsCount - 1).coerceAtLeast(0)
                transaction.update(proposalRef, "pendingApplicationsCount", newPendingCount)
            }

            null
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