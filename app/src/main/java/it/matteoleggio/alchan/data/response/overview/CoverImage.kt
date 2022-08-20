package it.matteoleggio.alchan.data.response.overview

import com.google.gson.annotations.SerializedName


data class CoverImage (

  @SerializedName("__typename" ) var _typename  : String? = null,
  @SerializedName("extraLarge" ) var extraLarge : String? = null

)