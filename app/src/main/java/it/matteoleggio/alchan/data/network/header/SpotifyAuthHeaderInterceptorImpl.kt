package it.matteoleggio.alchan.data.network.header

import it.matteoleggio.alchan.data.localstorage.TempStorageManager
import it.matteoleggio.alchan.data.repository.InfoRepository
import okhttp3.Interceptor
import okhttp3.Response

class SpotifyAuthHeaderInterceptorImpl(private val tempStorageManager: TempStorageManager) : SpotifyAuthHeaderInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        proceed(
            request().newBuilder()
                .addHeader("Authorization", tempStorageManager.spotifyKey ?: "")
                .build()
        )
    }
}