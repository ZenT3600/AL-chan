package it.matteoleggio.alchan.data.repository

import androidx.lifecycle.LiveData
import it.matteoleggio.alchan.data.datasource.InfoDataSource
import it.matteoleggio.alchan.data.localstorage.InfoManager
import it.matteoleggio.alchan.data.localstorage.TempStorageManager
import it.matteoleggio.alchan.data.network.Resource
import it.matteoleggio.alchan.data.response.Announcement
import it.matteoleggio.alchan.data.response.SpotifySearch
import it.matteoleggio.alchan.data.response.YouTubeSearch
import it.matteoleggio.alchan.helper.libs.SingleLiveEvent
import it.matteoleggio.alchan.helper.utils.AndroidUtility

class InfoRepositoryImpl(private val infoDataSource: InfoDataSource,
                         private val infoManager: InfoManager,
                         private val tempStorageManager: TempStorageManager) : InfoRepository {

    private val _announcementResponse = SingleLiveEvent<Resource<Announcement>>()
    override val announcementResponse: LiveData<Resource<Announcement>>
        get() = _announcementResponse

    override val lastAnnouncementId: Int?
        get() = infoManager.lastAnnouncementId

    private val _youTubeVideoResponse = SingleLiveEvent<Resource<YouTubeSearch>>()
    override val youTubeVideoResponse: LiveData<Resource<YouTubeSearch>>
        get() = _youTubeVideoResponse

    private val _spotifyTrackResponse = SingleLiveEvent<Resource<SpotifySearch>>()
    override val spotifyTrackResponse: LiveData<Resource<SpotifySearch>>
        get() = _spotifyTrackResponse

    override fun getAnnouncement() {
        infoDataSource.getAnnouncement().enqueue(AndroidUtility.apiCallback(_announcementResponse))
    }

    override fun setLastAnnouncementId(value: Int) {
        infoManager.setLastAnnouncementId(value)
    }

    override fun getYouTubeVideo(query: String) {}

    override fun getSpotifyTrack(query: String) {}
}