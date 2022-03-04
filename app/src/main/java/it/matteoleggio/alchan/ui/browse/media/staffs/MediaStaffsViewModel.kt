package it.matteoleggio.alchan.ui.browse.media.staffs

import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.data.repository.MediaRepository
import it.matteoleggio.alchan.helper.pojo.MediaStaffs
import type.MediaType

class MediaStaffsViewModel(private val mediaRepository: MediaRepository) : ViewModel() {

    var mediaId: Int? = null
    var mediaType: MediaType? = null
    var page = 1
    var hasNextPage = true

    var isInit = false
    var mediaStaffs = ArrayList<MediaStaffs?>()

    val mediaStaffsData by lazy {
        mediaRepository.mediaStaffsData
    }

    val triggerMediaStaff by lazy {
        mediaRepository.triggerMediaStaff
    }

    fun getMediaStaffs() {
        if (hasNextPage && mediaId != null) mediaRepository.getMediaStaffs(mediaId!!, page)
    }

    fun refresh() {
        mediaStaffs.clear()
        page = 1
        hasNextPage = true
        getMediaStaffs()
    }
}