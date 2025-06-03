package com.example.gpsapp.network

import com.example.gpsapp.data.model.LoginRequest
import com.example.gpsapp.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/android/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
