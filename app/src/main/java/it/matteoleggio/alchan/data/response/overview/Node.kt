package it.matteoleggio.alchan.data.response.overview

import com.google.gson.annotations.SerializedName


data class Node (

  @SerializedName("__typename"          ) var _typename           : String?              = null,
  @SerializedName("id"                  ) var id                  : Int?                 = null,
  @SerializedName("rating"              ) var rating              : Int?                 = null,
  @SerializedName("userRating"          ) var userRating          : String?              = null,
  @SerializedName("mediaRecommendation" ) var mediaRecommendation : MediaRecommendation? = MediaRecommendation()

)