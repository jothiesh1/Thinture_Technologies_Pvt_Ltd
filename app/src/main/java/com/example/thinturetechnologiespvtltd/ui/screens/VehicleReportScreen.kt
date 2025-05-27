package com.example.thinturetechnologiespvtltd.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thinturetechnologiespvtltd.R
import com.example.thinturetechnologiespvtltd.ui.components.ScaffoldWithDrawer

data class VehicleReportItem(
    val deviceId: String,
    val vehicle: String,
    val violationType: String,
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val time: String,
    val driverName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleReportScreen(navController: NavController) {
    ScaffoldWithDrawer(navController = navController, screenTitle = "Vehicle Report") { innerPadding ->

        val context = LocalContext.current
        var showFilterDialog by remember { mutableStateOf(false) }
        var selectedItem by remember { mutableStateOf<VehicleReportItem?>(null) }

        val vehicleList = remember { mutableStateListOf<VehicleReportItem>() }

        // Sample data
        LaunchedEffect(Unit) {
            if (vehicleList.isEmpty()) {
                for (i in 1..10) {
                    vehicleList.add(
                        VehicleReportItem(
                            deviceId = "Dev$i",
                            vehicle = "Car$i",
                            violationType = "Speeding",
                            latitude = 12.9 + i,
                            longitude = 77.6 + i,
                            speed = (40 + i).toFloat(),
                            time = "2025-05-22 10:0$i",
                            driverName = "Driver$i"
                        )
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Dark background image
            Image(
                painter = painterResource(id = R.drawable.imagelogin),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { showFilterDialog = true }) {
                        Text("Filter")
                    }
                    Button(onClick = {
                        Toast.makeText(context, "Download Excel clicked", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Download Excel")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("Dev", "Veh", "Viol", "Lat", "Long", "Spd", "Time", "Drv").forEach {
                                Text(
                                    it,
                                    modifier = Modifier.weight(1f),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Divider(color = Color.Gray)
                    }

                    items(vehicleList) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedItem = item }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(
                                item.deviceId,
                                item.vehicle,
                                item.violationType,
                                "${item.latitude}".take(6),
                                "${item.longitude}".take(6),
                                item.speed.toString(),
                                item.time,
                                item.driverName
                            ).forEach {
                                Text(
                                    it,
                                    modifier = Modifier.weight(1f),
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Divider(color = Color.Gray)
                    }
                }
            }

            // Filter Dialog
            if (showFilterDialog) {
                AlertDialog(
                    onDismissRequest = { showFilterDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showFilterDialog = false
                            Toast.makeText(context, "Filter applied", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Apply")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showFilterDialog = false }) {
                            Text("Cancel")
                        }
                    },
                    title = { Text("Filter Vehicles") },
                    text = {
                        Column {
                            Text("Quick Time, From Date, To Date, Vehicle Number, Driver Name (Coming Soon)", fontSize = 12.sp)
                        }
                    }
                )
            }

            // Detail Dialog
            selectedItem?.let { item ->
                AlertDialog(
                    onDismissRequest = { selectedItem = null },
                    confirmButton = {
                        TextButton(onClick = { selectedItem = null }) {
                            Text("Close")
                        }
                    },
                    title = { Text("Vehicle Details") },
                    text = {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Device ID: ${item.deviceId}")
                            Text("Vehicle: ${item.vehicle}")
                            Text("Violation: ${item.violationType}")
                            Text("Latitude: ${item.latitude}")
                            Text("Longitude: ${item.longitude}")
                            Text("Speed: ${item.speed} km/h")
                            Text("Time: ${item.time}")
                            Text("Driver: ${item.driverName}")
                        }
                    }
                )
            }
        }
    }
}
