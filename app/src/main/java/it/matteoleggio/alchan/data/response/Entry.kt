package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.SerializedName


data class Entry (

  @SerializedName("mal_id" ) var malId  : Int?    = null,
  @SerializedName("url"    ) var url    : String? = null,
  @SerializedName("images" ) var images : Images? = Images(),
  @SerializedName("title"  ) var title  : String? = null

)