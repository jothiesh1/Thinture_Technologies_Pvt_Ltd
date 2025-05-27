package com.example.thinturetechnologiespvtltd.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thinturetechnologiespvtltd.R
import com.example.thinturetechnologiespvtltd.ui.components.ScaffoldWithDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverReportScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    ScaffoldWithDrawer(navController = navController, screenTitle = "Driver Report") { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.imagelogin),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Thinture Fleet Management",
                    fontSize = 24.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Year Dropdown
                var selectedYear by remember { mutableStateOf("2025") }
                var yearExpanded by remember { mutableStateOf(false) }
                val yearOptions = listOf("2022", "2023", "2024", "2025")

                ExposedDropdownMenuBox(
                    expanded = yearExpanded,
                    onExpandedChange = { yearExpanded = !yearExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedYear,
                        onValueChange = {},
                        label = { Text("Year", color = Color.White) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White,
                        ),
                        textStyle = TextStyle(color = Color.White)
                    )
                    ExposedDropdownMenu(
                        expanded = yearExpanded,
                        onDismissRequest = { yearExpanded = false }
                    ) {
                        yearOptions.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year) },
                                onClick = {
                                    selectedYear = year
                                    yearExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Device ID Dropdown
                var selectedDeviceId by remember { mutableStateOf("Dev1") }
                var deviceExpanded by remember { mutableStateOf(false) }
                val deviceOptions = listOf("Dev1", "Dev2", "Dev3", "Dev4", "Dev5")

                ExposedDropdownMenuBox(
                    expanded = deviceExpanded,
                    onExpandedChange = { deviceExpanded = !deviceExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedDeviceId,
                        onValueChange = {},
                        label = { Text("Device ID", color = Color.White) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = deviceExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White,
                        ),
                        textStyle = TextStyle(color = Color.White)
                    )
                    ExposedDropdownMenu(
                        expanded = deviceExpanded,
                        onDismissRequest = { deviceExpanded = false }
                    ) {
                        deviceOptions.forEach { dev ->
                            DropdownMenuItem(
                                text = { Text(dev) },
                                onClick = {
                                    selectedDeviceId = dev
                                    deviceExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Dummy data display
                val dummyData = listOf(
                    Pair("120 km/h", "350 km"),
                    Pair("115 km/h", "300 km"),
                    Pair("130 km/h", "400 km"),
                    Pair("110 km/h", "270 km"),
                    Pair("125 km/h", "320 km")
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(dummyData) { (speed, distance) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .background(Color.DarkGray.copy(alpha = 0.2f))
                                .padding(12.dp)
                        ) {
                            Text("Max Speed: $speed", color = Color.White, modifier = Modifier.weight(1f))
                            Text("Total Distance: $distance", color = Color.White, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
