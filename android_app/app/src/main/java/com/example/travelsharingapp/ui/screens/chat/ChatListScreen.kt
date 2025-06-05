package com.example.travelsharingapp.ui.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import coil.compose.AsyncImage
import com.example.travelsharingapp.data.model.ApplicationStatus
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_application.TravelApplicationViewModel
import com.example.travelsharingapp.ui.screens.travel_proposal.TravelProposalViewModel

@Composable
fun ChatListScreen(
    userId: String,
    travelProposalViewModel: TravelProposalViewModel,
    travelApplicationViewModel: TravelApplicationViewModel,
    onNavigateToChat: (String) -> Unit,
    onNavigateBack: () -> Unit,
    topBarViewModel: TopBarViewModel
) {
    val allProposals by travelProposalViewModel.allProposals.collectAsState()
    val ownedProposals by travelProposalViewModel.ownedProposals.collectAsState()
    val allApplications by travelApplicationViewModel.applications.collectAsState()

    LaunchedEffect(userId) {
        travelApplicationViewModel.loadApplicationsForUser(userId)

        topBarViewModel.setConfig(
            title = "Chat",
            navigationIcon = {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = null
        )
    }

    val acceptedApplications = allApplications.filter {
        it.userId == userId && it.statusEnum == ApplicationStatus.Accepted
    }

    val joinedProposals = allProposals.filter { proposal ->
        acceptedApplications.any { it.proposalId == proposal.proposalId }
    }

    val chatProposals = (ownedProposals + joinedProposals)
        .distinctBy { it.proposalId }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(chatProposals) { proposal ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onNavigateToChat(proposal.proposalId) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = proposal.images.firstOrNull(),
                        contentDescription = proposal.name,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(proposal.name, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
