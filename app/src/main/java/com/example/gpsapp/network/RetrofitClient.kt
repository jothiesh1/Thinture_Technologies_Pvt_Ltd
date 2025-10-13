package com.example.gpsapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://43.205.58.131:8183/"

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Timber.tag("HTTP").d(message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY // Enable logging to debug API response times
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)   // ⬅️ Increased timeout
        .readTimeout(60, TimeUnit.SECONDS)      // ⬅️ Increased timeout
        .writeTimeout(60, TimeUnit.SECONDS)     // ⬅️ Added for safety
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val playbackApiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
