package com.example.travelsharingapp.ui.screens.travel_review

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.model.TravelProposalReview
import com.example.travelsharingapp.data.repository.TravelReviewRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TravelReviewViewModel(
    private val reviewRepository: TravelReviewRepository
) : ViewModel() {

    private val _proposalSpecificReviews = MutableStateFlow<List<TravelProposalReview>>(emptyList())
    val proposalSpecificReviews: StateFlow<List<TravelProposalReview>> = _proposalSpecificReviews.asStateFlow()

    private var reviewListenerJob: Job? = null

    fun startListeningReviewsForProposal(proposalId: String) {
        reviewListenerJob?.cancel()

        reviewListenerJob = viewModelScope.launch {
            reviewRepository.observeReviewsByProposalId(proposalId)
                .collect { latestReviews ->
                _proposalSpecificReviews.value = latestReviews
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        reviewListenerJob?.cancel()
    }

    fun addReview(proposalId: String, review: TravelProposalReview) {
        viewModelScope.launch {
            reviewRepository.addReview(proposalId, review)
        }
    }

    fun updateReview(proposalId: String, review: TravelProposalReview) {
        viewModelScope.launch {
            reviewRepository.updateReview(proposalId, review)
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            reviewRepository.deleteReview(reviewId)
        }
    }

    suspend fun uploadReviewImageToFirebase(context: Context, uri: Uri, userId: String): String? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                "image/jpg", "image/jpeg" -> "jpg"
                else -> "jpg"
            }

            val fileName = "review_${System.currentTimeMillis()}.$extension"
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                .reference.child("review_images/$userId/$fileName")

            val inputStream = contentResolver.openInputStream(uri) ?: return null
            storageRef.putStream(inputStream).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

class TravelReviewViewModelFactory(
    private val reviewRepository: TravelReviewRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass
            .getConstructor(TravelReviewRepository::class.java)
            .newInstance(reviewRepository)
    }
}