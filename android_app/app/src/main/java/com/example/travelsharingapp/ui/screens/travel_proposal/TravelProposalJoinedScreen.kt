package com.example.travelsharingapp.ui.screens.travel_proposal

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

import com.example.travelsharingapp.R
import com.example.travelsharingapp.data.model.ApplicationStatus
import com.example.travelsharingapp.data.model.TravelProposal
import com.example.travelsharingapp.ui.screens.main.AppRoutes
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_application.TravelApplicationViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class TabItem(val title: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelProposalJoinedScreen(
    modifier: Modifier,
    userId: String,
    travelProposalViewModel: TravelProposalViewModel,
    travelApplicationViewModel: TravelApplicationViewModel,
    topBarViewModel: TopBarViewModel,
    onNavigateToReviewPage: (String) -> Unit,
    onNavigateToProposalInfo: (String) -> Unit,
    navController: NavController
) {
    val tabs = listOf(
        TabItem("Upcoming", Icons.Filled.Upcoming),
        TabItem("Concluded", Icons.Filled.History)
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        travelApplicationViewModel.loadApplicationsForUser(userId)
        travelProposalViewModel.loadAllProposals()
    }

    val joinedApplications by travelApplicationViewModel.userSpecificApplications.collectAsState()
    val validApplications = joinedApplications.filter {
        it.statusEnum == ApplicationStatus.Pending || it.statusEnum == ApplicationStatus.Accepted
    }

    val allProposals by travelProposalViewModel.allProposals.collectAsState()
    val joinedProposals = validApplications.mapNotNull { app ->
        allProposals.find { it.proposalId == app.proposalId }
    }

    val today = LocalDate.now()
    val futureProposals = joinedProposals.filter {
        it.startDate?.toDate()?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate()
            ?.isAfter(today) == true
    }

    val pastProposals = joinedProposals.filter {
        val endDate = it.endDate?.toDate()?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate()
        endDate != null && (endDate.isBefore(today) || endDate.isEqual(today))
    }

    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "Joined Proposals",
            navigationIcon = { /* nothing */ },
            actions = {
                IconButton(onClick = { navController.navigate(AppRoutes.CHAT_LIST) }) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat")
                }
            }
        )
    }

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
            val proposalsToDisplay = if (page == 0) futureProposals else pastProposals
            val emptyMessage = if (page == 0) "No Upcoming trips found." else "No Concluded trips found."

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
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        count = proposalsToDisplay.size,
                        key = { index -> proposalsToDisplay[index].proposalId },
                        itemContent = { index ->
                            val travelProposal = proposalsToDisplay[index]
                            JoinedTravelProposalCard(
                                modifier = Modifier.fillMaxWidth(),
                                proposal = travelProposal,
                                onClick = { onNavigateToProposalInfo(travelProposal.proposalId) },
                                onReviewClick = { onNavigateToReviewPage(travelProposal.proposalId) }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun JoinedTravelProposalCard(
    modifier: Modifier,
    proposal: TravelProposal,
    onClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (proposal.images.isNotEmpty()) {
                val banners = proposal.images.mapIndexed { index, item ->
                    BannerModel(
                        imageUrl = item,
                        contentDescription = "Image ${index + 1}"
                    )
                }

                BannerCarouselWidget(
                    banners = banners,
                    modifier = Modifier
                        .width(140.dp)
                        .fillMaxHeight(),
                    pageSpacing = 0.dp,
                    contentPadding = PaddingValues(0.dp)
                )
            } else {
                AsyncImage(
                    model = R.drawable.placeholder_error,
                    contentDescription = "Destination image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(140.dp)
                        .fillMaxHeight()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = proposal.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                val formattedDate = proposal.startDate
                    ?.toDate()
                    ?.toInstant()
                    ?.atZone(java.time.ZoneId.systemDefault())
                    ?.toLocalDate()
                    ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) ?: "No date"

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "${proposal.participantsCount}/${proposal.maxParticipants} joined",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                if (proposal.status == "Concluded") {
                    Button(
                        onClick = onReviewClick,
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reviews")
                        }
                    }
                }
            }
        }
    }
}