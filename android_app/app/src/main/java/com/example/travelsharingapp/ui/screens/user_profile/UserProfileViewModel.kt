package com.example.travelsharingapp.ui.screens.user_profile

import android.content.Context
import android.net.Uri
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.model.UserProfile
import com.example.travelsharingapp.data.repository.UserRepository
import com.example.travelsharingapp.utils.toLocalDate
import com.example.travelsharingapp.utils.toTimestamp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class EditableProfileField {
    FirstName,
    LastName,
    Email,
    Nickname,
    BirthDate,
    PhoneNumber
}

enum class UserProfileUiState {
    Loading,
    Loaded,
    Error
}

class UserProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _showToast = MutableSharedFlow<Boolean>()
    val showToastMessage = _showToast.asSharedFlow()

    private val _selectedUserProfile = MutableStateFlow<UserProfile?>(null)
    val selectedUserProfile: StateFlow<UserProfile?> = _selectedUserProfile.asStateFlow()

    private val listeners = mutableMapOf<String, ListenerRegistration>()
    private var selectedUserProfileListenerRegistration: ListenerRegistration? = null

    private val _observedProfiles = mutableMapOf<String, MutableStateFlow<UserProfile?>>()
    private var originalUserProfile: UserProfile? = null

    var editedFirstName by mutableStateOf("")
    var editedLastName by mutableStateOf("")
    var editedEmail by mutableStateOf("")
    var editedNickname by mutableStateOf("")
    var editedBirthDate by mutableStateOf<Timestamp?>(null)
    var editedPhoneNumber by mutableStateOf("")
    var editedDescription by mutableStateOf("")
    var editedInterests = mutableStateListOf<String>()
    var editedDesiredDestinations = mutableStateListOf<String>()
    var editedProfileImageUri by mutableStateOf<Uri?>(null)

    private val _validationErrors = MutableStateFlow<Map<EditableProfileField, String>>(emptyMap())
    val validationErrors: StateFlow<Map<EditableProfileField, String>> = _validationErrors.asStateFlow()

    private val _hasUnsavedChanges = MutableStateFlow(false)

    fun observeUserProfileById(userId: String): StateFlow<UserProfile?> {
        return _observedProfiles.getOrPut(userId) {
            val flow = MutableStateFlow<UserProfile?>(null)
            userRepository.observeUserProfile(userId) { profile ->
                flow.value = profile
            }?.let { listener ->
                listeners[userId] = listener
            }
            flow
        }
    }

    fun clearUserSessionData() {
        selectedUserProfileListenerRegistration?.remove()
        selectedUserProfileListenerRegistration = null

        listeners.values.forEach { it.remove() }
        listeners.clear()

        _observedProfiles.clear()

        _selectedUserProfile.value = null
        originalUserProfile = null
        clearEditableFields()
    }

    override fun onCleared() {
        super.onCleared()
        clearUserSessionData()
    }

    fun refreshUserProfile(updatedProfile: UserProfile) {
        if (_selectedUserProfile.value?.userId == updatedProfile.userId) {
            _selectedUserProfile.value = updatedProfile
        }
    }

    fun selectUserProfile(userId: String) {
        if (_selectedUserProfile.value?.userId == userId) {
            return
        }

        selectedUserProfileListenerRegistration?.remove()
        selectedUserProfileListenerRegistration = null

        _selectedUserProfile.value = null
        originalUserProfile = null
        clearEditableFields()

        selectedUserProfileListenerRegistration = userRepository.observeUserProfile(userId) { profile ->
            _selectedUserProfile.value = profile
            if (originalUserProfile?.userId != profile?.userId || (originalUserProfile == null)) {
                originalUserProfile = profile?.copy()
                profile?.let {
                    editedFirstName = it.firstName
                    editedLastName = it.lastName
                    editedEmail = it.email
                    editedNickname = it.nickname
                    editedBirthDate = it.birthDate
                    editedPhoneNumber = it.phoneNumber
                    editedDescription = it.description
                    editedInterests.clear(); editedInterests.addAll(it.interests)
                    editedDesiredDestinations.clear(); editedDesiredDestinations.addAll(it.desiredDestinations)
                    editedProfileImageUri = it.profileImage?.toUri()
                    _hasUnsavedChanges.value = false
                    _validationErrors.value = emptyMap()
                } ?: run {
                    clearEditableFields()
                    originalUserProfile = null
                }
            }
        }
    }

    private fun clearEditableFields() {
        editedFirstName = ""
        editedLastName = ""
        editedEmail = ""
        editedNickname = ""
        editedBirthDate = null
        editedPhoneNumber = ""
        editedDescription = ""
        editedInterests.clear()
        editedDesiredDestinations.clear()
        editedProfileImageUri = null
        _hasUnsavedChanges.value = false
        _validationErrors.value = emptyMap()
    }

    fun updateFirstName(firstName: String) {
        editedFirstName = firstName
        _hasUnsavedChanges.value = (originalUserProfile?.firstName != editedFirstName)
    }

    fun updateLastName(lastName: String) {
        editedLastName = lastName
        _hasUnsavedChanges.value = (originalUserProfile?.lastName != editedLastName)
    }

    fun updateEmail(email: String) {
        editedEmail = email
        _hasUnsavedChanges.value = (originalUserProfile?.email != editedEmail)
    }

    fun updateNickname(nickname: String) {
        editedNickname = nickname
        _hasUnsavedChanges.value = (originalUserProfile?.nickname != editedNickname)
    }

    fun updateBirthDate(birthDateMillis: Long?) {
        editedBirthDate = birthDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toTimestamp() }
        _hasUnsavedChanges.value = originalUserProfile?.birthDate != editedBirthDate
    }

    fun updatePhoneNumber(phoneNumber: String) {
        editedPhoneNumber = phoneNumber
        _hasUnsavedChanges.value = (originalUserProfile?.phoneNumber != editedPhoneNumber)
    }

    fun updateDescription(description: String) {
        editedDescription = description
        _hasUnsavedChanges.value = (originalUserProfile?.description != editedDescription)
    }

    fun addInterest(interest: String) {
        val trimmedInterest = interest.trim()
        if (trimmedInterest.isNotBlank() and !editedInterests.contains(trimmedInterest)) {
            editedInterests.add(trimmedInterest)
        }
        _hasUnsavedChanges.value = (originalUserProfile?.interests != editedInterests)
    }

    fun removeInterest(interest: String) {
        editedInterests.remove(interest)
        _hasUnsavedChanges.value = (originalUserProfile?.interests != editedInterests)
    }

    fun addDestination(destination: String) {
        val trimmedDestination = destination.trim()
        if (trimmedDestination.isNotBlank() and !editedDesiredDestinations.contains(trimmedDestination)) {
            editedDesiredDestinations.add(trimmedDestination)
        }
        _hasUnsavedChanges.value = (originalUserProfile?.desiredDestinations != editedDesiredDestinations)
    }

    fun removeDestination(destination: String) {
        editedDesiredDestinations.remove(destination)
        _hasUnsavedChanges.value = (originalUserProfile?.desiredDestinations != editedDesiredDestinations)
    }

    fun updateProfileImageUri(uri: Uri, context: Context) {
        viewModelScope.launch {
            val userId = _selectedUserProfile.value?.userId ?: return@launch
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                "image/jpg", "image/jpeg" -> "jpg"
                else -> "jpg"
            }

            val storageRef = FirebaseStorage.getInstance().reference
                .child("profile_images/$userId.$extension")

            try {
                val inputStream = contentResolver.openInputStream(uri) ?: return@launch
                storageRef.putStream(inputStream).await()
                val downloadUrl = storageRef.downloadUrl.await()

                editedProfileImageUri = downloadUrl.toString().toUri()
                _hasUnsavedChanges.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun validateFields(): Boolean {
        val errors = mutableMapOf<EditableProfileField, String>()

        if (editedFirstName.isBlank()) {
            errors[EditableProfileField.FirstName] = "First name is required"
        }

        if (editedLastName.isBlank()) {
            errors[EditableProfileField.LastName] = "First name is required"
        }

        if (editedEmail.isBlank()) {
            errors[EditableProfileField.Email] = "Email is required"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(editedEmail).matches()) {
            errors[EditableProfileField.Email] = "Invalid email format"
        }

        if (editedNickname.isBlank()) {
            errors[EditableProfileField.Nickname] = "Nickname is required"
        }

        val birthDate = editedBirthDate?.toLocalDate()
        if (birthDate == null) {
            errors[EditableProfileField.BirthDate] = "Birth date is required"
        } else {
            val minAge = LocalDate.now().minusYears(18)
            if (birthDate.isAfter(minAge)) errors[EditableProfileField.BirthDate] = "User must be at least 18 years old"
        }

        if (editedPhoneNumber.isBlank()) {
            errors[EditableProfileField.PhoneNumber] = "Phone number is required"
        } else if (!Patterns.PHONE.matcher(editedPhoneNumber).matches()) {
            errors[EditableProfileField.PhoneNumber] = "Invalid phone number format"
        }

        _validationErrors.value = errors
        return errors.isEmpty()
    }

    fun saveProfile(): Boolean {
        val currentSelectedProfile = _selectedUserProfile.value ?: return false
        val imageChanged = originalUserProfile?.profileImage != editedProfileImageUri?.toString()
        val favoritesChanged = originalUserProfile?.favoriteProposals != currentSelectedProfile.favoriteProposals

        if (!_hasUnsavedChanges.value && !imageChanged && !favoritesChanged)
            return true

        _validationErrors.value = emptyMap()
        if (!validateFields()) return false

        val updatedProfile = currentSelectedProfile.copy(
            firstName = editedFirstName,
            lastName = editedLastName,
            email = editedEmail,
            nickname = editedNickname,
            birthDate = editedBirthDate,
            phoneNumber = editedPhoneNumber,
            description = editedDescription,
            interests = editedInterests.toList(),
            desiredDestinations = editedDesiredDestinations.toList(),
            profileImage = editedProfileImageUri?.toString()
        )

        viewModelScope.launch {
            val success = userRepository.updateUserProfile(updatedProfile)
            if (success) {
                _selectedUserProfile.value = updatedProfile
                originalUserProfile = updatedProfile.copy()
                _hasUnsavedChanges.value = false
                _showToast.emit(true)
            }
        }
        return true
    }

    fun addFavorite(proposalId: String) {
        val currentUserId = _selectedUserProfile.value?.userId
        if (currentUserId != null) {
            viewModelScope.launch {
                userRepository.addFavorite(currentUserId, proposalId)
                val updatedProfile = userRepository.getUserProfile(currentUserId)
                if (updatedProfile != null) {
                    refreshUserProfile(updatedProfile)
                }
            }
        }
    }

    fun removeFavorite(proposalId: String) {
        val currentUserId = _selectedUserProfile.value?.userId
        if (currentUserId != null) {
            viewModelScope.launch {
                userRepository.removeFavorite(currentUserId, proposalId)
                val updatedProfile = userRepository.getUserProfile(currentUserId)
                if (updatedProfile != null) {
                    refreshUserProfile(updatedProfile)
                }
            }
        }
    }
}

fun convertMillisToLocalDate(millis: Long?): LocalDate? {
    return millis?.let {
        Instant.ofEpochMilli(it)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}


class UserProfileViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass
            .getConstructor(UserRepository::class.java)
            .newInstance(userRepository)
    }
}
