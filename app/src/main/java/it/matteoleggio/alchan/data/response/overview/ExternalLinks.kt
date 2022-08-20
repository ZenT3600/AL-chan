package it.matteoleggio.alchan.data.response.overview

import com.google.gson.annotations.SerializedName


data class ExternalLinks (

  @SerializedName("__typename" ) var _typename : String? = null,
  @SerializedName("id"         ) var id        : Int?    = null,
  @SerializedName("url"        ) var url       : String? = null,
  @SerializedName("site"       ) var site      : String? = null

)