package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.SerializedName

class HatedCharacter(
    @SerializedName("image"        ) var image       : String?           = null,
    @SerializedName("id"           ) var id          : Int?             = null
)