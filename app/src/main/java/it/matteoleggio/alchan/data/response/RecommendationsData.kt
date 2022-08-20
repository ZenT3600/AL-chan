package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.SerializedName


data class RecommendationsData (

  @SerializedName("entry" ) var entry : RecommendationsEntry? = RecommendationsEntry(),
  @SerializedName("url" ) var url: String? = "",
  @SerializedName("votes" ) var votes: Int? = 0

)