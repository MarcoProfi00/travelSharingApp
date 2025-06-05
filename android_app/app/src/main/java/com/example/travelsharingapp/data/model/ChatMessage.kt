package com.example.travelsharingapp.data.model

import com.google.firebase.Timestamp

data class ChatMessage(
    val messageId: String = "",
    val proposalId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now()
)