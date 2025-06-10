package com.example.travelsharingapp.ui.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.travelsharingapp.data.model.ApplicationStatus
import com.example.travelsharingapp.data.model.ProposalStatus
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_application.TravelApplicationViewModel
import com.example.travelsharingapp.ui.screens.travel_proposal.JoinedTravelProposalCard
import com.example.travelsharingapp.ui.screens.travel_proposal.TabItem
import com.example.travelsharingapp.ui.screens.travel_proposal.TravelProposalViewModel
import com.example.travelsharingapp.ui.screens.travel_proposal.determineTravelDisplayStatus
import kotlinx.coroutines.launch

@Composable
fun ChatListScreen(
    modifier: Modifier,
    userId: String,
    travelProposalViewModel: TravelProposalViewModel,
    travelApplicationViewModel: TravelApplicationViewModel,
    onNavigateToChat: (String) -> Unit,
    onNavigateBack: () -> Unit,
    topBarViewModel: TopBarViewModel,
    chatViewModel: ChatViewModel
) {
    val allProposals by travelProposalViewModel.allProposals.collectAsState()
    val ownedProposals by travelProposalViewModel.ownedProposals.collectAsState()
    val applications by travelApplicationViewModel.userSpecificApplications.collectAsState()
    val unreadCounts by chatViewModel.unreadMessagesCount.collectAsState()

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

    val tabs = listOf(
        TabItem("Own travel", Icons.Default.AccountCircle),
        TabItem("Applied to", Icons.Default.PersonAdd)
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SecondaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            tabs.forEachIndexed { index, tabItem ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(tabItem.title) },
                    icon = {
                        Icon(
                            imageVector = tabItem.icon,
                            contentDescription = tabItem.title
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.onSurface,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->

            val proposalsToDisplay = if (page == 0) ownedProposals else joinedProposals
            val emptyMessage = if (page == 0) "No owned trips found." else "No trips applied to found."

            if (proposalsToDisplay.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emptyMessage)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(
                        count = proposalsToDisplay.size,
                        key = { index -> proposalsToDisplay[index].proposalId },
                        contentType = { "TravelProposalsCard" },
                        itemContent = { index ->
                            val proposal = proposalsToDisplay[index]
                            val travelStatus = determineTravelDisplayStatus(
                                proposal = proposal,
                                isContextUpcoming = proposal.statusEnum != ProposalStatus.Concluded
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clickable { onNavigateToChat(proposal.proposalId) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.padding(end = 16.dp)
                                ) {
                                    Box(modifier = Modifier
                                        .width(140.dp)
                                        .fillMaxHeight()
                                    ) {
                                        AsyncImage(
                                            model = proposal.thumbnails.firstOrNull() ?: proposal.images.firstOrNull(),
                                            contentDescription = proposal.name,
                                            modifier = Modifier
                                                .height(120.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                        val count = unreadCounts[proposal.proposalId] ?: 0
                                        if (count > 0) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(2.dp)
                                            ) {
                                                Text(
                                                    text = count.toString(),
                                                    color = MaterialTheme.colorScheme.onError,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.width(16.dp))

                                    Column (
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 16.dp)
                                    ) {
                                        Text(
                                            text = proposal.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.weight(1f))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            AssistChip(
                                                onClick = { /* */ },
                                                label = { Text(travelStatus.displayText, style = MaterialTheme.typography.labelSmall) },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = travelStatus.getContainerColor(),
                                                    labelColor = travelStatus.getLabelColor()
                                                ),
                                                modifier = Modifier.height(24.dp),
                                                border = null
                                            )
                                            Spacer(modifier = Modifier.weight(1f))

                                            Text(
                                                text = "${proposal.participantsCount}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Filled.Group,
                                                contentDescription = "Participants",
                                                modifier = Modifier.height(16.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
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
