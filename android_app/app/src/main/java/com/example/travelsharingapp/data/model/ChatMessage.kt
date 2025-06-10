package com.example.travelsharingapp.data.model

import com.google.firebase.Timestamp

data class ChatMessage(
    val messageId: String = "",
    val proposalId: String = "",
    val senderId: String = "",
    val message: String = "",
    val imageUrl: String? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val replyToMessage: ChatMessage? = null,
    val deleted: Boolean = false
)
