package com.example.gpsapp.data.model

import com.google.gson.annotations.SerializedName

data class VehicleStatusResponse(
    @SerializedName("totalVehicles")
    val total: Int,

    @SerializedName("onlineVehicles")
    val online: Int,

    @SerializedName("movingVehicles")
    val moving: Int,

    @SerializedName("idleVehicles")
    val idle: Int,

    @SerializedName("parkedVehicles")
    val parked: Int,

    @SerializedName("offlineVehicles")
    val offline: Int,

    @SerializedName("timestamp")
    val timestamp: String
)