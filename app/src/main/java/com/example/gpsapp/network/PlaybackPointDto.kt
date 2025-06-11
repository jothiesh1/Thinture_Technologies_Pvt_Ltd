package com.example.gpsapp.network

data class PlaybackPointDto(
    val latitude: Double,
    val longitude: Double,
    val speed: Int,
    val timestamp: String,
    val course: Int,
    val device_id: String
)
