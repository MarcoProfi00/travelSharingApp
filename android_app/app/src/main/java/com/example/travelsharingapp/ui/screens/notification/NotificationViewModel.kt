package com.example.travelsharingapp.ui.screens.notification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.model.Notification
import com.example.travelsharingapp.data.repository.NotificationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private var notificationListenerJob: Job? = null

    fun startListeningNotificationsForUser(userId: String) {
        if (notificationListenerJob?.isActive == true) {
            return
        }

        notificationListenerJob?.cancel()

        notificationListenerJob = viewModelScope.launch {
            repository.observeNotificationsForUser(userId)
                .collect { notificationsList ->
                    _notifications.value = notificationsList
                }
        }
    }

    fun markNotificationAsRead(userId: String, notificationId: String) {
        viewModelScope.launch {
            try {
                repository.markNotificationAsRead(userId, notificationId)
            } catch (e: Exception) {
                Log.e("NotifVM", "Error marking notification $notificationId as read", e)
            }
        }
    }

    fun deleteNotificationOnClick(userId: String, notificationId: String) {
        viewModelScope.launch {
            Log.d("NotifVM", "Notification $notificationId for user $userId clicked. Attempting to delete.")
            try {
                repository.deleteNotification(userId, notificationId)
                _notifications.value = _notifications.value.filterNot { it.notificationId == notificationId }
            } catch (e: Exception) {
                Log.e("NotifVM", "Failed to delete notification $notificationId for user $userId", e)
            }
        }
    }

    fun clearNotificationData() {
        notificationListenerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        clearNotificationData()
    }
}

class NotificationViewModelFactory(
    private val repository: NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass
            .getConstructor(NotificationRepository::class.java)
            .newInstance(repository)
    }
}
