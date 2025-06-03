package com.example.travelsharingapp.ui.screens.travel_proposal

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.example.travelsharingapp.R
import com.example.travelsharingapp.data.model.ApplicationStatus
import com.example.travelsharingapp.data.model.ItineraryStop
import com.example.travelsharingapp.data.model.ProposalStatus
import com.example.travelsharingapp.data.model.TravelApplication
import com.example.travelsharingapp.data.model.TravelProposal
import com.example.travelsharingapp.data.model.Typology
import com.example.travelsharingapp.data.model.UserProfile
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_application.ParticipantsPreviewRow
import com.example.travelsharingapp.ui.screens.travel_application.ProfileAvatar
import com.example.travelsharingapp.ui.screens.travel_application.TravelApplicationViewModel
import com.example.travelsharingapp.ui.screens.travel_review.TravelReviewViewModel
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileViewModel
import com.example.travelsharingapp.utils.shouldUseTabletLayout
import com.example.travelsharingapp.utils.toTypologyOrNull
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelProposalInfoScreen(
    modifier: Modifier,
    proposalId: String,
    userId: String,
    userViewModel: UserProfileViewModel,
    applicationViewModel: TravelApplicationViewModel,
    proposalViewModel: TravelProposalViewModel,
    reviewViewModel: TravelReviewViewModel,
    topBarViewModel: TopBarViewModel,
    onNavigateToTravelProposalApply: () -> Unit,
    onNavigateToTravelProposalEdit: () -> Unit,
    onNavigateToTravelProposalDuplicate: () -> Unit,
    onNavigateToCompanionsReview: () -> Unit,
    onNavigateToManageApplications: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    onBack: () -> Unit,
) {
    val proposalState = remember { mutableStateOf<TravelProposal?>(null) }

    LaunchedEffect(proposalId) {
        proposalViewModel.getProposalById(proposalId) {
            proposalState.value = it
        }
    }

    if (proposalState.value == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading proposal...")
        }
        return
    }

    val proposal = proposalState.value!!
    val isOwner = proposal.organizerId == userId
    val showDeleteDialog = remember { mutableStateOf(false) }
    val showWithdrawDialog = remember { mutableStateOf(false) }

    val organizer = userViewModel.getUserProfileById(proposal.organizerId)
    val applications by applicationViewModel.applications.collectAsState()
    val acceptedParticipantsProfiles = remember { mutableStateListOf<UserProfile>() }

    LaunchedEffect(key1 = proposalId, key2 = applications) {
        val allAcceptedIds = applicationViewModel.getAcceptedParticipants(proposalId, userId)
        val profiles = allAcceptedIds.mapNotNull { acceptedUserId ->
            userViewModel.getOrFetchUserProfileById(acceptedUserId)
        }
        acceptedParticipantsProfiles.clear()

        if (!isOwner) {
            acceptedParticipantsProfiles.add(userViewModel.getOrFetchUserProfileById(proposal.organizerId)!!)
        }

        acceptedParticipantsProfiles.addAll(profiles)
    }

    val scrollState = rememberScrollState()

    val homeCameraPosition = remember(proposal.itinerary) {
        val firstStopPosition = proposal.itinerary.firstOrNull()?.position
        val initialLatLng = firstStopPosition?.let { LatLng(it.latitude, it.longitude) }
            ?: LatLng(0.0, 0.0)
        val initialZoom = if (firstStopPosition != null) 10f else 2f
        CameraPosition.fromLatLngZoom(initialLatLng, initialZoom)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = homeCameraPosition
    }

    LaunchedEffect(proposalId) {
        reviewViewModel.loadReviewsForProposal(proposalId)
    }

    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = proposal.name,
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (proposal.statusEnum != ProposalStatus.Concluded) {
                    if (isOwner) {
                        IconButton(onClick = onNavigateToTravelProposalEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Proposal")
                        }
                    } else {
                        IconButton(onClick = onNavigateToTravelProposalDuplicate) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate Proposal")
                        }
                    }
                }
            }
        )
    }

    val currentUserApplication = applications.find {
        it.proposalId == proposal.proposalId && it.userId == userId
    }

    val showParticipantsReviewRow = (proposal.statusEnum == ProposalStatus.Concluded &&
        (isOwner || currentUserApplication?.statusEnum == ApplicationStatus.Accepted) &&
        acceptedParticipantsProfiles.isNotEmpty()
    )

    val configuration = LocalConfiguration.current
    val isTablet = shouldUseTabletLayout()
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTabletInLandscape = isTablet && isLandscape

    if (isTabletInLandscape) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GoogleMapCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    proposal = proposal,
                    cameraPositionState = cameraPositionState,
                    homeCameraPosition = homeCameraPosition
                )
            }
            Column(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TravelHeaderSection(
                    proposal = proposal,
                    organizer = organizer,
                    onOrganizerClick = { userId ->
                        onNavigateToUserProfile(userId)
                    }
                )

                if (showParticipantsReviewRow) {
                    ParticipantsPreviewRow(
                        participants = acceptedParticipantsProfiles,
                        onClick = onNavigateToCompanionsReview
                    )
                }

                TravelDescriptionCard(proposal)

                Text(
                    text = "ITINERARY STOPS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (proposal.itinerary.isEmpty()) {
                    Text("No stops added in the itinerary.")
                } else {
                    proposal.itinerary.forEach { itineraryStop ->
                        ItineraryStopListItemCard(
                            itineraryStop = itineraryStop,
                            onClick = {
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                    itineraryStop.position?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(0.0, 0.0),
                                    15f
                                )
                            }
                        )
                    }
                }

                SuggestedActivitiesSection(activities = proposal.suggestedActivities)
                HorizontalDivider()

                ProposalStatusCard(
                    proposal = proposal,
                    isOwner = isOwner,
                    userId = userId,
                    applications = applications,
                    showDeleteDialog = showDeleteDialog,
                    showWithdrawDialog = showWithdrawDialog,
                    onNavigateToManageApplications = onNavigateToManageApplications,
                    onNavigateToTravelProposalApply = onNavigateToTravelProposalApply,
                    applicationViewModel = applicationViewModel,
                )
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState, enabled = !cameraPositionState.isMoving)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TravelHeaderSection(
                proposal = proposal,
                organizer = organizer,
                onOrganizerClick = { userId ->
                    onNavigateToUserProfile(userId)
                }
            )

            if (showParticipantsReviewRow) {
                ParticipantsPreviewRow(
                    participants = acceptedParticipantsProfiles,
                    onClick = onNavigateToCompanionsReview,
                )
            }

            TravelDescriptionCard(proposal)

            GoogleMapCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                proposal = proposal,
                cameraPositionState = cameraPositionState,
                homeCameraPosition = homeCameraPosition
            )

            Text(
                text = "ITINERARY STOPS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (proposal.itinerary.isEmpty()) {
                Text("No stops added in the itinerary.")
            } else {
                proposal.itinerary.forEach { itineraryStop ->
                    ItineraryStopListItemCard(
                        itineraryStop = itineraryStop,
                        onClick = {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                itineraryStop.position?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(0.0, 0.0),
                                15f
                            )
                        }
                    )
                }
            }

            SuggestedActivitiesSection(activities = proposal.suggestedActivities)
            HorizontalDivider()

            ProposalStatusCard(
                proposal = proposal,
                isOwner = isOwner,
                userId = userId,
                applications = applications,
                showDeleteDialog = showDeleteDialog,
                showWithdrawDialog = showWithdrawDialog,
                onNavigateToManageApplications = onNavigateToManageApplications,
                onNavigateToTravelProposalApply = onNavigateToTravelProposalApply,
                applicationViewModel = applicationViewModel,
            )
        }
    }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            confirmButton = { TextButton(onClick = { proposalViewModel.deleteProposal(proposal.proposalId); showDeleteDialog.value = false; onBack() }) { Text("Confirm") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog.value = false }) { Text("Back") } },
            title = { Text("Are you sure?") },
            text = { Text("Do you want to delete this proposal?") }
        )
    }

    if (showWithdrawDialog.value) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog.value = false },
            confirmButton = { TextButton(onClick = { showWithdrawDialog.value = false; applicationViewModel.withdrawApplication(userId, proposal.proposalId) }) { Text("Confirm") } },
            dismissButton = { TextButton(onClick = { showWithdrawDialog.value = false }) { Text("Back") } },
            title = { Text("Confirm withdrawn") },
            text = { Text("Do you want to withdraw from this trip?") }
        )
    }
}

data class BannerModel(
    val imageUrl: String,
    val contentDescription: String
)

@Composable
fun BannerWidget(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    imageHeight: Dp = 200.dp
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        placeholder = painterResource(id = R.drawable.placeholder_travel),
        error = painterResource(id = R.drawable.placeholder_error),
        modifier = modifier
            .fillMaxWidth()
            .height(imageHeight)
            .clip(RoundedCornerShape(8.dp))
    )
}

@Composable
fun BannerCarouselWidget(
    banners: List<BannerModel>,
    modifier: Modifier = Modifier,
    pageSpacing: Dp,
    contentPadding: PaddingValues
) {
    if (banners.isEmpty()) {
        Box(modifier = modifier)
        return
    }

    val pagerState = rememberPagerState(pageCount = { banners.size })
    val context = LocalContext.current
    val imageLoader = context.imageLoader

    LaunchedEffect(pagerState.currentPage, banners) {
        val currentPage = pagerState.currentPage
        val prefetchIndices = setOf(currentPage + 1, currentPage + 2)

        prefetchIndices.forEach { index ->
            if (index < banners.size) {
                val url = banners[index].imageUrl
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .build()
                imageLoader.enqueue(request)
            }
        }
    }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = contentPadding,
            pageSpacing = pageSpacing,
            verticalAlignment = Alignment.Top,
        ) { page ->
            BannerWidget(
                imageUrl = banners[page].imageUrl,
                contentDescription = banners[page].contentDescription
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

@Composable
fun TravelHeaderSection(
    proposal: TravelProposal,
    organizer: UserProfile?,
    onOrganizerClick: (userId: String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        val banners = proposal.images.mapIndexed { index, item ->
            BannerModel(
                imageUrl = item,
                contentDescription = "Banner ${index + 1}"
            )
        }

        BannerCarouselWidget(
            banners = banners,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Travel dates",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    Text("${proposal.startDate?.toDate()?.let { formatter.format(it) }} - ${proposal.endDate?.toDate()?.let { formatter.format(it) }}")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PriceChange,
                        contentDescription = "Price range",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("€${proposal.minPrice} - €${proposal.maxPrice}")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Group,
                        contentDescription = "Participants",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${proposal.participantsCount} / ${proposal.maxParticipants} participants")
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            BoxWithConstraints {
                val showText = this.maxWidth > 100.dp
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFF9800))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                getIconForTypology(proposal.typology.toTypologyOrNull() ?: Typology.Adventure),
                                contentDescription = "${proposal.typology} type",
                                tint = Color.White
                            )
                            if (showText) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(proposal.typology, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    organizer?.let { org ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onOrganizerClick(org.userId) }
                                .padding(vertical = 4.dp, horizontal = if (showText) 8.dp else 4.dp)
                        ) {
                            ProfileAvatar(
                                imageSize = 36.dp,
                                user = org,
                                onClick = { onOrganizerClick(org.userId) }
                            )
                            if (showText) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${org.firstName} ${org.lastName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TravelDescriptionCard(proposal: TravelProposal) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(modifier = Modifier.width(8.dp))
            Text(proposal.description)
        }
    }
}

@Composable
fun GoogleMapCard(
    modifier: Modifier = Modifier,
    proposal: TravelProposal,
    cameraPositionState: CameraPositionState,
    homeCameraPosition: CameraPosition
) {
    val scope = rememberCoroutineScope()

    Box {
        Card(
            modifier = modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapType = MapType.NORMAL),
                uiSettings = MapUiSettings(zoomControlsEnabled = true, mapToolbarEnabled = true)
            ) {
                proposal.itinerary.forEachIndexed { index, item ->
                    MarkerComposable(
                        state = rememberMarkerState(
                            position = item.position?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(0.0, 0.0)
                        ),
                        title = item.place
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                modifier = Modifier
                                    .padding(bottom = 12.dp)
                                    .size(12.dp),
                                color = Color.Red
                            ) { }
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.matchParentSize()
                            )
                            Text(
                                text = (index + 1).toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = {
                scope.launch {
                    cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(homeCameraPosition))
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Reset map view",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryStopListItemCard(
    itineraryStop: ItineraryStop,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                itineraryStop.place,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (itineraryStop.description.isNotBlank()) {
                Text(
                    itineraryStop.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            AssistChip(
                onClick = { /* */ },
                label = { Text(if (itineraryStop.isGroup) "Group Activity" else "Free Time") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (itineraryStop.isGroup) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = if (itineraryStop.isGroup) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}

@Composable
fun SuggestedActivitiesSection(activities: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "SUGGESTED ACTIVITIES",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        if (activities.isEmpty()) {
            Text("No activities listed.")
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(activities.size) { index ->
                    AssistChip(
                        onClick = {},
                        label = { Text(activities[index]) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ProposalStatusCard(
    proposal: TravelProposal,
    isOwner: Boolean,
    userId: String,
    applications: List<TravelApplication>,
    showDeleteDialog: MutableState<Boolean>,
    showWithdrawDialog: MutableState<Boolean>,
    onNavigateToManageApplications: () -> Unit,
    onNavigateToTravelProposalApply: () -> Unit,
    applicationViewModel: TravelApplicationViewModel,
) {
    if (proposal.statusEnum != ProposalStatus.Concluded) {
        if (isOwner) {
            Button(
                onClick = onNavigateToManageApplications,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage Applications")
            }
            Button(
                onClick = { showDeleteDialog.value = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Proposal", color = MaterialTheme.colorScheme.error)
            }
        } else {
            if (!applicationViewModel.isUserApplied(userId, proposal.proposalId)) {

                if (proposal.statusEnum == ProposalStatus.Full) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Text("This trip is full.", color = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {
                    Button(
                        onClick = onNavigateToTravelProposalApply,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Join the trip")
                    }
                }

            } else {
                val application = applications.find { it.proposalId == proposal.proposalId && it.userId == userId }
                if (application != null) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text("Application Status: ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    text = application.statusEnum?.toString() ?: "Unknown",
                                    color = when (application.status) {
                                        ApplicationStatus.Accepted.toString() ->  Color(0xFF4CAF50)
                                        ApplicationStatus.Pending.toString() -> Color(0xFFFF9800)
                                        ApplicationStatus.Rejected.toString() -> Color(0xFFF44336)
                                        else -> Color.Gray
                                    },
                                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            if (application.statusEnum == ApplicationStatus.Accepted || application.statusEnum == ApplicationStatus.Pending) {
                                Button(
                                    onClick = { showWithdrawDialog.value = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Withdraw Application")
                                }
                            } else if (application.statusEnum == ApplicationStatus.Rejected) {
                                Text("Your application was not accepted.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                } else {
                    Text("Error: Your application status is not available.", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
            }
        }
    }
}