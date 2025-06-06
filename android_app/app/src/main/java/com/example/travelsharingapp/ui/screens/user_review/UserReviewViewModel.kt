package com.example.travelsharingapp.ui.screens.user_review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.model.UserReview
import com.example.travelsharingapp.data.repository.UserReviewRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserReviewViewModel(
    private val repository: UserReviewRepository
) : ViewModel() {

    private val _userReviews = MutableStateFlow<List<UserReview>>(emptyList())
    val userReviews: StateFlow<List<UserReview>> = _userReviews.asStateFlow()

    private val _proposalReviews = MutableStateFlow<List<UserReview>>(emptyList())
    val proposalReviews: StateFlow<List<UserReview>> = _proposalReviews.asStateFlow()

    private var reviewListenerJob: Job? = null

    fun loadReviewsForUser(userId: String) {
        viewModelScope.launch {
            _userReviews.value = repository.getReviewsForUser(userId)
        }
    }

    fun startListeningReviewsForProposal(proposalId: String) {
        reviewListenerJob?.cancel()

        reviewListenerJob = viewModelScope.launch {
            repository.observeReviewsByProposalId(proposalId)
                .collect { updatedReviews ->
                _proposalReviews.value = updatedReviews
            }
        }
    }

    fun addReview(review: UserReview) {
        viewModelScope.launch {
            repository.addReview(review)
        }
    }

    fun updateReview(updatedReview: UserReview, oldRating: Float) {
        viewModelScope.launch {
            repository.updateReview(updatedReview, oldRating)
        }
    }

    fun deleteReview(reviewToDelete: UserReview) {
        viewModelScope.launch {
            repository.deleteReview(reviewToDelete)
        }
    }

    override fun onCleared() {
        super.onCleared()
        reviewListenerJob?.cancel()
    }
}


class UserReviewViewModelFactory(
    private val userReviewRepository: UserReviewRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserReviewViewModel::class.java)) {
            return UserReviewViewModel(userReviewRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

