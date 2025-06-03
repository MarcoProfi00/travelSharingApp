package com.example.travelsharingapp.ui.screens.travel_application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.model.ApplicationStatus
import com.example.travelsharingapp.data.model.TravelApplication
import com.example.travelsharingapp.data.repository.TravelApplicationRepository
import com.example.travelsharingapp.data.repository.TravelProposalRepository
import com.example.travelsharingapp.utils.toApplicationStatusOrNull
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TravelApplicationViewModel(
    private val applicationRepository: TravelApplicationRepository,
    private val proposalRepository: TravelProposalRepository
) : ViewModel() {

    private val _applications = MutableStateFlow<List<TravelApplication>>(emptyList())
    val applications: StateFlow<List<TravelApplication>> = _applications.asStateFlow()

    private val _proposalSpecificApplications = MutableStateFlow<List<TravelApplication>>(emptyList())
    val proposalSpecificApplications: StateFlow<List<TravelApplication>> = _proposalSpecificApplications.asStateFlow()

    private val _userSpecificApplications = MutableStateFlow<List<TravelApplication>>(emptyList())
    val userSpecificApplications: StateFlow<List<TravelApplication>> = _userSpecificApplications.asStateFlow()

    private var appListener: ListenerRegistration? = null

    init {
        loadApplications()
    }

    private fun loadApplications() {
        viewModelScope.launch {
            _applications.value = applicationRepository.getAllApplications()
        }
    }

    fun loadApplicationsForProposal(proposalId: String) {
        viewModelScope.launch {
            _proposalSpecificApplications.value = applicationRepository.getApplicationsByProposalId(proposalId)
        }
    }

    fun loadApplicationsForUser(userId: String) {
        viewModelScope.launch {
            _userSpecificApplications.value = applicationRepository.getApplicationsByUserId(userId)
        }
    }

    fun startRealtimeUpdatesForUser(userId: String) {
        if (appListener == null) {
            appListener = applicationRepository.listenApplicationsByUserId(userId) { list ->
                _applications.value = list
                _userSpecificApplications.value = list
            }
        }
    }

    fun addApplication(application: TravelApplication) {
        viewModelScope.launch {
            val newApplication = applicationRepository.addApplication(application)

            applicationRepository.addApplicationIdToProposal(
                newApplication.proposalId,
                newApplication.applicationId
            )
            applicationRepository.addApplicationIdToUser(
                newApplication.userId,
                newApplication.applicationId
            )

            updateProposalCountsAndStatus(
                proposalId = newApplication.proposalId,
                userId = newApplication.userId
            )

            if (_applications.value.none { it.applicationId == newApplication.applicationId }) {
                _applications.value = _applications.value + newApplication
            }

            delay(500)
            loadApplications()
        }
    }

    fun withdrawApplication(userId: String, proposalId: String) {
        viewModelScope.launch {
            val application = applications.value
                .find { it.userId == userId && it.proposalId == proposalId } ?: return@launch

            val originalStatus = application.status.toApplicationStatusOrNull()

            applicationRepository.removeApplication(application.applicationId)

            applicationRepository.removeApplicationIdFromProposal(proposalId, application.applicationId)
            applicationRepository.removeApplicationIdFromUser(userId, application.applicationId)

            if (originalStatus == ApplicationStatus.Pending || originalStatus == ApplicationStatus.Accepted) {
                updateProposalCountsAndStatus(
                    proposalId = proposalId,
                    userId = userId,
                    withdrawnApplicationOriginalStatus = originalStatus
                )
            }

            _applications.value = _applications.value
                .filterNot { it.applicationId == application.applicationId }

            delay(500)
            loadApplications()
        }
    }

    fun isUserApplied(userId: String, proposalId: String): Boolean {
        return applications.value.any { it.userId == userId && it.proposalId == proposalId }
    }

    fun acceptApplication(application: TravelApplication) {
        val originalStatus = application.status.toApplicationStatusOrNull()
        if (originalStatus == ApplicationStatus.Accepted) return

        viewModelScope.launch {
            applicationRepository.updateApplicationStatus(application.applicationId, ApplicationStatus.Accepted)
            updateProposalCountsAndStatus(
                application.proposalId,
                application.userId,
                acceptedApplicationOriginalStatus = originalStatus
            )
            loadApplications()
        }
    }

    fun rejectApplication(application: TravelApplication) {
        val originalStatus = application.status.toApplicationStatusOrNull()
        if (originalStatus == ApplicationStatus.Rejected) return

        viewModelScope.launch {
            applicationRepository.updateApplicationStatus(application.applicationId, ApplicationStatus.Rejected)
            updateProposalCountsAndStatus(
                application.proposalId,
                application.userId,
                rejectedApplicationOriginalStatus = originalStatus
            )
            loadApplications()
        }
    }

    private suspend fun updateProposalCountsAndStatus(
        proposalId: String,
        userId: String,
        acceptedApplicationOriginalStatus: ApplicationStatus? = null,
        rejectedApplicationOriginalStatus: ApplicationStatus? = null,
        withdrawnApplicationOriginalStatus: ApplicationStatus? = null
    ) {
        val proposal = proposalRepository.getProposalById(proposalId) ?: return
        val applicationsForProposal = applicationRepository.getApplicationsByProposalId(proposalId)
        val application = applicationsForProposal.find { it.userId == userId }

        var newParticipantsCount = proposal.participantsCount

        if (acceptedApplicationOriginalStatus == ApplicationStatus.Pending && application != null) {
            newParticipantsCount += 1 + application.accompanyingGuests.size
        } else if (
            (withdrawnApplicationOriginalStatus == ApplicationStatus.Pending || withdrawnApplicationOriginalStatus == ApplicationStatus.Accepted)
        ) {
            val guests = application?.accompanyingGuests?.size ?: 0
            newParticipantsCount -= 1 + guests
        } else if (
            (rejectedApplicationOriginalStatus == ApplicationStatus.Pending || rejectedApplicationOriginalStatus == ApplicationStatus.Accepted) && application != null
        ) {
            newParticipantsCount -= 1 + application.accompanyingGuests.size
        }

        val newPendingApplicationsCount = applicationsForProposal.count {
            it.status.toApplicationStatusOrNull() == ApplicationStatus.Pending
        }

        val newStatus = when {
            newParticipantsCount >= proposal.maxParticipants -> "Full"
            else -> "Published"
        }

        val updatedProposal = proposal.copy(
            participantsCount = newParticipantsCount.coerceAtLeast(0),
            pendingApplicationsCount = newPendingApplicationsCount,
            status = newStatus
        )
        proposalRepository.updateProposal(updatedProposal, emptyList())
    }

    suspend fun getAcceptedParticipants(proposalId: String, excludingUserId: String): List<String> {
        return applicationRepository
            .getApplicationsByProposalId(proposalId)
            .filter { it.status.toApplicationStatusOrNull() == ApplicationStatus.Accepted && it.userId != excludingUserId }
            .map { it.userId }
    }

    override fun onCleared() {
        super.onCleared()
        appListener?.remove()
    }
}

class TravelApplicationViewModelFactory(
    private val applicationRepository: TravelApplicationRepository,
    private val proposalRepository: TravelProposalRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel > create(modelClass: Class<T>): T {
        return modelClass
            .getConstructor(TravelApplicationRepository::class.java, TravelProposalRepository::class.java)
            .newInstance(applicationRepository, proposalRepository)
    }
}