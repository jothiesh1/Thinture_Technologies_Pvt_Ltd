package com.example.thinturetechnologiespvtltd.ui.navigation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.thinturetechnologiespvtltd.ui.screens.*

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
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(Screen.LiveMap.route) {
            LiveMapScreen(navController)
        }
        composable(Screen.Playback.route) {
            PlaybackMapScreen(navController)
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
