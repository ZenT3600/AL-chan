package it.matteoleggio.alchan.helper

import com.google.gson.Gson
import it.matteoleggio.alchan.data.response.AnimeStats
import it.matteoleggio.alchan.data.response.MangaStats
import it.matteoleggio.alchan.data.response.MediaRecommendations
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.reflect.Type

class JikanApiHelper {
    private val client = OkHttpClient()

    private fun makeRequest(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()

        return client.newCall(request).execute().body?.string() ?: ""
    }

    private fun makeRequestAsClass(url: String, cls: Type): Any {
        val response = makeRequest(url)
        return Gson().fromJson(response, cls)
    }

    fun getMangaStats(malId: Int): MangaStats {
        return makeRequestAsClass("https://api.jikan.moe/v4/manga/$malId/statistics", MangaStats().javaClass) as MangaStats
    }

    fun getMangaRecommendations(malId: Int): MediaRecommendations {
        return makeRequestAsClass("https://api.jikan.moe/v4/manga/$malId/recommendations", MediaRecommendations().javaClass) as MediaRecommendations
    }

    fun getAnimeStats(malId: Int): AnimeStats {
        return makeRequestAsClass("https://api.jikan.moe/v4/anime/$malId/statistics", AnimeStats().javaClass) as AnimeStats
    }

    fun getAnimeRecommendations(malId: Int): MediaRecommendations {
        return makeRequestAsClass("https://api.jikan.moe/v4/anime/$malId/recommendations", MediaRecommendations().javaClass) as MediaRecommendations
    }
}