package it.matteoleggio.alchan.data.network.header

import it.matteoleggio.alchan.data.localstorage.TempStorageManager
import okhttp3.Interceptor
import okhttp3.Response

class SpotifyHeaderInterceptorImpl(private val tempStorageManager: TempStorageManager) : SpotifyHeaderInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        proceed(
            request().newBuilder()
                .addHeader("Authorization", "Bearer ${tempStorageManager.spotifyAccessToken}")
                .build()
        )
    }
}