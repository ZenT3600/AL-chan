package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MediaRecommendations(
    @SerializedName("recommendations")
    @Expose
    val recommendations: List<MediaRecommendation>
)