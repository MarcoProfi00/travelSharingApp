package com.example.travelsharingapp.data.repository

import android.net.Uri
import com.example.travelsharingapp.data.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import android.content.ContentResolver
import android.content.Context

class ChatRepository(private val context: Context) {
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

    suspend fun updateMessage(proposalId: String, messageId: String, newText: String) {
        val messageRef = db.collection("travelProposals")
            .document(proposalId)
            .collection("messages")
            .document(messageId)
        messageRef.update("message", newText).await()
    }

    suspend fun deleteMessage(proposalId: String, message: ChatMessage) {
        if (!message.imageUrl.isNullOrEmpty()) {
            try {
                FirebaseStorage.getInstance()
                    .getReferenceFromUrl(message.imageUrl)
                    .delete().await()
            } catch (_: Exception) {  }
        }

        db.collection("travelProposals")
            .document(proposalId)
            .collection("messages")
            .document(message.messageId)
            .update(
                mapOf(
                    "message" to "__deleted__",
                    "imageUrl" to null
                )
            ).await()
    }

    suspend fun sendMessage(proposalId: String, message: ChatMessage) {
        val messageRef = db.collection("travelProposals")
            .document(proposalId)
            .collection("messages")
            .document()

        val messageWithId = message.copy(messageId = messageRef.id)
        messageRef.set(messageWithId).await()
    }

    suspend fun uploadChatImage(proposalId: String, uri: Uri): String? {
        return try {
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val ext = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                "image/jpg", "image/jpeg" -> "jpg"
                else -> "jpg"
            }
            val name = "${proposalId}_${System.currentTimeMillis()}_${UUID.randomUUID()}.$ext"
            val ref = FirebaseStorage.getInstance().reference.child("chat_images/$name")

            context.contentResolver.openInputStream(uri)?.use {
                ref.putStream(it).await()
            }

            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun sendImageMessage(proposalId: String, senderId: String, senderName: String, imageUri: Uri) {
        val imageUrl = uploadChatImage(proposalId, imageUri)
        if (imageUrl != null) {
            val messageRef = db.collection("travelProposals")
                .document(proposalId)
                .collection("messages")
                .document()

            val chatMessage = ChatMessage(
                messageId = messageRef.id,
                proposalId = proposalId,
                senderId = senderId,
                senderName = senderName,
                message = "",
                imageUrl = imageUrl
            )
            messageRef.set(chatMessage).await()
        }
    }
}
