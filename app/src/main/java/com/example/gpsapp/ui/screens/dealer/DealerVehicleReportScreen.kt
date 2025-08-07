package com.example.gpsapp.ui.screens.dealer

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gpsapp.R
import com.example.gpsapp.network.RetrofitClient
import com.example.gpsapp.ui.components.ScaffoldWithDrawer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


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
fun DealerVehicleReportScreen(navController: NavController) {
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
        var isLoading by remember { mutableStateOf(true) }

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
            isLoading = true
            try {
                val resp = RetrofitClient.apiService.getVehicleViolations()
                if (resp.isSuccessful) {
                    val list = resp.body().orEmpty()
                    vehicleList.clear()
                    filteredList.clear()

                    list.forEach { item ->
                        val violationType = item.additionalData ?: "No Violations"
                        vehicleList.add(
                            VehicleReportItem(
                                deviceId = item.deviceId ?: "N/A",
                                vehicle = item.vehicleNumber ?: "N/A",
                                violationType = violationType,
                                latitude = item.latitude.toDoubleOrNull() ?: 0.0,
                                longitude = item.longitude.toDoubleOrNull() ?: 0.0,
                                speed = item.speed.toFloatOrNull() ?: 0f,
                                time = item.timestamp ?: "N/A",
                                driverName = "N/A"
                            )
                        )
                    }

                    filteredList.addAll(vehicleList)
                } else {
                    Toast.makeText(context, "API error ${resp.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
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
                    Button(onClick = { showDownloadDialog = true }) {
                        Text("Download Report")
                    }

                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Fetching Data...", color = Color.White, fontSize = 16.sp)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(
                                    "Dev",
                                    "Veh",
                                    "Viol",
                                    "Lat",
                                    "Long",
                                    "Spd",
                                    "Time",
                                    "Drv"
                                ).forEach {
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
            }
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

        if (showDownloadDialog) {
            AlertDialog(
                onDismissRequest = { showDownloadDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDownloadDialog = false
                        showDownloadProgress = true
                        startDownload = true
                    }) {
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
                                    .clickable { selectedFormat = format }
                                    .padding(vertical = 4.dp),
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
                    TextButton(onClick = {
                        showDownloadCompleteDialog = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {}
            )
        }
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

        // Draw logo
        val scaledLogo = Bitmap.createScaledBitmap(logo, 100, logoHeight, false)
        canvas.drawBitmap(scaledLogo, margin.toFloat(), margin.toFloat(), null)

        // Draw header row
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

        // Draw rows
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

            // Draw horizontal line under row
            canvas.drawLine(
                margin.toFloat(),
                (y + 5).toFloat(),
                (pageWidth - margin).toFloat(),
                (y + 5).toFloat(),
                textPaint
            )
            y += lineHeight
        }

        // Draw vertical lines for borders
        for (x in columnX) {
            canvas.drawLine(x.toFloat(), tableStartY.toFloat(), x.toFloat(), y.toFloat(), textPaint)
        }

        // Draw footer
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
                "\"${it.time}\"", // ⬅️ Wrap time in quotes
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
