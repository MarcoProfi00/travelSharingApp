package com.example.travelsharingapp.data.model

import com.google.firebase.Timestamp


data class TravelProposalReview(
    val reviewId: String = "",
    val proposalId: String = "",
    val reviewerId: String = "",
    val reviewerFirstName: String = "",
    val reviewerLastName: String = "",
    val images: List<String> = emptyList(),
    val tips: List<String> = emptyList(),
    val rating: Float = 0.0f,
    val comment: String = "",
    val date: Timestamp = Timestamp.now()
)