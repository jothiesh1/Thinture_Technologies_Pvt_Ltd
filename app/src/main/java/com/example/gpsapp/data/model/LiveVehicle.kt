package com.example.gpsapp.data.model

data class LiveVehicle(
    val vehicleId: Int,
    val deviceId: String,
    val vehicleNumber: String,
    val vehicleType: String,
    val ownerName: String,
    val imei: String,
    val latitude: Double,
    val longitude: Double,
    val speed: String,
    val course: String,
    val timestamp: String,
    val ignition: String,
    val vehicleStatus: String,
    val status: String,
    val liveStatus: String,
    val statusColor: String,
    val statusIcon: String,
    val isOnline: Boolean,
    val lastUpdateMinutes: Int
)
