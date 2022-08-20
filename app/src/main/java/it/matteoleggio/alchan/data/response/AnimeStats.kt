package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AnimeStats(
    @SerializedName("watching")
    @Expose
    val watching: Int,
    @SerializedName("completed")
    @Expose
    val completed: Int,
    @SerializedName("on_hold")
    @Expose
    val on_hold: Int,
    @SerializedName("dropped")
    @Expose
    val dropped: Int,
    @SerializedName("plan_to_watch")
    @Expose
    val plan_to_watch: Int,
    @SerializedName("total")
    @Expose
    val total: Int,
    @SerializedName("scores")
    @Expose
    val scores: List<ScoreEntry>,
)