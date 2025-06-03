package com.example.travelsharingapp.data.repository

import android.util.Log
import com.example.travelsharingapp.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NotificationRepository(firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val collectionRef = firestore.collection("users")

    fun getNotificationsForUser(userId: String): Flow<List<Notification>> {
        return collectionRef.document(userId)
            .collection("user_notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val notificationObject = doc.toObject(Notification::class.java)
                    notificationObject?.copy(notificationId = doc.id)
                }
            }
    }

    suspend fun markNotificationAsRead(userId: String, notificationId: String) {
        try {
            collectionRef.document(userId)
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
            collectionRef.document(userId)
                .collection("user_notifications").document(notificationId)
                .delete().await()
        } catch (e: Exception) {
            Log.e("NotifRepo", "Error deleting notification $notificationId for user $userId", e)
            throw e
        }
    }
}