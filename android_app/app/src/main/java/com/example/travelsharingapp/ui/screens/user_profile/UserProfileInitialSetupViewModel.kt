package com.example.travelsharingapp.ui.screens.user_profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.model.UserProfile
import com.example.travelsharingapp.data.repository.UserRepository
import com.example.travelsharingapp.utils.toTimestamp
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class ProfileSetupState {
    object Idle : ProfileSetupState()
    object Loading : ProfileSetupState()
    object Success : ProfileSetupState()
    data class Error(val message: String) : ProfileSetupState()
}

class UserProfileInitialSetupViewModel(
    private val userRepository: UserRepository,
    val userId: String,
    val email: String
) : ViewModel() {

    val firstName = mutableStateOf("")
    val lastName = mutableStateOf("")
    val nickname = mutableStateOf("")

    var birthDate by mutableStateOf<Timestamp?>(null)
        private set
    val phoneNumber = mutableStateOf("")
    val description = mutableStateOf("")
    val interests = mutableStateListOf<String>()
    val desiredDestinations = mutableStateListOf<String>()

    companion object {
        const val TOTAL_STEPS = 3
    }

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _uiState = MutableStateFlow<ProfileSetupState>(ProfileSetupState.Idle)
    val uiState: StateFlow<ProfileSetupState> = _uiState.asStateFlow()

    fun nextStep() {
        val canProceed = when (_currentStep.value) {
            1 -> validateStep1()
            2 -> validateStep2()
            else -> true
        }

        if (canProceed) {
            if (_currentStep.value < TOTAL_STEPS) {
                _currentStep.value++
                _uiState.value = ProfileSetupState.Idle
            }
        }
    }

    fun previousStep() {
        if (_currentStep.value > 1) {
            _currentStep.value--
            _uiState.value = ProfileSetupState.Idle
        }
    }

    private fun validateStep1(): Boolean {
        if (firstName.value.isBlank() || lastName.value.isBlank() || nickname.value.isBlank()) {
            _uiState.value = ProfileSetupState.Error("First name, last name, and nickname are required.")
            return false
        }
        return true
    }

    private fun validateStep2(): Boolean {
        if (birthDate != null) {
            val local = birthDate!!.toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            if (local.isAfter(LocalDate.now())) {
                _uiState.value = ProfileSetupState.Error("Birthdate cannot be in the future.")
                return false
            }
        }


        if (phoneNumber.value.isBlank()) {
             _uiState.value = ProfileSetupState.Error("Phone number is required for this step.")
             return false
         }
        return true
    }

    fun saveUserProfile() {
        if (!validateStep1() || !validateStep2()) {
            return
        }

        _uiState.value = ProfileSetupState.Loading
        viewModelScope.launch {
            val userProfile = UserProfile(
                userId = userId,
                firstName = firstName.value.trim(),
                lastName = lastName.value.trim(),
                email = email,
                nickname = nickname.value.trim(),
                birthDate = birthDate,
                phoneNumber = phoneNumber.value.trim(),
                description = description.value.trim(),
                interests = interests.map { it.trim() }.filter { it.isNotEmpty() },
                desiredDestinations = desiredDestinations.map { it.trim() }.filter { it.isNotEmpty() },
                rating = 0f,
                numberOfReviews = 0,
                profileImage = null, // Can be added later
            )

            val success = userRepository.createUserProfile(userProfile)
            if (success) {
                _uiState.value = ProfileSetupState.Success
            } else {
                _uiState.value = ProfileSetupState.Error("Failed to save profile. Please try again.")
            }
        }
    }

    fun resetState() {
        _uiState.value = ProfileSetupState.Idle
        _currentStep.value = 1
    }

    fun endProfileSetup() {
        _uiState.value = ProfileSetupState.Idle
    }

    fun updateBirthDate(birthDateMillis: Long?) {
        birthDate = birthDateMillis?.let { convertMillisToLocalDate(it)?.toTimestamp() }
    }


    fun addInterest(interest: String) {
        val trimmedInterest = interest.trim()
        if (trimmedInterest.isNotBlank() and !interests.contains(trimmedInterest)) {
            interests.add(trimmedInterest)
        }
    }

    fun removeInterest(interest: String) {
        interests.remove(interest)
    }

    fun addDestination(destination: String) {
        val trimmedDestination = destination.trim()
        if (trimmedDestination.isNotBlank() and !desiredDestinations.contains(trimmedDestination)) {
            desiredDestinations.add(trimmedDestination)
        }
    }

    fun removeDestination(destination: String) {
        desiredDestinations.remove(destination)
    }
}

class UserProfileInitialSetupFactory(
    private val userRepository: UserRepository,
    private val userId: String,
    private val email: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel > create(modelClass: Class<T>): T {
        return modelClass
            .getConstructor(UserRepository::class.java, String::class.java, String::class.java)
            .newInstance(userRepository, userId, email)
    }
}