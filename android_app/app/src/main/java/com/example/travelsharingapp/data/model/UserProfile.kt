package com.example.travelsharingapp.data.model

import com.google.firebase.Timestamp

data class UserProfile(
    val userId: String = "",

    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val nickname: String = "",
    val birthDate: Timestamp? = null,
    val phoneNumber: String = "",
    val description: String = "",
    val interests: List<String> = emptyList(),
    val desiredDestinations: List<String> = emptyList(),

    val rating: Float = 0.0f,
    val numberOfReviews: Int = 0,

    val profileImage: String? = null,
    val applicationIds: List<String> = emptyList(),
    val ownProposals: List<String> = emptyList(),
    val favoriteProposals: List<String> = emptyList(),

    val fcmTokens: List<String> = emptyList()
)