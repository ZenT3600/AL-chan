package it.matteoleggio.alchan.data.response.overview

import com.google.gson.annotations.SerializedName


data class StartDate (

  @SerializedName("__typename" ) var _typename : String? = null,
  @SerializedName("year"       ) var year      : Int?    = null,
  @SerializedName("month"      ) var month     : Int?    = null,
  @SerializedName("day"        ) var day       : Int?    = null

)