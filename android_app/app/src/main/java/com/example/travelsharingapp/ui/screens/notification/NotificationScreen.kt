package com.example.travelsharingapp.ui.screens.notification

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelsharingapp.data.model.Notification
import com.example.travelsharingapp.data.model.NotificationType
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.settings.NotificationSettingsViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    modifier: Modifier = Modifier,
    currentUserId: String,
    topBarViewModel: TopBarViewModel,
    notificationsViewModel: NotificationViewModel,
    notificationSettingsViewModel: NotificationSettingsViewModel = viewModel(),
    onNavigateToProposal: (String) -> Unit,
    onNavigateToTravelReviews: (travelId: String) -> Unit,
    onNavigateToUserReviewsList: (userId: String) -> Unit,
    onNavigateToManageTravelApplications: (travelId: String) -> Unit,
    onBack: () -> Unit
) {
    val notifications by notificationsViewModel.notifications.collectAsState()
    val hasSeenSwipeGuide by notificationSettingsViewModel.hasSeenSwipeGuide.collectAsState()

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
        ) {
            items(
                count = notifications.size,
                key = { index -> notifications[index].notificationId },
                itemContent = { index ->
                    val notification = notifications[index]

                    val runIntroAnimation = index == 0 && !hasSeenSwipeGuide

                    SwipeToDismissContainer(
                        onDelete = { notificationsViewModel.deleteNotificationOnClick(currentUserId, notification.notificationId) },
                        runIntroAnimation = runIntroAnimation,
                        onIntroAnimationFinished = { notificationSettingsViewModel.markSwipeGuideAsSeen() }
                    ) {
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
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun SwipeToDismissContainer(
    onDelete: () -> Unit,
    runIntroAnimation: Boolean,
    onIntroAnimationFinished: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { newValue ->
            if(newValue == SwipeToDismissBoxValue.EndToStart){
                onDelete()
                true
            } else {
                false
            }
        }
    )

    val offsetX = remember { Animatable(0f) }
    val density = LocalDensity.current

    LaunchedEffect(runIntroAnimation) {
        if (runIntroAnimation) {
            val revealAmountPx = with(density) { -150.dp.toPx() }
            delay(750)
            offsetX.animateTo(
                targetValue = revealAmountPx,
                animationSpec = tween(durationMillis = 600)
            )
            delay(1500)
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 400)
            )
            onIntroAnimationFinished()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier
            .offset { IntOffset(offsetX.value.roundToInt(), 0) },
        backgroundContent = {
            if (dismissState.dismissDirection.name == SwipeToDismissBoxValue.EndToStart.name || runIntroAnimation) {
                Box(
                    modifier = Modifier.offset { IntOffset(-offsetX.value.roundToInt(), 0) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.error),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "delete",
                            tint = MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier.padding(16.dp).size(24.dp)
                        )
                    }
                }
            }
        },
        enableDismissFromStartToEnd = false,
        content = {content()}
    )
}

@Composable
fun NotificationItem(
    notification: Notification,
    onCardClick: () -> Unit
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
            .padding(16.dp),
        onClick = { onCardClick() }
    ){

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NotificationIcon(
                    type = notification.type,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Bold,
                        color = titleColor.copy(alpha = contentEmphasisAlpha)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
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
    }
}

@Composable
fun NotificationIcon(type: String, modifier: Modifier = Modifier) {
    val iconVector = when (NotificationType.fromKey(type)) {
        NotificationType.NEW_TRAVEL_REVIEW -> Icons.Default.RateReview
        NotificationType.NEW_USER_REVIEW -> Icons.Default.RateReview
        NotificationType.NEW_TRAVEL_APPLICATION -> Icons.AutoMirrored.Filled.Assignment
        NotificationType.TRAVEL_APPLICATION_ACCEPTED -> Icons.Default.CheckCircle
        NotificationType.TRAVEL_APPLICATION_REJECTED -> Icons.Default.Cancel
        NotificationType.LAST_MINUTE_TRIP -> Icons.Default.LocalFireDepartment
        null -> Icons.Default.Notifications
    }

    Icon(
        imageVector = iconVector,
        contentDescription = "Notification Type",
        modifier = modifier,
        tint = MaterialTheme.colorScheme.primary
    )
}

fun formatTimestamp(date: Date): String {
    val formatter = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
    return formatter.format(date)
}
