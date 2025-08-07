package com.example.gpsapp.data.model

data class ViolationReportResponse(
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

data class AdditionalData(
    val violationType: String?
)
