package com.example.travelsharingapp.ui.screens.notification

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.travelsharingapp.data.model.Notification
import com.example.travelsharingapp.data.model.NotificationType
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationScreen(
    modifier: Modifier = Modifier,
    currentUserId: String,
    topBarViewModel: TopBarViewModel,
    notificationsViewModel: NotificationViewModel,
    onNavigateToProposal: (String) -> Unit,
    onNavigateToTravelReviews: (travelId: String) -> Unit,
    onNavigateToUserReviewsList: (userId: String) -> Unit,
    onNavigateToManageTravelApplications: (travelId: String) -> Unit,
    onBack: () -> Unit
) {
    val notifications by notificationsViewModel.notifications.collectAsState()

    LaunchedEffect(currentUserId) {
        notificationsViewModel.startListeningNotificationsForUser(currentUserId)
    }

    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "Notifications",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = { /* nothing */ },
            floatingActionButton = { /* nothing */ }
        )
    }

    if (notifications.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("You have no new notifications")
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items (
                count = notifications.size,
                key = { index -> notifications[index].notificationId },
                itemContent = { index ->
                    val notification = notifications[index]
                    NotificationItem(
                        notification = notification,
                        onCardClick = {
                            if (!notification.read) {
                                notificationsViewModel.markNotificationAsRead(currentUserId, notification.notificationId)
                            }

                            when (notification.type) {
                                NotificationType.NEW_TRAVEL_REVIEW.key -> {
                                    notification.proposalId?.let { travelId ->
                                        onNavigateToTravelReviews(travelId)
                                    }
                                }
                                NotificationType.NEW_USER_REVIEW.key -> {
                                    onNavigateToUserReviewsList(currentUserId)
                                }
                                NotificationType.NEW_TRAVEL_APPLICATION.key -> {
                                    notification.proposalId?.let { travelId ->
                                        onNavigateToManageTravelApplications(travelId)
                                    }
                                }
                                else -> {
                                    if (notification.proposalId != null) {
                                        onNavigateToProposal(notification.proposalId)
                                    }
                                }
                            }
                        },
                        onDeleteClick = {
                            notificationsViewModel.deleteNotificationOnClick(currentUserId, notification.notificationId)
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val titleColor = MaterialTheme.colorScheme.primary
    val messageColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dateColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    val cardContainerColor = if (notification.read) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentEmphasisAlpha = if (notification.read) 0.7f else 1.0f


    ElevatedCard (
        colors = CardDefaults.cardColors(
            containerColor = cardContainerColor,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = contentEmphasisAlpha)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.read) 2.dp else 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = { onCardClick() }
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Bold,
                        color = titleColor.copy(alpha = contentEmphasisAlpha)
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = messageColor.copy(alpha = contentEmphasisAlpha)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatTimestamp(notification.timestamp.toDate()),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = dateColor.copy(alpha = contentEmphasisAlpha)
                    )
                )
            }

            IconButton(
                onClick = { onDeleteClick() },
                modifier = Modifier.align(Alignment.Top)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Notification",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = contentEmphasisAlpha)
                )
            }
        }
    }
}

fun formatTimestamp(date: Date): String {
    val formatter = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
    return formatter.format(date)
}
