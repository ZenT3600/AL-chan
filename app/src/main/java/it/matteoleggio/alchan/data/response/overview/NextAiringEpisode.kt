package it.matteoleggio.alchan.data.response.overview

import com.google.gson.annotations.SerializedName


data class NextAiringEpisode (

  @SerializedName("__typename" ) var _typename : String? = null,
  @SerializedName("airingAt"   ) var airingAt  : Int?    = null,
  @SerializedName("episode"    ) var episode   : Int?    = null

)