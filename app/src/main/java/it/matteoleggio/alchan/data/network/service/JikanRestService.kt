package it.matteoleggio.alchan.data.network.service

import it.matteoleggio.alchan.data.response.AnimeDetails
import it.matteoleggio.alchan.data.response.AnimeVideo
import it.matteoleggio.alchan.data.response.MangaDetails
import it.matteoleggio.alchan.helper.Constant
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface JikanRestService {

    @GET("manga/{malId}")
    fun getMangaDetails(@Path("malId") malId: Int): Call<MangaDetails>

    @GET("anime/{malId}/videos")
    fun getAnimeVideos(@Path("malId") malId: Int): Call<AnimeVideo>

    @GET("anime/{malId}")
    fun getAnimeDetails(@Path("malId") malId: Int): Call<AnimeDetails>

    companion object {
        operator fun invoke(): JikanRestService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(Constant.JIKAN_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(JikanRestService::class.java)
        }
    }
}