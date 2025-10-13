package com.example.gpsapp.data.model

data class VehicleViolationResponse(
    val deviceId: String,
    val vehicleNumber: String,
    val vehicleType: String,
    val dealerName: String,
    val ownerName: String,
    val additionalData: String?,
    val timestamp: String,
    val latitude: String,
    val longitude: String,
    val speed: String,
    val status: String
)
