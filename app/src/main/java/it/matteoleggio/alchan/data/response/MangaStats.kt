package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.SerializedName


data class MangaStats (

  @SerializedName("data" ) var data : MangaStatsData? = MangaStatsData()

)