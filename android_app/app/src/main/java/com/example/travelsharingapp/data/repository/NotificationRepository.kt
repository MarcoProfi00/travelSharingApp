package com.example.travelsharingapp.data.repository

import android.util.Log
import com.example.travelsharingapp.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository(firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val userCollection = firestore.collection("users")

    fun observeNotificationsForUser(userId: String): Flow<List<Notification>> = callbackFlow {
        val notificationsCollection = userCollection.document(userId)
            .collection("user_notifications")

        val registration: ListenerRegistration = notificationsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    cancel("Snapshot error", error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(Notification::class.java) ?: emptyList()
                trySend(list)
            }

        awaitClose { registration.remove() }
    }

    suspend fun markNotificationAsRead(userId: String, notificationId: String) {
        try {
            userCollection.document(userId)
                .collection("user_notifications").document(notificationId)
                .update("read", true)
                .await()
        } catch (e: Exception) {
            Log.e("NotifRepo", "Error marking notification $notificationId as read for user $userId", e)
            throw e
        }
    }

    suspend fun deleteNotification(userId: String, notificationId: String) {
        try {
            userCollection.document(userId)
                .collection("user_notifications").document(notificationId)
                .delete().await()
        } catch (e: Exception) {
            Log.e("NotifRepo", "Error deleting notification $notificationId for user $userId", e)
            throw e
        }
    }
}