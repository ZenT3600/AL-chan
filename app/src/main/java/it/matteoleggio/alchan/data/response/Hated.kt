package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.SerializedName

class Hated(
    @SerializedName("characters"        ) var characters       : List<HatedCharacter>?           = null,
)