package it.matteoleggio.alchan.data.response.overview

import com.google.gson.annotations.SerializedName


data class Title (

  @SerializedName("__typename"    ) var _typename     : String? = null,
  @SerializedName("userPreferred" ) var userPreferred : String? = null

)