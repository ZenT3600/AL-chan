package it.matteoleggio.alchan.ui.browse.reviews.editor

import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.data.repository.BrowseRepository
import it.matteoleggio.alchan.data.repository.MediaRepository
import it.matteoleggio.alchan.data.repository.UserRepository
import it.matteoleggio.alchan.data.response.User
import type.MediaType

class ReviewEditorViewModel(private val mediaRepository: MediaRepository) : ViewModel() {

    var reviewId: Int? = null
    var mediaId: Int? = null

    var reviewString = ""
    var summaryString = ""
    var score = 0
    var isPrivate = false

    val checkReviewResponse by lazy {
        mediaRepository.checkReviewResponse
    }

    val saveReviewResponse by lazy {
        mediaRepository.saveReviewResponse
    }

    val deleteReviewResponse by lazy {
        mediaRepository.deleteReviewResponse
    }

    fun checkReview() {
        if (mediaId != null) {
            mediaRepository.checkReview(mediaId!!)
        }
    }

    fun saveReview() {
        if (mediaId != null) {
            mediaRepository.saveReview(if (reviewId == 0) null else reviewId, mediaId!!, reviewString, summaryString, score, isPrivate)
        }
    }

    fun deleteReview() {
        if (reviewId != null && reviewId != 0) {
            mediaRepository.deleteReview(reviewId!!)
        }
    }
}