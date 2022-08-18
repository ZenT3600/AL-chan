package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MediaRecommendation(
    @SerializedName("mal_id")
    @Expose
    val malId: Int,
    @SerializedName("url")
    @Expose
    val url: String,
    @SerializedName("images")
    @Expose
    val images: MediaImages,
    @SerializedName("title")
    @Expose
    val title: String
)