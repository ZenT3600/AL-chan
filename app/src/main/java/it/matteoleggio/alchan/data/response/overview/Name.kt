package it.matteoleggio.alchan.data.response.overview

import com.google.gson.annotations.SerializedName


data class Name (

  @SerializedName("__typename" ) var _typename : String? = null,
  @SerializedName("full"       ) var full      : String? = null

)