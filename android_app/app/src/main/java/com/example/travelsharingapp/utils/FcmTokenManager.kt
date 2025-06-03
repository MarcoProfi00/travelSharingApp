package com.example.travelsharingapp.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object FcmTokenManager {
    private const val TAG = "FcmTokenManager"

    fun registerTokenForCurrentUser() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.d(TAG, "No user logged in, cannot register FCM token.")
            return
        }
        val userId = currentUser.uid

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed for user $userId", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            if (token == null) {
                Log.w(TAG, "FCM token is null for user $userId")
                return@addOnCompleteListener
            }
            Log.d(TAG, "Registering FCM Token for user $userId: $token")
            updateUserFcmToken(userId, token, true)
        }
    }

    fun unregisterTokenForCurrentUser(onComplete: ((Boolean) -> Unit)? = null) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.d(TAG, "No user logged in, cannot unregister FCM token.")
            onComplete?.invoke(true)
            return
        }
        val userId = currentUser.uid

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM token for unregistration failed for user $userId", task.exception)
                onComplete?.invoke(false)
                return@addOnCompleteListener
            }
            val token = task.result
            if (token == null) {
                Log.w(TAG, "FCM token is null, cannot unregister for user $userId")
                onComplete?.invoke(false)
                return@addOnCompleteListener
            }
            Log.d(TAG, "Unregistering FCM Token for user $userId: $token")
            updateUserFcmToken(userId, token, false) { success ->
                if (success) {
                    FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            Log.d(TAG, "FCM token instance deleted successfully.")
                        } else {
                            Log.w(TAG, "Failed to delete FCM token instance.", deleteTask.exception)
                        }
                        onComplete?.invoke(true)
                    }
                } else {
                    onComplete?.invoke(false)
                }
            }
        }
    }

    fun handleTokenRefresh(newToken: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            Log.d(TAG, "Token refreshed while user $userId is logged in. Updating token: $newToken")
            updateUserFcmToken(userId, newToken, true)
        } else {
            Log.d(TAG, "Token refreshed, but no user is currently logged in: $newToken")
        }
    }

    private fun updateUserFcmToken(userId: String, token: String, isRegister: Boolean, onComplete: ((Boolean) -> Unit)? = null) {
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        val firestoreOperation = if (isRegister) {
            userDocRef.update("fcmTokens", FieldValue.arrayUnion(token))
        } else {
            userDocRef.update("fcmTokens", FieldValue.arrayRemove(token))
        }

        firestoreOperation
            .addOnSuccessListener {
                val action = if (isRegister) "registered" else "unregistered"
                Log.d(TAG, "FCM Token $action for user: $userId")
                onComplete?.invoke(true)
            }
            .addOnFailureListener { e ->
                val action = if (isRegister) "register" else "unregister"
                Log.w(TAG, "Error trying to $action FCM Token for $userId", e)
                onComplete?.invoke(false)
            }
    }
}