package com.example.gpsapp.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.gpsapp.ui.components.ScaffoldWithDrawer

@Composable
fun LogoutScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(true) }

    ScaffoldWithDrawer(
        navController = navController,
        screenTitle = "Logout",
        role = "superadmin"
    ) { innerPadding ->
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
