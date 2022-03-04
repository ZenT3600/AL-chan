package it.matteoleggio.alchan.ui.browse.media.stats

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import it.matteoleggio.alchan.data.repository.AppSettingsRepository
import it.matteoleggio.alchan.data.repository.MediaRepository

class MediaStatsViewModel(private val mediaRepository: MediaRepository,
                          private val appSettingsRepository: AppSettingsRepository,
                          val gson: Gson
) : ViewModel() {

    var mediaId: Int? = null
    var mediaData: MediaStatsQuery.Media? = null

    val mediaStatsData by lazy {
        mediaRepository.mediaStatsData
    }

    val showStatsAutomatically: Boolean
        get() = appSettingsRepository.appSettings.showStatsAutomatically != false

    fun getMediaStats() {
        if (mediaId != null) mediaRepository.getMediaStats(mediaId!!)
    }
}