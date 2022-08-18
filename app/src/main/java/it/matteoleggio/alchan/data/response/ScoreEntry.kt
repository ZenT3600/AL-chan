package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ScoreEntry(
    /**
     * Number of voted users.
     */
    @SerializedName("votes")
    @Expose
    val votes: Int? = null,
    @SerializedName("score")
    @Expose
    val score: Int? = null,

    /**
     * Percentage share of overall stat result.
     */
    @SerializedName("percentage")
    @Expose
    val percentage: Double? = null
)
