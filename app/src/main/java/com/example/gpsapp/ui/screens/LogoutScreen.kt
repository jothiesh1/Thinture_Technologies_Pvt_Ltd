package com.example.gpsapp.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gpsapp.ui.components.ScaffoldWithDrawer
import com.example.gpsapp.ui.navigation.Screen
import com.example.gpsapp.data.local.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun LogoutScreen(navController: NavController, role: String = "superadmin") {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userPrefs = remember { UserPreferences(context) }
    val sharedPrefs = remember {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }

    // Show dialog after composition
    LaunchedEffect(Unit) {
        showDialog = true
    }

    ScaffoldWithDrawer(
        navController = navController,
        screenTitle = "Logout",
        role = role
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {
                        // User clicked outside dialog - cancel logout
                        showDialog = false
                        navigateToDashboard(navController, role)
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_dialog_alert),
                            contentDescription = "Logout Icon",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = {
                        Text(
                            "Logout Confirmation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color(0xFF212121)
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                "Are you sure you want to logout?",
                                fontSize = 16.sp,
                                color = Color(0xFF424242),
                                lineHeight = 24.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "You will need to sign in again to access your account.",
                                fontSize = 14.sp,
                                color = Color(0xFF757575),
                                lineHeight = 20.sp
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        // Clear ALL saved credentials immediately
                                        userPrefs.clearLogin()
                                        sharedPrefs.edit().clear().commit()
                                    } catch (e: Exception) {
                                        // Even if clearLogin fails, ensure SharedPreferences is cleared
                                        sharedPrefs.edit().clear().commit()
                                    } finally {
                                        // Navigate to login screen
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 6.dp
                            ),
                            modifier = Modifier
                                .height(44.dp)
                                .widthIn(min = 100.dp)
                        ) {
                            Text(
                                "Yes, Logout",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = {
                                // User clicked cancel - go back to dashboard
                                showDialog = false
                                navigateToDashboard(navController, role)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF1976D2)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                Color(0xFF1976D2)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(44.dp)
                                .widthIn(min = 100.dp)
                        ) {
                            Text(
                                "Cancel",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                )
            }
        }
    }
}

private fun navigateToDashboard(navController: NavController, role: String) {
    val dashboardRoute = when (role.lowercase()) {
        "superadmin" -> Screen.Dashboard.route
        "admin" -> Screen.AdminDashboard.route
        "dealer" -> Screen.DealerDashboard.route
        "client" -> Screen.ClientDashboard.route
        "user" -> Screen.UserDashboard.route
        else -> Screen.Dashboard.route
    }

    navController.navigate(dashboardRoute) {
        popUpTo(Screen.Logout.route) { inclusive = true }
        launchSingleTop = true
    }
}