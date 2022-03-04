package it.matteoleggio.alchan.ui.seasonal

import it.matteoleggio.alchan.data.response.SeasonalAnime

interface SeasonalListener {
    fun openDetail(seasonalAnime: SeasonalAnime)
    fun openAnime(id: Int)
    fun addToPlanning(id: Int)
}