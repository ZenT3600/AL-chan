package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.SerializedName


data class AnimeStatsData (

  @SerializedName("watching"      ) var watching    : Int?              = null,
  @SerializedName("completed"    ) var completed  : Int?              = null,
  @SerializedName("on_hold"      ) var onHold     : Int?              = null,
  @SerializedName("dropped"      ) var dropped    : Int?              = null,
  @SerializedName("plan_to_watch" ) var planToWatch : Int?              = null,
  @SerializedName("total"        ) var total      : Int?              = null,
  @SerializedName("scores"       ) var scores     : ArrayList<Scores> = arrayListOf()

)