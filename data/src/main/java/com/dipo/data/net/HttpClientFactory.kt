package com.dipo.data.net

import android.R.attr.level
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object HttpClientFactory {

    fun provideOkHttpClient(debug: Boolean): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .dispatcher(Dispatcher().apply { maxRequests = 32; maxRequestsPerHost = 8 })
            .connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
            .build()
    }
}