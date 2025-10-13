package com.example.gpsapp.ui.screens.superadmin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.TextPaint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gpsapp.R
import com.example.gpsapp.network.RetrofitClient
import com.example.gpsapp.ui.components.ScaffoldWithDrawer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.orEmpty


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
    ScaffoldWithDrawer(
        navController = navController,
        screenTitle = "Vehicle Report",
        role = "superadmin"
    ) { innerPadding ->
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        var downloadFilePath by remember { mutableStateOf("") }
        var startDownload by remember { mutableStateOf(false) }

        var showFilterDialog by remember { mutableStateOf(false) }
        var showDownloadDialog by remember { mutableStateOf(false) }
        var showDownloadProgress by remember { mutableStateOf(false) }
        var showDownloadCompleteDialog by remember { mutableStateOf(false) }
        var selectedFormat by remember { mutableStateOf("PDF") }

        var selectedItem by remember { mutableStateOf<VehicleReportItem?>(null) }
        var isLoading by remember { mutableStateOf(false) }

        var page by remember { mutableStateOf(1) }
        val pageLimit = 100
        var isLoadingMore by remember { mutableStateOf(false) }
        var hasMoreData by remember { mutableStateOf(true) }

        val vehicleList = remember { mutableStateListOf<VehicleReportItem>() }
        val filteredList = remember { mutableStateListOf<VehicleReportItem>() }

        var fromDateTime by remember { mutableStateOf("Select From Date & Time") }
        var toDateTime by remember { mutableStateOf("Select To Date & Time") }
        var selectedViolation by remember { mutableStateOf("All Violations") }
        var violationExpanded by remember { mutableStateOf(false) }

        val availableDevices = remember { mutableStateListOf<String>() }
        var selectedDevice by remember { mutableStateOf("All Devices") }
        var deviceExpanded by remember { mutableStateOf(false) }
        var isLoadingDevices by remember { mutableStateOf(false) }

        val violationOptions = listOf(
            "All Violations",
            "Over Speed",
            "Harsh Braking",
            "Harsh Acceleration"
        )

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun fetchAvailableDevices() {
            coroutineScope.launch {
                isLoadingDevices = true
                try {
                    val resp = RetrofitClient.apiService.getLiveVehicles()
                    if (resp.isSuccessful) {
                        val devices = resp.body()
                        availableDevices.clear()
                        availableDevices.add("All Devices")

                        when {
                            devices == null -> {
                                Toast.makeText(context, "No devices found", Toast.LENGTH_SHORT).show()
                            }
                            devices is List<*> -> {
                                devices.forEach { vehicle ->
                                    try {
                                        val deviceId = when (vehicle) {
                                            is Map<*, *> -> vehicle["deviceId"]?.toString() ?: vehicle["device_id"]?.toString()
                                            else -> {
                                                val field = vehicle?.javaClass?.getDeclaredField("deviceId")
                                                field?.isAccessible = true
                                                field?.get(vehicle)?.toString()
                                            }
                                        }

                                        if (!deviceId.isNullOrEmpty()) {
                                            availableDevices.add(deviceId)
                                        }
                                    } catch (e: Exception) {
                                        // Skip this item if we can't extract deviceId
                                    }
                                }

                                if (availableDevices.size == 1) {
                                    Toast.makeText(context, "No valid device IDs found", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else -> {
                                Toast.makeText(context, "Unexpected response format", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Failed to fetch devices: ${resp.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isLoadingDevices = false
                }
            }
        }

        LaunchedEffect(showFilterDialog) {
            if (showFilterDialog && availableDevices.isEmpty()) {
                fetchAvailableDevices()
            }
        }

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

        fun fetchVehicleReports(reset: Boolean = false) {
            coroutineScope.launch {
                if (fromDateTime == "Select From Date & Time" || toDateTime == "Select To Date & Time") {
                    Toast.makeText(context, "Please select From & To date before fetching data.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                if (reset) {
                    isLoading = true
                    page = 1
                    hasMoreData = true
                    vehicleList.clear()
                    filteredList.clear()
                } else {
                    isLoadingMore = true
                }

                try {
                    val apiFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val pickerFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

                    val now = Calendar.getInstance()
                    val past48Hours = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, -48) }

                    fun toApi(ts: String): String {
                        return if (ts == "Select From Date & Time" || ts == "Select To Date & Time") {
                            apiFmt.format(now.time)
                        } else {
                            apiFmt.format(pickerFmt.parse(ts)!!)
                        }
                    }

                    val fromDate = if (fromDateTime == "Select From Date & Time") {
                        apiFmt.format(past48Hours.time)
                    } else {
                        toApi(fromDateTime)
                    }

                    val toDate = if (toDateTime == "Select To Date & Time") {
                        apiFmt.format(now.time)
                    } else {
                        toApi(toDateTime)
                    }

                    val deviceIdParam = if (selectedDevice != "All Devices") selectedDevice else null

                    val resp = RetrofitClient.apiService.getViolationReports(
                        fromDate = fromDate,
                        toDate = toDate,
                        deviceId = deviceIdParam,
                        violationType = if (selectedViolation != "All Violations" && selectedViolation != "Select Violation") selectedViolation else null,
                        page = page,
                        limit = pageLimit
                    )

                    if (resp.isSuccessful) {
                        val list = resp.body().orEmpty()

                        if (list.isEmpty()) {
                            hasMoreData = false
                        } else {
                            list.forEach { item ->
                                vehicleList.add(
                                    VehicleReportItem(
                                        deviceId = item.deviceId,
                                        vehicle = item.vehicleNumber,
                                        violationType = when (item.additionalData?.lowercase()) {
                                            "over speed" -> "Over Speed"
                                            "harsh braking" -> "Harsh Braking"
                                            "harsh acceleration" -> "Harsh Acceleration"
                                            else -> item.additionalData ?: "No Violations"
                                        },
                                        latitude = item.latitude.toDoubleOrNull() ?: 0.0,
                                        longitude = item.longitude.toDoubleOrNull() ?: 0.0,
                                        speed = item.speed.toFloatOrNull() ?: 0f,
                                        time = item.timestamp,
                                        driverName = item.ownerName
                                    )
                                )
                            }
                            if (list.size < pageLimit) hasMoreData = false
                            page++
                            filteredList.clear()
                            filteredList.addAll(
                                if (selectedViolation != "All Violations" && selectedViolation != "Select Violation") {
                                    val normalizedSelected = selectedViolation.trim().lowercase().replace(" ", "")
                                    vehicleList.filter {
                                        it.violationType.trim().lowercase().replace(" ", "") == normalizedSelected
                                    }
                                } else {
                                    vehicleList
                                }
                            )
                        }
                    } else {
                        Toast.makeText(context, "API Error: ${resp.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: java.net.SocketTimeoutException) {
                    Toast.makeText(context, "Request timed out. Please try a smaller date range.", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } finally {
                    isLoading = false
                    isLoadingMore = false
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
                    text = "Vehicle Violation Report",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center
                )

                // Filter and Download Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Filter",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Filter", fontSize = 14.sp)
                    }

                    Button(
                        onClick = { showDownloadDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Download", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Fetching Data...", color = Color.White, fontSize = 16.sp)
                        }
                    }
                } else if (filteredList.isNotEmpty()) {
                    // Report Table
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xCC000000)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            // Table Header
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1976D2))
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Device ID",
                                        modifier = Modifier.weight(1.5f),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Violation",
                                        modifier = Modifier.weight(1.5f),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Speed",
                                        modifier = Modifier.weight(1f),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        "Timestamp",
                                        modifier = Modifier.weight(2f),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Table Rows
                            items(filteredList) { item ->
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedItem = item }
                                            .background(Color(0x22FFFFFF))
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.deviceId,
                                            modifier = Modifier.weight(1.5f),
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = item.violationType,
                                            modifier = Modifier.weight(1.5f),
                                            color = when(item.violationType) {
                                                "Over Speed" -> Color(0xFFFF5252)
                                                "Harsh Braking" -> Color(0xFFFFC107)
                                                "Harsh Acceleration" -> Color(0xFFFF9800)
                                                else -> Color.White
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${item.speed.toInt()} km/h",
                                            modifier = Modifier.weight(1f),
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = item.time.take(16).replace("T", " "),
                                            modifier = Modifier.weight(2f),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Divider(color = Color(0x33FFFFFF), thickness = 0.5.dp)
                                }
                            }

                            // Load More Button
                            if (fromDateTime != "Select From Date & Time" && toDateTime != "Select To Date & Time") {
                                if (hasMoreData && !isLoadingMore) {
                                    item {
                                        OutlinedButton(
                                            onClick = { fetchVehicleReports() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Text("Load More", color = Color.White)
                                        }
                                    }
                                }
                            }

                            if (isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No data available. Please apply filters to fetch reports.",
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Filter Dialog - IMPROVED UI
        if (showFilterDialog) {
            AlertDialog(
                onDismissRequest = { showFilterDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            if (fromDateTime == "Select From Date & Time" || toDateTime == "Select To Date & Time") {
                                Toast.makeText(context, "Please select From & To date before fetching data.", Toast.LENGTH_SHORT).show()
                            } else {
                                showFilterDialog = false
                                fetchVehicleReports(reset = true)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
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
                    Text(
                        "Filter Reports",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        // From Date & Time
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "From Date & Time",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                            OutlinedButton(
                                onClick = { showDateTimePicker { fromDateTime = it } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    fromDateTime,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // To Date & Time
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "To Date & Time",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                            OutlinedButton(
                                onClick = { showDateTimePicker { toDateTime = it } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    toDateTime,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Device ID Dropdown
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Device ID",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                            Box {
                                OutlinedButton(
                                    onClick = { deviceExpanded = !deviceExpanded },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        selectedDevice,
                                        modifier = Modifier.weight(1f),
                                        fontSize = 13.sp
                                    )
                                    if (isLoadingDevices) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .height(16.dp)
                                                .width(16.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null
                                        )
                                    }
                                }
                                androidx.compose.material3.DropdownMenu(
                                    expanded = deviceExpanded,
                                    onDismissRequest = { deviceExpanded = false }
                                ) {
                                    availableDevices.forEach { device ->
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { Text(device) },
                                            onClick = {
                                                selectedDevice = device
                                                deviceExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Violation Type Dropdown
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Violation Type",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                            Box {
                                OutlinedButton(
                                    onClick = { violationExpanded = !violationExpanded },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        selectedViolation,
                                        modifier = Modifier.weight(1f),
                                        fontSize = 13.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                }
                                androidx.compose.material3.DropdownMenu(
                                    expanded = violationExpanded,
                                    onDismissRequest = { violationExpanded = false }
                                ) {
                                    violationOptions.forEach { option ->
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                selectedViolation = option
                                                violationExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }

        // Download Dialog
        if (showDownloadDialog) {
            AlertDialog(
                onDismissRequest = { showDownloadDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            showDownloadDialog = false
                            showDownloadProgress = true
                            startDownload = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Download")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDownloadDialog = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Choose Format", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("PDF", "Excel").forEach { format ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedFormat = format }
                                    .background(if (selectedFormat == format) Color(0x22000000) else Color.Transparent)
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.RadioButton(
                                    selected = selectedFormat == format,
                                    onClick = { selectedFormat = format }
                                )
                                Text(text = format, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            )
        }

        // Detail Dialog - Shows all information
        selectedItem?.let { item ->
            AlertDialog(
                onDismissRequest = { selectedItem = null },
                confirmButton = {
                    Button(
                        onClick = { selectedItem = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Text("Close")
                    }
                },
                title = {
                    Text(
                        "Vehicle Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailRow("Device ID", item.deviceId)
                        DetailRow("Vehicle Number", item.vehicle)
                        DetailRow("Violation Type", item.violationType)
                        DetailRow("Speed", "${item.speed} km/h")
                        DetailRow("Latitude", item.latitude.toString())
                        DetailRow("Longitude", item.longitude.toString())
                        DetailRow("Timestamp", item.time)
                        DetailRow("Driver Name", item.driverName)
                    }
                }
            )
        }

        if (showDownloadProgress) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text("Downloading $selectedFormat...", fontWeight = FontWeight.SemiBold)
                },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 16.dp))
                        Text("Please wait while the file is being saved.")
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }

        if (showDownloadCompleteDialog) {
            AlertDialog(
                onDismissRequest = { showDownloadCompleteDialog = false },
                title = { Text("Download Completed", fontWeight = FontWeight.Bold) },
                text = {
                    Text("File saved successfully in:\n\n$downloadFilePath")
                },
                confirmButton = {
                    Button(
                        onClick = { showDownloadCompleteDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {}
            )
        }

        LaunchedEffect(startDownload) {
            if (startDownload) {
                showDownloadDialog = true
                showDownloadProgress = true

                withContext(Dispatchers.IO) {
                    val path = if (selectedFormat == "PDF") {
                        downloadAsPDF(filteredList, context)
                    } else {
                        downloadAsExcel(filteredList, context)
                    }

                    withContext(Dispatchers.Main) {
                        downloadFilePath = path
                        showDownloadProgress = false
                        showDownloadDialog = false
                        showDownloadCompleteDialog = true
                        startDownload = false
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End
        )
    }
}

suspend fun downloadAsPDF(
    data: List<VehicleReportItem>,
    context: Context
): String {
    val pdf = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    val margin = 20
    val lineHeight = 18
    val logoHeight = 50
    val headerHeight = 40
    val footerHeight = 30
    val tableStartY = margin + logoHeight + headerHeight
    val usableHeight = pageHeight - tableStartY - footerHeight

    val rowsPerPage = usableHeight / lineHeight

    val textPaint = TextPaint().apply {
        textSize = 10f
        typeface = android.graphics.Typeface.MONOSPACE
        color = android.graphics.Color.BLACK
    }

    val headerColumns = listOf("DeviceID", "Vehicle", "Violation", "Lat", "Long", "Spd", "Time", "Driver")
    val columnWidths = listOf(60, 60, 80, 60, 60, 40, 120, 60)
    val columnX = columnWidths.runningFold(margin) { acc, w -> acc + w }

    val logo = BitmapFactory.decodeResource(context.resources, R.drawable.thinlogo)

    val totalPages = (data.size + rowsPerPage - 1) / rowsPerPage

    for (pageIndex in 0 until totalPages) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val scaledLogo = Bitmap.createScaledBitmap(logo, 100, logoHeight, false)
        canvas.drawBitmap(scaledLogo, margin.toFloat(), margin.toFloat(), null)

        val headerY = margin + logoHeight + 10
        for (i in headerColumns.indices) {
            canvas.drawText(
                headerColumns[i],
                columnX[i].toFloat(),
                headerY.toFloat(),
                textPaint
            )
        }

        val lineTop = headerY + 5
        canvas.drawLine(
            margin.toFloat(),
            lineTop.toFloat(),
            (pageWidth - margin).toFloat(),
            lineTop.toFloat(),
            textPaint
        )

        val start = pageIndex * rowsPerPage
        val end = minOf(start + rowsPerPage, data.size)
        var y = lineTop + 15

        for (item in data.subList(start, end)) {
            val row = listOf(
                item.deviceId.take(10),
                item.vehicle.take(10),
                item.violationType.take(15),
                "%.5f".format(item.latitude),
                "%.5f".format(item.longitude),
                "%.1f".format(item.speed),
                item.time.take(19),
                item.driverName.take(10)
            )
            for (i in row.indices) {
                canvas.drawText(row[i], columnX[i].toFloat(), y.toFloat(), textPaint)
            }

            canvas.drawLine(
                margin.toFloat(),
                (y + 5).toFloat(),
                (pageWidth - margin).toFloat(),
                (y + 5).toFloat(),
                textPaint
            )
            y += lineHeight
        }

        for (x in columnX) {
            canvas.drawLine(x.toFloat(), tableStartY.toFloat(), x.toFloat(), y.toFloat(), textPaint)
        }

        val footerText = "Generated by GPSApp • Page ${pageIndex + 1} of $totalPages"
        canvas.drawText(
            footerText,
            margin.toFloat(),
            (pageHeight - margin).toFloat(),
            textPaint
        )

        pdf.finishPage(page)
    }

    val fileName = "vehicle_report_${System.currentTimeMillis()}.pdf"
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        fileName
    )

    try {
        pdf.writeTo(FileOutputStream(file))
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "PDF saved: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        }
    } catch (e: IOException) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "PDF failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    } finally {
        pdf.close()
    }

    return file.absolutePath
}

suspend fun downloadAsExcel(
    data: List<VehicleReportItem>,
    context: Context
): String {
    return try {
        val fileName = "vehicle_report_${System.currentTimeMillis()}.csv"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val filePath = File(downloadsDir, fileName)

        val csvHeader = "Device ID,Vehicle,Violation,Latitude,Longitude,Speed,Time,Driver"
        val csvRows = data.joinToString("\n") {
            listOf(
                it.deviceId,
                it.vehicle,
                it.violationType,
                it.latitude.toString(),
                it.longitude.toString(),
                it.speed.toString(),
                "\"${it.time}\"",
                it.driverName
            ).joinToString(",")
        }

        FileOutputStream(filePath).use {
            it.write((csvHeader + "\n" + csvRows).toByteArray())
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "CSV saved: ${filePath.absolutePath}", Toast.LENGTH_SHORT).show()
        }

        filePath.absolutePath
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Excel failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
        ""
    }
}