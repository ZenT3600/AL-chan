package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.SerializedName


data class AnimeStats (

    @SerializedName("data" ) var data : AnimeStatsData? = AnimeStatsData()

)