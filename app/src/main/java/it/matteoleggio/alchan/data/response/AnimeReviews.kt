package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AnimeReviews(
    @SerializedName("reviews")
    @Expose
    val reviews: List<AnimeReview>
)