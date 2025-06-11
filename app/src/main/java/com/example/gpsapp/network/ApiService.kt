package com.example.gpsapp.network

import com.example.gpsapp.data.model.EventReportItem
import com.example.gpsapp.data.model.LoginRequest
import com.example.gpsapp.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // Login endpoint
    @POST("api/android/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Event report endpoint
    @GET("api/android/vehicle/history1/{deviceId}")
    suspend fun getEventReport(
        @Path("deviceId") deviceId: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<List<EventReportItem>>

    @GET("api/android/vehicle/history/{deviceId}")
    suspend fun getPlaybackData(
        @Path("deviceId") deviceId: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): List<PlaybackPointDto>
}
