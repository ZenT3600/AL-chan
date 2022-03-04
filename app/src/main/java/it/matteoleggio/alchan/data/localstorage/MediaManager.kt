package it.matteoleggio.alchan.data.localstorage

import it.matteoleggio.alchan.data.response.MediaTagCollection

interface MediaManager {
    val genreList: List<String?>
    val genreListLastRetrieved: Long?
    val tagList: List<MediaTagCollection>
    val tagListLastRetrieved: Long?

    val mostTrendingAnimeBanner: String?

    fun setGenreList(genres: List<String?>)
    fun setTagList(tags: List<MediaTagCollection>)

    fun setMostTrendingAnimeBanner(url: String?)
}