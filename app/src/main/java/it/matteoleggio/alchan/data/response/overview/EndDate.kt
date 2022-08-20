package it.matteoleggio.alchan.data.response.overview

import com.google.gson.annotations.SerializedName


data class EndDate (

  @SerializedName("__typename" ) var _typename : String? = null,
  @SerializedName("year"       ) var year      : String? = null,
  @SerializedName("month"      ) var month     : String? = null,
  @SerializedName("day"        ) var day       : String? = null

)