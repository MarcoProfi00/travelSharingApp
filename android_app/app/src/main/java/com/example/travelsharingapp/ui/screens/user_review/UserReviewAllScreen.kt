package com.example.travelsharingapp.ui.screens.user_review

import android.content.res.Configuration
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelsharingapp.data.model.ApplicationStatus
import com.example.travelsharingapp.data.model.UserProfile
import com.example.travelsharingapp.data.model.UserReview
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_application.ProfileAvatar
import com.example.travelsharingapp.ui.screens.travel_application.TravelApplicationViewModel
import com.example.travelsharingapp.ui.screens.travel_proposal.TravelProposalViewModel
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileViewModel
import com.example.travelsharingapp.utils.shouldUseTabletLayout
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReviewAllScreen(
    modifier: Modifier,
    userId: String,
    proposalId: String,
    travelProposalViewModel: TravelProposalViewModel,
    applicationViewModel: TravelApplicationViewModel,
    userProfileViewModel: UserProfileViewModel,
    userReviewViewModel: UserReviewViewModel,
    topBarViewModel: TopBarViewModel,
    onNavigateToUserProfileInfo: (String) -> Unit,
    onBack: () -> Unit
) {
    val observedUser by userProfileViewModel.selectedUserProfile.collectAsState()
    val observedProposal by travelProposalViewModel.selectedProposal.collectAsState()
    val reviews by userReviewViewModel.proposalReviews.collectAsState()
    val applications by applicationViewModel.proposalSpecificApplications.collectAsState()

    var acceptedParticipantIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingParticipantIds by remember { mutableStateOf(true) }

    LaunchedEffect(proposalId) {
        if (proposalId.isNotBlank()) {
            travelProposalViewModel.setDetailProposalId(proposalId)
            userReviewViewModel.observeReviewsForProposal(proposalId)
            applicationViewModel.startListeningApplicationsForProposal(proposalId)
        }
    }

    if (observedProposal == null) {
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

    LaunchedEffect(applications) {
        isLoadingParticipantIds = true
        acceptedParticipantIds = try {
            applications.filter { it.statusEnum == ApplicationStatus.Accepted && it.userId != userId }.map { it.userId }
        } catch (_: Exception) {
            emptyList()
        } finally {
            isLoadingParticipantIds = false
        }
    }

    val currentUser = observedUser!!
    val proposal = observedProposal!!

    val companions = remember { mutableStateListOf<UserProfile>() }
    var selectedCompanionForDetail by remember { mutableStateOf<UserProfile?>(null) }
    var selectedCompanionIndexForDetail by remember { mutableStateOf<Int?>(null) }

    val configuration = LocalConfiguration.current
    val isTablet = shouldUseTabletLayout()
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTabletInLandscape = isTablet && isLandscape

    val organizerProfile: UserProfile? = proposal.let { p ->
        if (p.organizerId != userId) {
            userProfileViewModel.observeUserProfileById(p.organizerId).collectAsState().value
        } else {
            null
        }
    }

    val acceptedCompanionProfileStates: List<UserProfile?> = acceptedParticipantIds.map { id ->
        userProfileViewModel.observeUserProfileById(id).collectAsState().value
    }

    LaunchedEffect(
        proposal,
        organizerProfile,
        acceptedCompanionProfileStates,
        userId,
        isLoadingParticipantIds
    ) {
        if (isLoadingParticipantIds) {
            return@LaunchedEffect
        }

        selectedCompanionForDetail = null
        selectedCompanionIndexForDetail = null
        if (companions.isNotEmpty()) companions.clear()

        if (proposal.organizerId != userId) {
            organizerProfile?.let { companions.add(it) }
        }
        companions.addAll(acceptedCompanionProfileStates.filterNotNull())

        if (isTabletInLandscape && selectedCompanionForDetail == null && companions.isNotEmpty()) {
            selectedCompanionForDetail = companions.first()
            selectedCompanionIndexForDetail = 0
        } else {
            val currentSelectedId = selectedCompanionForDetail?.userId
            val newSelection = if (currentSelectedId != null) companions.find { it.userId == currentSelectedId } else null

            if (newSelection != null) {
                selectedCompanionForDetail = newSelection
                selectedCompanionIndexForDetail = companions.indexOf(newSelection).takeIf { it != -1 }
            } else if (isTabletInLandscape && companions.isNotEmpty()) {
                selectedCompanionForDetail = companions.first()
                selectedCompanionIndexForDetail = 0
            } else {
                selectedCompanionForDetail = null
                selectedCompanionIndexForDetail = null
            }
        }
    }

    val isPopulatingCompanions = isLoadingParticipantIds ||
            (proposal.organizerId != userId && organizerProfile == null && !acceptedParticipantIds.contains(proposal.organizerId)) ||
            (acceptedParticipantIds.isNotEmpty() && acceptedCompanionProfileStates.any { it == null })


    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "Review Participants",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = { /* No specific actions for this screen*/ }
        )
    }

    if (isPopulatingCompanions) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Text("Loading trip details...")
        }
    } else {
        if (isTabletInLandscape) {
            Row(modifier = modifier.fillMaxSize().padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.35f)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(companions) { index, companion ->
                        CompanionListItem(
                            user = companion,
                            isSelected = selectedCompanionForDetail?.userId == companion.userId,
                            onClick = {
                                selectedCompanionForDetail = companion
                                selectedCompanionIndexForDetail = index
                            },
                            isOwner = index == 0
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Box(modifier = Modifier.weight(0.65f).fillMaxHeight()) {
                    if (selectedCompanionForDetail != null && selectedCompanionIndexForDetail != null) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            item {
                                ReviewCard(
                                    proposalId = proposalId,
                                    user = selectedCompanionForDetail!!,
                                    userIndex = selectedCompanionIndexForDetail!!,
                                    companions = companions,
                                    reviews = reviews,
                                    currentUser = currentUser,
                                    userProfileViewModel = userProfileViewModel,
                                    userReviewViewModel = userReviewViewModel,
                                    onNavigateToUserProfileInfo = onNavigateToUserProfileInfo
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select a companion to review.")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items (
                    count = companions.size,
                    key = { index -> companions[index].userId },
                    itemContent = { index ->
                        val companion = companions[index]
                        ReviewCard(
                            proposalId = proposalId,
                            user = companion,
                            userIndex = index,
                            companions = companions,
                            reviews = reviews,
                            currentUser = currentUser,
                            userProfileViewModel = userProfileViewModel,
                            userReviewViewModel = userReviewViewModel,
                            onNavigateToUserProfileInfo = onNavigateToUserProfileInfo
                        )

                    }
                )
            }
        }
    }
}

@Composable
fun CompanionListItem(
    user: UserProfile,
    isSelected: Boolean,
    onClick: () -> Unit,
    isOwner: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(imageSize = 40.dp, user = user)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Rating: %.1f ★".format(user.rating),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            if (isOwner) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Owner",
                    tint = Color.Yellow
                )
            }
        }
    }
}

@Composable
fun ReviewCard(
    proposalId: String,
    user: UserProfile,
    userIndex: Int,
    companions: MutableList<UserProfile>,
    reviews: List<UserReview>,
    currentUser: UserProfile,
    userProfileViewModel: UserProfileViewModel,
    userReviewViewModel: UserReviewViewModel,
    onNavigateToUserProfileInfo: (String) -> Unit
) {
    var existingReview by remember(user.userId, currentUser.userId, reviews) {
        mutableStateOf(reviews.find { it.reviewedUserId == user.userId && it.reviewerId == currentUser.userId })
    }
    var reviewText by remember(existingReview) { mutableStateOf(existingReview?.comment ?: "") }
    var reviewRating by remember(existingReview) { mutableFloatStateOf(existingReview?.rating ?: 0.0f) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditingReview by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(user, reviews, currentUser) {
        val matchingReview = reviews.find {
            it.reviewedUserId == user.userId && it.reviewerId == currentUser.userId
        }
        if (existingReview?.reviewId != matchingReview?.reviewId) {
            existingReview = matchingReview
            reviewText = matchingReview?.comment ?: ""
            reviewRating = matchingReview?.rating ?: 0.0f
        }
    }

    Box {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                ProfileAvatar(
                    imageSize = 50.dp,
                    user = user,
                    onClick = { onNavigateToUserProfileInfo(user.userId) }
                )
                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {

                        Column(modifier = Modifier.clickable { onNavigateToUserProfileInfo(user.userId) }) {
                            Text(
                                text = user.firstName + " " + user.lastName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "Overall Rating: %.1f ★".format(user.rating),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (existingReview != null) {
                            IconButton(onClick = {
                                isEditingReview = true
                            }, enabled = !isEditingReview) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit Review",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                showDeleteDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete Review",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Spacer(modifier = Modifier.weight(1f))

                    Text("Your Rating:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        (1..5).forEach { starIndex ->
                            IconButton(
                                onClick = { reviewRating = starIndex.toFloat() },
                                enabled = existingReview == null || isEditingReview
                            ) {
                                Icon(
                                    imageVector = if (starIndex <= reviewRating) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = "Rate $starIndex stars",
                                    tint = if (starIndex <= reviewRating)
                                            (if (existingReview == null || isEditingReview) Color(0xFFFFD700) else Color(0x80FFD700))
                                            else Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Comment") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Share your experience...") },
                        minLines = 1,
                        maxLines = 3,
                        enabled = existingReview == null || isEditingReview
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if ((reviewText.isNotBlank() && reviewRating > 0.0f && existingReview == null) || isEditingReview) {
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                if (existingReview != null) {
                                    val oldRatingValue = existingReview!!.rating
                                    val updatedReviewData = existingReview!!.copy(
                                        rating = reviewRating,
                                        comment = reviewText,
                                        date = Timestamp.now()
                                    )
                                    userReviewViewModel.updateReview(updatedReviewData, oldRatingValue) { updatedUserProfile ->
                                        if (updatedUserProfile != null) {
                                            companions[userIndex] = updatedUserProfile
                                            existingReview = updatedReviewData
                                            userProfileViewModel.refreshUserProfile(updatedUserProfile)

                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Review updated!")
                                            }
                                        }
                                    }
                                    isEditingReview = false
                                } else {
                                    val newReview = UserReview(
                                        reviewerId = currentUser.userId,
                                        reviewerFirstName = currentUser.firstName,
                                        reviewerLastName = currentUser.lastName,
                                        reviewedUserId = user.userId,
                                        proposalId = proposalId,
                                        rating = reviewRating,
                                        comment = reviewText,
                                        date = Timestamp.now()
                                    )
                                    userReviewViewModel.addReview(newReview) { updatedUserProfile ->
                                        if (updatedUserProfile != null) {
                                            companions[userIndex] = updatedUserProfile
                                            existingReview = newReview
                                            userProfileViewModel.refreshUserProfile(updatedUserProfile)

                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Review submitted!")
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.End),
                            enabled = reviewText.isNotBlank() && reviewRating > 0.0f
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Review",
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(if (existingReview != null) "Update Review" else "Submit Review")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && existingReview != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    userReviewViewModel.deleteReview(existingReview!!) { updatedUserProfile ->
                        if (updatedUserProfile != null) {
                            companions[userIndex] = updatedUserProfile
                            userProfileViewModel.refreshUserProfile(updatedUserProfile)
                        }

                        reviewText = ""
                        reviewRating = 0.0f
                        existingReview = null
                        showDeleteDialog = false
                    }
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Review") },
            text = { Text("Are you sure you want to delete this review?") }
        )
    }
}
