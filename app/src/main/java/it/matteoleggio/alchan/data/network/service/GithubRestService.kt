package it.matteoleggio.alchan.data.network.service

import it.matteoleggio.alchan.data.response.Announcement
import it.matteoleggio.alchan.helper.Constant
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface GithubRestService {
    @GET("docs/json/announcement.json")
    fun getAnnouncement() : Call<Announcement>

    companion object {
        operator fun invoke(): GithubRestService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(Constant.RAW_GITHUB_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GithubRestService::class.java)
        }
    }
}