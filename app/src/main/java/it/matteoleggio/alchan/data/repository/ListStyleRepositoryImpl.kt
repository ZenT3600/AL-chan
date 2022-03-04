package it.matteoleggio.alchan.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.matteoleggio.alchan.data.localstorage.ListStyleManager
import it.matteoleggio.alchan.helper.enums.ListType
import it.matteoleggio.alchan.helper.pojo.ListStyle

class ListStyleRepositoryImpl(private val listStyleManager: ListStyleManager) : ListStyleRepository {

    override val animeListStyle: ListStyle
        get() = listStyleManager.animeListStyle

    override val mangaListStyle: ListStyle
        get() = listStyleManager.mangaListStyle

    private val _animeListStyleLiveData = MutableLiveData<ListStyle>()
    override val animeListStyleLiveData: LiveData<ListStyle>
        get() = _animeListStyleLiveData

    private val _mangaListStyleLiveData = MutableLiveData<ListStyle>()
    override val mangaListStyleLiveData: LiveData<ListStyle>
        get() = _mangaListStyleLiveData

    override fun saveAnimeListStyle(newAnimeListStyle: ListStyle) {
        listStyleManager.saveAnimeListStyle(newAnimeListStyle)
        _animeListStyleLiveData.postValue(newAnimeListStyle)
    }

    override fun saveMangaListStyle(newMangaListStyle: ListStyle) {
        listStyleManager.saveMangaListStyle(newMangaListStyle)
        _mangaListStyleLiveData.postValue(newMangaListStyle)
    }
}