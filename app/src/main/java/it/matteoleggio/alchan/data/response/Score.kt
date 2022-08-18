package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Score(
    /**
     * Score entry for score 1.
     */
    @SerializedName("1")
    @Expose
    val score1: ScoreEntry? = null,

    /**
     * Score entry for score 2.
     */
    @SerializedName("2")
    @Expose
    val score2: ScoreEntry? = null,

    /**
     * Score entry for score 3.
     */
    @SerializedName("3")
    @Expose
    val score3: ScoreEntry? = null,

    /**
     * Score entry for score 4.
     */
    @SerializedName("4")
    @Expose
    val score4: ScoreEntry? = null,

    /**
     * Score entry for score 5.
     */
    @SerializedName("5")
    @Expose
    val score5: ScoreEntry? = null,

    /**
     * Score entry for score 6.
     */
    @SerializedName("6")
    @Expose
    val score6: ScoreEntry? = null,

    /**
     * Score entry for score 7.
     */
    @SerializedName("7")
    @Expose
    val score7: ScoreEntry? = null,

    /**
     * Score entry for score 8.
     */
    @SerializedName("8")
    @Expose
    val score8: ScoreEntry? = null,

    /**
     * Score entry for score 9.
     */
    @SerializedName("9")
    @Expose
    val score9: ScoreEntry? = null,

    /**
     * Score entry for score 10.
     */
    @SerializedName("10")
    @Expose
    val score10: ScoreEntry? = null
)