package com.example.travelsharingapp.ui.screens.user_profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.travelsharingapp.utils.toLocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileInitialSetupScreen (
    modifier: Modifier = Modifier,
    viewModel: UserProfileInitialSetupViewModel,
    onProfileSavedSuccessfully: () -> Unit,
    onSetupCancelled: () -> Unit
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.resetState() // reset so we are at step 1
    }

    LaunchedEffect(uiState) {
        if (uiState is ProfileSetupState.Success) {
            onProfileSavedSuccessfully()
            viewModel.endProfileSetup()
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Profile Setup?") },
            text = { Text("Are you sure you want to cancel? You cannot proceed if you do not complete the profile and will be redirected to the login screen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        viewModel.resetState()
                        onSetupCancelled()
                    }
                ) { Text("Yes, Cancel") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("No, Continue") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Your Profile") },
                actions = {
                    TextButton(onClick = {
                        showCancelDialog = true
                    }) {
                        Text("Cancel")
                    }
                }
            )
        },
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            ProfileSetupProgressIndicator(
                currentStep = currentStep,
                totalSteps = UserProfileInitialSetupViewModel.TOTAL_STEPS
            )

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { fullWidth -> fullWidth } togetherWith
                                slideOutHorizontally { fullWidth -> -fullWidth }
                    } else {
                        slideInHorizontally { fullWidth -> -fullWidth } togetherWith
                                slideOutHorizontally { fullWidth -> fullWidth }
                    }
                },
                label = "StepContentAnimation"
            ) { step ->
                when (step) {
                    1 -> Step1_EssentialIdentity(
                        modifier = modifier.fillMaxSize(),
                        viewModel = viewModel,
                        onNext = { viewModel.nextStep() }
                    )
                    2 -> Step2_PersonalDetails(
                        modifier = modifier.fillMaxSize(),
                        viewModel = viewModel,
                        onNext = { viewModel.nextStep() },
                        onBack = { viewModel.previousStep() }
                    )
                    3 -> Step3_TravelPreferences(
                        modifier = modifier.fillMaxSize(),
                        viewModel = viewModel,
                        onSave = { viewModel.saveUserProfile() },
                        onBack = { viewModel.previousStep() }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileSetupProgressIndicator(currentStep: Int, totalSteps: Int) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(8.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Step $currentStep of $totalSteps",
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun Step1_EssentialIdentity(
    modifier: Modifier = Modifier,
    viewModel: UserProfileInitialSetupViewModel,
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()

    val firstName by viewModel.firstName
    val lastName by viewModel.lastName
    val nickname by viewModel.nickname


    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(200.dp))

        Text("Tell us about yourself", style = MaterialTheme.typography.headlineSmall)
        Text("Let's start with the basics.", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = { viewModel.firstName.value = it },
            label = { Text("First Name*") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { viewModel.lastName.value = it },
            label = { Text("Last Name*") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { viewModel.nickname.value = it },
            label = { Text("Nickname*") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (uiState is ProfileSetupState.Error && currentStep == 1) {
            Text(
                text = (uiState as ProfileSetupState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Next")
        }
    }
}

@Composable
fun Step2_PersonalDetails(
    modifier: Modifier = Modifier,
    viewModel: UserProfileInitialSetupViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()

    val birthDate by remember { derivedStateOf { viewModel.birthDate } }
    val phoneNumber by viewModel.phoneNumber

    var showDatePicker by remember { mutableStateOf(false) }

    val isBirthDateErrorInState = uiState is ProfileSetupState.Error &&
            (uiState as ProfileSetupState.Error).message.contains("Birthdate", ignoreCase = true)

    val itFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(200.dp))

        Text("A few more details", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { showDatePicker = true })
            ) {
                OutlinedTextField(
                    value = birthDate?.toLocalDate()?.format(itFormatter) ?: "Select Birthdate",
                    onValueChange = { },
                    label = { Text("Birthdate") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true, // Prevent keyboard input
                    enabled = false, // Disables the field visually but click still works on wrapper Box
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = if (isBirthDateErrorInState && currentStep == 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = if (isBirthDateErrorInState && currentStep == 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        disabledTrailingIconColor = if (isBirthDateErrorInState && currentStep == 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = if (isBirthDateErrorInState && currentStep == 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    },
                    isError = isBirthDateErrorInState && currentStep == 2,
                    supportingText = {
                        if (isBirthDateErrorInState && currentStep == 2 && uiState is ProfileSetupState.Error) {
                            Text(text = (uiState as ProfileSetupState.Error).message, color = MaterialTheme.colorScheme.error)
                        }
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { viewModel.phoneNumber.value = it },
            label = { Text("Phone Number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (uiState is ProfileSetupState.Error && currentStep == 2 && !isBirthDateErrorInState) {
            Text(
                text = (uiState as ProfileSetupState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Back")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onNext, modifier = Modifier.weight(1f)) {
                Text("Next")
            }
        }
    }

    if (showDatePicker) {
        val initialSelectedMillis = remember(viewModel.birthDate) {
            viewModel.birthDate?.toDate()?.time
        }

        DatePickerModal(
            initialSelectedDateMillis = initialSelectedMillis,
            onDateSelected = { millis ->
                viewModel.updateBirthDate(millis)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun Step3_TravelPreferences(
    modifier: Modifier = Modifier,
    viewModel: UserProfileInitialSetupViewModel,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()

    val description by viewModel.description

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        Text("Share your travel style", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { viewModel.description.value = it },
            label = { Text("About Me / Description") },
            minLines = 3,
            maxLines = 5,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        EditableUserProfileInterests(
            title = "Interests",
            subtitle = "No interests added yet",
            label = "Add interest",
            interests = viewModel.interests,
            onAddInterest = viewModel::addInterest ,
            onRemoveInterest = viewModel::removeInterest
        )
        Spacer(modifier = Modifier.height(16.dp))

        EditableUserProfileDestinations(
            label = "Most Desired Destinations",
            destinations = viewModel.desiredDestinations,
            onAddDestination = viewModel::addDestination,
            onRemoveDestination = viewModel::removeDestination
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (uiState is ProfileSetupState.Error && currentStep == 3) {
            Text(
                text = (uiState as ProfileSetupState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Back")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onSave,
                enabled = uiState !is ProfileSetupState.Loading,
                modifier = Modifier.weight(1f)
            ) {
                if (uiState is ProfileSetupState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Profile")
                }
            }
        }
    }
}