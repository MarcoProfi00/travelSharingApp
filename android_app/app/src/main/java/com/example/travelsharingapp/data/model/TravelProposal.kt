package com.example.travelsharingapp.data.model

import com.example.travelsharingapp.utils.toProposalStatusOrNull
import com.example.travelsharingapp.utils.toTypologyOrNull
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

data class ItineraryStop(
    val place: String = "",
    val position: GeoPoint? = null,
    val description: String = "",
    @get:PropertyName("isGroup") @set:PropertyName("isGroup")
    var isGroup: Boolean = false
)

enum class Typology {
    Adventure,
    Relax,
    Cultural,
    Nature,
    Luxury,
    Sport,
    CityBreak,
    RoadTrip
}

enum class ProposalStatus {
    Published,
    Full,
    Concluded
}

data class Message(
    val senderId: Int = 0,
    val receiverId: Int = 0,
    val content: String = "",
    val timestamp: String = ""
)

data class TravelProposal(
    val proposalId: String = "",

    val organizerId: String = "",

    val name: String = "",
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val minPrice: Int = 0,
    val maxPrice: Int = 0,
    val maxParticipants: Int = 0,
    val typology: String = "",
    val description: String = "",
    val suggestedActivities: List<String> = emptyList(),
    val itinerary: List<ItineraryStop> = emptyList(),
    val images: List<String> = emptyList(),
    val thumbnails: List<String> = emptyList(),
    val status: String = "",

    val applicationIds: List<String> = emptyList(),
    val messages: List<Message> = emptyList(),
    val participantsCount: Int = 0,
    val pendingApplicationsCount: Int = 0,
) {
    @get:Exclude
    val typologyEnum: Typology?
        get() = typology.toTypologyOrNull()

    @get:Exclude
    val statusEnum: ProposalStatus?
        get() = status.toProposalStatusOrNull()
}
