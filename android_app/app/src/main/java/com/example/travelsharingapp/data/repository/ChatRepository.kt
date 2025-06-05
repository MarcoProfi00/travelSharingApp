package com.example.travelsharingapp.data.repository

import com.example.travelsharingapp.data.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getMessages(proposalId: String, onResult: (List<ChatMessage>) -> Unit) {
        db.collection("travelProposals")
            .document(proposalId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                val messages = snapshot.documents.mapNotNull { it.toObject(ChatMessage::class.java) }
                onResult(messages)
            }
    }

    suspend fun sendMessage(proposalId: String, message: ChatMessage) {
        val messageRef = db.collection("travelProposals")
            .document(proposalId)
            .collection("messages")
            .document()

        val messageWithId = message.copy(messageId = messageRef.id)
        messageRef.set(messageWithId).await()
    }
}