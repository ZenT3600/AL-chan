package it.matteoleggio.alchan.data.localstorage

interface InfoManager {
    val lastAnnouncementId: Int?

    fun setLastAnnouncementId(value: Int)
}