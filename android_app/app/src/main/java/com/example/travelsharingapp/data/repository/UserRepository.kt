package com.example.travelsharingapp.data.repository

import android.util.Log
import com.example.travelsharingapp.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class UserRepository() {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun doesUserProfileExist(userId: String): Boolean {
        if (userId.isBlank()) return false
        return try {
            val document = usersCollection.document(userId).get().await()
            document.exists()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error checking if user profile exists for $userId", e)
            false
        }
    }

    suspend fun createUserProfile(userProfile: UserProfile): Boolean {
        return try {
            usersCollection.document(userProfile.userId).set(userProfile).await()
            Log.d("UserRepository", "User profile created for ${userProfile.userId}")
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error creating user profile for ${userProfile.userId}", e)
            false
        }
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        if (userId.isBlank()) return null
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            val profile = documentSnapshot.toObject(UserProfile::class.java)
            profile
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user profile $userId", e)
            null
        }
    }

    fun observeUserProfile(userId: String, onResult: (UserProfile?) -> Unit): ListenerRegistration? {
        if (userId.isBlank()) return null
        return try {
            usersCollection.document(userId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("UserRepository", "Error observing user profile $userId", error)
                    onResult(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val profile = snapshot.toObject(UserProfile::class.java)
                    onResult(profile)
                } else {
                    onResult(null)
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception observing user profile $userId", e)
            null
        }
    }

    suspend fun getAllUserProfiles(): List<UserProfile> {
        return try {
            val snapshot = usersCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(UserProfile::class.java) }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting all user profiles", e)
            emptyList()
        }
    }

    suspend fun updateUserProfile(userProfile: UserProfile): Boolean {
        return try {
            usersCollection.document(userProfile.userId).set(userProfile).await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user profile", e)
            false
        }
    }

    suspend fun addFavorite(userId: String, proposalId: String) {
        val user = getUserProfile(userId)
        user?.let {
            if (!it.favoriteProposals.contains(proposalId)) {
                val updated = it.copy(favoriteProposals = it.favoriteProposals + proposalId)
                updateUserProfile(updated)
            }
        }
    }

    suspend fun removeFavorite(userId: String, proposalId: String) {
        val user = getUserProfile(userId)
        user?.let {
            if (it.favoriteProposals.contains(proposalId)) {
                val updated = it.copy(favoriteProposals = it.favoriteProposals - proposalId)
                updateUserProfile(updated)
            }
        }
    }
}
