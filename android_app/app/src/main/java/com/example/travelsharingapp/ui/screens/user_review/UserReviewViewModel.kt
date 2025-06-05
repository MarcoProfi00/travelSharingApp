package com.example.travelsharingapp.ui.screens.user_review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.model.UserProfile
import com.example.travelsharingapp.data.model.UserReview
import com.example.travelsharingapp.data.repository.UserRepository
import com.example.travelsharingapp.data.repository.UserReviewRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserReviewViewModel(
    private val repository: UserReviewRepository,
    private val userProfileRepository: UserRepository
) : ViewModel() {

    private val _userReviews = MutableStateFlow<List<UserReview>>(emptyList())
    val userReviews: StateFlow<List<UserReview>> = _userReviews.asStateFlow()

    private val _proposalReviews = MutableStateFlow<List<UserReview>>(emptyList())
    val proposalReviews: StateFlow<List<UserReview>> = _proposalReviews.asStateFlow()

    fun loadReviewsForUser(userId: String) {
        viewModelScope.launch {
            _userReviews.value = repository.getReviewsForUser(userId)
        }
    }

    private var reviewListener: ListenerRegistration? = null

    fun observeReviewsForProposal(proposalId: String) {
        reviewListener?.remove()
        reviewListener = repository.observeReviewsForProposal(proposalId) { updatedReviews ->
            _proposalReviews.value = updatedReviews
        }
    }

    fun addReview(review: UserReview, callback: (UserProfile?) -> Unit) {
        viewModelScope.launch {
            val reviewer = userProfileRepository.getUserProfile(review.reviewerId)
            if (reviewer != null) {
                review.reviewerFirstName = reviewer.firstName
                review.reviewerLastName = reviewer.lastName
            }

            repository.addReview(review)

            val userProfile = userProfileRepository.getUserProfile(review.reviewedUserId)
            if (userProfile != null) {
                val newNumberOfReviews = userProfile.numberOfReviews + 1
                val newAverageRating =
                    ((userProfile.rating * userProfile.numberOfReviews) + review.rating) / newNumberOfReviews

                val updatedUserProfile = userProfile.copy(
                    rating = newAverageRating,
                    numberOfReviews = newNumberOfReviews
                )

                userProfileRepository.updateUserProfile(updatedUserProfile)
                callback(updatedUserProfile)
            } else {
                callback(null)
            }
        }
    }


    fun updateReview(updatedReview: UserReview, oldRating: Float, callback: (UserProfile?) -> Unit) {
        viewModelScope.launch {
            repository.updateReview(updatedReview)

            val userProfile = userProfileRepository.getUserProfile(updatedReview.reviewedUserId)
            if (userProfile != null && userProfile.numberOfReviews > 0) {
                val currentTotalRatingSum = userProfile.rating * userProfile.numberOfReviews
                val newTotalRatingSum = currentTotalRatingSum - oldRating + updatedReview.rating
                val newAverageRating = newTotalRatingSum / userProfile.numberOfReviews

                val updatedUserProfile = userProfile.copy(rating = newAverageRating)
                userProfileRepository.updateUserProfile(updatedUserProfile)
                callback(updatedUserProfile)
            } else {
                callback(userProfile)
            }
        }
    }

    fun deleteReview(reviewToDelete: UserReview, callback: (UserProfile?) -> Unit) {
        viewModelScope.launch {
            repository.deleteReview(reviewToDelete.reviewId)

            val userProfile = userProfileRepository.getUserProfile(reviewToDelete.reviewedUserId)
            if (userProfile != null) {
                val newNumberOfReviews = userProfile.numberOfReviews - 1
                val newAverageRating = if (newNumberOfReviews <= 0) {
                    0.0f
                } else {
                    ((userProfile.rating * userProfile.numberOfReviews) - reviewToDelete.rating) / newNumberOfReviews
                }

                val updatedUserProfile = userProfile.copy(
                    rating = newAverageRating,
                    numberOfReviews = if (newNumberOfReviews < 0) 0 else newNumberOfReviews
                )
                userProfileRepository.updateUserProfile(updatedUserProfile)
                callback(updatedUserProfile)
            } else {
                callback(null)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        reviewListener?.remove()
    }
}


class UserReviewViewModelFactory(
    private val userReviewRepository: UserReviewRepository,
    private val userProfileRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserReviewViewModel::class.java)) {
            return UserReviewViewModel(userReviewRepository, userProfileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

