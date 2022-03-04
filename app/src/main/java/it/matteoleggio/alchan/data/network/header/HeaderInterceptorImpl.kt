package it.matteoleggio.alchan.data.network.header

import it.matteoleggio.alchan.data.localstorage.LocalStorage
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptorImpl(private val localStorage: LocalStorage) :
    HeaderInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        proceed(
            request().newBuilder()
                .addHeader("Authorization", "Bearer ${localStorage.bearerToken}")
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build()
        )
    }
}