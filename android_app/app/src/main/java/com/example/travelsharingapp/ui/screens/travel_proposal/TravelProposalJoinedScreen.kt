package com.example.travelsharingapp.ui.screens.travel_proposal

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.travelsharingapp.R
import com.example.travelsharingapp.data.model.ApplicationStatus
import com.example.travelsharingapp.data.model.ProposalStatus
import com.example.travelsharingapp.data.model.TravelApplication
import com.example.travelsharingapp.data.model.TravelProposal
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_application.TravelApplicationViewModel
import com.example.travelsharingapp.ui.theme.customColorsPalette
import com.example.travelsharingapp.utils.shouldUseTabletLayout
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

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
    onNavigateToChat: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = shouldUseTabletLayout()

    val numColumns = remember(configuration.orientation) {
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (isTablet) 3 else 2
        } else {
            if (isTablet) 2 else 1
        }
    }

    val tabs = listOf(
        TabItem("Upcoming", Icons.Filled.Upcoming),
        TabItem("Concluded", Icons.Filled.History)
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        travelApplicationViewModel.startListeningApplicationsForUser(userId)
    }

    val applications by travelApplicationViewModel.userSpecificApplications.collectAsState()
    val validApplications = applications.filter {
        it.statusEnum == ApplicationStatus.Pending || it.statusEnum == ApplicationStatus.Accepted
    }

    val allProposals by travelProposalViewModel.allProposals.collectAsState()
    val joinedProposals = validApplications.mapNotNull { app ->
        allProposals.find { it.proposalId == app.proposalId }
    }

    val today = LocalDate.now()
    val futureProposals = joinedProposals.filter {
        it.startDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
            ?.isAfter(today) == true
    }

    val pastProposals = joinedProposals.filter {
        val endDate = it.endDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
        endDate != null && (endDate.isBefore(today) || endDate.isEqual(today))
    }

    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "Joined Proposals",
            navigationIcon = { /* nothing */ },
            actions = {
                IconButton(onClick = { onNavigateToChat() }) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat")
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
            val isUpcomingList = page == 0

            if (proposalsToDisplay.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emptyMessage)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(numColumns),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(
                        count = proposalsToDisplay.size,
                        key = { index -> proposalsToDisplay[index].proposalId },
                        itemContent = { index ->
                            val travelProposal = proposalsToDisplay[index]
                            JoinedTravelProposalCard(
                                modifier = Modifier.fillMaxWidth(),
                                proposal = travelProposal,
                                isUpcoming = isUpcomingList,
                                applications = validApplications,
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
    isUpcoming: Boolean,
    applications: List<TravelApplication>,
    onClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    val travelStatus = determineTravelDisplayStatus(
        proposal = proposal,
        isContextUpcoming = isUpcoming
    )

    Card(
        modifier = modifier.fillMaxWidth().height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier
                .width(140.dp)
                .fillMaxHeight()
            ) {
                val imageList = if (proposal.thumbnails.isNotEmpty()) {
                    proposal.thumbnails
                } else {
                    proposal.images
                }

                if (imageList.isNotEmpty()) {
                    val banners = imageList.mapIndexed { index, item ->
                        BannerModel(
                            imageUrl = item,
                            contentDescription = "Image ${index + 1}"
                        )
                    }

                    BannerCarouselWidget(
                        banners = banners,
                        modifier = Modifier.fillMaxSize(),
                        pageSpacing = 0.dp,
                        contentPadding = PaddingValues(0.dp)
                    )
                } else {
                    AsyncImage(
                        model = R.drawable.placeholder_error,
                        contentDescription = "Destination image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                val application = applications.find { it.proposalId == proposal.proposalId }
                val applicationStatus = application?.statusEnum

                if (applicationStatus != null) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            applicationStatus.name,
                            color = when (applicationStatus) {
                                ApplicationStatus.Accepted -> MaterialTheme.customColorsPalette.extraColorGreen
                                ApplicationStatus.Pending -> MaterialTheme.customColorsPalette.extraColorOrange
                                ApplicationStatus.Rejected -> MaterialTheme.customColorsPalette.extraColorRed
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = proposal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Event,
                        contentDescription = "Date",
                        modifier = Modifier.height(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val dateToDisplay = if (travelStatus is TravelDisplayStatus.Upcoming || travelStatus is TravelDisplayStatus.Ongoing) {
                        proposal.startDate.toLocalDateOrNull()
                    } else {
                        proposal.endDate.toLocalDateOrNull() ?: proposal.startDate.toLocalDateOrNull()
                    }
                    val formattedDate = dateToDisplay
                        ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "Date not set"
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = "Participants",
                        modifier = Modifier.height(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${proposal.participantsCount} / ${proposal.maxParticipants} joined",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { /* */ },
                        label = { Text(travelStatus.displayText, style = MaterialTheme.typography.labelMedium) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = travelStatus.getContainerColor(),
                            labelColor = travelStatus.getLabelColor()
                        ),
                        border = null
                    )


                    if (!isUpcoming && proposal.statusEnum == ProposalStatus.Concluded) {
                        Button(
                            onClick = onReviewClick,
                            modifier = Modifier,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Review",
                                    tint = Color(0xFFFFC107)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Review", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(0.dp))
                    }
                }
            }
        }
    }
}

sealed class TravelDisplayStatus(val displayText: String) {
    @Composable
    abstract fun getContainerColor(): Color
    @Composable
    abstract fun getLabelColor(): Color

    object Concluded : TravelDisplayStatus("Concluded") {
        @Composable override fun getContainerColor() = MaterialTheme.colorScheme.primaryContainer
        @Composable override fun getLabelColor() = MaterialTheme.colorScheme.onPrimaryContainer
    }

    object Ongoing : TravelDisplayStatus("Ongoing") {
        @Composable override fun getContainerColor() = MaterialTheme.colorScheme.tertiaryContainer
        @Composable override fun getLabelColor() = MaterialTheme.colorScheme.onTertiaryContainer
    }

    data class Upcoming(val countdown: String) : TravelDisplayStatus(countdown) {
        @Composable override fun getContainerColor() = MaterialTheme.colorScheme.tertiaryContainer
        @Composable override fun getLabelColor() = MaterialTheme.colorScheme.onTertiaryContainer
    }

    data class Unknown(val reason: String = "Status N/A") : TravelDisplayStatus(reason) {
        @Composable override fun getContainerColor() = MaterialTheme.colorScheme.secondaryContainer
        @Composable override fun getLabelColor() = MaterialTheme.colorScheme.onSecondaryContainer
    }
}

fun Timestamp?.toLocalDateOrNull(): LocalDate? {
    return this?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
}

@Composable
fun determineTravelDisplayStatus(
    proposal: TravelProposal,
    isContextUpcoming: Boolean,
    today: LocalDate = LocalDate.now()
): TravelDisplayStatus {
    val startDate = proposal.startDate.toLocalDateOrNull()
    val endDate = proposal.endDate.toLocalDateOrNull()

    if (proposal.status == "Concluded") {
        return TravelDisplayStatus.Concluded
    }

    if (startDate != null && !startDate.isAfter(today)) {
        if (endDate == null || !endDate.isBefore(today)) {
            return TravelDisplayStatus.Ongoing
        }
        if (endDate.isBefore(today)) {
            return TravelDisplayStatus.Concluded
        }
    }

    if (startDate != null && startDate.isAfter(today)) {
        val daysUntil = ChronoUnit.DAYS.between(today, startDate)
        val weeksUntil = ChronoUnit.WEEKS.between(today, startDate)
        val monthsUntil = ChronoUnit.MONTHS.between(today, startDate)

        val countdownText = when {
            monthsUntil >= 1 -> "In $monthsUntil months"
            weeksUntil >= 1 -> "In $weeksUntil weeks"
            daysUntil > 1 -> "In $daysUntil days"
            daysUntil == 1L -> "Tomorrow"
            else -> "Upcoming"
        }
        return TravelDisplayStatus.Upcoming(countdownText)
    }

    if (!isContextUpcoming) {
        return TravelDisplayStatus.Concluded
    }

    val unknownReason = when {
        startDate == null && endDate == null -> "Dates N/A"
        startDate == null -> "Start Date N/A"
        else -> "Status Unclear"
    }
    return TravelDisplayStatus.Unknown(unknownReason)
}