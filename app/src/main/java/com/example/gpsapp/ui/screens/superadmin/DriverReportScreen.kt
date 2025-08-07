package com.example.gpsapp.ui.screens.superadmin

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gpsapp.R
import com.example.gpsapp.ui.components.ScaffoldWithDrawer
import java.util.Calendar

data class DriverReportItem(
    val year: String,
    val month: String,
    val deviceId: String,
    val maxSpeed: String,
    val totalDistance: String
)

@Composable
fun DriverReportScreen(navController: NavController) {
    val context = LocalContext.current
    var showFilterDialog by remember { mutableStateOf(false) }

    // Filter state
    var selectedYear by remember { mutableStateOf("Select Year") }
    var selectedMonth by remember { mutableStateOf("Select Month") }
    var selectedDevice by remember { mutableStateOf("Select Device") }

    // Dummy data
    val allData = remember {
        listOf(
            DriverReportItem("2025", "June", "Dev1", "120 km/h", "350 km"),
            DriverReportItem("2025", "May", "Dev2", "115 km/h", "300 km"),
            DriverReportItem("2024", "December", "Dev3", "130 km/h", "400 km"),
            DriverReportItem("2024", "November", "Dev1", "110 km/h", "270 km"),
            DriverReportItem("2023", "August", "Dev4", "125 km/h", "320 km")
        )
    }

    val filteredData = remember(selectedYear, selectedMonth, selectedDevice) {
        allData.filter {
            (selectedYear == "Select Year" || it.year == selectedYear) &&
                    (selectedMonth == "Select Month" || it.month == selectedMonth) &&
                    (selectedDevice == "Select Device" || it.deviceId == selectedDevice)
        }
    }

    ScaffoldWithDrawer(
        navController = navController,
        screenTitle = "Driver Report",
        role = "superadmin"
    ) { innerPadding ->
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
                    text = "Driver RAG Report",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { showFilterDialog = true }) {
                        Text("Filter")
                    }

                    Button(onClick = {
                        selectedYear = "Select Year"
                        selectedMonth = "Select Month"
                        selectedDevice = "Select Device"
                        Toast.makeText(context, "Filters Reset", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Reset Filter")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(filteredData) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .background(Color.DarkGray.copy(alpha = 0.2f))
                                .padding(12.dp)
                        ) {
                            Text("Max Speed: ${it.maxSpeed}", color = Color.White, modifier = Modifier.weight(1f))
                            Text("Total Distance: ${it.totalDistance}", color = Color.White, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (showFilterDialog) {
                FilterPopup(
                    selectedYear = selectedYear,
                    selectedMonth = selectedMonth,
                    selectedDevice = selectedDevice,
                    onDismiss = { showFilterDialog = false },
                    onApply = { year, month, device ->
                        if (year == "Select Year" || month == "Select Month" || device == "Select Device") {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        } else {
                            selectedYear = year
                            selectedMonth = month
                            selectedDevice = device
                            showFilterDialog = false
                        }
                    }
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPopup(
    selectedYear: String,
    selectedMonth: String,
    selectedDevice: String,
    onDismiss: () -> Unit,
    onApply: (String, String, String) -> Unit
) {
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonthIndex = calendar.get(Calendar.MONTH)

    val yearOptions = listOf("Select Year") + (2015..currentYear).map { it.toString() }.reversed()

    var selectedYearInternal by remember { mutableStateOf(selectedYear) }
    var selectedMonthInternal by remember { mutableStateOf(selectedMonth) }
    var selectedDeviceInternal by remember { mutableStateOf(selectedDevice) }

    val allMonths = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val monthOptions = remember(selectedYearInternal) {
        val months = if (selectedYearInternal == currentYear.toString()) {
            allMonths.take(currentMonthIndex + 1)
        } else {
            allMonths
        }
        listOf("Select Month") + months
    }

    val deviceOptions = listOf("Select Device", "Dev1", "Dev2", "Dev3", "Dev4", "Dev5")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onApply(selectedYearInternal, selectedMonthInternal, selectedDeviceInternal)
            }) {
                Text("Apply Filter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Filter Options") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DropdownField(
                    label = "Year",
                    options = yearOptions,
                    selected = selectedYearInternal,
                    onSelected = {
                        selectedYearInternal = it
                        selectedMonthInternal = "Select Month"
                    }
                )
                DropdownField(
                    label = "Month",
                    options = monthOptions,
                    selected = selectedMonthInternal,
                    onSelected = { selectedMonthInternal = it }
                )
                DropdownField(
                    label = "Device ID",
                    options = deviceOptions,
                    selected = selectedDeviceInternal,
                    onSelected = { selectedDeviceInternal = it }
                )
            }
        },
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color.Black
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selected,
            onValueChange = {},
            label = { Text(label, color = Color.Black) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
            ),
            textStyle = TextStyle(color = Color.Black)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
