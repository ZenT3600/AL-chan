package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MediaImages(
    @SerializedName("jpg")
    @Expose
    val jpg: Images,
    @SerializedName("webp")
    @Expose
    val webp: Images
)