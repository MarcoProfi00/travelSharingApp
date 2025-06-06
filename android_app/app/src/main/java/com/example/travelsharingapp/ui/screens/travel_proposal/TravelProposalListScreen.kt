package com.example.travelsharingapp.ui.screens.travel_proposal

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NewLabel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.travelsharingapp.R
import com.example.travelsharingapp.data.model.ApplicationStatus
import com.example.travelsharingapp.data.model.ProposalStatus
import com.example.travelsharingapp.data.model.TravelApplication
import com.example.travelsharingapp.data.model.TravelProposal
import com.example.travelsharingapp.data.model.Typology
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_application.TravelApplicationViewModel
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileViewModel
import com.example.travelsharingapp.utils.shouldUseTabletLayout
import com.example.travelsharingapp.utils.toTypologyOrNull
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun TravelProposalListScreen(
    modifier: Modifier,
    userId: String,
    userViewModel: UserProfileViewModel,
    proposalViewModel: TravelProposalViewModel,
    applicationViewModel: TravelApplicationViewModel,
    topBarViewModel: TopBarViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToTravelProposalInfo: (String) -> Unit,
    onNavigateToTravelProposalEdit: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()

    val experienceTypologyFilter = remember { mutableStateListOf<String>() }
    val destinationFilter = remember { mutableStateOf<String?>(null) }
    val minPriceFilter = remember { mutableStateOf<Int?>(null) }
    val maxPriceFilter = remember { mutableStateOf<Int?>(null) }
    val durationFilter = remember { mutableStateOf<Int?>(null) }
    val selectedStartDateFilter = remember { mutableStateOf<LocalDate?>(null) }
    val selectedEndDateFilter = remember { mutableStateOf<LocalDate?>(null) }
    val numParticipantsFilter = remember { mutableStateOf<Int?>(null) }

    var applyMainFilters by remember { mutableStateOf(false) }
    var applyAdvancedFilters by remember { mutableStateOf(false) }
    var filterTrigger by remember { mutableIntStateOf(0) }
    var showAdvancedFiltersSheet by remember { mutableStateOf(false) }
    val advancedFiltersSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val searchBarHeight = 80.dp
    var searchBarExpanded by remember { mutableStateOf(false) }

    val isTablet = shouldUseTabletLayout()

    val proposalsAreLoading by proposalViewModel.isLoading.collectAsState()
    val allProposals by proposalViewModel.allProposals.collectAsState()
    val userProfile by userViewModel.selectedUserProfile.collectAsState()
    val userApplications by applicationViewModel.userSpecificApplications.collectAsState()

    LaunchedEffect(Unit) {
        proposalViewModel.startListeningAllProposals()
        proposalViewModel.startListeningOwnedProposals(userId)
        applicationViewModel.startListeningApplicationsForUser(userId)
    }

    if (proposalsAreLoading || userProfile == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Text("Loading proposals...",
                style = MaterialTheme.typography.titleMedium
            )
        }
        return
    }

    val filteredProposals = remember(allProposals) {
        allProposals.filter { it.organizerId != userId && it.status != "Concluded" }
    }

    val favorites: List<String> = remember(userProfile) { userProfile?.favoriteProposals ?: emptyList() }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    val textFieldState = remember { TextFieldState() }
    val searchResults = remember(filteredProposals, textFieldState.text) {
        if (textFieldState.text.toString().isBlank()) {
            emptyList()
        } else
            filteredProposals.filter { proposal ->
                    proposal.name.contains(textFieldState.text.toString(), ignoreCase = true) ||
                            ((textFieldState.text.length > 3) &&
                                    proposal.description.contains(textFieldState.text.toString(), ignoreCase = true))
            }
    }

    val filteredTravelProposalList = remember(
        filteredProposals,
        applyMainFilters,
        applyAdvancedFilters,
        textFieldState.text,
        filterTrigger,
        experienceTypologyFilter,
        showOnlyFavorites,
        favorites
    ) {
        val baseList = if (textFieldState.text.toString().isNotBlank()) {
            searchResults
        } else {
            filteredProposals
        }

        val favoritesFilteredList = if (showOnlyFavorites) {
            baseList.filter { proposal -> favorites.contains(proposal.proposalId) }
        } else {
            baseList
        }

        if (applyMainFilters || applyAdvancedFilters) {
            favoritesFilteredList.filter { proposal ->
                val typologyMatch = experienceTypologyFilter.isEmpty() ||
                        experienceTypologyFilter.any { it.equals(proposal.typology, ignoreCase = true) }

                val destinationMatch = destinationFilter.value.isNullOrBlank() ||
                        proposal.name.contains(destinationFilter.value!!, ignoreCase = true)

                val priceMatch = (minPriceFilter.value == null && maxPriceFilter.value == null) ||
                        (proposal.minPrice >= minPriceFilter.value!! && proposal.minPrice <= maxPriceFilter.value!!)

                val startDateMatch = selectedStartDateFilter.value == null ||
                        (proposal.startDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.MIN) >= selectedStartDateFilter.value!!

                val endDateMatch = selectedEndDateFilter.value == null ||
                        (proposal.endDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.MAX) <= selectedEndDateFilter.value!!

                val durationMatch = durationFilter.value == null || run {
                    val start = proposal.startDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                    val end = proposal.endDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                    if (start != null && end != null) {
                        val days = ChronoUnit.DAYS.between(start, end) + 1
                        days.toInt() <= durationFilter.value!!
                    } else false
                }

                val participantsMatch = numParticipantsFilter.value == null ||
                        proposal.maxParticipants <= numParticipantsFilter.value!!

                typologyMatch && destinationMatch && priceMatch &&
                        startDateMatch && endDateMatch && durationMatch && participantsMatch
            }
        } else {
            favoritesFilteredList
        }
    }

    val numberAdvancedFilterApplied by remember {
        derivedStateOf {
            var count = 0

            if (experienceTypologyFilter.isNotEmpty()) { count++ }
            if (!destinationFilter.value.isNullOrBlank()) { count++ }
            if (minPriceFilter.value != null || maxPriceFilter.value != null ) {  count++ }
            if (selectedStartDateFilter.value != null || selectedEndDateFilter.value != null) { count++ }
            if (durationFilter.value != null) { count++ }
            if (numParticipantsFilter.value != null) { count++ }

            count
        }
    }

    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "All Travel Proposals",
            navigationIcon = { /* nothing */ },
            actions = {
                IconButton(onClick = { onNavigateToChat() }) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat")
                }
            },
            floatingActionButton = { /* nothing */ }
        )
    }

    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = searchBarHeight)
                ) {
                    TravelProposalLazyList(
                        travelProposalList = filteredTravelProposalList,
                        numGridCells = if (isTablet) 4 else 3,
                        favorites = favorites,
                        userId = userId,
                        userProfileViewModel = userViewModel,
                        userApplications = userApplications,
                        onNavigateToTravelProposalInfo = onNavigateToTravelProposalInfo,
                        onNavigateToTravelProposalEdit = onNavigateToTravelProposalEdit
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(start = 0.4f.dp * configuration.screenWidthDp, top = 16.dp, end = 16.dp)
                ) {
                    AssistChip(
                        onClick = { showAdvancedFiltersSheet = true },
                        label = { Text("Advanced" + if (numberAdvancedFilterApplied == 0) "" else ": $numberAdvancedFilterApplied") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.FilterAlt,
                                contentDescription = "Advanced Filters",
                                Modifier.size(AssistChipDefaults.IconSize)
                            )
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.onPrimary,
                            leadingIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )

                    IconToggleButton(
                        checked = showOnlyFavorites,
                        onCheckedChange = { showOnlyFavorites = it }
                    ) {
                        Icon(
                            imageVector = if (showOnlyFavorites) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Filter Favorites",
                            tint = if (showOnlyFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TravelProposalListFilters(
                        selectedFilters = experienceTypologyFilter
                            .mapNotNull { it.toTypologyOrNull() }
                            .toMutableStateList(),
                        onFilterToggle = { filter, isSelected ->
                            if (isSelected) {
                                experienceTypologyFilter.add(filter.name)
                            } else {
                                experienceTypologyFilter.remove(filter.name)
                            }
                            applyMainFilters = experienceTypologyFilter.isNotEmpty()
                        }
                    )
                }

                // Over all the other content but under the search bar
                if (isTablet && searchBarExpanded) {
                    Box(
                        modifier = modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                searchBarExpanded = false
                            }
                    ) {
                        // No content
                    }
                }

                SearchBarSuggestions(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth(fraction = if (!searchBarExpanded || isTablet) 0.4f else 1f)
                        .padding(top = if (isTablet) 12.dp else 0.dp)
                        .padding(horizontal = if (isTablet && searchBarExpanded) 16.dp else 0.dp),
                    textFieldState = textFieldState,
                    searchResults = searchResults,
                    expanded = searchBarExpanded,
                    onExpandedChange = { searchBarExpanded = it },
                    isTablet = isTablet
                )
            }
        }
        else -> { // portrait mode
            Box(
                modifier = modifier
                    .fillMaxSize()
            ) {
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(top = searchBarHeight)
                ) {
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        AssistChip(
                            onClick = { showAdvancedFiltersSheet = true },
                            label = { Text("Advanced" + if (numberAdvancedFilterApplied == 0) "" else ": $numberAdvancedFilterApplied") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.FilterAlt,
                                    contentDescription = "Advanced Filters",
                                    Modifier.size(AssistChipDefaults.IconSize)
                                )

                            },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                labelColor = MaterialTheme.colorScheme.onPrimary,
                                leadingIconContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )

                        IconToggleButton(
                            checked = showOnlyFavorites,
                            onCheckedChange = { showOnlyFavorites = it }
                        ) {
                            Icon(
                                imageVector = if (showOnlyFavorites) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Filter Favorites",
                                tint = if (showOnlyFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TravelProposalListFilters(
                            selectedFilters = experienceTypologyFilter
                                .mapNotNull { it.toTypologyOrNull() }
                                .toMutableStateList(),
                            onFilterToggle = { filter, isSelected ->
                                if (isSelected) {
                                    experienceTypologyFilter.add(filter.name)
                                } else {
                                    experienceTypologyFilter.remove(filter.name)
                                }
                                applyMainFilters = experienceTypologyFilter.isNotEmpty()
                            }
                        )
                    }

                    TravelProposalLazyList(
                        travelProposalList = filteredTravelProposalList,
                        numGridCells = if(isTablet) 3 else 2,
                        favorites = favorites,
                        userId = userId,
                        userProfileViewModel = userViewModel,
                        userApplications = userApplications,
                        onNavigateToTravelProposalInfo = onNavigateToTravelProposalInfo,
                        onNavigateToTravelProposalEdit = onNavigateToTravelProposalEdit
                    )
                }

                // Over all the other content but under the search bar
                if (isTablet && searchBarExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                searchBarExpanded = false
                            }
                    ) {
                        // No content
                    }
                }

                SearchBarSuggestions(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(top = if (isTablet) 16.dp else 0.dp)
                        .padding(horizontal = if (isTablet && searchBarExpanded) 16.dp else 0.dp),
                    textFieldState = textFieldState,
                    searchResults = searchResults,
                    expanded = searchBarExpanded,
                    onExpandedChange = { searchBarExpanded = it },
                    isTablet = isTablet
                )
            }
        }
    }

    AdvancedFilterModal(
        showAdvancedFiltersSheet = showAdvancedFiltersSheet,
        advancedFiltersSheetState = advancedFiltersSheetState,
        destinationState = destinationFilter,
        minPriceState = minPriceFilter,
        maxPriceState = maxPriceFilter,
        selectedTypologyState = experienceTypologyFilter,
        selectedStartDateState = selectedStartDateFilter,
        selectedEndDateState = selectedEndDateFilter,
        maxDurationState = durationFilter,
        numParticipantsState = numParticipantsFilter,
        onDismiss = { showAdvancedFiltersSheet = false },
        onReset = {
            applyAdvancedFilters = false
            filterTrigger++
            destinationFilter.value = null
            minPriceFilter.value = null
            maxPriceFilter.value = null
            durationFilter.value = null
            selectedStartDateFilter.value = null
            selectedEndDateFilter.value = null
            numParticipantsFilter.value = null
            experienceTypologyFilter.clear()
            scope.launch { advancedFiltersSheetState.hide() }.invokeOnCompletion {
                if (!advancedFiltersSheetState.isVisible) {
                    showAdvancedFiltersSheet = false
                }
            }
        },
        onApply = {
            applyAdvancedFilters = true
            filterTrigger++
            scope.launch { advancedFiltersSheetState.hide() }.invokeOnCompletion {
                if (!advancedFiltersSheetState.isVisible) {
                    showAdvancedFiltersSheet = false
                }
            }
        }
    )
}

@Composable
fun FilterSection(
    title: String,
    onReset: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.padding(top = 24.dp, bottom = 4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onReset, contentPadding = PaddingValues(horizontal = 4.dp)) {
                Text("Reset")
            }
        }

        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
object FutureSelectableDates: SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return utcTimeMillis >= System.currentTimeMillis()
    }

    override fun isSelectableYear(year: Int): Boolean {
        return year >= LocalDate.now().year
    }
}

fun Long.customToLocalDate(): LocalDate = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdvancedFilterModal(
    showAdvancedFiltersSheet: Boolean,
    advancedFiltersSheetState: SheetState,
    destinationState: MutableState<String?>,
    minPriceState: MutableState<Int?>,
    maxPriceState: MutableState<Int?>,
    selectedTypologyState: MutableList<String>,
    selectedStartDateState: MutableState<LocalDate?>,
    selectedEndDateState: MutableState<LocalDate?>,
    maxDurationState: MutableState<Int?>,
    numParticipantsState: MutableState<Int?>,
    onDismiss: () -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit
) {
    var showDateRangePicker by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()
    val showTopFade by remember { derivedStateOf { lazyListState.canScrollBackward } }
    val showBottomFade by remember { derivedStateOf { lazyListState.canScrollForward } }
    val sheetBackgroundColor = MaterialTheme.colorScheme.surface

    val initialSelectedStartDateMillis = selectedStartDateState.value?.atStartOfDay()?.atZone(ZoneOffset.systemDefault())?.toInstant()?.toEpochMilli()
    val initialSelectedEndDateMillis = selectedEndDateState.value?.atStartOfDay()?.atZone(ZoneOffset.systemDefault())?.toInstant()?.toEpochMilli()

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialSelectedStartDateMillis,
        initialSelectedEndDateMillis = initialSelectedEndDateMillis,
        selectableDates = FutureSelectableDates
    )

    val priceSliderRange = 0f..5000f
    var currentPriceRange by remember(minPriceState.value, maxPriceState.value) {
        val start = minPriceState.value?.toFloat() ?: priceSliderRange.start
        val end = maxPriceState.value?.toFloat() ?: priceSliderRange.endInclusive
        mutableStateOf(start..end)
    }

    val participantsSliderRange = 1f..20f
    var currentParticipants by remember(numParticipantsState.value) {
        val curr = numParticipantsState.value?.toFloat() ?: participantsSliderRange.start
        mutableFloatStateOf(curr)
    }

    if (showAdvancedFiltersSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = advancedFiltersSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(horizontal = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Advanced Filters",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 16.dp)
                ) {

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Destination filter
                        item {
                            FilterSection(
                                title = "Destination",
                                onReset = { destinationState.value = null }
                            ) {
                                OutlinedTextField(
                                    value = destinationState.value ?: "",
                                    onValueChange = { destinationState.value = it.ifBlank { null } },
                                    label = { Text("Search Destination") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                                )
                            }
                        }

                        // Price range filter
                        item {
                            FilterSection(
                                title = "Price Range",
                                onReset = {
                                    minPriceState.value = null
                                    maxPriceState.value = null
                                    currentPriceRange = priceSliderRange.start..priceSliderRange.endInclusive
                                }
                            ) {
                                RangeSlider(
                                    value = currentPriceRange,
                                    onValueChange = { currentPriceRange = it },
                                    valueRange = priceSliderRange,
                                    onValueChangeFinished = {
                                        minPriceState.value = currentPriceRange.start.roundToInt()
                                        maxPriceState.value = currentPriceRange.endInclusive.roundToInt()
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("€${currentPriceRange.start.roundToInt()}")
                                    Text("€${currentPriceRange.endInclusive.roundToInt()}")
                                }
                            }
                        }

                        // Experience typology filter
                        item {
                            FilterSection(
                                title = "Experience Typology",
                                onReset = { selectedTypologyState.clear() }
                            ) {
                                val filters = Typology.entries.map { it.name }
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =  Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (filters.isEmpty()) {
                                        Text("No typology filters selected yet.", style = MaterialTheme.typography.bodySmall)
                                    } else {
                                        filters.forEach { filter ->
                                            FilterChipSelectable(
                                                onClick = {
                                                    if (selectedTypologyState.contains(filter)) {
                                                        selectedTypologyState.remove(filter)
                                                    } else {
                                                        selectedTypologyState.add(filter)
                                                    }
                                                },
                                                isSelected = selectedTypologyState.contains(filter),
                                                label = filter
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Date range filter
                        item {
                            FilterSection(
                                title = "Date Range",
                                onReset = {
                                    selectedStartDateState.value = null
                                    selectedEndDateState.value = null
                                    dateRangePickerState.setSelection(null, null)
                                }
                            ) {
                                val startText = initialSelectedStartDateMillis?.customToLocalDate()?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                                val endText = initialSelectedEndDateMillis?.customToLocalDate()?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                                OutlinedButton(
                                    onClick = { showDateRangePicker = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date Range", modifier = Modifier.size(ButtonDefaults.IconSize))
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(if(initialSelectedStartDateMillis != null || initialSelectedEndDateMillis != null) "$startText - $endText" else "Select Dates")
                                }
                            }
                        }

                        // Max duration (days) filter
                        item {
                            FilterSection(
                                title = "Max Duration",
                                onReset = { maxDurationState.value = null }
                            ) {
                                OutlinedTextField(
                                    value = maxDurationState.value?.toString() ?: "",
                                    onValueChange = { input ->
                                        maxDurationState.value = input.toIntOrNull()?.coerceAtLeast(1)
                                    },
                                    label = { Text("Number of days") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }

                        // Group size filter
                        item {
                            FilterSection(
                                title = "Max Group Size",
                                onReset = {
                                    numParticipantsState.value = null
                                    currentParticipants = participantsSliderRange.start
                                }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Slider(
                                        value = currentParticipants,
                                        steps = 20,
                                        onValueChange = { currentParticipants = it },
                                        valueRange = participantsSliderRange,
                                        onValueChangeFinished = {
                                            numParticipantsState.value = currentParticipants.roundToInt()
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = (if (numParticipantsState.value == null) "Any" else currentParticipants.roundToInt()).toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier
                                            .width(40.dp)
                                            .padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (showTopFade) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .height(32.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(sheetBackgroundColor, Color.Transparent)
                                    )
                                )
                        ) {}
                    }

                    if (showBottomFade) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, sheetBackgroundColor)
                                    )
                                )
                        ) {}
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onReset,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Reset All")
                    }
                    Button(
                        onClick = onApply,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Apply Filters")
                    }
                }
            }

            if (showDateRangePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDateRangePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDateRangePicker = false
                                selectedStartDateState.value = dateRangePickerState.selectedStartDateMillis?.customToLocalDate()
                                selectedEndDateState.value = dateRangePickerState.selectedEndDateMillis?.customToLocalDate()
                            },
                            enabled = dateRangePickerState.selectedStartDateMillis != null &&
                                    dateRangePickerState.selectedEndDateMillis != null &&
                                    (dateRangePickerState.selectedEndDateMillis ?: 0) >=
                                    (dateRangePickerState.selectedStartDateMillis ?: 0)
                        ) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDateRangePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DateRangePicker(
                        state = dateRangePickerState,
                        modifier = Modifier.weight(1f),
                        title = null,
                        headline = null
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipSelectable(
    onClick: () -> Unit,
    isSelected: Boolean = false,
    label: String,
) {
    FilterChip(
        onClick = { onClick() },
        label = {
            Text(label)
        },
        selected = isSelected,
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Done icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TravelProposalListFilters(
    selectedFilters: MutableList<Typology> = mutableStateListOf(),
    onFilterToggle: (filter: Typology, isSelected: Boolean) -> Unit
) {
    val filters = Typology.entries

    LazyRow (
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        horizontalArrangement =  Arrangement.spacedBy(8.dp),
    ) {
        items(
            count = filters.size,
            key = { index -> filters[index].hashCode() },
            itemContent = { index ->
                val filter = filters[index]
                FilterChipSelectable(
                    onClick = {
                        if (selectedFilters.contains(filter)) {
                            selectedFilters.remove(filter)
                            onFilterToggle(filter, false)
                        } else {
                            selectedFilters.add(filter)
                            onFilterToggle(filter, true)
                        }
                    },
                    isSelected = filter in selectedFilters,
                    label = filter.toString()
                )
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TravelProposalLazyList(
    travelProposalList: List<TravelProposal>,
    favorites: List<String>,
    userId: String,
    numGridCells: Int = 2,
    verticalSpacing: Dp = 12.dp,
    modifier: Modifier = Modifier,
    userProfileViewModel: UserProfileViewModel,
    userApplications: List<TravelApplication>,
    onNavigateToTravelProposalInfo: (String) -> Unit,
    onNavigateToTravelProposalEdit: (String) -> Unit
) {
    if (travelProposalList.isNotEmpty()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(numGridCells),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
            modifier = modifier
                .fillMaxSize()
        ) {
            items(
                count = travelProposalList.size,
                key = { index -> travelProposalList[index].proposalId },
                itemContent = { index ->
                    val travelProposal = travelProposalList[index]
                    val application = userApplications.find { it.proposalId == travelProposal.proposalId }
                    TravelProposalCard(
                        travelProposal = travelProposal,
                        favorites = favorites,
                        userId = userId,
                        applicationStatus = application?.statusEnum,
                        modifier = Modifier.fillMaxWidth(),
                        onAddFavorite = { userProfileViewModel.addFavorite(travelProposal.proposalId) },
                        onRemoveFavorite = { userProfileViewModel.removeFavorite(travelProposal.proposalId) },
                        onCardClick = {
                            onNavigateToTravelProposalInfo(travelProposal.proposalId)
                        },
                        onEditClick = {
                            onNavigateToTravelProposalEdit(travelProposal.proposalId)
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun FavoriteButton(
    modifier: Modifier = Modifier,
    isInFavoriteList: Boolean,
    isUserTravelProposal: Boolean,
    onClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(isInFavoriteList) }

    IconToggleButton(
        checked = isFavorite,
        onCheckedChange = {
            isFavorite = !isFavorite
            onClick()
        }
    ) {
        if (!isUserTravelProposal) {
            if (isFavorite) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFDB0707),
                    modifier = modifier.graphicsLayer {
                        scaleX = 1.8f
                        scaleY = 1.8f
                    }
                )
            } else {
                Box {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color.LightGray.copy(alpha = 0.5f),
                        modifier = modifier.graphicsLayer {
                            scaleX = 1.8f
                            scaleY = 1.8f
                        }
                    )
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = modifier.graphicsLayer {
                            scaleX = 1.8f
                            scaleY = 1.8f
                        }
                    )
                }
            }
        } else {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = Color.White,
                modifier = modifier.graphicsLayer {
                    scaleX = 1.8f
                    scaleY = 1.8f
                }
            )
        }
    }
}

@Composable
fun TravelProposalCard(
    travelProposal: TravelProposal,
    favorites: List<String>,
    userId: String,
    applicationStatus: ApplicationStatus?,
    modifier: Modifier,
    onAddFavorite: () -> Unit,
    onRemoveFavorite: () -> Unit,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit
) {
    ElevatedCard (
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = modifier.height(310.dp),
        onClick = {
            onCardClick()
        }
    ){
        Column {
            Box {
                if (travelProposal.images.isNotEmpty()) {
                    val banners = travelProposal.images.mapIndexed { index, item ->
                        BannerModel(
                            imageUrl = item,
                            contentDescription = "Banner ${index + 1}"
                        )
                    }

                    BannerCarouselWidget(
                        banners = banners,
                        modifier = Modifier.fillMaxWidth(),
                        pageSpacing = 0.dp,
                        contentPadding = PaddingValues(horizontal = 0.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.Gray.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = R.drawable.placeholder_error,
                            contentDescription = "Placeholder image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .align(Alignment.TopEnd),
                    color = Color.Transparent
                ) {
                    FavoriteButton(
                        modifier = Modifier.padding(8.dp),
                        isInFavoriteList = favorites.contains(travelProposal.proposalId),
                        isUserTravelProposal = travelProposal.organizerId == userId,
                        onClick = {
                            if (travelProposal.organizerId == userId) {
                                onEditClick()
                            } else {
                                if (favorites.contains(travelProposal.proposalId)) {
                                    onRemoveFavorite()
                                } else {
                                    onAddFavorite()
                                }
                            }
                        }
                    )
                }

                if (applicationStatus != null) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            applicationStatus.name,
                            color = when (applicationStatus) {
                                ApplicationStatus.Accepted -> Color(0xFF4CAF50)
                                ApplicationStatus.Pending -> Color(0xFFFF9800)
                                ApplicationStatus.Rejected -> Color(0xFFF44336)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = travelProposal.name,
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 2.dp)
                    .padding(horizontal = 8.dp)
                    .basicMarquee(animationMode = MarqueeAnimationMode.Immediately),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            val start = travelProposal.startDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
            val end = travelProposal.endDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
            val numDaysOfTravel = if (start != null && end != null) ChronoUnit.DAYS.between(start, end) + 1 else 0
            Text(
                text = (travelProposal.startDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                    ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) ?: "") +
                        " - " + numDaysOfTravel + " days",
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    imageVector = getIconForTypology(travelProposal.typologyEnum ?: Typology.Adventure),
                    contentDescription = travelProposal.typologyEnum?.name ?: travelProposal.typology
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 2.dp),
                        text = ("from"),
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic
                    )

                    Text(
                        text = (travelProposal.minPrice.toString() + "€"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarSuggestions(
    modifier : Modifier = Modifier,
    textFieldState: TextFieldState,
    searchResults: List<TravelProposal>,
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit,
    isTablet: Boolean,
) {

    val searchBarPadding by animateDpAsState(
        targetValue = if(expanded) 0.dp else 16.dp,
        label = "Search bar padding"
    )

    Box(modifier = modifier
        .fillMaxWidth()
        .semantics { isTraversalGroup = true }
    ) {
        if (!isTablet) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .semantics { traversalIndex = 0f }
                    .padding(horizontal = searchBarPadding),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = textFieldState.text.toString(),
                        onQueryChange = { textFieldState.edit { replace(0, length, it) } },
                        onSearch = { onExpandedChange(false)},
                        expanded = expanded,
                        onExpandedChange = { onExpandedChange(it) },
                        placeholder = {
                            Text(
                                "Search Destinations...",
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (textFieldState.text.isEmpty().not()) {
                                IconButton(onClick = {
                                    onExpandedChange(false)
                                    textFieldState.clearText()
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            }
                        }
                    )
                },
                expanded = expanded,
                onExpandedChange = { onExpandedChange(it) },
                shadowElevation = 4.dp,
                tonalElevation = 4.dp,
                windowInsets = WindowInsets(0.dp),
            ) {
                // Display search results in a scrollable column
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    searchResults.forEach { result ->
                        ListItem(
                            headlineContent = { Text(result.name) },
                            modifier = Modifier
                                .clickable {
                                    textFieldState.edit { replace(0, length, result.name) }
                                    onExpandedChange(false)
                                }
                                .fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            DockedSearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .semantics { traversalIndex = 0f }
                    .padding(horizontal = searchBarPadding),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = textFieldState.text.toString(),
                        onQueryChange = { textFieldState.edit { replace(0, length, it) } },
                        onSearch = { onExpandedChange(false)},
                        expanded = expanded,
                        onExpandedChange = { onExpandedChange(it) },
                        placeholder = {
                            Text(
                                "Search Destinations...",
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (textFieldState.text.isEmpty().not()) {
                                IconButton(onClick = {
                                    onExpandedChange(false)
                                    textFieldState.clearText()
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            }
                        }
                    )
                },
                expanded = expanded,
                onExpandedChange = { onExpandedChange(it) },
                shadowElevation = 4.dp,
                tonalElevation = 4.dp,
                //windowInsets = WindowInsets(0.dp),
            ) {
                // Display search results in a scrollable column
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    searchResults.forEach { result ->
                        ListItem(
                            headlineContent = { Text(result.name) },
                            modifier = Modifier
                                .clickable {
                                    textFieldState.edit { replace(0, length, result.name) }
                                    onExpandedChange(false)
                                }
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun getIconForTypology(typology: Typology): ImageVector {
    return when (typology) {
        Typology.Adventure -> Icons.Filled.TravelExplore
        Typology.Relax -> Icons.Filled.BeachAccess
        Typology.Cultural -> Icons.Filled.Book
        Typology.Nature -> Icons.Filled.Hiking
        Typology.Luxury -> Icons.Filled.Hotel
        Typology.Sport -> Icons.AutoMirrored.Filled.DirectionsBike
        Typology.CityBreak -> Icons.Filled.LocationCity
        Typology.RoadTrip -> Icons.Filled.Map
    }
}

@Composable
fun getIconForTravelStatus(status: ProposalStatus): ImageVector {
    return when (status) {
        ProposalStatus.Published -> Icons.Default.NewLabel
        ProposalStatus.Full -> Icons.Default.Close
        ProposalStatus.Concluded -> Icons.Default.HistoryToggleOff
    }
}