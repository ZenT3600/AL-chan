package it.matteoleggio.alchan.data.response.overview

import com.google.gson.annotations.SerializedName


data class Tags (

  @SerializedName("__typename"       ) var _typename        : String?  = null,
  @SerializedName("id"               ) var id               : Int?     = null,
  @SerializedName("name"             ) var name             : String?  = null,
  @SerializedName("description"      ) var description      : String?  = null,
  @SerializedName("rank"             ) var rank             : Int?     = null,
  @SerializedName("isGeneralSpoiler" ) var isGeneralSpoiler : Boolean? = null,
  @SerializedName("isMediaSpoiler"   ) var isMediaSpoiler   : Boolean? = null,
  @SerializedName("isAdult"          ) var isAdult          : Boolean? = null

)