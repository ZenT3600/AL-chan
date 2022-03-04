package it.matteoleggio.alchan.ui.mangalist.list

import it.matteoleggio.alchan.data.response.Media
import it.matteoleggio.alchan.data.response.MediaList

interface MangaListListener {
    fun openEditor(entryId: Int)
    fun openScoreDialog(mediaList: MediaList)
    fun openProgressDialog(mediaList: MediaList, isVolume: Boolean = false)
    fun incrementProgress(mediaList: MediaList, isVolume: Boolean = false)
    fun openBrowsePage(media: Media)
    fun showDetail(entryId: Int)
}