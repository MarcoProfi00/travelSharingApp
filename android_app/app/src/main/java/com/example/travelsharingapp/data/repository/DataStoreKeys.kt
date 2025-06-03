package com.example.travelsharingapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

internal val Context.dataStoreInstance: DataStore<Preferences> by preferencesDataStore(name = "settings")

object AuthPreferenceKeys {
    fun profileExistsKey(userId: String): Preferences.Key<Boolean> =
        booleanPreferencesKey("profile_exists_$userId")
}

object ThemePreferenceKeys {
    val THEME_KEY = stringPreferencesKey("theme_preference")
}

object NotificationPreferenceKeys {
    val MASTER_NOTIFICATIONS_ENABLED = booleanPreferencesKey("pref_master_notifications_enabled")
}