package com.example.travelsharingapp.ui.screens.user_profile

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.net.Uri
import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.travelsharingapp.R
import com.example.travelsharingapp.data.model.UserProfile
import com.example.travelsharingapp.data.model.UserReview
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_application.ProfileAvatar
import com.example.travelsharingapp.ui.screens.user_review.UserReviewViewModel
import com.example.travelsharingapp.utils.toLocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    modifier: Modifier,
    isOwnProfile: Boolean,
    userId: String,
    onNavigateToAllUserReviews: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSettings: () -> Unit,
    userViewModel: UserProfileViewModel,
    userReviewViewModel: UserReviewViewModel,
    topBarViewModel: TopBarViewModel
) {
    val configuration = LocalConfiguration.current

    val userProfile by userViewModel.visualizedUserProfile.collectAsState()
    val reviews by userReviewViewModel.userReviews.collectAsState()
    val uiState by userViewModel.uiState.collectAsState()


    LaunchedEffect(userId) {
        userViewModel.visualizeUserProfile(userId)
        userReviewViewModel.loadReviewsForUser(userId)
    }

    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "User Profile",
            navigationIcon = { /* nothing */},
            actions = {
                if(isOwnProfile) {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            }
        )
    }

    when (uiState) {
        UserProfileUiState.Loading -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text("Loading user profile...")
            }
        }

        UserProfileUiState.Error -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Something went wrong retrieving user profile info")
            }
        }

        UserProfileUiState.Loaded -> {
            userProfile?.let { profile ->
                when (configuration.orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        Row(
                            modifier = modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(0.3f)
                                    .padding(end = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ProfileHeaderSection(
                                    firstName = profile.firstName,
                                    lastName = profile.lastName,
                                    originalImageUri = profile.profileImage,
                                    rating = profile.rating
                                )
                            }

                            UserInfoSection(
                                userProfile = profile,
                                modifier = Modifier
                                    .weight(0.7f)
                                    .imePadding()
                                    .verticalScroll(rememberScrollState())
                                    .padding(start = 16.dp)
                            )
                        }
                    }
                    else -> {
                        Column(
                            modifier = modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ProfileHeaderSection(
                                firstName = profile.firstName,
                                lastName = profile.lastName,
                                originalImageUri = profile.profileImage,
                                rating = profile.rating
                            )

                            UserInfoSection(userProfile = profile)

                            if (reviews.isNotEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                UserReviewPreviewSection(
                                    reviews = reviews,
                                    userViewModel = userViewModel,
                                    onViewAllClick = onNavigateToAllUserReviews
                                )
                            }
                        }
                    }
                }
            }
        }
    } ?: Column(modifier = modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator()
        Text("Something went wrong retrieving user profile info")
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun UserInfoSection(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                LabeledInfoCardItem(Icons.Default.Person, "Name", userProfile.firstName)
                LabeledInfoCardItem(Icons.Default.Person, "Surname", userProfile.lastName)
                LabeledInfoCardItem(Icons.Default.Email, "Email", userProfile.email)
                LabeledInfoCardItem(Icons.Default.AccountCircle, "Nickname", userProfile.nickname)
                LabeledInfoCardItem(
                    Icons.Default.Cake,
                    "Birthdate",
                    userProfile.birthDate?.toLocalDate()
                        ?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""
                )
                LabeledInfoCardItem(Icons.Default.Phone, "Mobile phone", userProfile.phoneNumber)
                LabeledInfoCardItem(Icons.Default.Description, "Description", userProfile.description)
            }
        }

        UserProfileInterests(label = "Interests", interests = userProfile.interests)

        Text(
            text = "Most Desired Destinations",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (userProfile.desiredDestinations.isEmpty()) {
                item {
                    Text(
                        "No desired destinations yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            } else {
                items(count = userProfile.desiredDestinations.size) { index ->
                    val destination = userProfile.desiredDestinations[index]
                    DestinationCard(destinationName = destination)
                }
            }
        }
    }
}


@Composable
fun RatingStars(rating: Float = 5f) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        val starColor = Color(0xFFFFD700) // Gold
        val maxStars = 5

        repeat(maxStars) { index ->
            val starValue = index + 1
            Box(modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Filled.StarBorder,
                    contentDescription = "Empty star",
                    tint = starColor,
                    modifier = Modifier.matchParentSize()
                )

                if (rating >= starValue) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Full star",
                        tint = starColor,
                        modifier = Modifier.matchParentSize()
                    )
                } else if (rating > index && rating < starValue) {
                    val fraction = rating - index
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Partial star",
                        tint = starColor,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(FractionalRectangleShape(0f, fraction))
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

class FractionalRectangleShape(private val startFraction: Float, private val endFraction: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(
            Rect(
                left = size.width * startFraction,
                top = 0f,
                right = size.width * endFraction,
                bottom = size.height
            )
        )
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun getDestinationImage(destinationName: String): Int {
    val resourceName = "dest_" + destinationName
        .trim()
        .replace("\\s+".toRegex(), "_")
        .lowercase()

    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(
        resourceName,
        "drawable",
        context.packageName
    )
    return if (resourceId != 0) {
        resourceId
    } else {
        R.drawable.placeholder_travel
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserProfileInterests(
    label: String,
    interests: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (interests.isEmpty()) {
                Text("No interests", style = MaterialTheme.typography.bodySmall)
            } else {
                interests.forEach { interest ->
                    SuggestionChip(
                        onClick = {},
                        label = { Text(interest) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}


@Composable
fun DestinationCard(
    destinationName: String,
    isEditing: Boolean = false,
    onRemove: () -> Unit = {}
) {
    val imageId = getDestinationImage(destinationName)

    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .height(150.dp)
            .width(200.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = imageId),
                contentDescription = destinationName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 150f
                        )
                    )
            )

            Text(
                text = destinationName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )

            if (isEditing) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .align(Alignment.TopEnd),
                    color = Color.White.copy(alpha = 0.6f)
                ) {
                    IconButton(
                        onClick = { onRemove() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove $destinationName",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileImage(
    firstName: String,
    lastName: String,
    originalImageUri: String?,
    pendingImageUri: Uri? = null,
    isEditable: Boolean = false,
    onEditClick: () -> Unit = {}
) {
    val imageSize = 150.dp
    val imageModel: Any? = pendingImageUri ?: originalImageUri

    // Use pending image if available, otherwise use original image
    val showUserInitials = imageModel == null || (imageModel is String && imageModel.isBlank())
    val userInitials = "${firstName.first().uppercaseChar()}${lastName.first().uppercaseChar()}"

    Box(contentAlignment = Alignment.Center) {
        Box(modifier = Modifier
            .size(imageSize)
            .clip(CircleShape)
            .border(
                width = if (isEditable) 2.dp else 0.dp,
                color = if (isEditable) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .clickable(enabled = isEditable, onClick = onEditClick), // we can click on it only in editing mode
            contentAlignment = Alignment.Center
        ) {
            if (!showUserInitials) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = "Profile Image",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop // crop to circle
                )
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFF512DA8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userInitials,
                        color = Color.White,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }


        if (isEditable) {
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(imageSize / 3.5f)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = "Change Profile Picture",
                    tint = Color.White,
                    modifier = Modifier.size(imageSize / 5f)
                )
            }
        }
    }
}


@Composable
fun LabeledInfoCardItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun UserReviewPreviewSection(
    reviews: List<UserReview>,
    userViewModel: UserProfileViewModel,
    onViewAllClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Reviews received", style = MaterialTheme.typography.titleMedium)

        reviews.take(2).forEach { review ->
            var reviewerProfile by remember { mutableStateOf<UserProfile?>(null) }

            LaunchedEffect(review.reviewerId) {
                reviewerProfile = userViewModel.getOrFetchUserProfileById(review.reviewerId)
            }

            if (reviewerProfile != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {

                        ProfileAvatar(
                            imageSize = 50.dp,
                            user = reviewerProfile!!
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${review.reviewerFirstName} ${review.reviewerLastName}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(5) { i ->
                                    val tint = if (i < review.rating) Color(0xFFFFD700) else Color.LightGray
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = tint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = review.comment)
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Reviewer info unavailable",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        if (reviews.size > 2) {
            Text(
                "Show all reviews",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { onViewAllClick() }
            )
        }
    }
}

