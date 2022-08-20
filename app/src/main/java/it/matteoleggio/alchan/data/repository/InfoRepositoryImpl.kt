package it.matteoleggio.alchan.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import it.matteoleggio.alchan.data.datasource.InfoDataSource
import it.matteoleggio.alchan.data.localstorage.InfoManager
import it.matteoleggio.alchan.data.localstorage.TempStorageManager
import it.matteoleggio.alchan.data.network.Resource
import it.matteoleggio.alchan.data.response.Announcement
import it.matteoleggio.alchan.data.response.SpotifySearch
import it.matteoleggio.alchan.data.response.YouTubeSearch
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.helper.libs.SingleLiveEvent
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import java.net.URLEncoder


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

    override fun getYouTubeVideo(ctx: Context, query: String) {
        val intent = Intent(Intent.ACTION_SEARCH)
        intent.setPackage("com.google.android.youtube")
        intent.putExtra("query", query)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            startActivity(ctx, intent, null)
        } catch (e: android.content.ActivityNotFoundException) {
            startActivity(ctx, Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(URLEncoder.encode("${Constant.YOUTUBE_URL}/results?search_query=$query", "utf-8"))), "Youtube URL"), null)
        }
    }

    /*override fun getSpotifyTrack(query: String) {
        _spotifyTrackResponse.postValue(Resource.Loading())

        if (tempStorageManager.spotifyAccessToken == null || Utility.getCurrentTimestamp() > (tempStorageManager.spotifyAccessTokenLastRetrieve ?: 0) + 3600000) {
            val ref = FirebaseDatabase.getInstance().getReference("keys")
            ref.child("spotify").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tempStorageManager.spotifyKey = snapshot.value as String

                    infoDataSource.getSpotifyAccessToken().enqueue(object : Callback<SpotifyAccessToken> {
                        override fun onResponse(
                            call: Call<SpotifyAccessToken>,
                            response: Response<SpotifyAccessToken>
                        ) {
                            if (response.isSuccessful) {
                                val accessToken = response.body()
                                tempStorageManager.spotifyAccessTokenLastRetrieve = Utility.getCurrentTimestamp()
                                tempStorageManager.spotifyAccessToken = accessToken?.accessToken
                                tempStorageManager.spotifyAccessTokenExpiresIn = accessToken?.expiresIn
                                infoDataSource.getSpotifyTrack(query).enqueue(AndroidUtility.apiCallback(_spotifyTrackResponse))
                            } else {
                                _spotifyTrackResponse.postValue(Resource.Error(response.errorBody()?.string() ?: ""))
                            }
                        }

                        override fun onFailure(call: Call<SpotifyAccessToken>, t: Throwable) {
                            _spotifyTrackResponse.postValue(Resource.Error(t.localizedMessage ?: ""))
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    _spotifyTrackResponse.postValue(Resource.Error(error.message))
                }
            })
        } else {
            infoDataSource.getSpotifyTrack(query).enqueue(AndroidUtility.apiCallback(_spotifyTrackResponse))
        }
    }*/
}