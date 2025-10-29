package com.example.gpsapp.ui.screens.admin

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.text.TextPaint
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.datastore.core.IOException
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
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEventReportScreen(navController: NavController) {
    ScaffoldWithDrawer(
        navController = navController,
        screenTitle = "Event Report",
        role = "admin"
    ) { innerPadding ->

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        var showFilterDialog by remember { mutableStateOf(false) }
        var showDownloadDialog by remember { mutableStateOf(false) }
        var showDownloadProgress by remember { mutableStateOf(false) }
        var downloadProgress by remember { mutableStateOf(0f) }
        var isDownloading by remember { mutableStateOf(false) }
        var selectedItem by remember { mutableStateOf<EventReportItem?>(null) }

        var showDownloadComplete by remember { mutableStateOf(false) }
        var downloadedFileName by remember { mutableStateOf("") }
        var downloadedFilePath by remember { mutableStateOf("") }

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

        var currentFilteredDeviceId by remember { mutableStateOf<String?>(null) }
        var isFiltered by remember { mutableStateOf(false) }

        val statusOptions = listOf("All", "Running", "Idle", "Parked")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

        fun getDownloadsDirectory(): File {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath)
            } else {
                File(Environment.getExternalStorageDirectory(), "Download")
            }
        }

        fun buildCsvContent(items: List<EventReportItem>): String {
            val header = "Device ID,Timestamp,Latitude,Longitude,Speed,Ignition,Vehicle Status,Additional Data\n"
            val rows = items.joinToString("\n") { item ->
                "\"${item.deviceId ?: "Unknown"}\",\"${item.timestamp ?: "Unknown"}\",\"${item.latitude ?: "-"}\",\"${item.longitude ?: "-"}\",\"${item.speed ?: "-"}\",\"${item.ignition ?: "-"}\",\"${item.vehicleStatus ?: "-"}\",\"${item.additionalData ?: "-"}\""
            }
            return header + rows
        }

        fun generateCsvFile(fileName: String, items: List<EventReportItem>): File {
            val downloadsDir = getDownloadsDirectory()
            downloadsDir.mkdirs()

            val file = File(downloadsDir, fileName)
            val csvContent = buildCsvContent(items)
            file.writeText(csvContent)

            println("CSV file created: ${file.absolutePath}")
            println("CSV file exists: ${file.exists()}")
            println("CSV file size: ${file.length()} bytes")

            return file
        }

        fun generatePdfFile(fileName: String, items: List<EventReportItem>): File {
            val downloadsDir = getDownloadsDirectory()
            downloadsDir.mkdirs()

            val file = File(downloadsDir, fileName)
            val pdf = PdfDocument()

            val pageWidth = 595
            val pageHeight = 842
            val margin = 20
            val lineHeight = 16
            val usableHeight = pageHeight - (margin * 2)
            val rowsPerPage = usableHeight / lineHeight

            val textPaint = TextPaint().apply {
                textSize = 9f
                typeface = android.graphics.Typeface.MONOSPACE
                color = android.graphics.Color.BLACK
            }

            val headerColumns = listOf("Device ID", "Timestamp", "Lat", "Long", "Speed", "Ignition", "Status", "Data")
            val columnWidths = listOf(70, 100, 50, 50, 50, 50, 50, 60)
            val columnX = columnWidths.runningFold(margin) { acc, w -> acc + w }

            val totalPages = (items.size + rowsPerPage - 1) / rowsPerPage

            for (pageIndex in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
                val page = pdf.startPage(pageInfo)
                val canvas = page.canvas

                // Draw title
                val titlePaint = TextPaint().apply {
                    textSize = 14f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    color = android.graphics.Color.BLACK
                }
                canvas.drawText("EVENT REPORT", margin.toFloat(), (margin + 15).toFloat(), titlePaint)

                // Draw timestamp
                val timestamp = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
                val metaPaint = TextPaint().apply {
                    textSize = 8f
                    color = android.graphics.Color.GRAY
                }
                canvas.drawText("Generated: $timestamp", margin.toFloat(), (margin + 30).toFloat(), metaPaint)

                val headerY = margin + 45

                // Draw column headers
                for (i in headerColumns.indices) {
                    canvas.drawText(
                        headerColumns[i],
                        columnX[i].toFloat(),
                        headerY.toFloat(),
                        textPaint.apply { typeface = android.graphics.Typeface.DEFAULT_BOLD }
                    )
                }

                // Draw header separator line
                val lineTop = headerY + 5
                canvas.drawLine(
                    margin.toFloat(),
                    lineTop.toFloat(),
                    (pageWidth - margin).toFloat(),
                    lineTop.toFloat(),
                    textPaint
                )

                // Draw table data
                val start = pageIndex * rowsPerPage
                val end = minOf(start + rowsPerPage, items.size)
                var y = lineTop + 12

                textPaint.typeface = android.graphics.Typeface.MONOSPACE

                for (item in items.subList(start, end)) {
                    fun truncate(text: String?, length: Int, default: String = "-"): String {
                        return (text ?: default).let {
                            if (it.length > length) it.substring(0, length) else it
                        }
                    }

                    val row = listOf(
                        truncate(item.deviceId, 10, "Unknown"),
                        truncate(item.timestamp, 19, "Unknown"),
                        truncate(item.latitude, 6),
                        truncate(item.longitude, 6),
                        truncate(item.speed?.toString(), 5),
                        truncate(item.ignition, 5),
                        truncate(item.vehicleStatus, 8),
                        truncate(item.additionalData, 10)
                    )

                    for (i in row.indices) {
                        canvas.drawText(row[i], columnX[i].toFloat(), y.toFloat(), textPaint)
                    }

                    y += lineHeight
                }

                // Draw footer
                val footerText = "Page ${pageIndex + 1} of $totalPages"
                canvas.drawText(
                    footerText,
                    margin.toFloat(),
                    (pageHeight - margin).toFloat(),
                    metaPaint
                )

                pdf.finishPage(page)
            }

            try {
                pdf.writeTo(FileOutputStream(file))
                println("✅ PDF file created: ${file.absolutePath}")
                println("PDF file size: ${file.length()} bytes")
            } catch (e: IOException) {
                println("❌ PDF creation error: ${e.message}")
                e.printStackTrace()
            } finally {
                pdf.close()
            }

            return file
        }

        fun downloadReport(format: String) {
            coroutineScope.launch {
                isDownloading = true
                showDownloadProgress = true
                downloadProgress = 0f

                try {
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
                    val fileName = "EventReport_$timestamp.$format"

                    println("Starting download: $fileName")

                    downloadProgress = 0.2f
                    kotlinx.coroutines.delay(300)

                    downloadProgress = 0.4f
                    println("Generating $format report with ${reportList.size} items...")

                    val file = if (format == "csv") {
                        generateCsvFile(fileName, reportList)
                    } else {
                        generatePdfFile(fileName, reportList)
                    }

                    downloadProgress = 0.8f
                    kotlinx.coroutines.delay(300)

                    if (!file.exists()) {
                        throw Exception("Failed to create file: $fileName")
                    }

                    downloadProgress = 1f
                    downloadedFileName = fileName
                    downloadedFilePath = file.absolutePath

                    isDownloading = false
                    showDownloadProgress = false
                    showDownloadDialog = false
                    showDownloadComplete = true

                    println("✅ Download completed successfully!")
                    println("File: $downloadedFileName")
                    println("Path: $downloadedFilePath")
                    println("Size: ${file.length()} bytes")

                    Toast.makeText(context, "Report downloaded successfully", Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    println("❌ Download error: ${e.message}")
                    e.printStackTrace()
                    isDownloading = false
                    showDownloadProgress = false
                    Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

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

                    android.app.TimePickerDialog(
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

        LaunchedEffect(Unit) {
            viewModel.fetchEventReports("", "", "", null, reset = true)
        }

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
                Text(
                    text = "Event Status Report",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                        enabled = reportList.isNotEmpty() && !isDownloading
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

                if (showDownloadProgress) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Downloading...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${(downloadProgress * 100).toInt()}%",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF0066CC),
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

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

            // Download Format Dialog
            if (showDownloadDialog && !showDownloadProgress) {
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
                                onClick = { downloadReport("pdf") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isDownloading
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Download as PDF")
                            }

                            Button(
                                onClick = { downloadReport("csv") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isDownloading
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.size(8.dp))
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

            // Download Complete Dialog
            if (showDownloadComplete) {
                AlertDialog(
                    onDismissRequest = { showDownloadComplete = false },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = Color(0xFF00AA44)
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Text(
                                "Download Completed",
                                color = Color(0xFF00AA44),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                "Your report has been downloaded successfully!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF0066CC).copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.material3.CardDefaults.outlinedCardBorder()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            "File Name",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF0066CC)
                                        )
                                        Text(
                                            downloadedFileName,
                                            fontSize = 13.sp,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    HorizontalDivider(
                                        color = Color(0xFF0066CC).copy(alpha = 0.3f),
                                        thickness = 1.dp
                                    )

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            "Location",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF0066CC)
                                        )
                                        Text(
                                            "Downloads Folder",
                                            fontSize = 13.sp,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    HorizontalDivider(
                                        color = Color(0xFF0066CC).copy(alpha = 0.3f),
                                        thickness = 1.dp
                                    )

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            "Status",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF0066CC)
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = Color(0xFF00AA44)
                                            )
                                            Text(
                                                "Ready to use",
                                                fontSize = 13.sp,
                                                color = Color(0xFF00AA44),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showDownloadComplete = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0066CC)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Done", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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