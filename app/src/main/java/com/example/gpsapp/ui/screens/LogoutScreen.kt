package com.example.gpsapp.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.example.gpsapp.ui.components.ScaffoldWithDrawer

@Composable
fun LogoutScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(true) }

    ScaffoldWithDrawer(navController = navController, screenTitle = "Logout") { innerPadding ->
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    navController.navigate("dashboard") {
                        popUpTo("logout") { inclusive = true }
                    }
                },
                title = { Text("Logout") },
                text = { Text("Do you want to log out of your account?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        navController.navigate("login") {
                            popUpTo("logout") { inclusive = true }
                        }
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        navController.navigate("dashboard") {
                            popUpTo("logout") { inclusive = true }
                        }
                    }) {
                        Text("No")
                    }
                }
            )
        } else {
            // Optional fallback UI or empty screen
            Text("Redirecting...", modifier = Modifier.padding(innerPadding))
        }
    }
}
