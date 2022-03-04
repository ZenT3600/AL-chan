package it.matteoleggio.alchan.helper.pojo

import it.matteoleggio.alchan.helper.enums.BrowsePage

class FavoriteItem(
    val id: Int?,
    val name: String?,
    val image: String?,
    val favouriteOrder: Int,
    val browsePage: BrowsePage
)