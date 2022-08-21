package it.matteoleggio.alchan.data.response

import com.google.gson.annotations.SerializedName

class UserResponse(
    @SerializedName("data"        ) var data       : User?           = null,
)