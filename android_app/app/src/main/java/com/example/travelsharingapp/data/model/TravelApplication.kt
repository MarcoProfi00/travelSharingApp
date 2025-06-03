package com.example.travelsharingapp.data.model

import com.example.travelsharingapp.utils.toApplicationStatusOrNull
import com.google.firebase.firestore.Exclude

enum class ApplicationStatus {
    Pending,
    Accepted,
    Rejected
}

data class GuestApplicant(
    val id: String = "",
    var name: String = "",
    var email: String = ""
)

data class TravelApplication(
    val applicationId: String = "",
    val proposalId: String = "",
    val userId: String = "",
    val motivation: String = "",
    var status: String = ApplicationStatus.Pending.name,
    val accompanyingGuests: List<GuestApplicant> = emptyList()
) {
    @get:Exclude
    val statusEnum: ApplicationStatus?
        get() = status.toApplicationStatusOrNull()
}

