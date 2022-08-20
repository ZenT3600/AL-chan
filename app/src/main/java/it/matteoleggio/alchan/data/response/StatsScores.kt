package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.SerializedName


data class Scores (

  @SerializedName("score"      ) var score      : Int?    = null,
  @SerializedName("votes"      ) var votes      : Int?    = null,
  @SerializedName("percentage" ) var percentage : Double? = null

)