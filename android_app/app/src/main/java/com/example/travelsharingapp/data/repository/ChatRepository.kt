package com.example.travelsharingapp.data.repository

import android.content.Context
import android.net.Uri
import com.example.travelsharingapp.data.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepository(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val collection = db.collection("travelProposals")

    fun observeMessagesByProposal(proposalId: String): Flow<List<ChatMessage>> = callbackFlow {
        val messagessCollection = collection.document(proposalId)
            .collection("messages")

        val registration: ListenerRegistration = messagessCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error  ->
            if (error != null) {
                cancel("Snapshot error", error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects(ChatMessage::class.java) ?: emptyList())
        }

        awaitClose { registration.remove() }
    }

    suspend fun updateMessage(proposalId: String, messageId: String, newText: String) {
        val messagessCollection = collection.document(proposalId)
            .collection("messages")

        val messageRef = messagessCollection
            .document(messageId)
        messageRef.update("message", newText).await()
    }

    suspend fun deleteMessage(proposalId: String, message: ChatMessage) {
        val messagessCollection = collection.document(proposalId)
            .collection("messages")

        if (!message.imageUrl.isNullOrEmpty()) {
            try {
                FirebaseStorage.getInstance()
                    .getReferenceFromUrl(message.imageUrl)
                    .delete().await()
            } catch (_: Exception) {  }
        }

        messagessCollection
            .document(message.messageId)
            .update(
                mapOf(
                    "deleted" to true,
                    "imageUrl" to null
                )
            ).await()
    }

    suspend fun sendMessage(proposalId: String, message: ChatMessage) {
        val messagessCollection = collection.document(proposalId)
            .collection("messages")

        val messageRef = messagessCollection.document()
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
            val ref = storage.reference.child("chat_images/$name")

            context.contentResolver.openInputStream(uri)?.use {
                ref.putStream(it).await()
            }

            ref.downloadUrl.await().toString()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun updateLastReadTimestamp(proposalId: String, userId: String) {
        val now = com.google.firebase.Timestamp.now()
        FirebaseFirestore.getInstance()
            .collection("travelProposals")
            .document(proposalId)
            .collection("readStatus")
            .document(userId)
            .set(mapOf("lastReadTimestamp" to now))
            .await()
    }

    fun getUnreadMessagesCount(proposalId: String, userId: String): Flow<Int> = callbackFlow {
        val readStatusRef = db.collection("travelProposals")
            .document(proposalId)
            .collection("readStatus")
            .document(userId)

        val messagesRef = db.collection("travelProposals")
            .document(proposalId)
            .collection("messages")

        var messagesListener: ListenerRegistration? = null

        val readStatusListener = readStatusRef.addSnapshotListener { readStatusSnapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            messagesListener?.remove()

            val lastReadTimestamp = readStatusSnapshot?.getTimestamp("lastReadTimestamp")

            val query = if (lastReadTimestamp != null) {
                messagesRef.whereGreaterThan("timestamp", lastReadTimestamp)
            } else {
                messagesRef
            }

            messagesListener = query.addSnapshotListener { messagesSnapshot, messagesError ->
                if (messagesError != null) {
                    println("Error listening for unread messages: $messagesError")
                    return@addSnapshotListener
                }

                val unreadCount = messagesSnapshot?.documents?.count { doc ->
                    doc.getString("senderId") != userId
                } ?: 0

                trySend(unreadCount)
            }
        }

        awaitClose {
            readStatusListener.remove()
            messagesListener?.remove()
        }
    }
}
