package it.matteoleggio.alchan.data.datasource

import it.matteoleggio.alchan.data.response.Announcement
import it.matteoleggio.alchan.data.response.SpotifyAccessToken
import it.matteoleggio.alchan.data.response.SpotifySearch
import it.matteoleggio.alchan.data.response.YouTubeSearch
import retrofit2.Call

interface InfoDataSource {
    fun getAnnouncement(): Call<Announcement>
    fun getYouTubeVideo(key: String, query: String): Call<YouTubeSearch>
    fun getSpotifyAccessToken(): Call<SpotifyAccessToken>
    fun getSpotifyTrack(query: String): Call<SpotifySearch>
}