package it.matteoleggio.alchan.data.response.overview

import com.google.gson.annotations.SerializedName


data class Edges (

  @SerializedName("__typename" ) var _typename : String? = null,
  @SerializedName("node"       ) var node      : Node?   = Node()

)