package it.matteoleggio.alchan.helper.pojo

import android.media.MediaFormat
import okhttp3.MediaType

class MediaRecommendations(
    val mediaId: Int,
    val rating: Int?,
    val title: String?,
    val format: MediaFormat?,
    val type: MediaType?,
    val averageScore: Int?,
    val favourites: Int?,
    val coverImage: String?
)