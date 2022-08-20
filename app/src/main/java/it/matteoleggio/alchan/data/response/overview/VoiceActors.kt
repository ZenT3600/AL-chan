package it.matteoleggio.alchan.data.response.overview

import com.google.gson.annotations.SerializedName


data class VoiceActors (

  @SerializedName("__typename" ) var _typename : String? = null,
  @SerializedName("id"         ) var id        : Int?    = null,
  @SerializedName("name"       ) var name      : Name?   = Name(),
  @SerializedName("language"   ) var language  : String? = null,
  @SerializedName("image"      ) var image     : Image?  = Image()

)