package com.example.travelsharingapp.ui.screens.user_review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelsharingapp.data.model.UserProfile
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_application.ProfileAvatar
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReviewListScreen(
    modifier: Modifier,
    userId: String,
    userProfileViewModel: UserProfileViewModel,
    userReviewViewModel: UserReviewViewModel,
    topBarViewModel: TopBarViewModel,
    onBack: () -> Unit
) {

    val reviews by userReviewViewModel.userReviews.collectAsState()
    LaunchedEffect(userId) {
        userReviewViewModel.loadReviewsForUser(userId)
    }

    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "All Reviews",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = { /* No specific actions for this screen*/ }
        )
    }


    if (reviews.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No reviews yet.")
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                count = reviews.size,
                key = { index -> reviews[index].reviewId },
                itemContent = { index ->
                    val review = reviews[index]
                    val user by userProfileViewModel.observeUserProfileById(review.reviewerId).collectAsState()

                    val fallbackUser = UserProfile(
                        userId = review.reviewerId,
                        firstName = review.reviewerFirstName,
                        lastName = review.reviewerLastName,
                        email = "",
                        nickname = "",
                        birthDate = null,
                        phoneNumber = "",
                        description = "",
                        interests = emptyList(),
                        desiredDestinations = emptyList(),
                        profileImage = null,
                        rating = 0f,
                        numberOfReviews = 0
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            ProfileAvatar(
                                imageSize = 50.dp,
                                user = user ?: fallbackUser
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "${review.reviewerFirstName} ${review.reviewerLastName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(5) { i ->
                                        val tint =
                                            if (i < review.rating) Color(0xFFFFD700) else Color.LightGray
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = tint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(review.comment)
                            }
                        }
                    }
                }
            )
        }
    }
}