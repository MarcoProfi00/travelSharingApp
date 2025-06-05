package com.example.travelsharingapp.ui.screens.travel_review

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.travelsharingapp.data.model.TravelProposalReview
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_application.ProfileAvatar
import com.example.travelsharingapp.ui.screens.user_profile.EditableUserProfileInterests
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileViewModel
import com.example.travelsharingapp.utils.rememberImagePickerActions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelReviewAddNewScreen(
    modifier: Modifier,
    proposalId: String,
    userProfileViewModel: UserProfileViewModel,
    reviewViewModel: TravelReviewViewModel,
    topBarViewModel: TopBarViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val userProfile by userProfileViewModel.selectedUserProfile.collectAsState()
    if (userProfile == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val currentUser = userProfile!!
    val reviewerName = "${currentUser.firstName} ${currentUser.lastName}"


    LaunchedEffect(proposalId) {
        reviewViewModel.observeReviews(proposalId)
    }

    val reviews by reviewViewModel.proposalSpecificReviews.collectAsState()
    val userReview = reviews.find { it.reviewerId == currentUser.userId }

    var comment by remember { mutableStateOf(userReview?.comment ?: "") }
    val tipsList = remember(userReview?.tips) { mutableStateListOf<String>().apply { addAll(userReview?.tips ?: emptyList()) } }
    var rating by remember { mutableFloatStateOf(userReview?.rating ?: 0.0f) }
    val imageModels = remember(userReview?.images) {
        val initialList = userReview?.images ?: emptyList()
        mutableStateListOf<Any>().apply { addAll(initialList) }
    }

    var showDialog by remember { mutableStateOf(false) }

    val imagePickerActions = rememberImagePickerActions(
        onImageSelected = { uri ->
            uri?.let {
                if (imageModels.size < 3) {
                    imageModels.add(it)
                }
            }
        },
        onImagesSelected = { uris ->
            val currentSize = imageModels.size
            val remainingSpace = 3 - currentSize
            if (remainingSpace > 0) {
                imageModels.addAll(uris.take(remainingSpace))
            }
        }
    )

    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "Add a Review",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {}
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            ProfileAvatar(
                imageSize = 50.dp,
                user = currentUser
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(reviewerName, style = MaterialTheme.typography.titleMedium)
                Text("Public post", style = MaterialTheme.typography.bodySmall)
            }
        }

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            repeat(5) { index ->
                IconButton(onClick = { rating = index + 1.0f }) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (index < rating) Color(0xFFFFC107) else Color.LightGray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = { Text("This experience was...") }
        )

        EditableUserProfileInterests(
            title = "Tips for other travelers",
            subtitle = "No tips added yet",
            label = "Add tip",
            interests = tipsList,
            onAddInterest = { tipsList.add(it) } ,
            onRemoveInterest = { tipsList.remove(it) }
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(imageModels) { model ->
                ElevatedCard(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF4FF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .height(200.dp)
                        .width(200.dp),
                    onClick = { }
                ) {
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(model)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Destination photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Surface(
                            shape = CircleShape,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(32.dp)
                                .align(Alignment.BottomEnd),
                            color = Color.White.copy(alpha = 0.5f)
                        ) {
                            IconButton(
                                onClick = { imageModels.remove(model) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth(),
            enabled = imageModels.size < 3
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add a photo (max 3)")
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    val uploadedImages = imageModels.mapNotNull { model ->
                        when (model) {
                            is Uri -> reviewViewModel.uploadReviewImageToFirebase(context, model, currentUser.userId)
                            is String -> model // già URL valido (Firebase)
                            else -> null
                        }
                    }

                    val review = TravelProposalReview(
                        reviewId = userReview?.reviewId ?: "",
                        reviewerId = currentUser.userId,
                        reviewerFirstName = currentUser.firstName,
                        reviewerLastName = currentUser.lastName,
                        images = uploadedImages,
                        tips = tipsList.toList(),
                        rating = rating,
                        comment = comment,
                        date = userReview?.date ?: com.google.firebase.Timestamp.now()
                    )

                    if (userReview != null) {
                        reviewViewModel.updateReview(proposalId, review)
                        Toast.makeText(context, "Review updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        reviewViewModel.addReview(proposalId, review)
                        Toast.makeText(context, "Review published successfully", Toast.LENGTH_SHORT).show()
                    }

                    onBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = comment.isNotBlank() && rating > 0,
            shape = MaterialTheme.shapes.medium,
        ) {
            Text( if(userReview != null) "Update" else "Publish" )
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Add a Review")
                    }
                },
                text = {
                    Column {
                        TextButton(onClick = {
                            showDialog = false
                            imagePickerActions.launchGalleryMultiple()
                        }) { Text("Select from gallery") }
                        TextButton(onClick = {
                            showDialog = false
                            if (imageModels.size < 3) {
                                imagePickerActions.launchCamera()
                            }
                        }) { Text("Take a photo") }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }
    }
}