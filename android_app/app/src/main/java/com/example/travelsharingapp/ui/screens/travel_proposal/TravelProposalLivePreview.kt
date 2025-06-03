package com.example.travelsharingapp.ui.screens.travel_proposal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelsharingapp.data.model.ItineraryStop
import com.example.travelsharingapp.data.model.Typology
import com.example.travelsharingapp.data.model.UserProfile
import com.example.travelsharingapp.utils.toTypologyOrNull
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@Composable
fun TravelProposalLivePreview(
    proposalViewModel: TravelProposalViewModel,
    modifier: Modifier = Modifier
) {
    val name by proposalViewModel.name.collectAsState()
    val description by proposalViewModel.description.collectAsState()
    val imageUris by proposalViewModel.imageUris.collectAsState()
    val startDate by proposalViewModel.startDate.collectAsState()
    val endDate by proposalViewModel.endDate.collectAsState()
    val minPrice by proposalViewModel.minPrice.collectAsState()
    val maxPrice by proposalViewModel.maxPrice.collectAsState()
    val maxParticipants by proposalViewModel.maxParticipantsAllowed.collectAsState()
    val typologyName by proposalViewModel.typology.collectAsState()
    val itineraryStops by proposalViewModel.itinerary.collectAsState()
    val suggestedActivities by proposalViewModel.suggestedActivities.collectAsState()

    val bannerModels = remember(imageUris) {
        imageUris.mapIndexed { index, travelImage ->
            when (travelImage) {
                is TravelImage.UriImage -> BannerModel(travelImage.uri.toString(), "Preview Image ${index + 1}")
            }
        }
    }

    val scrollState = rememberScrollState()
    val firstStopWithPosition = remember(itineraryStops) {
        itineraryStops.firstNotNullOfOrNull { stop ->
            stop.position?.let { LatLng(it.latitude, it.longitude) }
        }
    }
    val homeCameraPosition = remember(firstStopWithPosition) {
        val initialLatLng = firstStopWithPosition ?: LatLng(41.902782, 12.496366)
        val initialZoom = if (firstStopWithPosition != null) 10f else 5f
        CameraPosition.fromLatLngZoom(initialLatLng, initialZoom)
    }
    val cameraPositionState = rememberCameraPositionState {
        position = homeCameraPosition
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState, enabled = !cameraPositionState.isMoving)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            name.ifBlank { "Travel Name" } + " - PREVIEW",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (bannerModels.isNotEmpty()) {
            BannerCarouselWidget(
                banners = bannerModels,
                modifier = Modifier.fillMaxWidth(),
                pageSpacing = 8.dp,
                contentPadding = PaddingValues(horizontal = 16.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text("Images will appear here", color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, "Travel dates", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${startDate?.let { date -> formatter.format(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())) } ?: "..."} - " +
                                (endDate?.let { date -> formatter.format(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())) } ?: "...")
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PriceChange, "Price range", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("€${minPrice.toInt()} - €${maxPrice.toInt()}")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Group, "Participants", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Max ${maxParticipants.ifBlank { "..." }} participants")
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            BoxWithConstraints {
                val showText = this.maxWidth > 100.dp
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (typologyName.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF9800))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    getIconForTypology(typologyName.toTypologyOrNull() ?: Typology.Adventure),
                                    contentDescription = "$typologyName type",
                                    tint = Color.White
                                )
                                if (showText) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(typologyName, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)).padding(vertical = 4.dp, horizontal = if (showText) 8.dp else 4.dp)
                    ) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            Text("Y", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        }
                        if (showText) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "You", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        if (description.isNotBlank()) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = "Description",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(description, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }

        PreviewGoogleMapCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            itineraryStops = itineraryStops,
            cameraPositionState = cameraPositionState,
            homeCameraPosition = homeCameraPosition
        )

        Text(
            text = "ITINERARY STOPS",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        if (itineraryStops.isEmpty()) {
            Text("No stops added in the itinerary.")
        } else {
            itineraryStops.forEachIndexed { index, stop ->
                ItineraryStopListItemCard(
                    itineraryStop = stop,
                    onClick = {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            stop.position?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(0.0, 0.0),
                            15f
                        )
                    }
                )
            }
        }

        Text(
            text = "SUGGESTED ACTIVITIES",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        if (suggestedActivities.isEmpty()) {
            Text("No activities listed.")
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(suggestedActivities.size) { index ->
                    AssistChip(
                        onClick = {},
                        label = { Text(suggestedActivities[index]) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PreviewGoogleMapCard(
    modifier: Modifier = Modifier,
    itineraryStops: List<ItineraryStop>,
    cameraPositionState: CameraPositionState,
    homeCameraPosition: CameraPosition
) {
    val scope = rememberCoroutineScope()

    Box(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapType = MapType.NORMAL),
                uiSettings = MapUiSettings(zoomControlsEnabled = true, mapToolbarEnabled = true)
            ) {
                itineraryStops.forEachIndexed { index, stop ->
                    stop.position?.let { pos ->
                        val markerLatLng = LatLng(pos.latitude, pos.longitude)
                        MarkerComposable(
                            state = rememberMarkerState(position = markerLatLng),
                            title = stop.place
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .padding(bottom = 12.dp)
                                        .size(12.dp),
                                    color = Color.Red
                                ) { }
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.matchParentSize()
                                )
                                Text(
                                    text = (index + 1).toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = {
                scope.launch {
                    cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(homeCameraPosition))
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Reset map view",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
