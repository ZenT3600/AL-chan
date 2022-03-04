package it.matteoleggio.alchan.data.network.service

import it.matteoleggio.alchan.data.network.header.SpotifyAuthHeaderInterceptor
import it.matteoleggio.alchan.data.response.SpotifyAccessToken
import it.matteoleggio.alchan.helper.Constant
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface SpotifyAuthRestService {

    @FormUrlEncoded
    @POST("token")
    fun getAccessToken(@Field("grant_type") grantType: String): Call<SpotifyAccessToken>

    companion object {
        operator fun invoke(spotifyAuthHeaderInterceptor: SpotifyAuthHeaderInterceptor): SpotifyAuthRestService {
            val okHttpClient = OkHttpClient.Builder()
                .addNetworkInterceptor(spotifyAuthHeaderInterceptor)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(Constant.SPOTIFY_AUTH_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SpotifyAuthRestService::class.java)
        }
    }
}