package it.matteoleggio.alchan.ui.search

import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.helper.enums.BrowsePage

class SearchViewModel : ViewModel() {

    var selectedPage = BrowsePage.ANIME

    val searchPageList = listOf(
        BrowsePage.ANIME, BrowsePage.MANGA, BrowsePage.CHARACTER, BrowsePage.STAFF, BrowsePage.STUDIO, BrowsePage.USER
    )
}