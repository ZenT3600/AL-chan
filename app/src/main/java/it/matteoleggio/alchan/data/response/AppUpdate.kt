package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.SerializedName


data class AppUpdate (

    @SerializedName("version"      ) var version     : Int?              = null,
    @SerializedName("url"          ) var url         : String?           = null,
    @SerializedName("new_features" ) var newFeatures : ArrayList<String> = arrayListOf()

)