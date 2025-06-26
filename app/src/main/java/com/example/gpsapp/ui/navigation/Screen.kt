package com.example.gpsapp.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object AdminDashboard : Screen("admin_dashboard")
    object DealerDashboard : Screen("dealer_dashboard")
    object ClientDashboard : Screen("client_dashboard")
    object UserDashboard : Screen("user_dashboard")

    object LiveMap : Screen("live_map")
    object Playback : Screen("playback_map/{deviceId}") {
        fun createRoute(deviceId: String) = "playback_map/$deviceId"
    }
    object EventReport : Screen("event_report")
    object VehicleReport : Screen("vehicle_report")
    object DriverReport : Screen("driver_report")
    object Logout : Screen("logout")
}
