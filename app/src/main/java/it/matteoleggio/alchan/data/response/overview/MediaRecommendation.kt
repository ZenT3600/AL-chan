package it.matteoleggio.alchan.data.response.overview

import com.google.gson.annotations.SerializedName


data class MediaRecommendation (

  @SerializedName("__typename"   ) var _typename    : String?     = null,
  @SerializedName("id"           ) var id           : Int?        = null,
  @SerializedName("title"        ) var title        : Title?      = Title(),
  @SerializedName("seasonYear"   ) var seasonYear   : Int?        = null,
  @SerializedName("format"       ) var format       : String?     = null,
  @SerializedName("type"         ) var type         : String?     = null,
  @SerializedName("averageScore" ) var averageScore : Int?        = null,
  @SerializedName("favourites"   ) var favourites   : Int?        = null,
  @SerializedName("coverImage"   ) var coverImage   : CoverImage? = CoverImage()

)