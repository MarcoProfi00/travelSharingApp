package com.example.travelsharingapp.data.model

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.google.firebase.Timestamp

enum class NotificationType(
    val key: String,
    val displayName: String,
    val dataStoreKey: Preferences.Key<Boolean>
) {
    NEW_TRAVEL_REVIEW(
        "new_travel_review",
        "New Travel Reviews",
        booleanPreferencesKey("pref_notif_type_new_travel_review_enabled")
    ),
    NEW_USER_REVIEW(
        "new_user_review",
        "New User Reviews",
        booleanPreferencesKey("pref_notif_type_new_user_review_enabled")
    ),
    NEW_TRAVEL_APPLICATION(
        "new_travel_application",
        "New Travel Applications",
        booleanPreferencesKey("pref_notif_type_new_travel_application_enabled")
    ),
    TRAVEL_APPLICATION_ACCEPTED(
        "application_accepted",
        "Travel Application Accepted",
        booleanPreferencesKey("pref_notif_type_travel_application_accepted_enabled")
    ),
    TRAVEL_APPLICATION_REJECTED(
        "application_rejected",
        "Travel Application Rejected",
        booleanPreferencesKey("pref_notif_type_travel_application_rejected_enabled")
    ),
    LAST_MINUTE_TRIP(
        "last_minute_trip",
        "Last Minute Trip",
        booleanPreferencesKey("pref_notif_type_last_minute_travel_enabled")
    ),
    NEW_CHAT_MESSAGE(
        "new_chat_message",
        "New Chat Message",
        booleanPreferencesKey("pref_notif_type_new_chat_message_enabled")
    );

    companion object {
        fun fromKey(key: String?): NotificationType? {
            return entries.find { it.key == key }
        }
    }
}

data class Notification(
    val notificationId: String = "",
    val recipientId: String = "",
    val type: String = "",
    val title: String = "",
    val message: String = "",
    val relatedUserId: String? = null,
    val proposalId: String? = null,
    val applicationId: String? = null,
    val applicantId: String? = null,
    val reviewId: String? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val read: Boolean = false
)
