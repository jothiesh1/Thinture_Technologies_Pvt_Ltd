package com.example.gpsapp.network

data class PlaybackPointDto(
    val id: Int,
    val admin: Any?,            // Change to a proper type if defined
    val dealer: Any?,           // Change to a proper type if defined
    val client: Any?,
    val user: Any?,
    val timestamp: String,
    val latitude: String,
    val longitude: String,
    val speed: Double,
    val course: String?,        // String in your JSON, so make it nullable
    val additionalData: String?,
    val sequenceNumber: String?,
    val ignition: String?,
    val vehicleStatus: String?,
    val status: String?,
    val timeIntervals: String?,
    val distanceItervals: String?,
    val gsmStrength: String?,
    val panic: String?,
    val serialNo: String?,
    val dealerName: String?,
    val imei: String?,
    val device_id: String? = null
)
