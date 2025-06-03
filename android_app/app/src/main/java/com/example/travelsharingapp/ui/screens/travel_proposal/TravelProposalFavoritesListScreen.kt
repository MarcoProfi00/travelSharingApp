package com.example.travelsharingapp.ui.screens.travel_proposal

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_application.TravelApplicationViewModel
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelProposalFavoriteListScreen(
    modifier: Modifier,
    userId: String,
    applicationViewModel: TravelApplicationViewModel,
    userViewModel: UserProfileViewModel,
    proposalViewModel: TravelProposalViewModel,
    topBarViewModel: TopBarViewModel,
    onNavigateToTravelProposalInfo: (String) -> Unit,
    onNavigateToTravelProposalEdit: (String) -> Unit,
    onBack: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val userProfile by userViewModel.selectedUserProfile.collectAsState()

    val favorites: List<String> = remember(userProfile) { userProfile?.favoriteProposals ?: emptyList() }

    val allProposals by proposalViewModel.allProposals.collectAsState()
    val favoritesProposals = remember(favorites, allProposals) {
        allProposals.filter { favorites.contains(it.proposalId) }
    }

    LaunchedEffect(Unit) {
        proposalViewModel.loadAllProposals()
    }
    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "Favorites",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = { /* No specific actions for this screen*/ }
        )
    }

    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            TravelProposalLazyList(
                modifier = modifier.padding(16.dp),
                travelProposalList = favoritesProposals,
                numGridCells = 2,
                verticalSpacing = 24.dp,
                favorites = favorites,
                userId = userId,
                applicationViewModel = applicationViewModel,
                userProfileViewModel = userViewModel,
                onNavigateToTravelProposalInfo = onNavigateToTravelProposalInfo,
                onNavigateToTravelProposalEdit = onNavigateToTravelProposalEdit
            )
        }
        else -> { // portrait mode
            TravelProposalLazyList(
                modifier = modifier.padding(16.dp),
                travelProposalList = favoritesProposals,
                numGridCells = 1,
                verticalSpacing = 24.dp,
                favorites = favorites,
                userId = userId,
                applicationViewModel = applicationViewModel,
                userProfileViewModel = userViewModel,
                onNavigateToTravelProposalInfo = onNavigateToTravelProposalInfo,
                onNavigateToTravelProposalEdit = onNavigateToTravelProposalEdit
            )
        }
    }
}