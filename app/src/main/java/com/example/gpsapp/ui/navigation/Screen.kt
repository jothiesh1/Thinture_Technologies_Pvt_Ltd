package com.example.gpsapp.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object ForgotPassword : Screen("forgot_password")


    //Dashboard objects
    object Dashboard : Screen("dashboard")
    object AdminDashboard : Screen("admin_dashboard")
    object DealerDashboard : Screen("dealer_dashboard")
    object ClientDashboard : Screen("client_dashboard")
    object UserDashboard : Screen("user_dashboard")


    //Live Map Objects
    object LiveMap : Screen("live_map")
    object AdminLiveMap : Screen("admin_live_map")
    object DealerLiveMap : Screen("dealer_live_map")
    object ClientLiveMap : Screen("client_live_map")
    object UserLiveMap : Screen("user_live_map")


    //Playback Map Objects
    object Playback : Screen("playback_map/{deviceId}") {
        fun createRoute(deviceId: String) = "playback_map/$deviceId"
    }
    object AdminPlaybackMap : Screen("admin_playback_map") {
        fun createRoute(deviceId: String) = "$route/$deviceId"
    }
    object DealerPlaybackMap : Screen("dealer_playback_map") {
        fun createRoute(deviceId: String) = "$route/$deviceId"
    }
    object ClientPlaybackMap : Screen("client_playback_map") {
        fun createRoute(deviceId: String) = "$route/$deviceId"
    }
    object UserPlaybackMap : Screen("user_playback_map") {
        fun createRoute(deviceId: String) = "$route/$deviceId"
    }


    //Event Report Objects
    object EventReport : Screen("event_report")
    object AdminEventReport : Screen("admin_event_report")
    object DealerEventReport : Screen("dealer_event_report")
    object ClientEventReport : Screen("client_event_report")
    object UserEventReport : Screen("user_event_report")


    //Vehicle Report Objects
    object VehicleReport : Screen("vehicle_report")
    object AdminVehicleReport : Screen("admin_vehicle_report")
    object DealerVehicleReport : Screen("dealer_vehicle_report")
    object ClientVehicleReport : Screen("client_vehicle_report")
    object UserVehicleReport : Screen("user_vehicle_report")


    //Driver Report Objects
    object DriverReport : Screen("driver_report")
    object AdminDriverReport : Screen("admin_driver_report")
    object DealerDriverReport : Screen("dealer_driver_report")
    object ClientDriverReport : Screen("client_driver_report")
    object UserDriverReport : Screen("user_driver_report")



    object Logout : Screen("logout/{role}") {
        fun createRoute(role: String) = "logout/$role"
    }}