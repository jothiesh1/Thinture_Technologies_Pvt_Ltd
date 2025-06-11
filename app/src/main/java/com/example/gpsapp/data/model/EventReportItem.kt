package com.example.gpsapp.data.model

data class EventReportItem(
    val id: Int,
    val deviceId: String,
    val timestamp: String,
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val ignition: String,
    val vehicleStatus: String,
    val additionalData: String
)
