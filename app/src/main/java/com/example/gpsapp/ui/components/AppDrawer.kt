package com.example.gpsapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.gpsapp.ui.navigation.Screen
import com.example.gpsapp.R


@Composable
fun AppDrawer(navController: NavController, onCloseDrawer: () -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(Color(0xFFEFF3F9))
            .padding(16.dp)
    ) {
        // Logo/Header
        DrawerHeader()

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation items
        DrawerItem("Dashboard", Screen.Dashboard.route, currentRoute, navController, onCloseDrawer)
        DrawerItem("Live Map", Screen.LiveMap.route, currentRoute, navController, onCloseDrawer)
        DrawerItem("Playback Map", Screen.Playback.route, currentRoute, navController, onCloseDrawer)

        Spacer(modifier = Modifier.height(8.dp))

        // Collapsible reports section
        ReportsSection(currentRoute, navController, onCloseDrawer)

        Spacer(modifier = Modifier.weight(1f)) // Pushes logout button to bottom

        // Logout button
        DrawerItem("Logout", Screen.Logout.route, currentRoute, navController, onCloseDrawer)
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
            contentDescription = "Thinture Logo",
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
                        popUpTo(Screen.Dashboard.route)
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
    onCloseDrawer: () -> Unit
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
            DrawerItem("Event Report", Screen.EventReport.route, currentRoute, navController, onCloseDrawer)
            DrawerItem("Vehicle Report", Screen.VehicleReport.route, currentRoute, navController, onCloseDrawer)
            DrawerItem("Driver Report", Screen.DriverReport.route, currentRoute, navController, onCloseDrawer)
        }
    }
}
