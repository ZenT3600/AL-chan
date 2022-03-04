package it.matteoleggio.alchan.data.localstorage

import it.matteoleggio.alchan.helper.enums.ListType
import it.matteoleggio.alchan.helper.pojo.ListStyle

interface ListStyleManager {
    val animeListStyle: ListStyle
    val mangaListStyle: ListStyle

    fun saveAnimeListStyle(newAnimeListStyle: ListStyle)
    fun saveMangaListStyle(newMangaListStyle: ListStyle)
}