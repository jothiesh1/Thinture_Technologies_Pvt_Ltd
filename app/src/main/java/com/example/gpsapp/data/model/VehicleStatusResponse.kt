package com.example.gpsapp.data.model

data class VehicleStatusResponse(
    val total: Int,
    val online: Int,
    val moving: Int,
    val idle: Int,
    val parked: Int,
    val offline: Int,
    val timestamp: String
)
