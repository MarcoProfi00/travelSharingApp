package com.example.travelsharingapp.ui.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Badge
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.travelsharingapp.data.model.ApplicationStatus
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_application.TravelApplicationViewModel
import com.example.travelsharingapp.ui.screens.travel_proposal.TravelProposalViewModel

@Composable
fun ChatListScreen(
    modifier: Modifier,
    userId: String,
    travelProposalViewModel: TravelProposalViewModel,
    travelApplicationViewModel: TravelApplicationViewModel,
    onNavigateToChat: (String) -> Unit,
    onNavigateBack: () -> Unit,
    topBarViewModel: TopBarViewModel,
    unreadMessagesCount: Map<String, Int>
) {
    val allProposals by travelProposalViewModel.allProposals.collectAsState()
    val ownedProposals by travelProposalViewModel.ownedProposals.collectAsState()
    val applications by travelApplicationViewModel.userSpecificApplications.collectAsState()

    LaunchedEffect(Unit) {

        topBarViewModel.setConfig(
            title = "Chat",
            navigationIcon = {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = null
        )
    }

    LaunchedEffect(userId) {
        travelApplicationViewModel.startListeningApplicationsForUser(userId)
        travelProposalViewModel.startListeningOwnedProposals(userId)
    }

    val acceptedApplications = applications.filter {
        it.userId == userId && it.statusEnum == ApplicationStatus.Accepted
    }

    val joinedProposals = allProposals.filter { proposal ->
        acceptedApplications.any { it.proposalId == proposal.proposalId }
    }

    val chatProposals = (ownedProposals + joinedProposals)
        .distinctBy { it.proposalId }

    LazyColumn(
        modifier = modifier.padding(16.dp)
    ) {
        items(
            count = chatProposals.size,
            key = { index -> chatProposals[index].proposalId },
            contentType = { "TravelProposalsCard" },
            itemContent = { index ->
                val proposal = chatProposals[index]
                val cardColor = if (index % 2 == 0)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    Color(0xFFE8EAF6)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToChat(proposal.proposalId) },
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                            containerColor = cardColor
                        ),
                        elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(12.dp)
                        ) {
                            Box(modifier = Modifier.size(64.dp)) {
                                AsyncImage(
                                    model = proposal.thumbnails.firstOrNull() ?: proposal.images.firstOrNull(),
                                    contentDescription = proposal.name,
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                if ((unreadMessagesCount[proposal.proposalId] ?: 0) > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(2.dp)
                                    ) {
                                        Text(
                                            text = unreadMessagesCount[proposal.proposalId].toString(),
                                            color = MaterialTheme.colorScheme.onError,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = proposal.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Group chat available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ChatBubbleCard(
    message: ChatMessage,
    currentUserId: String,
    index: Int
) {
    val isCurrentUser = message.senderId == currentUserId

    val backgroundColor = if (isCurrentUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        if (index % 2 == 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer
    }

    Row(
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        ElevatedCard(
            shape = RoundedCornerShape(12.dp),
            colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                containerColor = backgroundColor,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    currentUserId: String
) {
    var lastSenderId by remember { mutableStateOf<String?>(null) }
    var lastMessageWasFile by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(messages.size) { index ->
            val message = messages[index]
            val isFileMessage = message.isFileMessage

            val showFileHeader = (lastSenderId != message.senderId) || (lastMessageWasFile != isFileMessage && isFileMessage)

            if (showFileHeader && isFileMessage) {
                Text(
                    text = "${message.senderName} sent file(s)",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ChatBubbleCard(message = message, currentUserId = currentUserId, index = index)

            lastSenderId = message.senderId
            lastMessageWasFile = isFileMessage
        }
    }
}

data class ChatMessage(
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isFileMessage: Boolean = false
)
