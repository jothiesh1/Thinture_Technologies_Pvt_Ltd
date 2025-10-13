package com.example.gpsapp.ui.screens.superadmin

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import android.app.TimePickerDialog
import com.example.gpsapp.ui.components.DateTimePicker
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventReportScreen(navController: NavController) {
    ScaffoldWithDrawer(
        navController = navController,
        screenTitle = "Event Report",
        role = "superadmin"
    ) { innerPadding ->

        val context = LocalContext.current
        var showFilterDialog by remember { mutableStateOf(false) }
        var showDownloadDialog by remember { mutableStateOf(false) }
        var selectedItem by remember { mutableStateOf<EventReportItem?>(null) }

        val viewModel: EventReportViewModel = viewModel()
        val reportList by viewModel.eventReports.collectAsState()
        val loading by viewModel.isLoading.collectAsState()
        val errorMessage by viewModel.errorMessage.collectAsState()
        val deviceList by viewModel.deviceList.collectAsState()
        val loadingDevices by viewModel.isLoadingDevices.collectAsState()
        val selectedDeviceId by viewModel.selectedDeviceId.collectAsState()

        val listState = rememberLazyListState()
        val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

        var fromDate by remember { mutableStateOf("") }
        var toDate by remember { mutableStateOf("") }
        var vehicleNumber by remember { mutableStateOf("") }
        var status by remember { mutableStateOf("All") }

        // Store the currently filtered device ID
        var currentFilteredDeviceId by remember { mutableStateOf<String?>(null) }
        var isFiltered by remember { mutableStateOf(false) }

        val statusOptions = listOf("All", "Running", "Idle", "Parked")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

        fun showDateTimePicker(setValue: (String) -> Unit, isToDate: Boolean = false) {
            val now = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    val selected = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                    }

                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            selected.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            selected.set(Calendar.MINUTE, minute)
                            selected.set(Calendar.SECOND, if (isToDate) 59 else 0)
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

        fun clearFilters() {
            fromDate = ""
            toDate = ""
            vehicleNumber = ""
            status = "All"
            currentFilteredDeviceId = null
            isFiltered = false
            viewModel.fetchEventReports("", "", "", null, reset = true)
        }

        // Initial fetch
        LaunchedEffect(Unit) {
            viewModel.fetchEventReports("", "", "", null, reset = true)
        }

        // Fetch devices when filter dialog is opened
        LaunchedEffect(showFilterDialog) {
            if (showFilterDialog) {
                viewModel.fetchDevices()
            }
        }

        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .distinctUntilChanged()
                .filterNotNull()
                .collect { lastVisibleItem ->
                    if (lastVisibleItem >= reportList.size - 3 && !loading) {
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
                // Header
                Text(
                    text = "Event Status Report",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Filter")
                    }

                    OutlinedButton(
                        onClick = { showDownloadDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = reportList.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Download")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Info Card
                if (isFiltered) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Active Filters",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (currentFilteredDeviceId != null) {
                                    Text(
                                        text = "Device: $currentFilteredDeviceId",
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                                if (status != "All") {
                                    Text(
                                        text = "Status: $status",
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            IconButton(onClick = { clearFilters() }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Filters",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Loading indicator
                if (loading || loadingDevices) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Red.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = errorMessage ?: "Unknown error",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = { clearFilters() }
                ) {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("Device ID", "Time", "Lat", "Long").forEach {
                                    Text(
                                        text = it,
                                        modifier = Modifier.weight(1f),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.5f), thickness = 2.dp)
                        }

                        items(reportList) { item ->
                            val lat = item.latitude?.let { if (it.length >= 6) it.substring(0, 6) else it } ?: "-"
                            val lon = item.longitude?.let { if (it.length >= 6) it.substring(0, 6) else it } ?: "-"

                            // Display the device ID - prioritize the filtered device ID if available
                            val displayDeviceId = item.deviceId
                                ?: currentFilteredDeviceId
                                ?: selectedDeviceId
                                ?: "Unknown"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedItem = item }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(
                                    displayDeviceId,
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
                            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                        }

                        if (reportList.isEmpty() && !loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No data available",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Filter Dialog
            if (showFilterDialog) {
                AlertDialog(
                    onDismissRequest = { showFilterDialog = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (fromDate.isEmpty() || toDate.isEmpty()) {
                                    Toast.makeText(context, "Please select From and To date", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (vehicleNumber.isEmpty()) {
                                    Toast.makeText(context, "Please select a Device ID", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                // Store the filtered device ID
                                currentFilteredDeviceId = vehicleNumber.trim()
                                isFiltered = true

                                viewModel.fetchEventReports(
                                    fromDate = fromDate,
                                    toDate = toDate,
                                    vehicleNumber = vehicleNumber.trim(),
                                    status = if (status == "All") null else status,
                                    reset = true
                                )
                                showFilterDialog = false
                            }
                        ) {
                            Text("Apply Filter")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showFilterDialog = false }) {
                            Text("Cancel")
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Filter Events")
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // From Date
                            OutlinedTextField(
                                value = if (fromDate.isEmpty()) "" else {
                                    try {
                                        displayDateFormat.format(dateFormat.parse(fromDate)!!)
                                    } catch (e: Exception) {
                                        fromDate
                                    }
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("From Date & Time *") },
                                placeholder = { Text("Select date and time") },
                                trailingIcon = {
                                    IconButton(onClick = { showDateTimePicker({ fromDate = it }, false) }) {
                                        Icon(Icons.Default.DateRange, "Select date")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors()
                            )

                            // To Date
                            OutlinedTextField(
                                value = if (toDate.isEmpty()) "" else {
                                    try {
                                        displayDateFormat.format(dateFormat.parse(toDate)!!)
                                    } catch (e: Exception) {
                                        toDate
                                    }
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("To Date & Time *") },
                                placeholder = { Text("Select date and time") },
                                trailingIcon = {
                                    IconButton(onClick = { showDateTimePicker({ toDate = it }, true) }) {
                                        Icon(Icons.Default.DateRange, "Select date")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors()
                            )

                            // Device ID Dropdown
                            var deviceExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = deviceExpanded,
                                onExpandedChange = { deviceExpanded = !deviceExpanded }
                            ) {
                                OutlinedTextField(
                                    value = vehicleNumber,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Device ID *") },
                                    placeholder = { Text("Select a device") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = deviceExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors()
                                )
                                ExposedDropdownMenu(
                                    expanded = deviceExpanded,
                                    onDismissRequest = { deviceExpanded = false }
                                ) {
                                    if (deviceList.isEmpty() && !loadingDevices) {
                                        DropdownMenuItem(
                                            text = { Text("No devices available") },
                                            onClick = { }
                                        )
                                    } else if (loadingDevices) {
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    horizontalArrangement = Arrangement.Center,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                                }
                                            },
                                            onClick = { }
                                        )
                                    } else {
                                        deviceList.forEach { device ->
                                            DropdownMenuItem(
                                                text = { Text(device) },
                                                onClick = {
                                                    vehicleNumber = device
                                                    deviceExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Status Dropdown
                            var statusExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = statusExpanded,
                                onExpandedChange = { statusExpanded = !statusExpanded }
                            ) {
                                OutlinedTextField(
                                    value = status,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Vehicle Status") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors()
                                )
                                ExposedDropdownMenu(
                                    expanded = statusExpanded,
                                    onDismissRequest = { statusExpanded = false }
                                ) {
                                    statusOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                status = option
                                                statusExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    fromDate = ""
                                    toDate = ""
                                    vehicleNumber = ""
                                    status = "All"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Clear All Fields")
                            }

                            Text(
                                text = "* Required fields",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                )
            }

            // Download Dialog
            if (showDownloadDialog) {
                AlertDialog(
                    onDismissRequest = { showDownloadDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Download Report")
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Choose download format:")

                            Button(
                                onClick = {
                                    Toast.makeText(context, "Downloading as PDF...", Toast.LENGTH_SHORT).show()
                                    showDownloadDialog = false
                                    // TODO: Implement PDF download
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Download as PDF")
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, "Downloading as CSV...", Toast.LENGTH_SHORT).show()
                                    showDownloadDialog = false
                                    // TODO: Implement CSV download
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Download as CSV")
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showDownloadDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Event Details Dialog
            selectedItem?.let { item ->
                val detailDeviceId = item.deviceId
                    ?: currentFilteredDeviceId
                    ?: selectedDeviceId
                    ?: "-"

                AlertDialog(
                    onDismissRequest = { selectedItem = null },
                    title = { Text("Event Details") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "Device ID" to detailDeviceId,
                                "Timestamp" to (item.timestamp ?: "-"),
                                "Latitude" to (item.latitude ?: "-"),
                                "Longitude" to (item.longitude ?: "-"),
                                "Speed" to (item.speed ?: "-"),
                                "Ignition" to (item.ignition ?: "-"),
                                "Vehicle Status" to (item.vehicleStatus ?: "-"),
                                "Additional Data" to (item.additionalData ?: "-")
                            ).forEach { (label, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$label:",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(0.4f)
                                    )
                                    Text(
                                        text = value.toString(),
                                        modifier = Modifier.weight(0.6f)
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { selectedItem = null }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}