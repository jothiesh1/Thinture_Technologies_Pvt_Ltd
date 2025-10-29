package com.example.gpsapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gpsapp.ui.screens.ForgotPasswordScreen
import com.example.gpsapp.ui.screens.LoginScreen
import com.example.gpsapp.ui.screens.LogoutScreen
import com.example.gpsapp.ui.screens.admin.AdminDashboardScreen
import com.example.gpsapp.ui.screens.admin.AdminDriverReportScreen
import com.example.gpsapp.ui.screens.admin.AdminEventReportScreen
import com.example.gpsapp.ui.screens.admin.AdminLiveMapScreen
import com.example.gpsapp.ui.screens.admin.AdminVehicleReportScreen
import com.example.gpsapp.ui.screens.client.ClientDashboardScreen
import com.example.gpsapp.ui.screens.client.ClientDriverReportScreen
import com.example.gpsapp.ui.screens.client.ClientEventReportScreen
import com.example.gpsapp.ui.screens.client.ClientLiveMapScreen
import com.example.gpsapp.ui.screens.client.ClientPlaybackMapScreen
import com.example.gpsapp.ui.screens.client.ClientVehicleReportScreen
import com.example.gpsapp.ui.screens.dealer.DealerDashboardScreen
import com.example.gpsapp.ui.screens.dealer.DealerDriverReportScreen
import com.example.gpsapp.ui.screens.dealer.DealerEventReportScreen
import com.example.gpsapp.ui.screens.dealer.DealerLiveMapScreen
import com.example.gpsapp.ui.screens.dealer.DealerPlaybackMapScreen
import com.example.gpsapp.ui.screens.dealer.DealerVehicleReportScreen
import com.example.gpsapp.ui.screens.superadmin.DashboardScreen
import com.example.gpsapp.ui.screens.superadmin.DriverReportScreen
import com.example.gpsapp.ui.screens.superadmin.EventReportScreen
import com.example.gpsapp.ui.screens.superadmin.LiveMapScreen
import com.example.gpsapp.ui.screens.superadmin.PlaybackMapScreen
import com.example.gpsapp.ui.screens.superadmin.VehicleReportScreen
import com.example.gpsapp.ui.screens.user.UserDashboardScreen
import com.example.gpsapp.ui.screens.user.UserDriverReportScreen
import com.example.gpsapp.ui.screens.user.UserEventReportScreen
import com.example.gpsapp.ui.screens.user.UserLiveMapScreen
import com.example.gpsapp.ui.screens.user.UserPlaybackMapScreen
import com.example.gpsapp.ui.screens.user.UserVehicleReportScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType


@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController)
        }



        //Dashboard
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController)
        }
        composable(Screen.DealerDashboard.route) {
            DealerDashboardScreen(navController)
        }
        composable(Screen.ClientDashboard.route) {
            ClientDashboardScreen(navController)
        }
        composable(Screen.UserDashboard.route) {
            UserDashboardScreen(navController)
        }


        //Live Map
        composable(Screen.LiveMap.route) {
            LiveMapScreen(navController)
        }
        composable(Screen.AdminLiveMap.route) {
            AdminLiveMapScreen(navController = navController)
        }
        composable(Screen.DealerLiveMap.route) {
            DealerLiveMapScreen(navController = navController)
        }
        composable(Screen.ClientLiveMap.route) {
            ClientLiveMapScreen(navController = navController)
        }
        composable(Screen.UserLiveMap.route) {
            UserLiveMapScreen(navController = navController)
        }


        //Playback Map
        composable(Screen.Playback.route) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            PlaybackMapScreen(navController = navController, DeviceId = deviceId)
        }
        composable(Screen.AdminPlaybackMap.route + "/{deviceId}") { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            com.example.gpsapp.ui.screens.admin.AdminPlaybackMapScreen(navController, deviceId)
        }
        composable(Screen.DealerPlaybackMap.route + "/{deviceId}") { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            DealerPlaybackMapScreen(navController = navController, deviceId = deviceId)
        }
        composable(Screen.ClientPlaybackMap.route + "/{deviceId}") { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            ClientPlaybackMapScreen(navController = navController, deviceId = deviceId)
        }
        composable(Screen.UserPlaybackMap.route + "/{deviceId}") { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            UserPlaybackMapScreen(navController = navController, deviceId = deviceId)
        }


        //Event Report
        composable(Screen.EventReport.route) {
            EventReportScreen(navController)
        }
        composable("admin_event_report") {
            AdminEventReportScreen(navController)
        }
        composable("dealer_event_report") {
            DealerEventReportScreen(navController)
        }
        composable(Screen.ClientEventReport.route) {
            ClientEventReportScreen(navController)
        }
        composable(Screen.UserEventReport.route) {
            UserEventReportScreen(navController)
        }


        //Vehicle Report
        composable(Screen.VehicleReport.route) {
            VehicleReportScreen(navController)
        }
        composable("admin_vehicle_report") {
            AdminVehicleReportScreen(navController)
        }
        composable("dealer_vehicle_report") {
            DealerVehicleReportScreen(navController)
        }
        composable(Screen.ClientVehicleReport.route) {
            ClientVehicleReportScreen(navController)
        }
        composable(Screen.UserVehicleReport.route) {
            UserVehicleReportScreen(navController)
        }


        //Driver Report
        composable(Screen.DriverReport.route) {
            DriverReportScreen(navController)
        }
        composable("admin_driver_report") {
            AdminDriverReportScreen(navController)
        }
        composable("dealer_driver_report") {
            DealerDriverReportScreen(navController)
        }
        composable(Screen.ClientDriverReport.route) {
            ClientDriverReportScreen(navController)
        }
        composable(Screen.UserDriverReport.route) {
            UserDriverReportScreen(navController)
        }



        composable(
            route = "logout/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "superadmin"
            LogoutScreen(navController, role = role)
        }
    }
}
