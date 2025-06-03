package com.example.gpsapp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gpsapp.ui.components.ScaffoldWithDrawer
import java.text.SimpleDateFormat
import java.util.*
import com.example.gpsapp.R

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
        val filteredList = remember { mutableStateListOf<VehicleReportItem>() }

        var fromDateTime by remember { mutableStateOf("Select From Date & Time") }
        var toDateTime by remember { mutableStateOf("Select To Date & Time") }
        var deviceId by remember { mutableStateOf("") }
        var selectedViolation by remember { mutableStateOf("Select Violation") }
        var violationExpanded by remember { mutableStateOf(false) }

        val violationOptions = listOf(
            "Overspeeding", "Sharp Turning", "Harsh Acceleration",
            "Harsh Braking", "Theft/Towing"
        )

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun showDateTimePicker(setValue: (String) -> Unit) {
            val now = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            val selected = Calendar.getInstance().apply {
                                set(year, month, day, hour, minute)
                            }
                            setValue(dateFormat.format(selected.time))
                        },
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        LaunchedEffect(Unit) {
            if (vehicleList.isEmpty()) {
                for (i in 1..10) {
                    val timeString = "2025-05-22 10:0$i"
                    vehicleList.add(
                        VehicleReportItem(
                            deviceId = "Dev$i",
                            vehicle = "Car$i",
                            violationType = when (i % 5) {
                                0 -> "Overspeeding"
                                1 -> "Sharp Turning"
                                2 -> "Harsh Acceleration"
                                3 -> "Harsh Braking"
                                else -> "Theft/Towing"
                            },
                            latitude = 12.9 + i,
                            longitude = 77.6 + i,
                            speed = (40 + i).toFloat(),
                            time = timeString,
                            driverName = "Driver$i"
                        )
                    )
                }
                filteredList.clear()
                filteredList.addAll(vehicleList)
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
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
                Text(
                    text = "Vehicle Violation Report",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )

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

                    items(filteredList) { item ->
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

            if (showFilterDialog) {
                AlertDialog(
                    onDismissRequest = { showFilterDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            if (fromDateTime.contains("Select") ||
                                toDateTime.contains("Select") ||
                                deviceId.isBlank() ||
                                selectedViolation == "Select Violation"
                            ) {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            } else {
                                try {
                                    val fromDate = dateFormat.parse(fromDateTime)
                                    val toDate = dateFormat.parse(toDateTime)

                                    val filtered = vehicleList.filter {
                                        val itemDate = dateFormat.parse(it.time)
                                        itemDate != null &&
                                                itemDate.after(fromDate) &&
                                                itemDate.before(toDate) &&
                                                it.deviceId.contains(deviceId, ignoreCase = true) &&
                                                it.violationType.equals(selectedViolation, ignoreCase = true)
                                    }

                                    filteredList.clear()
                                    filteredList.addAll(filtered)
                                    showFilterDialog = false
                                    Toast.makeText(context, "Filters applied", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Invalid date format", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Text("Apply Filter")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showFilterDialog = false }) {
                            Text("Cancel")
                        }
                    },
                    title = { Text("Filter Vehicles") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { showDateTimePicker { fromDateTime = it } }) {
                                Text(fromDateTime)
                            }
                            Button(onClick = { showDateTimePicker { toDateTime = it } }) {
                                Text(toDateTime)
                            }
                            OutlinedTextField(
                                value = deviceId,
                                onValueChange = { deviceId = it },
                                label = { Text("Device ID") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            ExposedDropdownMenuBox(
                                expanded = violationExpanded,
                                onExpandedChange = { violationExpanded = !violationExpanded }
                            ) {
                                OutlinedTextField(
                                    readOnly = true,
                                    value = selectedViolation,
                                    onValueChange = {},
                                    label = { Text("Violation Type") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = violationExpanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = violationExpanded,
                                    onDismissRequest = { violationExpanded = false }
                                ) {
                                    violationOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                selectedViolation = option
                                                violationExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Button(onClick = {
                                // Reset all filter fields
                                fromDateTime = "Select From Date & Time"
                                toDateTime = "Select To Date & Time"
                                deviceId = ""
                                selectedViolation = "Select Violation"
                            }) {
                                Text("Reset Filter")
                            }
                        }
                    }
                )
            }

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

