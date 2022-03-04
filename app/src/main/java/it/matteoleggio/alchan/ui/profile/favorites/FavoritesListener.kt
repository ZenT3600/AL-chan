package it.matteoleggio.alchan.ui.profile.favorites

import it.matteoleggio.alchan.helper.enums.BrowsePage

interface FavoritesListener {
    fun passSelectedItem(id: Int, browsePage: BrowsePage)
}