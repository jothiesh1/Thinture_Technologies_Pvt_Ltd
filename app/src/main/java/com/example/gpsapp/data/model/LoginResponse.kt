package com.example.gpsapp.data.model

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val role: String,
    val username: String
)