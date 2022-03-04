package it.matteoleggio.alchan.data.repository

import androidx.lifecycle.LiveData
import it.matteoleggio.alchan.helper.enums.ListType
import it.matteoleggio.alchan.helper.pojo.ListStyle

interface ListStyleRepository {
    val animeListStyle: ListStyle
    val mangaListStyle: ListStyle

    val animeListStyleLiveData: LiveData<ListStyle>
    val mangaListStyleLiveData: LiveData<ListStyle>

    fun saveAnimeListStyle(newAnimeListStyle: ListStyle)
    fun saveMangaListStyle(newMangaListStyle: ListStyle)
}