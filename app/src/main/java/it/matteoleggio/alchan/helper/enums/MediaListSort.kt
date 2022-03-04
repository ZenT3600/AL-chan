package it.matteoleggio.alchan.helper.enums

enum class MediaListSort(val value: String) {
    TITLE("title"),
    SCORE("score"),
    PROGRESS("progress"),
    LAST_UPDATED("updatedAt"),
    LAST_ADDED("id"),
    START_DATE("startDate"),
    COMPLETED_DATE("completedDate"),
    RELEASE_DATE("releaseDate"),
    AVERAGE_SCORE("averageScore"),
    POPULARITY("popularity"),
    PRIORITY("priority"),
    NEXT_AIRING("nextAiring")
}