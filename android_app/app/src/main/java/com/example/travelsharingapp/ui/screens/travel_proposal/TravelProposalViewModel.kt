package com.example.travelsharingapp.ui.screens.travel_proposal

import android.util.Log
import androidx.compose.runtime.toMutableStateList
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.model.ItineraryStop
import com.example.travelsharingapp.data.model.Message
import com.example.travelsharingapp.data.model.TravelProposal
import com.example.travelsharingapp.data.repository.TravelProposalRepository
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

sealed class TravelImage {
    data class UriImage(val uri: String) : TravelImage()
}

class TravelProposalViewModel(
    private val repository: TravelProposalRepository
) : ViewModel() {

    private val _allProposals = MutableStateFlow<List<TravelProposal>>(emptyList())
    val allProposals: StateFlow<List<TravelProposal>> = _allProposals

    private val _ownedProposals = MutableStateFlow<List<TravelProposal>>(emptyList())
    val ownedProposals: StateFlow<List<TravelProposal>> = _ownedProposals

    private val _selectedProposal = MutableStateFlow<TravelProposal?>(null)
    val selectedProposal: StateFlow<TravelProposal?> = _selectedProposal

    private val _currentDetailProposalId = MutableStateFlow<String?>(null)
    val currentDetailProposalId: StateFlow<String?> = _currentDetailProposalId.asStateFlow()

    val name = MutableStateFlow("")
    val startDate = MutableStateFlow<LocalDate?>(null)
    val endDate = MutableStateFlow<LocalDate?>(null)
    val minPrice = MutableStateFlow(0f)
    val maxPrice = MutableStateFlow(10000f)
    val maxParticipantsAllowed = MutableStateFlow("")
    val typology = MutableStateFlow("")
    val description = MutableStateFlow("")
    val suggestedActivities = MutableStateFlow<List<String>>(emptyList())
    val itinerary = MutableStateFlow<List<ItineraryStop>>(emptyList())
    val organizerId = MutableStateFlow("")
    val imageUris = MutableStateFlow<List<TravelImage>>(emptyList())
    val messages = MutableStateFlow<List<Message>>(emptyList())
    val participantsCount = MutableStateFlow(0)
    val pendingApplicationsCount = MutableStateFlow(0)
    val status = MutableStateFlow("Published")

    val nameError = MutableStateFlow<String?>(null)
    val dateError = MutableStateFlow<String?>(null)
    val priceError = MutableStateFlow<String?>(null)
    val participantsError = MutableStateFlow<String?>(null)
    val typologyError = MutableStateFlow<String?>(null)
    val descriptionError = MutableStateFlow<String?>(null)
    val suggestedActivitiesError = MutableStateFlow<String?>(null)
    val itineraryError = MutableStateFlow<String?>(null)
    val imageError = MutableStateFlow<String?>(null)

    private val _applicationIds = MutableStateFlow<List<String>>(emptyList())
    val applicationIds: StateFlow<List<String>> = _applicationIds

    private val _creationSuccess = MutableStateFlow(false)
    //val creationSuccess: StateFlow<Boolean> = _creationSuccess

    private var ownedListenerJob: Job? = null
    private var exploreListenerJob: Job? = null

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun validateFields(): Boolean {
        var isValid = true

        if (name.value.isBlank()) {
            nameError.value = "Name cannot be empty"
            isValid = false
        } else nameError.value = null

        if (name.value.length < 2) {
            nameError.value = "Name must be at least 2 characters"
            isValid = false
        } else nameError.value = null

        if (startDate.value == null || endDate.value == null) {
            dateError.value = "Both start and end dates must be selected"
            isValid = false
        } else if (startDate.value!!.isAfter(endDate.value)) {
            dateError.value = "Start date must be before end date"
            isValid = false
        } else dateError.value = null

        if (startDate.value != null && startDate.value!!.isBefore(LocalDate.now())) {
            dateError.value = "Start date cannot be in the past"
            isValid = false
        }

        if (minPrice.value > maxPrice.value) {
            priceError.value = "Min price can't be higher than max price"
            isValid = false
        } else priceError.value = null

        if ((maxParticipantsAllowed.value.toIntOrNull() ?: 0) <= 0) {
            participantsError.value = "Must be at least 1 participant"
            isValid = false
        } else participantsError.value = null

        if (typology.value.isBlank()) {
            typologyError.value = "Typology cannot be empty"
            isValid = false
        } else typologyError.value = null

        if (description.value.length < 2) {
            descriptionError.value = "Description must be at least 2 characters"
            isValid = false
        } else descriptionError.value = null

        if (description.value.isBlank()) {
            descriptionError.value = "Description cannot be empty"
            isValid = false
        } else descriptionError.value = null

        if (suggestedActivities.value.isEmpty()) {
            suggestedActivitiesError.value = "At least one activity must be selected"
            isValid = false
        } else suggestedActivitiesError.value = null

        if (itinerary.value.isEmpty()) {
            itineraryError.value = "Please add at least one itinerary"
            isValid = false
        } else itineraryError.value = null

        return isValid
    }

    fun addSuggestedActivity(activity: String) {
        if (activity.isNotBlank()) {
            suggestedActivities.value = suggestedActivities.value + activity
        }
    }

    fun removeSuggestedActivity(activity: String) {
        suggestedActivities.value = suggestedActivities.value - activity
    }

    fun addItinerary(newItem: ItineraryStop) {
        itinerary.value = itinerary.value + newItem
    }

    fun updateItinerary(index: Int, updatedItem: ItineraryStop) {
        val currentList = itinerary.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = updatedItem
            itinerary.value = currentList
        }
    }

    fun removeItinerary(index: Int) {
        if (index in itinerary.value.indices) {
            itinerary.value = itinerary.value.toMutableList().apply {
                removeAt(index)
            }
        }
    }

    fun addImageUri(uri: String) {
        if (imageUris.value.size < 5) {
            imageUris.value = imageUris.value + TravelImage.UriImage(uri)
        }
    }

    fun removeImageUri(image: TravelImage) {
        imageUris.value = imageUris.value - image

        if (image is TravelImage.UriImage) {
            val url = image.uri
            if (url.startsWith("http://") || url.startsWith("https://")) {
                viewModelScope.launch {
                    try {
                        FirebaseStorage.getInstance().getReferenceFromUrl(url).delete().await()
                        Log.d("DELETE_IMAGE", "Deleted image from Storage: $url")
                    } catch (e: Exception) {
                        Log.e("DELETE_IMAGE", "Failed to delete image: $url", e)
                    }
                }
            }
        }
    }

    fun saveProposal() {
        if (!validateFields()) return

        viewModelScope.launch {
            _isLoading.value = true

            val urisToUpload = imageUris.value.mapNotNull {
                val uri = (it as? TravelImage.UriImage)?.uri
                if (!uri.isNullOrBlank() && (uri.startsWith("content://") || uri.startsWith("file://"))) {
                    uri.toUri()
                } else null
            }

            val proposal = TravelProposal(
                proposalId = "",
                name = name.value,
                startDate = Timestamp(Date.from(startDate.value!!.atStartOfDay(ZoneId.systemDefault()).toInstant())),
                endDate = Timestamp(Date.from(endDate.value!!.atStartOfDay(ZoneId.systemDefault()).toInstant())),
                minPrice = minPrice.value.toInt(),
                maxPrice = maxPrice.value.toInt(),
                maxParticipants = maxParticipantsAllowed.value.toIntOrNull() ?: 0,
                typology = typology.value,
                description = description.value,
                suggestedActivities = suggestedActivities.value,
                itinerary = itinerary.value,
                organizerId = organizerId.value,
                images = emptyList(),
                messages = messages.value,
                applicationIds = applicationIds.value,
                pendingApplicationsCount = pendingApplicationsCount.value,
                participantsCount = participantsCount.value,
                status = status.value
            )

            try {
                repository.addProposal(proposal, urisToUpload)
                _creationSuccess.value = true
            } catch (e: Exception) {
                Log.e("SaveProposal", "Error saving proposal", e)
            } finally {
                _isLoading.value = false
            }
            Log.d("UPLOAD_DEBUG", "urisToUpload = $urisToUpload")
        }
    }

    fun updateProposal(proposalId: String) {
        if (!validateFields()) return

        viewModelScope.launch {
            _isLoading.value = true

            val urisToUpload = imageUris.value.mapNotNull {
                val uri = (it as? TravelImage.UriImage)?.uri
                if (!uri.isNullOrBlank() && (uri.startsWith("content://") || uri.startsWith("file://"))) {
                    uri.toUri()
                } else null
            }

            val existingOnlineImages = imageUris.value.mapNotNull {
                val uri = (it as? TravelImage.UriImage)?.uri
                if (!uri.isNullOrBlank() && (uri.startsWith("http://") || uri.startsWith("https://"))) {
                    uri
                } else null
            }

            val updated = TravelProposal(
                proposalId = proposalId,
                name = name.value,
                startDate = Timestamp(Date.from(startDate.value!!.atStartOfDay(ZoneId.systemDefault()).toInstant())),
                endDate = Timestamp(Date.from(endDate.value!!.atStartOfDay(ZoneId.systemDefault()).toInstant())),
                minPrice = minPrice.value.toInt(),
                maxPrice = maxPrice.value.toInt(),
                maxParticipants = maxParticipantsAllowed.value.toIntOrNull() ?: 0,
                typology = typology.value,
                description = description.value,
                suggestedActivities = suggestedActivities.value,
                itinerary = itinerary.value,
                organizerId = organizerId.value,
                images = existingOnlineImages,
                messages = messages.value,
                applicationIds = applicationIds.value,
                pendingApplicationsCount = pendingApplicationsCount.value,
                participantsCount = participantsCount.value,
                status = status.value
            )

            try {
                repository.updateProposal(updated, urisToUpload)
            } catch (e: Exception) {
                Log.e("UpdateProposal", "Error updating proposal $proposalId", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProposal(proposalId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val proposal = repository.getProposalById(proposalId)

            proposal?.images?.forEach { imageUrl ->
                if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                    try {
                        FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).delete().await()
                        Log.d("DELETE_IMAGE", "Deleted image from Storage: $imageUrl")
                    } catch (e: Exception) {
                        Log.e("DELETE_IMAGE", "Failed to delete image: $imageUrl", e)
                    }
                }
            }

            repository.removeProposalById(proposalId)

            _ownedProposals.value = _ownedProposals.value.filterNot { it.proposalId == proposalId }
            _isLoading.value = false
        }
    }

    fun loadProposalToEdit(proposalId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val proposal = repository.getProposalById(proposalId)
                if (proposal != null) {
                    name.value = proposal.name
                    organizerId.value = proposal.organizerId
                    startDate.value = proposal.startDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                    endDate.value = proposal.endDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                    minPrice.value = proposal.minPrice.toFloat()
                    maxPrice.value = proposal.maxPrice.toFloat()
                    maxParticipantsAllowed.value = proposal.maxParticipants.toString()
                    typology.value = proposal.typology
                    description.value = proposal.description
                    suggestedActivities.value = proposal.suggestedActivities
                    itinerary.value = proposal.itinerary
                    imageUris.value = proposal.images.map { TravelImage.UriImage(it) }
                    messages.value = proposal.messages
                    _applicationIds.value = proposal.applicationIds
                    pendingApplicationsCount.value = proposal.pendingApplicationsCount
                    participantsCount.value = proposal.participantsCount
                    status.value = proposal.status
                    resetErrors()
                }
            } catch (e: Exception) {
                Log.e("LoadToEdit", "Error loading proposal $proposalId for edit", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadProposalToDuplicate(sourceProposalId: String, newOrganizerId: String) {
        viewModelScope.launch {

            _isLoading.value = true
            try {
                val originalProposal = repository.getProposalById(sourceProposalId)
                if (originalProposal != null) {
                    organizerId.value = newOrganizerId
                    name.value = originalProposal.name
                    startDate.value = originalProposal.startDate?.toDate()?.toInstant()
                        ?.atZone(ZoneId.systemDefault())?.toLocalDate()
                    endDate.value = originalProposal.endDate?.toDate()?.toInstant()
                        ?.atZone(ZoneId.systemDefault())?.toLocalDate()
                    minPrice.value = originalProposal.minPrice.toFloat()
                    maxPrice.value = originalProposal.maxPrice.toFloat()
                    maxParticipantsAllowed.value = originalProposal.maxParticipants.toString()
                    typology.value = originalProposal.typology
                    description.value = originalProposal.description
                    suggestedActivities.value = originalProposal.suggestedActivities.toList()
                    itinerary.value = originalProposal.itinerary.toList()
                    imageUris.value = originalProposal.images.map { TravelImage.UriImage(it) }
                        .toMutableStateList()
                    messages.value = emptyList()
                    _applicationIds.value = emptyList()
                    pendingApplicationsCount.value = 0
                    participantsCount.value = 0
                    status.value = "Published"
                    _creationSuccess.value = false
                    resetErrors()
                }
            } catch (e: Exception) {
                Log.e("LoadToEdit", "Error loading proposal $sourceProposalId for duplication", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun hasErrors(): Boolean {
        return listOf(
            nameError.value,
            dateError.value,
            priceError.value,
            participantsError.value,
            typologyError.value,
            descriptionError.value,
            suggestedActivitiesError.value,
            itineraryError.value
        ).any { it != null }

    }

    fun resetErrors() {
        nameError.value = null
        dateError.value = null
        priceError.value = null
        participantsError.value = null
        typologyError.value = null
        descriptionError.value = null
        suggestedActivitiesError.value = null
        itineraryError.value = null
        imageError.value = null
    }

    fun resetFields(organizerId: String) {
        name.value = ""
        startDate.value = null
        endDate.value = null
        minPrice.value = 0f
        maxPrice.value = 1000f
        maxParticipantsAllowed.value = "1"
        typology.value = ""
        description.value = ""
        suggestedActivities.value = emptyList()
        itinerary.value = emptyList()
        imageUris.value = emptyList()
        this.organizerId.value = organizerId
    }

    fun startListeningAllProposals() {
        if (exploreListenerJob?.isActive == true) {
            return
        }

        exploreListenerJob?.cancel()
        _isLoading.value = true

        exploreListenerJob = viewModelScope.launch {
            repository.observeAllProposals()
                .collect { proposalsList ->
                    _allProposals.value = proposalsList

                    _currentDetailProposalId.value.let { id ->
                        _selectedProposal.value = proposalsList.find { it.proposalId == id }
                    }

                    if (proposalsList.isNotEmpty() || _currentDetailProposalId.value == null) {
                        _isLoading.value = false
                    }
                }
        }
    }

    fun startListeningOwnedProposals(userId: String) {
        if (ownedListenerJob?.isActive == true) {
            return
        }

        ownedListenerJob?.cancel()
        _isLoading.value = true
        ownedListenerJob = viewModelScope.launch {
            repository.observeProposalsByOrganizer(userId)
                .collect { proposalsList ->
                    _ownedProposals.value = proposalsList.sortedByDescending { it.startDate }

                    if (proposalsList.isNotEmpty()) {
                        _isLoading.value = false
                    }
                }
        }
    }

    fun setDetailProposalId(proposalId: String?) {
        if (proposalId == null) {
            _selectedProposal.value = null
            _currentDetailProposalId.value = null
            _isLoading.value = false
            return
        }

        if (_currentDetailProposalId.value == proposalId &&
            _selectedProposal.value?.proposalId == proposalId &&
            !_isLoading.value
        ) {
            return
        }

        _currentDetailProposalId.value = proposalId

        val foundProposal = _allProposals.value.find { it.proposalId == proposalId }
        if (foundProposal != null) {
            _selectedProposal.value = foundProposal
            _isLoading.value = false
        } else {
            _selectedProposal.value = null
            _isLoading.value = true
            if (exploreListenerJob == null || !exploreListenerJob!!.isActive) {
                startListeningAllProposals()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        exploreListenerJob?.cancel()
        ownedListenerJob?.cancel()
    }
}

class TravelProposalViewModelFactory(
    private val repository: TravelProposalRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass
            .getConstructor(TravelProposalRepository::class.java)
            .newInstance(repository)
    }
}
