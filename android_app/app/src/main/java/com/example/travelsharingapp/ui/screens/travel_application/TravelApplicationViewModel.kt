package com.example.travelsharingapp.ui.screens.travel_application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.model.ApplicationStatus
import com.example.travelsharingapp.data.model.TravelApplication
import com.example.travelsharingapp.data.repository.TravelApplicationRepository
import com.example.travelsharingapp.data.repository.TravelProposalRepository
import com.example.travelsharingapp.utils.toApplicationStatusOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TravelApplicationViewModel(
    private val applicationRepository: TravelApplicationRepository,
    private val proposalRepository: TravelProposalRepository
) : ViewModel() {
    private val _proposalSpecificApplications = MutableStateFlow<List<TravelApplication>>(emptyList())
    val proposalSpecificApplications: StateFlow<List<TravelApplication>> = _proposalSpecificApplications.asStateFlow()

    private val _userSpecificApplications = MutableStateFlow<List<TravelApplication>>(emptyList())
    val userSpecificApplications: StateFlow<List<TravelApplication>> = _userSpecificApplications.asStateFlow()

    private var userApplicationsJob: Job? = null
    private var proposalApplicationsJob: Job? = null

    fun addApplication(application: TravelApplication) {
        viewModelScope.launch {
            val proposal = proposalRepository.getProposalById(application.proposalId) ?: return@launch
            applicationRepository.addApplication(application, proposal)
        }
    }

    fun withdrawApplication(userId: String, proposalId: String) {
        viewModelScope.launch {
            val application = _proposalSpecificApplications.value
                .find { it.userId == userId && it.proposalId == proposalId } ?: return@launch
            val proposal = proposalRepository.getProposalById(proposalId) ?: return@launch

            applicationRepository.withdrawApplication(application, proposal)
        }
    }

    fun acceptApplication(application: TravelApplication) {
        viewModelScope.launch {
            val proposal = proposalRepository.getProposalById(application.proposalId) ?: return@launch
            applicationRepository.acceptApplication(application, proposal)
        }
    }

    fun rejectApplication(application: TravelApplication) {
        viewModelScope.launch {
            val proposal = proposalRepository.getProposalById(application.proposalId) ?: return@launch
            applicationRepository.rejectApplication(application, proposal)
        }
    }

    fun startListeningApplicationsForUser(userId: String) {
        if (userApplicationsJob?.isActive == true) {
            return
        }

        userApplicationsJob?.cancel()
        userApplicationsJob = viewModelScope.launch {
            applicationRepository.observeApplicationsForUser(userId)
                .collect { applicationsList ->
                    _userSpecificApplications.value = applicationsList
                }
        }
    }

    fun startListeningApplicationsForProposal(proposalId: String) {
        proposalApplicationsJob?.cancel()
        proposalApplicationsJob = viewModelScope.launch {
            applicationRepository.observeApplicationsForProposal(proposalId)
                .collect { applicationsList ->
                    _proposalSpecificApplications.value = applicationsList
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        userApplicationsJob?.cancel()
        proposalApplicationsJob?.cancel()
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