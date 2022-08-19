package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.SerializedName


data class Images (

  @SerializedName("jpg"  ) var jpg  : Jpg?  = Jpg(),
  @SerializedName("webp" ) var webp : Webp? = Webp()

)