package com.example.travelsharingapp.ui.screens.travel_review

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.model.TravelProposalReview
import com.example.travelsharingapp.data.repository.TravelReviewRepository
import com.google.firebase.firestore.ListenerRegistration
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

    //StateFlow che rappresenta le review attualmente visibili
    private val _visibleReviews = MutableStateFlow<List<TravelProposalReview>>(emptyList())
    val visibleReviews: StateFlow<List<TravelProposalReview>> = _visibleReviews.asStateFlow()


    fun loadReviewsForProposal(proposalId: String) {
        viewModelScope.launch {
            val latestReviews = reviewRepository.getReviewsByProposalId(proposalId)
            _proposalSpecificReviews.value = latestReviews

            if (_visibleReviews.value.isEmpty()) {
                _visibleReviews.value = latestReviews
            }
        }
    }

    private var reviewListener: ListenerRegistration? = null

    fun observeReviews(proposalId: String) {
        reviewListener?.remove() // Rimuovi vecchio listener se presente

        reviewListener = reviewRepository.observeReviewsByProposalId(proposalId) { latestReviews ->
            val currentVisible = _visibleReviews.value

            // Aggiorna tutte le review
            _proposalSpecificReviews.value = latestReviews

            // Se è la prima volta o la lista era già sincronizzata, aggiorna anche visible
            if (_visibleReviews.value.isEmpty() || latestReviews == currentVisible) {
                _visibleReviews.value = latestReviews
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        reviewListener?.remove()
    }





    //mostra le review nuove
    fun acceptLatestReviews() {
        _visibleReviews.value = _proposalSpecificReviews.value
    }



    fun addReview(
        proposalId: String,
        review: TravelProposalReview,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            reviewRepository.addReview(proposalId, review)
            loadReviewsForProposal(proposalId)
            onComplete?.invoke()
        }
    }

    fun updateReview(
        proposalId: String,
        review: TravelProposalReview,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            reviewRepository.updateReview(proposalId, review)
            loadReviewsForProposal(proposalId)
            onComplete?.invoke()
        }
    }

    fun deleteReview(
        proposalId: String,
        reviewId: String,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            reviewRepository.deleteReview(reviewId)
            loadReviewsForProposal(proposalId)
            onComplete?.invoke()
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