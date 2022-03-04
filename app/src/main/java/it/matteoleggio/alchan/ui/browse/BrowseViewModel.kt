package it.matteoleggio.alchan.ui.browse

import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.data.repository.BrowseRepository

class BrowseViewModel(private val browseRepository: BrowseRepository) : ViewModel() {

    val idFromNameData by lazy {
        browseRepository.idFromNameData
    }

    fun getIdFromName(name: String) {
        browseRepository.getIdFromName(name)
    }
}