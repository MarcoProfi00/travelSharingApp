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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDate: StateFlow<LocalDate?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<LocalDate?>(null)
    val endDate: StateFlow<LocalDate?> = _endDate.asStateFlow()

    private val _minPrice = MutableStateFlow(0f)
    val minPrice: StateFlow<Float> = _minPrice.asStateFlow()

    private val _maxPrice = MutableStateFlow(10000f)
    val maxPrice: StateFlow<Float> = _maxPrice.asStateFlow()

    private val _maxParticipantsAllowed = MutableStateFlow("")
    val maxParticipantsAllowed: StateFlow<String> = _maxParticipantsAllowed.asStateFlow()

    private val _typology = MutableStateFlow("")
    val typology: StateFlow<String> = _typology.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _suggestedActivities = MutableStateFlow<List<String>>(emptyList())
    val suggestedActivities: StateFlow<List<String>> = _suggestedActivities.asStateFlow()

    private val _itinerary = MutableStateFlow<List<ItineraryStop>>(emptyList())
    val itinerary: StateFlow<List<ItineraryStop>> = _itinerary.asStateFlow()

    private val _organizerId = MutableStateFlow("")
    val organizerId: StateFlow<String> = _organizerId.asStateFlow()

    private val _imageUris = MutableStateFlow<List<TravelImage>>(emptyList())
    val imageUris: StateFlow<List<TravelImage>> = _imageUris.asStateFlow()

    val messages = MutableStateFlow<List<Message>>(emptyList())
    val participantsCount = MutableStateFlow(0)
    val pendingApplicationsCount = MutableStateFlow(0)
    val status = MutableStateFlow("Published")

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _dateError = MutableStateFlow<String?>(null)
    val dateError: StateFlow<String?> = _dateError.asStateFlow()

    private val _priceError = MutableStateFlow<String?>(null)
    val priceError: StateFlow<String?> = _priceError.asStateFlow()

    private val _participantsError = MutableStateFlow<String?>(null)
    val participantsError: StateFlow<String?> = _participantsError.asStateFlow()

    private val _typologyError = MutableStateFlow<String?>(null)
    val typologyError: StateFlow<String?> = _typologyError.asStateFlow()

    private val _descriptionError = MutableStateFlow<String?>(null)
    val descriptionError: StateFlow<String?> = _descriptionError.asStateFlow()

    private val _suggestedActivitiesError = MutableStateFlow<String?>(null)
    val suggestedActivitiesError: StateFlow<String?> = _suggestedActivitiesError.asStateFlow()

    private val _itineraryError = MutableStateFlow<String?>(null)
    val itineraryError: StateFlow<String?> = _itineraryError.asStateFlow()

    private val _imageError = MutableStateFlow<String?>(null)
    val imageError: StateFlow<String?> = _imageError.asStateFlow()

    private val _applicationIds = MutableStateFlow<List<String>>(emptyList())
    val applicationIds: StateFlow<List<String>> = _applicationIds

    private val _creationSuccess = MutableStateFlow(false)
    val creationSuccess: StateFlow<Boolean> = _creationSuccess

    private var ownedListenerJob: Job? = null
    private var exploreListenerJob: Job? = null

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun onNameChange(newName: String) { _name.value = newName }
    fun onDescriptionChange(newDescription: String) {
        if (newDescription.length <= 5000) _description.value = newDescription
    }
    fun onTypologyChange(newTypology: String) { _typology.value = newTypology }
    fun onDatesChange(newStart: LocalDate?, newEnd: LocalDate?) {
        _startDate.value = newStart
        _endDate.value = newEnd
    }
    fun onMinPriceChange(newMinPrice: Float) {
        _minPrice.value = newMinPrice
        if (newMinPrice > _maxPrice.value) _maxPrice.value = newMinPrice
    }
    fun onMaxPriceChange(newMaxPrice: Float) {
        _maxPrice.value = newMaxPrice
        if (newMaxPrice < _minPrice.value) _minPrice.value = newMaxPrice
    }
    fun onMaxParticipantsChange(count: String) { _maxParticipantsAllowed.value = count }

    init {
        _name.debounce(300L).onEach { performNameValidation(it) }.launchIn(viewModelScope)
        _description.debounce(300L).onEach { performDescriptionValidation(it) }.launchIn(viewModelScope)
        _typology.onEach { performTypologyValidation(it) }.launchIn(viewModelScope)
        combine(_startDate, _endDate) { start, end -> Pair(start, end) }
            .onEach { (start, end) -> performDateValidation(start, end) }
            .launchIn(viewModelScope)
        combine(_minPrice, _maxPrice) { min, max -> Pair(min, max) }
            .debounce(300L)
            .onEach { (min, max) -> performPriceValidation(min, max) }
            .launchIn(viewModelScope)
        _maxParticipantsAllowed.debounce(300L).onEach { performParticipantsValidation(it) }.launchIn(viewModelScope)
        _suggestedActivities.onEach { performSuggestedActivitiesValidation(it) }.launchIn(viewModelScope)
        _itinerary.onEach { performItineraryValidation(it) }.launchIn(viewModelScope)
        _imageUris.onEach { performImageUrisValidation(it) }.launchIn(viewModelScope)
    }

    private fun performNameValidation(currentName: String): Boolean {
        return when {
            currentName.isBlank() -> {
                _nameError.value = "Name cannot be empty"
                false
            }
            currentName.length < 2 -> {
                _nameError.value = "Name must be at least 2 characters"
                false
            }
            else -> {
                _nameError.value = null
                true
            }
        }
    }

    private fun performDateValidation(start: LocalDate?, end: LocalDate?): Boolean {
        return when {
            start == null || end == null -> {
                _dateError.value = "Both start and end dates must be selected"
                false
            }
            start.isAfter(end) -> {
                _dateError.value = "Start date must be before end date"
                false
            }
            start.isBefore(LocalDate.now()) -> {
                _dateError.value = "Start date cannot be in the past"
                false
            }
            else -> {
                _dateError.value = null
                true
            }
        }
    }

    private fun performPriceValidation(min: Float, max: Float): Boolean {
        return if (min > max) {
            _priceError.value = "Min price can't be higher than max price"
            false
        } else {
            _priceError.value = null
            true
        }
    }

    private fun performParticipantsValidation(countStr: String): Boolean {
        val count = countStr.toIntOrNull()
        return when {
            count == null || count <= 0 -> {
                _participantsError.value = "Must be at least 1 participant"
                false
            }
            else -> {
                _participantsError.value = null
                true
            }
        }
    }

    private fun performTypologyValidation(currentTypology: String): Boolean {
        return if (currentTypology.isBlank()) {
            _typologyError.value = "Typology cannot be empty"
            false
        } else {
            _typologyError.value = null
            true
        }
    }

    private fun performDescriptionValidation(currentDesc: String): Boolean {
        return when {
            currentDesc.isBlank() -> {
                _descriptionError.value = "Description cannot be empty"
                false
            }
            currentDesc.length < 2 -> {
                _descriptionError.value = "Description must be at least 2 characters"
                false
            }
            else -> {
                _descriptionError.value = null
                true
            }
        }
    }
    private fun performSuggestedActivitiesValidation(activities: List<String>): Boolean {
        return if (activities.isEmpty()) {
            _suggestedActivitiesError.value = "At least one activity must be selected"
            false
        } else {
            _suggestedActivitiesError.value = null
            true
        }
    }

    private fun performItineraryValidation(stops: List<ItineraryStop>): Boolean {
        return if (stops.isEmpty()) {
            _itineraryError.value = "Please add at least one itinerary stop"
            false
        } else {
            _itineraryError.value = null
            true
        }
    }
    private fun performImageUrisValidation(uris: List<TravelImage>): Boolean {
         if (uris.isEmpty()) {
             _imageError.value = "At least one image is required."
             return false
         }
        _imageError.value = null
        return true
    }


    fun validateFields(): Boolean {
        val isNameValid = performNameValidation(_name.value)
        val isDateValid = performDateValidation(_startDate.value, _endDate.value)
        val isPriceValid = performPriceValidation(_minPrice.value, _maxPrice.value)
        val isParticipantsValid = performParticipantsValidation(_maxParticipantsAllowed.value)
        val isTypologyValid = performTypologyValidation(_typology.value)
        val isDescriptionValid = performDescriptionValidation(_description.value)
        val isSuggestedActivitiesValid = performSuggestedActivitiesValidation(_suggestedActivities.value)
        val isItineraryValid = performItineraryValidation(_itinerary.value)
        val isImageValid = performImageUrisValidation(_imageUris.value)

        if (_startDate.value != null && _startDate.value!!.isBefore(LocalDate.now())) {
            _dateError.value = "Start date cannot be in the past"
        }

        return isNameValid && isDateValid && isPriceValid && isParticipantsValid &&
                isTypologyValid && isDescriptionValid && isSuggestedActivitiesValid &&
                isItineraryValid && isImageValid
    }

    fun addSuggestedActivity(activity: String) {
        if (activity.isNotBlank()) {
            _suggestedActivities.value = _suggestedActivities.value + activity
        }
    }

    fun removeSuggestedActivity(activity: String) {
        _suggestedActivities.value = _suggestedActivities.value - activity
    }

    fun addItinerary(newItem: ItineraryStop) {
        _itinerary.value = _itinerary.value + newItem
    }

    fun updateItinerary(index: Int, updatedItem: ItineraryStop) {
        val currentList = _itinerary.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = updatedItem
            _itinerary.value = currentList
        }
    }

    fun removeItinerary(index: Int) {
        if (index in _itinerary.value.indices) {
            _itinerary.value = _itinerary.value.toMutableList().apply {
                removeAt(index)
            }
        }
    }

    fun addImageUri(uri: String) {
        if (_imageUris.value.size < 5) {
            _imageUris.value = _imageUris.value + TravelImage.UriImage(uri)
        }
    }

    fun removeImageUri(image: TravelImage) {
        _imageUris.value = _imageUris.value - image

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
                    _name.value = proposal.name
                    _organizerId.value = proposal.organizerId
                    _startDate.value = proposal.startDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                    _endDate.value = proposal.endDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                    _minPrice.value = proposal.minPrice.toFloat()
                    _maxPrice.value = proposal.maxPrice.toFloat()
                    _maxParticipantsAllowed.value = proposal.maxParticipants.toString()
                    _typology.value = proposal.typology
                    _description.value = proposal.description
                    _suggestedActivities.value = proposal.suggestedActivities
                    _itinerary.value = proposal.itinerary
                    _imageUris.value = proposal.images.map { TravelImage.UriImage(it) }
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
                    _organizerId.value = newOrganizerId
                    _name.value = originalProposal.name
                    _startDate.value = originalProposal.startDate?.toDate()?.toInstant()
                        ?.atZone(ZoneId.systemDefault())?.toLocalDate()
                    _endDate.value = originalProposal.endDate?.toDate()?.toInstant()
                        ?.atZone(ZoneId.systemDefault())?.toLocalDate()
                    _minPrice.value = originalProposal.minPrice.toFloat()
                    _maxPrice.value = originalProposal.maxPrice.toFloat()
                    _maxParticipantsAllowed.value = originalProposal.maxParticipants.toString()
                    _typology.value = originalProposal.typology
                    _description.value = originalProposal.description
                    _suggestedActivities.value = originalProposal.suggestedActivities.toList()
                    _itinerary.value = originalProposal.itinerary.toList()
                    _imageUris.value = originalProposal.images.map { TravelImage.UriImage(it) }
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
        return _nameError.value != null ||
                _dateError.value != null ||
                _priceError.value != null ||
                _participantsError.value != null ||
                _typologyError.value != null ||
                _descriptionError.value != null ||
                _suggestedActivitiesError.value != null ||
                _itineraryError.value != null ||
                _imageError.value != null
    }

    fun resetErrors() {
        _nameError.value = null
        _dateError.value = null
        _priceError.value = null
        _participantsError.value = null
        _typologyError.value = null
        _descriptionError.value = null
        _suggestedActivitiesError.value = null
        _itineraryError.value = null
        _imageError.value = null
    }

    fun resetFields(organizerId: String) {
        _name.value = ""
        _startDate.value = null
        _endDate.value = null
        _minPrice.value = 0f
        _maxPrice.value = 1000f
        _maxParticipantsAllowed.value = "1"
        _typology.value = ""
        _description.value = ""
        _suggestedActivities.value = emptyList()
        _itinerary.value = emptyList()
        _imageUris.value = emptyList()
        _organizerId.value = organizerId
        resetErrors()
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
                    _isLoading.value = false
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

    fun clearTravelProposalData() {
        exploreListenerJob?.cancel()
        ownedListenerJob?.cancel()

        _allProposals.value = emptyList()
        _ownedProposals.value = emptyList()
        _selectedProposal.value = null
        _currentDetailProposalId.value = null
    }

    override fun onCleared() {
        super.onCleared()
        clearTravelProposalData()
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
