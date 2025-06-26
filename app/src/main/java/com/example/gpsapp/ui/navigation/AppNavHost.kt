package com.example.gpsapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gpsapp.ui.screens.LoginScreen
import com.example.gpsapp.ui.screens.admin.AdminDashboardScreen
import com.example.gpsapp.ui.screens.client.ClientDashboardScreen
import com.example.gpsapp.ui.screens.dealer.DealerDashboardScreen
import com.example.gpsapp.ui.screens.superadmin.DashboardScreen
import com.example.gpsapp.ui.screens.superadmin.DriverReportScreen
import com.example.gpsapp.ui.screens.superadmin.EventReportScreen
import com.example.gpsapp.ui.screens.superadmin.LiveMapScreen
import com.example.gpsapp.ui.screens.LogoutScreen
import com.example.gpsapp.ui.screens.superadmin.PlaybackMapScreen
import com.example.gpsapp.ui.screens.superadmin.VehicleReportScreen
import com.example.gpsapp.ui.screens.user.UserDashboardScreen

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

        // Super Admin
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }

        // Admin
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController)
        }

        // Dealer
        composable(Screen.DealerDashboard.route) {
            DealerDashboardScreen(navController)
        }

        // Client
        composable(Screen.ClientDashboard.route) {
            ClientDashboardScreen(navController)
        }

        // User
        composable(Screen.UserDashboard.route) {
            UserDashboardScreen(navController)
        }

        composable(Screen.LiveMap.route) {
            LiveMapScreen(navController)
        }

        composable("playback_map/{deviceId}") { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            PlaybackMapScreen(navController, deviceId)
        }

        composable(Screen.EventReport.route) {
            EventReportScreen(navController)
        }

        composable(Screen.VehicleReport.route) {
            VehicleReportScreen(navController)
        }

        composable(Screen.DriverReport.route) {
            DriverReportScreen(navController)
        }

        composable(Screen.Logout.route) {
            LogoutScreen(navController)
        }
    }
}
