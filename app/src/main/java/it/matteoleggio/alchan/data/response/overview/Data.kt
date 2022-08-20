package it.matteoleggio.alchan.data.response.overview

import com.google.gson.annotations.SerializedName


data class Data (

  @SerializedName("Media" ) var Media : Media? = Media()

)