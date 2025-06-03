package com.example.gpsapp.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
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


data class EventReportItem(
    val deviceId: String,
    val vehicleNumber: String,
    val status: String,
    val timestamp: String,
    val latitude: Double,
    val longitude: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventReportScreen(navController: NavController) {
    ScaffoldWithDrawer(navController = navController, screenTitle = "Event Report") { innerPadding ->
        val context = LocalContext.current
        var showFilterDialog by remember { mutableStateOf(false) }
        var selectedItem by remember { mutableStateOf<EventReportItem?>(null) }

        val masterList = remember { mutableStateListOf<EventReportItem>() }
        val eventList = remember { mutableStateListOf<EventReportItem>() }

        var fromDate by remember { mutableStateOf("Select From Date") }
        var toDate by remember { mutableStateOf("Select To Date") }
        var vehicleNumber by remember { mutableStateOf("") }
        var status by remember { mutableStateOf("All") }

        val statusOptions = listOf("All", "Running", "Idle", "Parked")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fun showDatePicker(setValue: (String) -> Unit) {
            val now = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    val selected = Calendar.getInstance().apply {
                        set(year, month, day)
                    }
                    setValue(dateFormat.format(selected.time))
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        LaunchedEffect(Unit) {
            if (masterList.isEmpty()) {
                for (i in 1..10) {
                    val item = EventReportItem(
                        deviceId = "Dev$i",
                        vehicleNumber = "Car$i",
                        status = when {
                            i % 3 == 0 -> "Idle"
                            i % 2 == 0 -> "Parked"
                            else -> "Running"
                        },
                        timestamp = "2025-05-${20 + i}",
                        latitude = 12.9 + i,
                        longitude = 77.6 + i
                    )
                    masterList.add(item)
                }
                eventList.clear()
                eventList.addAll(masterList)
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
                // Page Title
                Text(
                    text = "Event Status Report",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
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
                            listOf("Dev", "Veh", "Stat", "Time", "Lat", "Long").forEach {
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

                    items(eventList) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedItem = item }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(
                                item.deviceId,
                                item.vehicleNumber,
                                item.status,
                                item.timestamp,
                                "${item.latitude}".take(6),
                                "${item.longitude}".take(6)
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
                            if (fromDate.contains("Select") || toDate.contains("Select")) {
                                Toast.makeText(context, "Please select From and To date", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }

                            val filtered = masterList.filter { item ->
                                val dateMatch = item.timestamp >= fromDate && item.timestamp <= toDate
                                val vehicleMatch = vehicleNumber.isBlank() || item.vehicleNumber.contains(vehicleNumber, ignoreCase = true)
                                val statusMatch = status == "All" || item.status == status
                                dateMatch && vehicleMatch && statusMatch
                            }

                            eventList.clear()
                            eventList.addAll(filtered)
                            Toast.makeText(context, "Filters applied", Toast.LENGTH_SHORT).show()
                            showFilterDialog = false
                        }) {
                            Text("Apply Filter")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showFilterDialog = false }) {
                            Text("Cancel")
                        }
                    },
                    title = { Text("Filter Events") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { showDatePicker { fromDate = it } }) {
                                Text(fromDate)
                            }
                            Button(onClick = { showDatePicker { toDate = it } }) {
                                Text(toDate)
                            }
                            OutlinedTextField(
                                value = vehicleNumber,
                                onValueChange = { vehicleNumber = it },
                                label = { Text("Vehicle Number") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            var expanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    readOnly = true,
                                    value = status,
                                    onValueChange = {},
                                    label = { Text("Status") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    statusOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                status = option
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Button(onClick = {
                                fromDate = "Select From Date"
                                toDate = "Select To Date"
                                vehicleNumber = ""
                                status = "All"
                                eventList.clear()
                                eventList.addAll(masterList)
                                Toast.makeText(context, "Filters reset", Toast.LENGTH_SHORT).show()
                                showFilterDialog = false
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
                    title = { Text("Event Details") },
                    text = {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Device ID: ${item.deviceId}")
                            Text("Vehicle Number: ${item.vehicleNumber}")
                            Text("Status: ${item.status}")
                            Text("Timestamp: ${item.timestamp}")
                            Text("Latitude: ${item.latitude}")
                            Text("Longitude: ${item.longitude}")
                        }
                    }
                )
            }
        }
    }
}
