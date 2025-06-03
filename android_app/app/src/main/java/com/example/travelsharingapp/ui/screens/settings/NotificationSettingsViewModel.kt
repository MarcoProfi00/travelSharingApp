package com.example.travelsharingapp.ui.screens.settings

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.model.NotificationType
import com.example.travelsharingapp.data.repository.NotificationPreferenceKeys
import com.example.travelsharingapp.data.repository.dataStoreInstance
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStoreInstance

    val masterNotificationsEnabled: StateFlow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[NotificationPreferenceKeys.MASTER_NOTIFICATIONS_ENABLED] != false
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val notificationTypeEnabledStates: StateFlow<Map<NotificationType, Boolean>> =
        combine(NotificationType.entries.map { type ->
            dataStore.data.map { preferences ->
                type to (preferences[type.dataStoreKey] != false)
            }
        }) { flows ->
            flows.toMap()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotificationType.entries.associateWith { true }
        )

    fun setMasterNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { settings ->
                settings[NotificationPreferenceKeys.MASTER_NOTIFICATIONS_ENABLED] = enabled
            }
        }
    }

    fun setNotificationTypeEnabled(type: NotificationType, enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { settings ->
                settings[type.dataStoreKey] = enabled
            }
        }
    }
}