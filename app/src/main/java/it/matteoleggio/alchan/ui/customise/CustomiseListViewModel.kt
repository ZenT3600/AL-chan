package it.matteoleggio.alchan.ui.common.customise

import android.net.Uri
import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.data.repository.ListStyleRepository
import it.matteoleggio.alchan.helper.enums.ListType
import it.matteoleggio.alchan.helper.pojo.ListStyle
import type.MediaType

class CustomiseListViewModel(private val listStyleRepository: ListStyleRepository) : ViewModel() {

    var isInit = false
    var isImageChanged = false

    var mediaType: MediaType? = null
    var selectedImageUri: Uri? = null
    var selectedListStyle = ListStyle()
    val listTypeList = arrayListOf(ListType.LINEAR, ListType.GRID, ListType.SIMPLIFIED, ListType.ALBUM)

    val animeListStyle: ListStyle
        get() = listStyleRepository.animeListStyle

    val mangaListStyle: ListStyle
        get() = listStyleRepository.mangaListStyle

    fun saveListSettings() {
        if (mediaType == MediaType.ANIME) {
            listStyleRepository.saveAnimeListStyle(selectedListStyle)
        } else if (mediaType == MediaType.MANGA) {
            listStyleRepository.saveMangaListStyle(selectedListStyle)
        }
    }
}