package it.matteoleggio.alchan.data.network

import com.apollographql.apollo.ApolloClient
import it.matteoleggio.alchan.data.network.header.HeaderInterceptor
import it.matteoleggio.alchan.helper.Constant
import okhttp3.OkHttpClient
import type.CustomType
import java.util.concurrent.TimeUnit

class ApolloHandler(private val headerInterceptor: HeaderInterceptor) {

    private val okHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(headerInterceptor)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    val apolloClient = ApolloClient.builder()
        .serverUrl(Constant.ANILIST_API_URL)
        .okHttpClient(okHttpClient)
        .addCustomTypeAdapter(CustomType.JSON, JsonAdapter())
        .addCustomTypeAdapter(CustomType.COUNTRYCODE, CountryCodeAdapter())
        .build()
}