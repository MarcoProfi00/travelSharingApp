package com.example.travelsharingapp.ui.screens.travel_proposal

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.travelsharingapp.data.model.ItineraryStop
import com.example.travelsharingapp.data.model.Typology
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.user_profile.BackHandler
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileViewModel
import com.example.travelsharingapp.ui.theme.customColorsPalette
import com.example.travelsharingapp.utils.shouldUseTabletLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelProposalManageScreen(
    modifier: Modifier,
    placesClient: PlacesClient,
    isEditingProposal: Boolean,
    isDuplicatingProposal: Boolean,
    organizerId: String,
    proposalId: String = "",
    userViewModel: UserProfileViewModel,
    proposalViewModel: TravelProposalViewModel,
    topBarViewModel: TopBarViewModel,
    onBack: () -> Unit
) {
    val currentUser by userViewModel.selectedUserProfile.collectAsState()
    val currentUserProfile = currentUser!!

    val hasUnsavedChanges by proposalViewModel.hasUnsavedChanges.collectAsState()
    var showNavigateAwayDialog by rememberSaveable { mutableStateOf(false) }

    var pendingNavigation by remember { mutableStateOf<(() -> Unit)?>(null) }
    val navigationActionWithDialog = { onConfirmedNavigation: () -> Unit ->
        if (hasUnsavedChanges) {
            pendingNavigation = onConfirmedNavigation
            showNavigateAwayDialog = true
        } else {
            onConfirmedNavigation()
        }
    }

    DisposableEffect(hasUnsavedChanges) {
        topBarViewModel.setNavigateAwayAction(navigationActionWithDialog)
        onDispose { topBarViewModel.setNavigateAwayAction(null) }
    }

    BackHandler(enabled = true) { navigationActionWithDialog { onBack() } }

    if (showNavigateAwayDialog) {
        AlertDialog(
            onDismissRequest = { showNavigateAwayDialog = false },
            title = { Text("Discard Changes?") },
            text = { Text("If you navigate away, the changes you've made will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    showNavigateAwayDialog = false
                    proposalViewModel.resetUnsavedChangesFlag()
                    pendingNavigation?.invoke()
                }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showNavigateAwayDialog = false }) { Text("Cancel") }
            }
        )
    }

    proposalViewModel.resetErrors()

    if (isDuplicatingProposal) {
        LaunchedEffect(proposalId) {
            proposalViewModel.loadProposalToDuplicate(proposalId, newOrganizerId = currentUserProfile.userId)
        }
    } else if (isEditingProposal) {
        LaunchedEffect(proposalId) {
            proposalViewModel.loadProposalToEdit(proposalId)
        }
    } else {
        LaunchedEffect(Unit) {
            proposalViewModel.resetFields(organizerId = organizerId)
        }
    }

    val showConfirmationDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = if (isEditingProposal) "Edit Travel Proposal" else "New Travel Proposal",
            navigationIcon = {
                if (isEditingProposal || isDuplicatingProposal) {
                    IconButton(onClick = { navigationActionWithDialog { onBack() } }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        if (proposalViewModel.validateFields()) {
                            showConfirmationDialog.value = true
                        }
                    },
                    enabled = !proposalViewModel.hasErrors()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = "Save"
                    )
                }
            }
        )
    }

    val configuration = LocalConfiguration.current
    val isTablet = shouldUseTabletLayout()

    if (isTablet && configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .imePadding(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.weight(0.55f)) {
                TravelProposalLivePreview(
                    proposalViewModel = proposalViewModel
                )
            }

            Box(modifier = Modifier.weight(0.45f)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item { SectionHeader("Main details") }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                TravelNameField(proposalViewModel)
                                DescriptionField(proposalViewModel)
                                TypologyDropdownMenu(proposalViewModel)
                            }
                        }
                    }

                    item { SectionHeader(title = "Travel Dates") }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                DateRangeSelector(proposalViewModel)
                            }
                        }
                    }

                    item { SectionHeader(title = "Budget & Group Size") }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                PriceRangeSelector(proposalViewModel)
                                ParticipantsSelector(proposalViewModel)
                            }
                        }
                    }

                    item { SectionHeader(title = "Activities & Itinerary") }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                SuggestedActivitiesSelector(proposalViewModel)
                            }
                        }
                    }
                    item { ItinerarySection(proposalViewModel, placesClient) }

                    item { SectionHeader(title = "Travel Photos") }
                    item { ImagePickerSection(proposalViewModel) }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item { SectionHeader("Main details") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TravelNameField(proposalViewModel)
                        DescriptionField(proposalViewModel)
                        TypologyDropdownMenu(proposalViewModel)
                    }
                }
            }

            item { SectionHeader(title = "Travel Dates") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        DateRangeSelector(proposalViewModel)
                    }
                }
            }

            item { SectionHeader(title = "Budget & Group Size") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        PriceRangeSelector(proposalViewModel)
                        ParticipantsSelector(proposalViewModel)
                    }
                }
            }

            item { SectionHeader(title = "Activities & Itinerary") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SuggestedActivitiesSelector(proposalViewModel)
                    }
                }
            }
            item { ItinerarySection(proposalViewModel, placesClient) }

            item { SectionHeader(title = "Travel Photos") }
            item { ImagePickerSection(proposalViewModel) }
        }
    }

    if (showConfirmationDialog.value) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    if (isEditingProposal) {
                        proposalViewModel.updateProposal(proposalId)
                    } else {
                        proposalViewModel.saveProposal()
                    }
                    showConfirmationDialog.value = false
                    onBack()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog.value = false }) {
                    Text("No")
                }
            },
            title = { Text(if (isEditingProposal) "Confirm Update" else "Confirm Creation") },
            text = { Text(if (isEditingProposal) "Do you want to save the changes?" else "Are you sure you want to create this travel proposal?") }
        )
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun TravelNameField(
    viewModel: TravelProposalViewModel
) {
    val name by viewModel.name.collectAsState()
    val error by viewModel.nameError.collectAsState()

    OutlinedTextField(
        value = name,
        onValueChange = { viewModel.onNameChange(it) },
        isError = error != null,
        label = { Text("Travel Name", style = MaterialTheme.typography.bodySmall) },
        textStyle = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth(),
        supportingText = { if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error) }
    )
}

@Composable
fun DescriptionField(viewModel: TravelProposalViewModel) {
    val description = viewModel.description.collectAsState()
    val descriptionError by viewModel.descriptionError.collectAsState()
    val maxChars = 5000

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = description.value,
            onValueChange = { viewModel.onDescriptionChange(it) },
            label = { Text("Description", style = MaterialTheme.typography.bodySmall) },
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            singleLine = false,
            maxLines = 10,
            supportingText = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = descriptionError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${description.value.length} / $maxChars",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypologyDropdownMenu(viewModel: TravelProposalViewModel) {
    val expanded = remember { mutableStateOf(false) }
    val selectedTypology by viewModel.typology.collectAsState()
    val typologyError by viewModel.typologyError.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded.value,
            onExpandedChange = { expanded.value = !expanded.value }
        ) {
            OutlinedTextField(
                value = selectedTypology.toString(),
                onValueChange = { },
                readOnly = true,
                isError = typologyError != null,
                label = { Text("Typology", style = MaterialTheme.typography.bodySmall) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    ),
                textStyle = MaterialTheme.typography.bodyLarge,
                supportingText = {
                    if (typologyError != null) {
                        Text(
                            text = typologyError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            )

            ExposedDropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                Typology.entries.forEach { typology ->
                    DropdownMenuItem(
                        text = { Text(typology.toString()) },
                        onClick = {
                            viewModel.onTypologyChange(typology.name)
                            expanded.value = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeSelector(viewModel: TravelProposalViewModel) {
    val showPicker = remember { mutableStateOf(false) }
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDate?.atStartOfDay()?.atZone(ZoneOffset.systemDefault())?.toInstant()?.toEpochMilli(),
        initialSelectedEndDateMillis = endDate?.atStartOfDay()?.atZone(ZoneOffset.systemDefault())?.toInstant()?.toEpochMilli(),
        selectableDates = FutureSelectableDates
    )

    LaunchedEffect(startDate, endDate) {
        if (!showPicker.value) {
            dateRangePickerState.setSelection(
                startDate?.atStartOfDay()?.atZone(ZoneOffset.systemDefault())?.toInstant()?.toEpochMilli(),
                endDate?.atStartOfDay()?.atZone(ZoneOffset.systemDefault())?.toInstant()?.toEpochMilli()
            )
        }
    }

    val dateError by viewModel.dateError.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        val startText = dateRangePickerState.selectedStartDateMillis?.customToLocalDate()
            ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) ?: "No start date"
        val endText = dateRangePickerState.selectedEndDateMillis?.customToLocalDate()
            ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) ?: "No end date"

        OutlinedButton(
            onClick = { showPicker.value = true },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date Range", modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(if(dateRangePickerState.selectedStartDateMillis != null || dateRangePickerState.selectedEndDateMillis != null) "$startText - $endText" else "Select Dates")
        }

        if (dateError != null) {
            Text(
                text = dateError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (showPicker.value) {
            DatePickerDialog(
                onDismissRequest = {
                    showPicker.value = false
                },
                confirmButton = {
                    TextButton(onClick = {
                        val start = dateRangePickerState.selectedStartDateMillis?.let { formatDate(it) }
                        val end = dateRangePickerState.selectedEndDateMillis?.let { formatDate(it) }
                        viewModel.onDatesChange(start, end)
                        showPicker.value = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPicker.value = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DateRangePicker(state = dateRangePickerState,
                    modifier = Modifier.weight(1f),
                    title = null,
                    headline = null
                )
            }
        }
    }
}

fun formatDate(millis: Long): LocalDate {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

@Composable
fun PriceRangeSelector(viewModel: TravelProposalViewModel) {
    val minPriceFromViewModel by viewModel.minPrice.collectAsState()
    val maxPriceFromViewModel by viewModel.maxPrice.collectAsState()
    val priceError by viewModel.priceError.collectAsState()

    var minPriceText by remember(minPriceFromViewModel) { mutableStateOf(minPriceFromViewModel.toInt().toString()) }
    var maxPriceText by remember(maxPriceFromViewModel) { mutableStateOf(maxPriceFromViewModel.toInt().toString()) }

    LaunchedEffect(minPriceFromViewModel) {
        val vmMinString = minPriceFromViewModel.toInt().toString()
        if (vmMinString != minPriceText) {
            minPriceText = vmMinString
        }
    }

    LaunchedEffect(maxPriceFromViewModel) {
        val vmMaxString = maxPriceFromViewModel.toInt().toString()
        if (vmMaxString != maxPriceText) {
            maxPriceText = vmMaxString
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Price per person (€)", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))

        val sliderRangeStart = minOf(minPriceFromViewModel, maxPriceFromViewModel)
        val sliderRangeEnd = maxOf(minPriceFromViewModel, maxPriceFromViewModel)

        RangeSlider(
            value = sliderRangeStart..sliderRangeEnd,
            onValueChange = { range ->
                viewModel.onMinPriceChange(range.start)
                viewModel.onMaxPriceChange(range.endInclusive)
            },
            valueRange = 0f..10000f,
            steps = 0,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = minPriceText,
                onValueChange = { newText ->
                    minPriceText = newText
                    newText.toFloatOrNull()?.let { parsedValue  ->
                        viewModel.onMinPriceChange(parsedValue.coerceIn(0f, 10000f))
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Min €", style = MaterialTheme.typography.bodySmall) },
                textStyle = MaterialTheme.typography.bodyLarge,
                leadingIcon = { Text("€", style = MaterialTheme.typography.bodyLarge) }
            )

            OutlinedTextField(
                value = maxPriceText,
                onValueChange = { newText ->
                    maxPriceText = newText
                    newText.toFloatOrNull()?.let { parsedValue ->
                        viewModel.onMaxPriceChange(parsedValue.coerceIn(0f, 10000f))
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Max €", style = MaterialTheme.typography.bodySmall) },
                textStyle = MaterialTheme.typography.bodyLarge,
                leadingIcon = { Text("€", style = MaterialTheme.typography.bodyLarge) }
            )
        }

        if (priceError != null) {
            Text(
                text = priceError ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ParticipantsSelector(viewModel: TravelProposalViewModel) {
    val participants by viewModel.maxParticipantsAllowed.collectAsState()
    val error by viewModel.participantsError.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Maximum participants", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = {
                val current = viewModel.maxParticipantsAllowed.value.toIntOrNull() ?: 0
                if (current > 0) viewModel.onMaxParticipantsChange((current - 1).toString())
            }) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }

            OutlinedTextField(
                value = participants,
                onValueChange = {
                    if (it.all { ch -> ch.isDigit() }) {
                        viewModel.onMaxParticipantsChange(it)
                    }
                },
                modifier = Modifier.width(100.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.medium
            )

            IconButton(onClick = {
                val current = viewModel.maxParticipantsAllowed.value.toIntOrNull() ?: 0
                viewModel.onMaxParticipantsChange((current + 1).toString())
            }) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }

        if (error != null) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SuggestedActivitiesSelector(
    viewModel: TravelProposalViewModel
) {
    val allDefaultActivities = remember {
        mutableStateListOf(
            "Climbing", "Skiing", "Culinary experience",
            "Scuba",  "Diving", "Attraction seeking",
            "City Exploring", "Cycling", "Kayaking",
            "Rafting",
        )
    }
    var suggestedActivities = viewModel.suggestedActivities.collectAsState().value
    val combinedActivities = remember(allDefaultActivities, suggestedActivities) {
        val combined = allDefaultActivities.toMutableList()

        suggestedActivities.forEach { suggested ->
            if (!combined.contains(suggested)) {
                combined.add(suggested)
            }
        }
        combined
    }

    var newActivityText by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val addActivityAction = {
        if (newActivityText.isNotBlank()) {
            viewModel.addSuggestedActivity(newActivityText)
            newActivityText = ""
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =  Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            combinedActivities.forEach { activity ->
                FilterChipSelectable(
                    onClick = {
                        if (suggestedActivities.contains(activity)) {
                            viewModel.removeSuggestedActivity(activity)
                        } else {
                            viewModel.addSuggestedActivity(activity)
                        }
                    },
                    isSelected = activity in suggestedActivities,
                    label = activity.toString()
                )
            }
        }

        AddNewElementToListTravel(
            value = newActivityText,
            onValueChange = { newActivityText = it },
            onActionClick = addActivityAction,
            label = "Add Activity",
            actionContentDescription = "Add Activity"
        )
    }
}

@Composable
fun AddNewElementToListTravel(
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
                .onFocusChanged { focusState ->
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
@Composable
fun ItinerarySection(
    viewModel: TravelProposalViewModel,
    placesClient: PlacesClient
) {
    val context = LocalContext.current
    val showSheet = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val itineraries by viewModel.itinerary.collectAsState()
    val itineraryError by viewModel.itineraryError.collectAsState()

    var showMapSheet by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var addressDisplayString by remember { mutableStateOf("") }
    var gpsPosition by remember { mutableStateOf<LatLng?>(null) }

    val initialCameraPosition = CameraPosition.fromLatLngZoom(LatLng(41.9028, 12.4964), 5f)
    val cameraPositionState = rememberCameraPositionState { position = initialCameraPosition }
    var uiSettings by remember { mutableStateOf(MapUiSettings(zoomControlsEnabled = true)) }
    var properties by remember { mutableStateOf(MapProperties(mapType = MapType.NORMAL)) }

    var place by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isGroup by remember { mutableStateOf(true) }
    var editIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(gpsPosition) {
        gpsPosition?.let { pos ->
            val location = Location("").apply {
                latitude = pos.latitude
                longitude = pos.longitude
            }
            getAddressFromLocation(context, location) { fetchedAddress ->
                addressDisplayString = fetchedAddress ?: "Address not found"
            }
        } ?: run {
            addressDisplayString = ""
        }
    }

    Column( modifier = Modifier.fillMaxWidth()) {
        ItineraryList(
            viewModel,
            itineraries = itineraries
        ) { index, itinerary ->
            place = itinerary.place
            description = itinerary.description
            isGroup = itinerary.isGroup
            editIndex = index
            itinerary.position?.let { pos ->
                gpsPosition = LatLng(pos.latitude, pos.longitude)
                selectedLocation = LatLng(pos.latitude, pos.longitude)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(pos.latitude, pos.longitude), 15f)
            }
            showSheet.value = true
        }

        if (itineraryError != null) {
            Text(
                text = itineraryError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            onClick = {
                place = ""
                gpsPosition = null
                description = ""
                isGroup = true
                editIndex = null
                selectedLocation = null
                cameraPositionState.position = initialCameraPosition
                showSheet.value = true
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Add New Stop",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Stop"
                )
            }
        }
    }

    if (showSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showSheet.value = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (editIndex == null) "New Stop" else "Edit Stop",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                )

                OutlinedTextField(
                    value = place,
                    onValueChange = { place = it },
                    label = { Text("Stop name") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    singleLine = true,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = addressDisplayString,
                        onValueChange = { /* not editable */ },
                        label = { Text("Address") },
                        modifier = Modifier.weight(1f),
                        enabled = false
                    )

                    IconButton(onClick = {
                        if (gpsPosition != null) {
                            selectedLocation = gpsPosition
                            gpsPosition?.let { cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f) }
                        }
                        showMapSheet = true
                                         },
                        enabled = place.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Select on map"
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = !isGroup,
                        onClick = { isGroup = false },
                        label = { Text("Free") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6200EE),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Free",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    )

                    FilterChip(
                        selected = isGroup,
                        onClick = { isGroup = true },
                        label = { Text("Group") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6200EE),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Free",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    )
                }

                Button(onClick = {
                    if (place.isNotBlank() && description.isNotBlank() && gpsPosition != null) {
                        val item = ItineraryStop(
                            place = place,
                            position = gpsPosition?.let { GeoPoint(it.latitude, it.longitude) },
                            description = description,
                            isGroup = isGroup
                        )
                        if (editIndex != null) {
                            viewModel.updateItinerary(editIndex!!, item)
                        } else {
                            viewModel.addItinerary(item)
                        }

                        place = ""
                        gpsPosition = null
                        description = ""
                        editIndex = null
                        showSheet.value = false
                    }
                },
                    enabled = place.isNotBlank() && description.isNotBlank() && gpsPosition != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (editIndex == null) "Add" else "Update")
                }
            }
        }
    }

    if (showMapSheet) {
        if (selectedLocation == null) {
            GetCurrentLocation(
                onLocationFound = { latLng ->
                    if (latLng != null && selectedLocation == null) {
                        selectedLocation = latLng
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        val location = Location("").apply { latitude = latLng.latitude; longitude = latLng.longitude }
                        getAddressFromLocation(context, location) { /* maybe update a temporary display */ }
                    }
                },
                onAddressFetched = { /* Handle address if needed, maybe display temporarily */ }
            )
        }

        Dialog(
            onDismissRequest = { showMapSheet = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f),
                shape = RoundedCornerShape(16.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = properties,
                        uiSettings = uiSettings,
                        onMapClick = { latLng ->
                            selectedLocation = latLng
                            val location = Location("").apply { latitude = latLng.latitude; longitude = latLng.longitude }
                            getAddressFromLocation(context, location) { /* maybe update search bar? */ }
                        }
                    ) {
                        selectedLocation?.let { location ->
                            Marker(
                                state = MarkerState(position = location),
                                title = "Selected Location",
                                icon = BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_GREEN
                                )
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(8.dp)
                            .fillMaxWidth(fraction = 0.9f)
                    ){
                        var searchQuery by remember { mutableStateOf("") }
                        val predictions = remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
                        val searchJob = remember { mutableStateOf<Job?>(null) }

                        OutlinedTextFieldBackground {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { newText ->
                                    searchQuery = newText
                                    searchJob.value?.cancel()

                                    if (newText.length >= 3) {
                                        searchJob.value = coroutineScope.launch {
                                            delay(300)
                                            searchPlace(newText, placesClient) { results ->
                                                predictions.value = results
                                            }
                                        }
                                    } else {
                                        predictions.value = emptyList()
                                    }
                                },
                                label = {
                                    Text(
                                        text = "Search location",
                                        color = MaterialTheme.colorScheme.onSurface
                                )},
                                modifier = Modifier
                                    .fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        if (predictions.value.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                items(predictions.value) { prediction ->
                                    Text(
                                        text = prediction.getFullText(null).toString(),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val placeId = prediction.placeId
                                                searchQuery =
                                                    prediction.getFullText(null).toString()
                                                predictions.value = emptyList()

                                                val placeFields =
                                                    listOf(Field.LAT_LNG, Field.ADDRESS)
                                                val request = FetchPlaceRequest.newInstance(
                                                    placeId,
                                                    placeFields
                                                )

                                                coroutineScope.launch {
                                                    placesClient.fetchPlace(request)
                                                        .addOnSuccessListener { response ->
                                                            val placeResult = response.place
                                                            val fetchedLatLng = placeResult.location
                                                            val fetchedAddress =
                                                                placeResult.addressComponents?.toString()

                                                            if (fetchedLatLng != null) {
                                                                selectedLocation = fetchedLatLng
                                                                cameraPositionState.position =
                                                                    CameraPosition.fromLatLngZoom(
                                                                        fetchedLatLng,
                                                                        15f
                                                                    )
                                                                addressDisplayString =
                                                                    fetchedAddress
                                                                        ?: "Address not found"
                                                                searchQuery =
                                                                    fetchedAddress ?: searchQuery
                                                            }
                                                        }
                                                }
                                            }
                                            .padding(16.dp)
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            selectedLocation?.let { gpsPosition = it }
                            showMapSheet = false
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth(fraction = 0.7f),
                        enabled = selectedLocation != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.inversePrimary,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
fun OutlinedTextFieldBackground(
    content: @Composable () -> Unit
) {
    Box {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 10.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(6.dp)
                )
        )

        content()
    }
}

@Composable
fun ItineraryList(
    viewModel: TravelProposalViewModel,
    itineraries: List<ItineraryStop>,
    onEdit: (index: Int, itinerary: ItineraryStop) -> Unit = { _, _ -> },
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        itineraries.forEachIndexed { index, itinerary ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = spacedBy(8.dp)
                ) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(top = 8.dp)
                    ){
                        Text(
                            "${index+1}. " + itinerary.place,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Icon (
                            imageVector = Icons.Default.Person,
                            contentDescription = "Free",
                            modifier = Modifier.size(20.dp),
                            tint = if (itinerary.isGroup) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Icon (
                            imageVector = Icons.Default.Group,
                            contentDescription = "Group",
                            modifier = Modifier.size(22.dp),
                            tint = if (itinerary.isGroup) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    Text(
                        itinerary.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { onEdit(index, itinerary) }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { viewModel.removeItinerary(index) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.customColorsPalette.extraColorRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImagePickerSection(
    viewModel: TravelProposalViewModel
) {
    val context = LocalContext.current
    val imageUris by viewModel.imageUris.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val clipData = result.data?.clipData
        val singleUri = result.data?.data

        val selectedUris = mutableListOf<Uri>()
        if (clipData != null) {
            for (i in 0 until minOf(clipData.itemCount, 5 - imageUris.size)) {
                selectedUris.add(clipData.getItemAt(i).uri)
            }
        } else if (singleUri != null && imageUris.size < 5) {
            selectedUris.add(singleUri)
        }

        selectedUris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }

            viewModel.addImageUri(uri.toString())
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(imageUris) { image ->
                ElevatedCard(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF4FF)),
                    modifier = Modifier
                        .height(200.dp)
                        .width(200.dp),
                    onClick = { }
                ) {
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(
                                    when (image) {
                                        is TravelImage.UriImage -> image.uri
                                    }
                                )
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
                                onClick = { viewModel.removeImageUri(image) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.customColorsPalette.extraColorRed
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Always prevent picking more than 5 images, including already loaded ones (Firebase or local)
        Button(
            onClick = {
                launcher.launch(pickImagesIntent())
            },
            enabled = imageUris.size < 5,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Pick Images from Gallery (${imageUris.size}/5)")
        }
    }
}

fun pickImagesIntent(): Intent {
    return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        type  = "image/*"
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        addCategory(Intent.CATEGORY_OPENABLE)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    }
}

private fun searchPlace(
    query: String,
    placesClient: PlacesClient,
    onResults: (List<AutocompletePrediction>) -> Unit
) {
    val request = FindAutocompletePredictionsRequest.builder()
        .setQuery(query)
        .build()

    placesClient.findAutocompletePredictions(request)
        .addOnSuccessListener { response ->
            onResults(response.autocompletePredictions)
        }
        .addOnFailureListener { exception ->
            onResults(emptyList())
        }
}

@Composable
fun GetCurrentLocation(
    onLocationFound: (LatLng?) -> Unit,
    onAddressFetched: (String?) -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchLocationAndAddress(context, fusedLocationClient, onLocationFound, onAddressFetched)
        } else {
            onLocationFound(null)
            onAddressFetched("Permission denied")
        }
    }

    LaunchedEffect(Unit) {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            PermissionChecker.PERMISSION_GRANTED -> {
                fetchLocationAndAddress(context, fusedLocationClient, onLocationFound, onAddressFetched)
            }
            else -> locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}

@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
private fun fetchLocationAndAddress(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationFound: (LatLng?) -> Unit,
    onAddressFetched: (String?) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            val latLng = LatLng(location.latitude, location.longitude)
            onLocationFound(latLng)
            getAddressFromLocation(context, location) { address ->
                onAddressFetched(address)
            }
        } else {
            onLocationFound(null)
            onAddressFetched("Current location not available")
        }
    }.addOnFailureListener { exception ->
        onLocationFound(null)
        onAddressFetched("Error fetching location: ${exception.message}")
        exception.printStackTrace()
    }
}

// Function to convert location to address using Geocoder
private fun getAddressFromLocation(
    context: Context,
    location: Location,
    onAddressFetched: (String?) -> Unit
) {
    val geocoder = Geocoder(context, Locale.getDefault())
    val latitude = location.latitude
    val longitude = location.longitude

    try {
        @Suppress("DEPRECATION")
        val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0].getAddressLine(0)
            onAddressFetched(address)
        } else {
            onAddressFetched("No address found")
        }
    } catch (_: Exception) {
        onAddressFetched(null)
    }
}