package com.example.gpsapp.ui.screens.client

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gpsapp.R
import com.example.gpsapp.data.model.EventReportItem
import com.example.gpsapp.ui.components.ScaffoldWithDrawer
import com.example.gpsapp.ui.viewmodel.EventReportViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientEventReportScreen(navController: NavController) {
    ScaffoldWithDrawer(
        navController = navController,
        screenTitle = "Event Report",
        role = "client"
    ) { innerPadding ->

        val context = LocalContext.current
        var showFilterDialog by remember { mutableStateOf(false) }
        var selectedItem by remember { mutableStateOf<EventReportItem?>(null) }

        val viewModel: EventReportViewModel = viewModel()
        val reportList by viewModel.eventReports.collectAsState()
        val loading by viewModel.isLoading.collectAsState()
        val errorMessage by viewModel.errorMessage.collectAsState()

        val listState = rememberLazyListState()
        val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = loading)

        var fromDate by remember { mutableStateOf("Select From Date") }
        var toDate by remember { mutableStateOf("Select To Date") }
        var vehicleNumber by remember { mutableStateOf("") }
        var status by remember { mutableStateOf("All") }

        val statusOptions = listOf("All", "Running", "Idle", "Parked")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        fun showDatePicker(setValue: (String) -> Unit, isToDate: Boolean = false) {
            val now = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    val selected = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                        if (isToDate) {
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                        } else {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                        }
                    }
                    setValue(dateFormat.format(selected.time))
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Initial fetch
        LaunchedEffect(Unit) {
            viewModel.fetchEventReports("", "", "", null, reset = true)
        }

        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .distinctUntilChanged()
                .filterNotNull()
                .collect { lastVisibleItem ->
                    if (lastVisibleItem >= reportList.size - 3) {
                        viewModel.loadMoreData()
                    }
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
                    text = "Event Status Report",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
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

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }

                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = {
                        fromDate = "Select From Date"
                        toDate = "Select To Date"
                        vehicleNumber = ""
                        status = "All"
                        viewModel.fetchEventReports("", "", "", null, reset = true)
                    }
                ) {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("Dev", "Time", "Lat", "Long").forEach {
                                    Text(
                                        text = it,
                                        modifier = Modifier.weight(1f),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            HorizontalDivider(color = Color.Gray)
                        }

                        items(reportList) { item ->
                            val lat = item.latitude?.toString()?.takeIf { it.length >= 6 }?.substring(0, 6) ?: "-"
                            val lon = item.longitude?.toString()?.takeIf { it.length >= 6 }?.substring(0, 6) ?: "-"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedItem = item }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(
                                    item.deviceId ?: "Unknown",
                                    item.timestamp ?: "Unknown",
                                    lat,
                                    lon
                                ).forEach { text ->
                                    Text(
                                        text = text,
                                        modifier = Modifier.weight(1f),
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            HorizontalDivider(color = Color.Gray)
                        }

                        item {
                            if (loading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
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
                            if (vehicleNumber.isBlank()) {
                                Toast.makeText(context, "Please enter a valid Device ID", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            viewModel.fetchEventReports(
                                fromDate = fromDate,
                                toDate = toDate,
                                vehicleNumber = vehicleNumber.trim(),
                                status = if (status == "All") null else status,
                                reset = true
                            )
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
                            Button(onClick = { showDatePicker({ fromDate = it }) }) {
                                Text(fromDate)
                            }
                            Button(onClick = { showDatePicker({ toDate = it }, isToDate = true) }) {
                                Text(toDate)
                            }

                            OutlinedTextField(
                                value = vehicleNumber,
                                onValueChange = { vehicleNumber = it },
                                label = { Text("Device ID (required)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = status,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Status") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
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
                            }) {
                                Text("Clear Filters")
                            }
                        }
                    }
                )
            }

            selectedItem?.let { item ->
                AlertDialog(
                    onDismissRequest = { selectedItem = null },
                    title = { Text("Event Details") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Device ID: ${item.deviceId ?: "-"}")
                            Text("Timestamp: ${item.timestamp ?: "-"}")
                            Text("Latitude: ${item.latitude ?: "-"}")
                            Text("Longitude: ${item.longitude ?: "-"}")
                            Text("Speed: ${item.speed ?: "-"}")
                            Text("Ignition: ${item.ignition ?: "-"}")
                            Text("Vehicle Status: ${item.vehicleStatus ?: "-"}")
                            Text("Additional Data: ${item.additionalData ?: "-"}")
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedItem = null }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}
