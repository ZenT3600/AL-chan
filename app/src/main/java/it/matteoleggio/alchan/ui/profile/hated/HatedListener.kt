package it.matteoleggio.alchan.ui.profile.hated

import it.matteoleggio.alchan.helper.enums.BrowsePage

interface HatedListener {
    fun passSelectedItem(id: Int, browsePage: BrowsePage)
}