package com.example.gpsapp.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object LiveMap : Screen("live_map")
    object Playback : Screen("playback_map")
    object EventReport : Screen("event_report")
    object VehicleReport : Screen("vehicle_report")
    object DriverReport : Screen("driver_report")
    object Logout : Screen("logout")
}