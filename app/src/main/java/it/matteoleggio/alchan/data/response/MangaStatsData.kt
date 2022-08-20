package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.SerializedName


data class MangaStatsData (

  @SerializedName("reading"      ) var reading    : Int?              = null,
  @SerializedName("completed"    ) var completed  : Int?              = null,
  @SerializedName("on_hold"      ) var onHold     : Int?              = null,
  @SerializedName("dropped"      ) var dropped    : Int?              = null,
  @SerializedName("plan_to_read" ) var planToRead : Int?              = null,
  @SerializedName("total"        ) var total      : Int?              = null,
  @SerializedName("scores"       ) var scores     : ArrayList<Scores> = arrayListOf()

)