package com.example.travelsharingapp.ui.screens.user_profile

import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.utils.rememberImagePickerActions
import com.example.travelsharingapp.utils.toLocalDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileEditScreen(
    modifier: Modifier,
    onBack: () -> Unit,
    userViewModel: UserProfileViewModel,
    topBarViewModel: TopBarViewModel
) {
    val configuration = LocalConfiguration.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val userProfile by userViewModel.selectedUserProfile.collectAsState()
    val editedBirthDate = userViewModel.editedBirthDate
        ?.toLocalDate()
        ?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""

    val validationErrors by userViewModel.validationErrors.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val imagePickerActions = rememberImagePickerActions(
        onImageSelected = { uri ->
            uri?.let { userViewModel.updateProfileImageUri(it, context) }
        }
    )

    LaunchedEffect(true) {
        userViewModel.showToastMessage.collect { show ->
            if(show) {
                Toast.makeText(context, "User profile saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "Edit Profile",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = {
                    val saved = userViewModel.saveProfile()
                    if (saved) onBack()
                }) {
                    Icon(Icons.Default.Save, contentDescription = "Save")
                }
            }
        )
    }

    BackHandler(onBack = onBack)
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
                        firstName = userProfile?.firstName ?: "",
                        lastName = userProfile?.lastName ?: "",
                        originalImageUri = userProfile?.profileImage,
                        originalImageThumbnailUri = userProfile?.profileImageThumbnail,
                        pendingImageUri = userViewModel.editedProfileImageUri,
                        rating = userProfile?.rating ?: 0.0f,
                        isImageEditable = true,
                        onEditImageClick = { showImageSourceDialog = true },
                        isEditing = true
                    )
                }

                EditableFieldsSection(
                    userViewModel = userViewModel,
                    validationErrors = validationErrors,
                    editedBirthDate = editedBirthDate,
                    onShowDatePicker = { showDatePicker = true },
                    modifier = Modifier
                        .weight(0.7f)
                        .imePadding()
                        .verticalScroll(scrollState)
                        .padding(start = 16.dp)
                )
            }
        }

        else -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileHeaderSection(
                    firstName = userProfile?.firstName ?: "",
                    lastName = userProfile?.lastName ?: "",
                    originalImageUri = userProfile?.profileImage,
                    originalImageThumbnailUri = userProfile?.profileImageThumbnail,
                    pendingImageUri = userViewModel.editedProfileImageUri,
                    rating = userProfile?.rating ?: 0.0f,
                    isImageEditable = true,
                    onEditImageClick = { showImageSourceDialog = true },
                    isEditing = true
                )

                EditableFieldsSection(
                    userViewModel = userViewModel,
                    validationErrors = validationErrors,
                    editedBirthDate = editedBirthDate,
                    onShowDatePicker = { showDatePicker = true },
                )
            }
        }
    }

    if (showDatePicker) {
        val initialSelectedMillis = remember(userViewModel.editedBirthDate) {
            userViewModel.editedBirthDate?.toDate()?.time
        }


        DatePickerModal(
            initialSelectedDateMillis = initialSelectedMillis,
            onDateSelected = { millis ->
                userViewModel.updateBirthDate(millis)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showImageSourceDialog) {
        ImageSourceDialog(
            onDismissRequest = { showImageSourceDialog = false },
            onCameraClick = {
                showImageSourceDialog = false
                imagePickerActions.launchCamera()
            },
            onGalleryClick = {
                showImageSourceDialog = false
                imagePickerActions.launchGallerySingle()
            }
        )
    }
}

@Composable
fun ProfileHeaderSection(
    firstName: String,
    lastName: String,
    originalImageUri: String?,
    originalImageThumbnailUri: String? = null,
    pendingImageUri: Uri? = null,
    rating: Float,
    isImageEditable: Boolean = false,
    onEditImageClick: () -> Unit = {},
    isEditing: Boolean = false,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(top = 24.dp, bottom = 32.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            ProfileImage(
                firstName = firstName,
                lastName = lastName,
                originalImageUri = originalImageUri,
                originalImageThumbnailUri = originalImageThumbnailUri,
                pendingImageUri = pendingImageUri,
                isEditable = isImageEditable,
                onEditClick = onEditImageClick
            )

            Text(
                text = "$firstName $lastName",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            RatingStars(rating = rating)
        }
    }
}

@Composable
fun EditableFieldsSection(
    userViewModel: UserProfileViewModel,
    validationErrors: Map<EditableProfileField, String>,
    editedBirthDate: String,
    onShowDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StyledTextField(
            label = "First name",
            value = userViewModel.editedFirstName,
            onValueChange = userViewModel::updateFirstName,
            isError = validationErrors.containsKey(EditableProfileField.FirstName),
            errorText = validationErrors[EditableProfileField.FirstName],
            keyboardOptions = KeyboardOptions.Default
        )

        StyledTextField(
            label = "Last name",
            value = userViewModel.editedLastName,
            onValueChange = userViewModel::updateLastName,
            isError = validationErrors.containsKey(EditableProfileField.LastName),
            errorText = validationErrors[EditableProfileField.LastName],
            keyboardOptions = KeyboardOptions.Default
        )

        StyledTextField(
            label = "Email",
            value = userViewModel.editedEmail,
            onValueChange = userViewModel::updateEmail,
            isError = validationErrors.containsKey(EditableProfileField.Email),
            errorText = validationErrors[EditableProfileField.Email],
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        StyledTextField(
            label = "Nickname",
            value = userViewModel.editedNickname,
            onValueChange = userViewModel::updateNickname,
            isError = validationErrors.containsKey(EditableProfileField.Nickname),
            errorText = validationErrors[EditableProfileField.Nickname],
            keyboardOptions = KeyboardOptions.Default
        )

        val birthDateError = validationErrors[EditableProfileField.BirthDate]
        val isBirthDateError = birthDateError != null

        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onShowDatePicker)
            ) {
                OutlinedTextField(
                    value = editedBirthDate.ifBlank { "Select Birthdate" },
                    onValueChange = { }, // Disallow direct editing
                    label = {
                        Text(
                            "Birthdate",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    readOnly = true, // Prevent keyboard input
                    enabled = false, // Disables the field visually but click still works on wrapper Box
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = if (isBirthDateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = if (isBirthDateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        disabledTrailingIconColor = if (isBirthDateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = if (isBirthDateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    ),
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    },
                    isError = isBirthDateError,
                    supportingText = {
                        if (isBirthDateError) {
                            Text(text = birthDateError, color = MaterialTheme.colorScheme.error)
                        }
                    },
                )
            }
        }

        StyledTextField(
            label = "Mobile Phone",
            value = userViewModel.editedPhoneNumber,
            onValueChange = userViewModel::updatePhoneNumber,
            isError = validationErrors.containsKey(EditableProfileField.PhoneNumber),
            errorText = validationErrors[EditableProfileField.PhoneNumber],
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        StyledTextField(
            label = "Description",
            value = userViewModel.editedDescription,
            onValueChange = userViewModel::updateDescription,
            isError = false, // it is possible to not set a description
            keyboardOptions = KeyboardOptions.Default,
            isSingleLine = false // Description can be multiline
        )

        EditableUserProfileInterests(
            title = "Interests",
            subtitle = "No interests added yet",
            label = "Add interest",
            interests = userViewModel.editedInterests,
            onAddInterest = userViewModel::addInterest,
            onRemoveInterest = userViewModel::removeInterest
        )

        EditableUserProfileDestinations(
            label = "Most Desired Destinations",
            destinations = userViewModel.editedDesiredDestinations,
            onAddDestination = userViewModel::addDestination,
            onRemoveDestination = userViewModel::removeDestination
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ImageSourceDialog(
    onDismissRequest: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    AlertDialog(
        icon = { },
        title = { Text("Choose Image Source") },
        text = {
            Column {
                TextButton(
                    onClick = onCameraClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                        Spacer(Modifier.width(8.dp))
                        Text("Use the camera to take a picture")
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onGalleryClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery")
                        Spacer(Modifier.width(8.dp))
                        Text("Select an image from the phone gallery")
                    }
                }
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {

        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StyledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    errorText: String? = null,
    keyboardOptions: KeyboardOptions,
    isSingleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) },
        textStyle = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth(),
        singleLine = isSingleLine,
        isError = isError,
        supportingText = {
            if (isError && errorText != null) {
                Text(text = errorText, color = MaterialTheme.colorScheme.error)
            }
        },
        keyboardOptions = keyboardOptions
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditableUserProfileInterests(
    title: String,
    subtitle: String,
    label: String,
    interests: List<String>,
    onAddInterest: (String) -> Unit,
    onRemoveInterest: (String) -> Unit
) {
    var newInterestText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val addInterestAction = {
        if (newInterestText.isNotBlank() && newInterestText !in interests) {
            onAddInterest(newInterestText)
            newInterestText = ""
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =  Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (interests.isEmpty()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            } else {
                interests.forEach { interest ->
                    InputChip(
                        selected = false,
                        onClick = { },
                        label = { Text(interest) },
                        trailingIcon = {
                            IconButton(
                                onClick = { onRemoveInterest(interest) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove $interest",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                }
            }
        }

        AddNewElementToList(
            value = newInterestText,
            onValueChange = { newInterestText = it },
            onActionClick = addInterestAction,
            label = label,
            actionContentDescription = label
        )
    }
}

@Composable
fun EditableUserProfileDestinations(
    label: String,
    destinations: List<String>,
    onAddDestination: (String) -> Unit,
    onRemoveDestination: (String) -> Unit
) {
    var newDestinationText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val addDestinationAction = {
        if (newDestinationText.isNotBlank()) {
            onAddDestination(newDestinationText.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            })
            newDestinationText = ""
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (destinations.isEmpty()) {
                item {
                    Text(
                        "No desired destinations yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            } else {
                items(count = destinations.size) { index ->
                    val destinationName = destinations[index]
                    DestinationCard(
                        destinationName = destinationName,
                        isEditing = true,
                        onRemove = { onRemoveDestination(destinationName) }
                    )
                }
            }
        }

        AddNewElementToList(
            value = newDestinationText,
            onValueChange = { newDestinationText = it },
            onActionClick = addDestinationAction,
            label = "Add Destination",
            actionContentDescription = "Add Destination"
        )
    }
}

@Composable
fun AddNewElementToList(
    value: String,
    onValueChange: (String) -> Unit,
    onActionClick: () -> Unit,
    label: String,
    actionContentDescription: String
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 80.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .weight(1f)
                .onFocusChanged{ focusState ->
                    isFocused = focusState.isFocused
                },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default,
            keyboardActions = KeyboardActions(onDone = { onActionClick() })
        )

        AnimatedVisibility(
            visible = isFocused,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onActionClick,
                    enabled = value.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = actionContentDescription,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
object PastOrPresentSelectableDates: SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return utcTimeMillis <= System.currentTimeMillis()
    }

    override fun isSelectableYear(year: Int): Boolean {
        return year <= LocalDate.now().year
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    initialSelectedDateMillis: Long? = null,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis,
        selectableDates = PastOrPresentSelectableDates
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    // Safely update the current `onBack` lambda when a new one is provided
    val currentOnBack by rememberUpdatedState(onBack)
    // Remember in Composition a back callback that calls the `onBack` lambda
    val backCallback = remember {
        object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                currentOnBack()
            }
        }
    }
    // On every successful composition, update the callback with the `enabled` value
    SideEffect {
        backCallback.isEnabled = enabled
    }
    val backDispatcher = checkNotNull(LocalOnBackPressedDispatcherOwner.current) {
        "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner"
    }.onBackPressedDispatcher
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, backDispatcher) {
        // Add callback to the backDispatcher
        backDispatcher.addCallback(lifecycleOwner, backCallback)
        // When the effect leaves the Composition, remove the callback
        onDispose {
            backCallback.remove()
        }
    }
}