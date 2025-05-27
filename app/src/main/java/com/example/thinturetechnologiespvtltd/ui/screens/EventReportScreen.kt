package com.example.thinturetechnologiespvtltd.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thinturetechnologiespvtltd.R
import com.example.thinturetechnologiespvtltd.ui.components.ScaffoldWithDrawer

data class EventReportItem(
    val deviceId: String,
    val dateTime: String,
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val ignition: Boolean,
    val status: String,
    val address: String
)

@Composable
fun EventReportScreen(navController: NavController) {
    ScaffoldWithDrawer(navController = navController, screenTitle = "Event Report") { innerPadding ->
        val context = LocalContext.current
        var showFilterDialog by remember { mutableStateOf(false) }
        var selectedItem by remember { mutableStateOf<EventReportItem?>(null) }

        val eventList = remember { mutableStateListOf<EventReportItem>() }

        Image(
            painter = painterResource(id = R.drawable.imagelogin),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Mock data
        LaunchedEffect(Unit) {
            if (eventList.isEmpty()) {
                for (i in 1..10) {
                    eventList.add(
                        EventReportItem(
                            deviceId = "Dev$i",
                            dateTime = "2025-05-22 10:0$i",
                            latitude = 12.9 + i,
                            longitude = 77.6 + i,
                            speed = (30 + i).toFloat(),
                            ignition = i % 2 == 0,
                            status = if (i % 2 == 0) "Run" else "Stop",
                            address = "Loc $i"
                        )
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { showFilterDialog = true }) {
                    Text("Filter", color = Color.White)
                }
                Button(onClick = {
                    Toast.makeText(context, "Download Excel clicked", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Download Excel", color = Color.White)
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
                        listOf("Dev", "DateTime", "Lat", "Long", "Spd", "Ign", "Stat").forEach {
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
                            item.dateTime,
                            "${item.latitude}".take(6),
                            "${item.longitude}".take(6),
                            item.speed.toString(),
                            if (item.ignition) "On" else "Off",
                            item.status
                        ).forEach {
                            Text(
                                it,
                                modifier = Modifier.weight(1f),
                                color = Color.LightGray,
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
                        showFilterDialog = false
                        Toast.makeText(context, "Filter applied", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Apply", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFilterDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                title = { Text("Filter Events", color = Color.White) },
                text = {
                    Column {
                        Text(
                            "Quick Time, From Date, To Date, Vehicle Number, Status (Coming Soon)",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                },
                containerColor = Color.DarkGray
            )
        }

        selectedItem?.let { item ->
            AlertDialog(
                onDismissRequest = { selectedItem = null },
                confirmButton = {
                    TextButton(onClick = { selectedItem = null }) {
                        Text("Close", color = Color.White)
                    }
                },
                title = { Text("Event Details", color = Color.White) },
                text = {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Device ID: ${item.deviceId}", color = Color.White)
                        Text("DateTime: ${item.dateTime}", color = Color.White)
                        Text("Latitude: ${item.latitude}", color = Color.White)
                        Text("Longitude: ${item.longitude}", color = Color.White)
                        Text("Speed: ${item.speed} km/h", color = Color.White)
                        Text("Ignition: ${if (item.ignition) "On" else "Off"}", color = Color.White)
                        Text("Status: ${item.status}", color = Color.White)
                        Text("Address: ${item.address}", color = Color.White)
                    }
                },
                containerColor = Color.DarkGray
            )
        }
    }
}
