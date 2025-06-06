package com.example.travelsharingapp.ui.screens.travel_application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.model.TravelApplication
import com.example.travelsharingapp.data.repository.TravelApplicationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TravelApplicationViewModel(
    private val applicationRepository: TravelApplicationRepository
) : ViewModel() {
    private val _proposalSpecificApplications = MutableStateFlow<List<TravelApplication>>(emptyList())
    val proposalSpecificApplications: StateFlow<List<TravelApplication>> = _proposalSpecificApplications.asStateFlow()

    private val _userSpecificApplications = MutableStateFlow<List<TravelApplication>>(emptyList())
    val userSpecificApplications: StateFlow<List<TravelApplication>> = _userSpecificApplications.asStateFlow()

    private var userApplicationsJob: Job? = null
    private var proposalApplicationsJob: Job? = null

    fun addApplication(application: TravelApplication) {
        viewModelScope.launch {
            applicationRepository.addApplication(application)
        }
    }

    fun withdrawApplication(userId: String, proposalId: String) {
        viewModelScope.launch {
            val application = _proposalSpecificApplications.value
                .find { it.userId == userId && it.proposalId == proposalId } ?: return@launch

            applicationRepository.withdrawApplication(application)
        }
    }

    fun acceptApplication(application: TravelApplication) {
        viewModelScope.launch {
            applicationRepository.acceptApplication(application)
        }
    }

    fun rejectApplication(application: TravelApplication) {
        viewModelScope.launch {
            applicationRepository.rejectApplication(application)
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
    private val applicationRepository: TravelApplicationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel > create(modelClass: Class<T>): T {
        return modelClass
            .getConstructor(TravelApplicationRepository::class.java)
            .newInstance(applicationRepository)
    }
}