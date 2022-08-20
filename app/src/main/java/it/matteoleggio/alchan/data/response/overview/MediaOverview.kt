package it.matteoleggio.alchan.data.response.overview

import com.google.gson.annotations.SerializedName


data class MediaOverview (

  @SerializedName("data" ) var data : Data? = Data()

)