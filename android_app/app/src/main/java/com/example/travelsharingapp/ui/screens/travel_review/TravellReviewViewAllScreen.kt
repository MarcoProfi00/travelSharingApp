
package com.example.travelsharingapp.ui.screens.travel_review

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.travelsharingapp.data.model.TravelProposalReview
import com.example.travelsharingapp.data.model.UserProfile
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_application.ProfileAvatar
import com.example.travelsharingapp.ui.screens.travel_proposal.TravelProposalViewModel
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileViewModel
import com.example.travelsharingapp.utils.shouldUseTabletLayout
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelReviewViewAllScreen(
    modifier: Modifier,
    userId: String,
    proposalId: String,
    travelProposalViewModel: TravelProposalViewModel,
    reviewViewModel: TravelReviewViewModel,
    userProfileViewModel: UserProfileViewModel,
    topBarViewModel: TopBarViewModel,
    onEditReview: () -> Unit,
    onDeleteReview: (String) -> Unit,
    onAddReview: () -> Unit,
    onBack: () -> Unit,
    onNavigateToUserProfileInfo: (String) -> Unit,
) {

    LaunchedEffect(proposalId) {
        travelProposalViewModel.loadProposalById(proposalId)
        reviewViewModel.observeReviews(proposalId)

        if (userProfileViewModel.selectedUserProfile.value == null) {
            userProfileViewModel.selectUserProfile(userId)
        }
    }

    val showDeleteDialog = remember { mutableStateOf(false) }
    val reviewIdToDelete = remember { mutableStateOf<String?>(null) }

    val selectedUser by userProfileViewModel.selectedUserProfile.collectAsState()
    if (selectedUser == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val currentUser = selectedUser!!

    val allReviews by reviewViewModel.proposalSpecificReviews.collectAsState()

    val isTablet = shouldUseTabletLayout()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useSideBySideLayout = isTablet && isLandscape

    val proposal by travelProposalViewModel.selectedProposal.collectAsState()
    if (proposal == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val currentProposal = proposal!!
        val currentUserReview = allReviews.find { it.reviewerId == currentUser.userId }
        val otherReviews = allReviews.filter { it.reviewerId != currentUser.userId }
        val isOwner = currentProposal.organizerId == currentUser.userId

        val totalReviews = allReviews.size
        val averageRating = if (totalReviews > 0) allReviews.map { it.rating }.average() else 0.0
        val ratingCounts = (1..5).associateWith { star ->
            allReviews.count { it.rating == star.toFloat() }
        }

        LaunchedEffect(Unit, currentUserReview) {
            topBarViewModel.setConfig(
                title = "Reviews for ${currentProposal.name}",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    //No specific actions for this screen
                },
                floatingActionButton = {
                    if (!isOwner && currentUserReview == null) {
                        FloatingActionButton(
                            onClick = { onAddReview() }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add a review"
                            )
                        }
                    }
                }
            )
        }

        if (allReviews.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "There are no reviews for this trip yet." + if (!isOwner) "\nPress the '+' button to add one!" else "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            if (useSideBySideLayout) {
                Row(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                    ) {
                        ReviewSummary(
                            averageRating = averageRating,
                            totalReviews = totalReviews,
                            ratingCounts = ratingCounts
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(0.6f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        currentUserReview?.let { review ->
                            item {
                                ReviewCard(
                                    review = review,
                                    user = currentUser,
                                    ownReview = true,
                                    onEdit = { onEditReview() },
                                    onDelete = {
                                        reviewIdToDelete.value = review.reviewId
                                        showDeleteDialog.value = true
                                    },
                                    onNavigateToUserProfileInfo = onNavigateToUserProfileInfo
                                )
                            }
                        }
                        itemsIndexed(otherReviews) { index, review ->
                            val user by userProfileViewModel.observeUserProfileById(review.reviewerId)
                                .collectAsState(initial = null)
                            if (user != null) {
                                ReviewCard(
                                    index = index,
                                    review = review,
                                    user = user!!,
                                    ownReview = false,
                                    onEdit = { /**/ },
                                    onDelete = { /**/ },
                                    onNavigateToUserProfileInfo = onNavigateToUserProfileInfo
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ReviewSummary(
                            averageRating = averageRating,
                            totalReviews = totalReviews,
                            ratingCounts = ratingCounts
                        )
                    }

                    currentUserReview?.let { review ->
                        item {
                            ReviewCard(
                                review = review,
                                user = currentUser,
                                ownReview = true,
                                onEdit = { onEditReview() },
                                onDelete = {
                                    reviewIdToDelete.value = review.reviewId
                                    showDeleteDialog.value = true
                                },
                                onNavigateToUserProfileInfo = onNavigateToUserProfileInfo
                            )
                        }
                    }

                    itemsIndexed(otherReviews) { index, review ->
                        val user by userProfileViewModel.observeUserProfileById(review.reviewerId)
                            .collectAsState(initial = null)
                        if (user != null) {
                            ReviewCard(
                                index = index,
                                review = review,
                                user = user!!,
                                ownReview = false,
                                onEdit = { /**/ },
                                onDelete = { /**/ },
                                onNavigateToUserProfileInfo = onNavigateToUserProfileInfo
                            )
                        }
                    }
                }
            }
        }

        if (showDeleteDialog.value && reviewIdToDelete.value != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog.value = false },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteReview(reviewIdToDelete.value!!)
                        showDeleteDialog.value = false
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog.value = false
                    }) {
                        Text("Back")
                    }
                },
                title = { Text("Delete review") },
                text = { Text("Are you sure you want to delete this review?") }
            )
        }
    }
}

@Composable
fun ReviewSummary(
    averageRating: Double,
    totalReviews: Int,
    ratingCounts: Map<Int, Int>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = "Review Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = "%.1f".format(averageRating),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    val stars = averageRating.toInt()
                    repeat(5) { i ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i < stars) Color(0xFFFFC107) else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$totalReviews review${if (totalReviews != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                for (i in 5 downTo 1) {
                    val count = ratingCounts[i] ?: 0
                    val percentage = if (totalReviews > 0) count * 100 / totalReviews else 0

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "$i ★",
                            modifier = Modifier.width(32.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                        LinearProgressIndicator(
                            progress = { percentage / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp),
                            color = Color(0xFFFFC107),
                            trackColor = Color(0xFFFFECB3),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


@Composable
fun ReviewCard(
    index: Int = -1,
    review: TravelProposalReview,
    user: UserProfile,
    ownReview: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToUserProfileInfo: (String) -> Unit
) {

    val cardBackgroundColor = if (ownReview) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        if (index % 2 == 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer
    }
    val cardBorder = if (ownReview) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer)
    } else {
        BorderStroke(0.dp, cardBackgroundColor)
    }

    val username = if (ownReview) "You" else (user.firstName + " " + user.lastName)

    val formattedDate = remember(review.date) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        sdf.format(review.date.toDate())
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(cardBorder, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(6.dp),
        onClick = { onNavigateToUserProfileInfo(user.userId) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(
                    imageSize = 40.dp,
                    user = user
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }


                if (ownReview) {
                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit"
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) { index ->
                    val starColor = if (index < review.rating) Color(0xFFFFC107) else Color.LightGray
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = starColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"${review.comment}\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic
            )

            if (review.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(
                        count = review.images.size,
                        itemContent = { index ->
                            val model = review.images[index]
                            AsyncImage(
                                model = model,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    )
                }
            }

            if (review.tips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tips:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =  Arrangement.spacedBy(8.dp)
                ) {
                    review.tips.forEach { tip ->
                        InputChip(
                            selected = false,
                            onClick = { },
                            label = { Text(tip) },
                            trailingIcon = { },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = cardBackgroundColor,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}
