package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AnimeReview(
    @SerializedName("user")
    @Expose
    val user: User,
    @SerializedName("mal_id")
    @Expose
    val malId: Int,
    @SerializedName("url")
    @Expose
    val url: String,
    @SerializedName("type")
    @Expose
    val type: String,
    @SerializedName("votes")
    @Expose
    val votes: Int,
    @SerializedName("date")
    @Expose
    val date: String,
    @SerializedName("review")
    @Expose
    val review: String,
    @SerializedName("episodes_watched")
    @Expose
    val episodes_watched: Int,
    @SerializedName("scores")
    @Expose
    val scores: List<Int>
)