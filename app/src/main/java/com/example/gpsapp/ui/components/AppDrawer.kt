package com.example.gpsapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.gpsapp.R
import com.example.gpsapp.ui.navigation.Screen
@Composable
fun AppDrawer(
    navController: NavController,
    onCloseDrawer: () -> Unit,
    role: String
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(Color(0xFFEFF3F9))
            .padding(16.dp)
    ) {
        DrawerHeader()
        Spacer(modifier = Modifier.height(24.dp))

        when (role.lowercase()) {
            "superadmin" -> {
                DrawerItem(
                    "Dashboard",
                    Screen.Dashboard.route,
                    currentRoute,
                    navController,
                    onCloseDrawer
                )
                DrawerItem(
                    "Live Map",
                    Screen.LiveMap.route,
                    currentRoute,
                    navController,
                    onCloseDrawer
                )
                DrawerItem(
                    "Playback Map",
                    Screen.Playback.route,
                    currentRoute,
                    navController,
                    onCloseDrawer
                )
                Spacer(modifier = Modifier.height(8.dp))
                ReportsSection(currentRoute, navController, onCloseDrawer)
            }

            "admin" -> {
                DrawerItem(
                    "Dashboard",
                    Screen.AdminDashboard.route,
                    currentRoute,
                    navController,
                    onCloseDrawer
                )
                DrawerItem(
                    label = "Live Map",
                    route = Screen.AdminLiveMap.route,
                    currentRoute = currentRoute,
                    navController = navController,
                    onCloseDrawer = onCloseDrawer
                )
                DrawerItem(
                    label = "Playback Map",
                    route = Screen.AdminPlaybackMap.createRoute("admin"),
                    currentRoute = currentRoute,
                    navController = navController,
                    onCloseDrawer = onCloseDrawer
                )
                ReportsSection(currentRoute, navController, onCloseDrawer)
            }

            "dealer" -> {
                DrawerItem(
                    "Dashboard",
                    Screen.DealerDashboard.route,
                    currentRoute,
                    navController,
                    onCloseDrawer
                )
                DrawerItem(
                    "Live Map",
                    Screen.DealerLiveMap.route,
                    currentRoute,
                    navController,
                    onCloseDrawer
                )

                DrawerItem(
                    "Playback Map",
                    route = Screen.AdminPlaybackMap.createRoute("dealer"),
                    currentRoute,
                    navController,
                    onCloseDrawer
                )
                ReportsSection(currentRoute, navController, onCloseDrawer)
            }

            "client" -> {
                DrawerItem(
                    "Dashboard",
                    Screen.ClientDashboard.route,
                    currentRoute,
                    navController,
                    onCloseDrawer
                )
                DrawerItem(
                    "Live Map",
                    Screen.ClientLiveMap.route,
                    currentRoute,
                    navController,
                    onCloseDrawer
                )
                DrawerItem(
                    label = "Playback Map",
                    route = Screen.ClientPlaybackMap.createRoute("client"),
                    currentRoute = currentRoute,
                    navController = navController,
                    onCloseDrawer = onCloseDrawer
                )
                ReportsSection(currentRoute, navController, onCloseDrawer)
            }

            "user" -> {
                DrawerItem(
                            "Dashboard",
                            Screen.UserDashboard.route,
                            currentRoute,
                            navController,
                            onCloseDrawer
                )
                DrawerItem(
                    "Live Map",
                    Screen.UserLiveMap.route,
                    currentRoute,
                    navController,
                    onCloseDrawer
                )
                DrawerItem(
                    label = "Playback Map",
                    route = Screen.UserPlaybackMap.createRoute("user"),
                    currentRoute = currentRoute,
                    navController = navController,
                    onCloseDrawer = onCloseDrawer
                )
                ReportsSection(currentRoute, navController, onCloseDrawer)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Navigate to LogoutScreen with role parameter
                    navController.navigate(Screen.Logout.createRoute(role)) {
                        launchSingleTop = true
                    }
                    onCloseDrawer()
                }
                .padding(12.dp)
        ) {
            Text(
                text = "Logout",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
    }
}

@Composable
fun DrawerHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.thinlogo),
            contentDescription = "Logo",
            modifier = Modifier
                .height(80.dp)
                .padding(8.dp)
        )
    }
}

@Composable
fun DrawerItem(
    label: String,
    route: String,
    currentRoute: String?,
    navController: NavController,
    onCloseDrawer: () -> Unit
) {
    val isSelected = currentRoute == route
    val textColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!isSelected) {
                    navController.navigate(route) {
                        popUpTo(Screen.Login.route)
                        launchSingleTop = true
                    }
                    onCloseDrawer()
                }
            }
            .padding(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}

@Composable
fun ReportsSection(
    currentRoute: String?,
    navController: NavController,
    onCloseDrawer: () -> Unit,
    role: String = "superadmin"
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Reports",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (expanded) "Collapse" else "Expand"
        )
    }

    if (expanded) {
        Column(modifier = Modifier.padding(start = 16.dp)) {
            when (role.lowercase()) {

                "superadmin" -> {
                    DrawerItem("Event Report", Screen.EventReport.route, currentRoute, navController, onCloseDrawer)
                    DrawerItem("Vehicle Report", Screen.VehicleReport.route, currentRoute, navController, onCloseDrawer)
                    DrawerItem("Driver Report", Screen.DriverReport.route, currentRoute, navController, onCloseDrawer)
                }

                "admin" -> {
                    DrawerItem("Event Report", Screen.AdminEventReport.route, currentRoute, navController, onCloseDrawer)
                    DrawerItem("Vehicle Report", Screen.AdminVehicleReport.route, currentRoute, navController, onCloseDrawer)
                    DrawerItem("Driver Report", Screen.AdminDriverReport.route, currentRoute, navController, onCloseDrawer)
                }

                "dealer" -> {
                    DrawerItem("Event Report", Screen.DealerEventReport.route, currentRoute, navController, onCloseDrawer)
                    DrawerItem("Vehicle Report", Screen.DealerVehicleReport.route, currentRoute, navController, onCloseDrawer)
                    DrawerItem("Driver Report", Screen.DealerDriverReport.route, currentRoute, navController, onCloseDrawer)
                }

                "client" -> {
                    DrawerItem("Event Report", Screen.ClientEventReport.route, currentRoute, navController, onCloseDrawer)
                    DrawerItem("Vehicle Report", Screen.ClientVehicleReport.route, currentRoute, navController, onCloseDrawer)
                    DrawerItem("Driver Report", Screen.ClientDriverReport.route, currentRoute, navController, onCloseDrawer)
                }

                "user" -> {
                    DrawerItem("Event Report", Screen.UserEventReport.route, currentRoute, navController, onCloseDrawer)
                    DrawerItem("Vehicle Report", Screen.UserVehicleReport.route, currentRoute, navController, onCloseDrawer)
                    DrawerItem("Driver Report", Screen.UserDriverReport.route, currentRoute, navController, onCloseDrawer)
                }
            }
        }
    }
}

