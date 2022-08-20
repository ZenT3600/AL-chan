package it.matteoleggio.alchan.ui.browse.media.overview

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.matteoleggio.alchan.data.repository.InfoRepository

class ThemesPlayerViewModel(private val infoRepository: InfoRepository) : ViewModel() {

    var mediaTitle = ""
    var trackTitle = ""

    val youTubeVideoResponse by lazy {
        infoRepository.youTubeVideoResponse
    }

    val spotifyTrackResponse by lazy {
        infoRepository.spotifyTrackResponse
    }

    fun getYouTubeVideo(context: Context) {
        if (mediaTitle.isBlank() || trackTitle.isBlank()) {
            return
        }

        infoRepository.getYouTubeVideo(context, "$mediaTitle ${getQuery()}")
    }

    private fun getQuery(): String {
        var title = trackTitle.substring(trackTitle.indexOf("\"") + 1, trackTitle.lastIndexOf("\"")).trim()
        var artist = trackTitle.substring(trackTitle.indexOf("by", trackTitle.lastIndexOf("\"")) + 3).trim()

        if (title.endsWith(")")) {
            title = title.substring(0, title.lastIndexOf("(")).trim()
        }

        if (artist.contains("(ep")) {
            artist = artist.substring(0, artist.indexOf("(ep")).trim()
        }
        return "${title} ${artist}"
    }
}