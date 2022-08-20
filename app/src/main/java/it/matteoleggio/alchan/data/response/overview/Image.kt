package it.matteoleggio.alchan.data.response.overview

import com.google.gson.annotations.SerializedName


data class Image (

  @SerializedName("__typename" ) var _typename : String? = null,
  @SerializedName("large"      ) var large     : String? = null

)