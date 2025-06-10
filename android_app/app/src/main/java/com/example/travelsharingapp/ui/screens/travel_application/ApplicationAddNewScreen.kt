package com.example.travelsharingapp.ui.screens.travel_application

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelsharingapp.data.model.GuestApplicant
import com.example.travelsharingapp.data.model.TravelApplication
import com.example.travelsharingapp.data.model.TravelProposal
import com.example.travelsharingapp.data.model.UserProfile
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.screens.travel_proposal.TravelProposalViewModel
import com.example.travelsharingapp.ui.screens.user_profile.UserProfileViewModel
import com.example.travelsharingapp.ui.theme.customColorsPalette
import java.time.LocalDate
import java.time.Period

@Composable
fun ApplicationAddNewScreen(
    modifier: Modifier,
    proposalId: String,
    userViewModel: UserProfileViewModel,
    travelProposalViewModel: TravelProposalViewModel,
    applicationViewModel: TravelApplicationViewModel,
    topBarViewModel: TopBarViewModel,
    onBack: () -> Unit
) {
    val userProfile by userViewModel.selectedUserProfile.collectAsState()
    val observedProposal by travelProposalViewModel.selectedProposal.collectAsState()

    var motivationMessage by remember { mutableStateOf("") }
    val guestApplicants = remember { mutableStateListOf<GuestApplicant>() }

    LaunchedEffect(proposalId) {
        travelProposalViewModel.setDetailProposalId(proposalId)
    }

    if (observedProposal == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Text("Loading proposal data...")
        }
        return
    }

    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "Trip Application",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {}
        )
    }

    val currentUser = userProfile!!
    val proposal = observedProposal!!

    val availableSlots = proposal.maxParticipants - proposal.participantsCount - 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Number of participants : ${proposal.participantsCount}/${proposal.maxParticipants}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        ApplicantInfoCard(
            userProfile = currentUser,
            motivationMessage = motivationMessage,
            onMotivationChange = { motivationMessage = it }
        )

        if (availableSlots > 0) {
            GuestApplicantsSection(
                guestApplicants = guestApplicants,
                proposal = proposal,
                onAddGuest = { guestApplicants.add(GuestApplicant()) },
                onRemoveGuest = { guest -> guestApplicants.remove(guest) },
                onGuestChange = { index, updatedGuest ->
                    if (index in guestApplicants.indices) {
                        guestApplicants[index] = updatedGuest
                    }
                }
            )
        } else {
            Text(
                "No slots for guests available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
            ) {
                Text("Back")
            }

            Button(
                onClick = {
                    val newApplication = TravelApplication(
                        proposalId = proposal.proposalId,
                        userId = currentUser.userId,
                        motivation = motivationMessage,
                        accompanyingGuests = guestApplicants.toList()
                    )
                    applicationViewModel.addApplication(newApplication)
                    onBack()
                },
                enabled = motivationMessage.isNotBlank(),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text("Apply")
            }
        }
    }
}

@Composable
fun ApplicantInfoCard(
    userProfile: UserProfile,
    motivationMessage: String,
    onMotivationChange: (String) -> Unit
) {
    Card (
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                ProfileAvatar(
                    imageSize = 80.dp,
                    user = userProfile
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${userProfile.firstName} ${userProfile.lastName}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    userProfile.birthDate?.let { timestamp ->
                        val birthDate = timestamp.toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        val age = Period.between(birthDate, LocalDate.now()).years
                        Text(text = "Age: $age", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(text = userProfile.email, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Motivation Message",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = motivationMessage,
                onValueChange = onMotivationChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                label = { Text("Why do you want to join this trip?") },
                singleLine = false,
                maxLines = 6,
                supportingText = { Text("${motivationMessage.length} / 500") },
                isError = motivationMessage.isBlank()
            )
        }
    }
}

@Composable
fun GuestApplicantsSection(
    guestApplicants: SnapshotStateList<GuestApplicant>,
    proposal: TravelProposal,
    onAddGuest: () -> Unit,
    onRemoveGuest: (GuestApplicant) -> Unit,
    onGuestChange: (Int, GuestApplicant) -> Unit
) {

    val availableSlots = proposal.maxParticipants - proposal.participantsCount - 1
    val buttonEnabled = (guestApplicants.size + proposal.participantsCount + 1) < proposal.maxParticipants

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Accompanying Guests (${guestApplicants.size}/${availableSlots})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onAddGuest,
                enabled = buttonEnabled
            ) {
                Icon(
                    Icons.Filled.AddCircle,
                    contentDescription = "Add Guest Applicant",
                    tint = if (buttonEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        if (guestApplicants.isEmpty()) {
            Text(
                "No accompanying guests added. Click the '+' icon to add one.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            guestApplicants.forEachIndexed { index, guest ->
                key(guest.id) {
                    GuestApplicantCard(
                        index = index,
                        guest = guest,
                        onNameChange = { newName ->
                            onGuestChange(index, guest.copy(name = newName))
                        },
                        onEmailChange = { newEmail ->
                            onGuestChange(index, guest.copy(email = newEmail))
                        },
                        onRemove = { onRemoveGuest(guest) }
                    )
                }
            }
        }
    }
}

@Composable
fun GuestApplicantCard(
    index: Int,
    guest: GuestApplicant,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Guest n.${index+1}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Remove Guest",
                        tint = MaterialTheme.customColorsPalette.extraColorRed
                    )
                }
            }
            OutlinedTextField(
                value = guest.name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Full Name") },
                singleLine = true
            )
            OutlinedTextField(
                value = guest.email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email Address") },
                singleLine = true
            )
        }
    }
}
