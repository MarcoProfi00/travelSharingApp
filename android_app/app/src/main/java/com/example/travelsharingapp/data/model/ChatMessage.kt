package com.example.travelsharingapp.data.model

import com.google.firebase.Timestamp

data class ChatMessage(
    val messageId: String = "",
    val proposalId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderProfileImage: String? = null,
    val message: String = "",
    val imageUrl: String? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val replyToMessageId: String? = null,
    val replyPreview: String? = null
)
