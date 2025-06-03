package com.example.travelsharingapp.data.model

import com.google.firebase.Timestamp

data class UserReview(
    var reviewId: String = "",
    var reviewerId: String = "",
    var reviewerFirstName: String = "",
    var reviewerLastName: String = "",
    var reviewedUserId: String = "",
    var proposalId: String = "",
    var rating: Float = 0f,
    var comment: String = "",
    var date: Timestamp = Timestamp.now()
)