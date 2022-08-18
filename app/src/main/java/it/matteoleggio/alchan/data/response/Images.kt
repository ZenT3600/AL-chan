package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Images(
    @SerializedName("image_url")
    @Expose
    val image_url: String,
    @SerializedName("small_image_url")
    @Expose
    val small_image_url: String,
    @SerializedName("large_image_url")
    @Expose
    val large_image_url: String
)