package com.example.travelsharingapp.ui.screens.travel_application

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.travelsharingapp.data.model.ApplicationStatus
import com.example.travelsharingapp.data.model.TravelApplication
import com.example.travelsharingapp.data.model.TravelProposal
import com.example.travelsharingapp.data.model.UserProfile
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_proposal.TravelProposalViewModel
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileViewModel
import com.example.travelsharingapp.utils.toApplicationStatusOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationManageAllScreen(
    modifier: Modifier,
    proposalId: String,
    userProfileViewModel: UserProfileViewModel,
    proposalViewModel : TravelProposalViewModel,
    applicationViewModel: TravelApplicationViewModel,
    topBarViewModel: TopBarViewModel,
    onNavigateToUserProfileInfo: (String) -> Unit,
    onBack: () -> Unit
) {
    val observedProposal by proposalViewModel.selectedProposal.collectAsState()
    val applicationsForProposal by applicationViewModel.proposalSpecificApplications.collectAsState()

    val refreshTrigger = remember { mutableIntStateOf(0) }
    val allStatuses = ApplicationStatus.entries.map { it.name }
    val statusFilter = remember { allStatuses.toMutableStateList() }

    LaunchedEffect(proposalId, refreshTrigger.intValue) {
        proposalViewModel.setDetailProposalId(proposalId)
        applicationViewModel.startListeningApplicationsForProposal(proposalId)
    }

    if (observedProposal == null || applicationsForProposal.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Text("Loading proposal data...")
        }
        return
    }

    val proposal = observedProposal!!
    val currentApplications = applicationsForProposal

    val filteredApplications = currentApplications.filter { application ->
        statusFilter.isEmpty() || statusFilter.contains(application.status)
    }

    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "Manage Applications",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = { /* No specific actions for this screen*/ }
        )
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        ApplicationStatusFilterChips(
            selectedStatuses = statusFilter
        )

        if (currentApplications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No applications yet.")
            }
        } else if (filteredApplications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No applications matching the filters.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    count = filteredApplications.size,
                    key = { index -> filteredApplications[index].applicationId },
                    contentType = { "UserApplicationCard" },
                    itemContent = { index ->
                        val application = filteredApplications[index]
                        val user by userProfileViewModel.observeUserProfileById(application.userId).collectAsState()
                        user?.let {
                            ApplicationCard(
                                application = application,
                                user = user!!,
                                proposal = proposal,
                                onAccept = { app ->
                                    applicationViewModel.acceptApplication(app)
                                    refreshTrigger.intValue++
                                },
                                onReject = { app ->
                                    applicationViewModel.rejectApplication(app)
                                    refreshTrigger.intValue++
                                },
                                onCardClick = {
                                    onNavigateToUserProfileInfo(user!!.userId)
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ApplicationStatusFilterChips(
    selectedStatuses: MutableList<String>
) {
    val allStatuses = ApplicationStatus.entries.map { it.name }

    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(count = allStatuses.size) { index ->
            val status = allStatuses[index]
            FilterChip(
                selected = selectedStatuses.contains(status),
                onClick = {
                    if (selectedStatuses.contains(status)) {
                        selectedStatuses.remove(status)
                    } else {
                        selectedStatuses.add(status)
                    }
                },
                label = { Text(status) },
                leadingIcon = if (selectedStatuses.contains(status)) {
                    {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
fun ApplicationCard(
    application: TravelApplication,
    user: UserProfile,
    proposal: TravelProposal,
    onAccept: (TravelApplication) -> Unit,
    onReject: (TravelApplication) -> Unit,
    onCardClick: () -> Unit
) {
    val canAcceptMore = remember(proposal.participantsCount) { proposal.participantsCount  < proposal.maxParticipants }
    val username = (user.firstName + " " + user.lastName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        onClick = onCardClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileAvatar(
                        imageSize = 40.dp,
                        user = user
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                val statusEnum = application.statusEnum
                Box(
                    modifier = Modifier
                        .background(
                            color = when (statusEnum) {
                                ApplicationStatus.Accepted -> Color(0xFF2E7D32)
                                ApplicationStatus.Rejected -> Color(0xFFC62828)
                                ApplicationStatus.Pending -> Color(0xFF757575)
                                else -> Color.Gray
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusEnum?.name ?: "",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                thickness = 2.dp, color = MaterialTheme.colorScheme.surfaceVariant
            )

            Text(
                text = "Motivation",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = application.motivation,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Guests :",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = if (application.accompanyingGuests.isNotEmpty()) {
                        application.accompanyingGuests.joinToString(", ") { guest -> guest.name }
                    } else {
                        "none"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (application.status.toApplicationStatusOrNull() == ApplicationStatus.Pending) {
                if (!canAcceptMore) {
                    Text(
                        text = "Maximum number of participants reached.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onReject(application) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                MaterialTheme.colorScheme.errorContainer
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject")
                        }

                        Button(
                            onClick = { onAccept(application) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Accept")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileAvatar(
    imageSize: Dp,
    user: UserProfile?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {

    if (user == null) return

    val imageModel: Any? = user.profileImageThumbnail?.takeIf { it.isNotBlank() }
        ?: user.profileImage?.takeIf { it.isNotBlank() }
    val showUserInitials = imageModel == null
    val userInitials = "${user.firstName.first().uppercaseChar()}${user.lastName.first().uppercaseChar()}"

    Box(
        modifier = modifier
            .size(imageSize)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(if (showUserInitials) MaterialTheme.colorScheme.secondary else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (!showUserInitials) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = "Profile Image ${user.firstName} ${user.lastName}",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    text = userInitials,
                    color = Color.White,
                    fontSize = (imageSize.value / 2.8).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ParticipantsPreviewRow(
    participants: List<UserProfile>,
    maxVisible: Int = 3,
    avatarSize: Dp = 40.dp,
    overlapFactor: Float = 0.4f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val displayParticipants = participants.take(maxVisible)
    val remainingCount = participants.size - displayParticipants.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.height(avatarSize)) {
                displayParticipants.forEachIndexed { index, participant ->
                    val offsetX = (avatarSize.value * (1 - overlapFactor) * index).dp

                    ProfileAvatar(
                        imageSize = avatarSize,
                        user = participant,
                        modifier = Modifier
                            .offset(x = offsetX)
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            )
                    )
                }

                if (remainingCount > 0) {
                    val offsetX = (avatarSize.value * (1 - overlapFactor) * displayParticipants.size).dp
                    Box(
                        modifier = Modifier
                            .offset(x = offsetX)
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+$remainingCount",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = (avatarSize.value / 2.8).sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(100.dp))

            Text(
                text = "Review Participants",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}