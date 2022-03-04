package it.matteoleggio.alchan.data.response

class AiringSchedule(
    val id: Int,
    val airingAt: Int,
    val timeUntilAiring: Int,
    val episode: Int,
    val mediaId: Int
)