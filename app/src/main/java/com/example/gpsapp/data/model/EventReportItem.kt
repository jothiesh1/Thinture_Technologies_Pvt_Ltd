package com.example.gpsapp.data.model

data class EventReportItem(
    val id: Int,
    val deviceId: String?,
    val timestamp: String?,
    val latitude: String?,
    val longitude: String?,
    val speed: Double,
    val ignition: String,
    val vehicleStatus: String,
    val additionalData: String
)
