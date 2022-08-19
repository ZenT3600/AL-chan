package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.SerializedName


data class Data (

  @SerializedName("entry" ) var entry : Entry? = Entry()

)