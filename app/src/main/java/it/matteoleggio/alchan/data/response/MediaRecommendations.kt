package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.SerializedName


data class MediaRecommendations (

  @SerializedName("data" ) var data : ArrayList<RecommendationsData> = arrayListOf()

)