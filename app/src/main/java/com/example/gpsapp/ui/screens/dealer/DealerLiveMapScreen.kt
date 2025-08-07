@file:Suppress("DEPRECATION")
package com.example.gpsapp.ui.screens.dealer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.gpsapp.R
import com.example.gpsapp.data.model.LiveVehicle
import com.example.gpsapp.network.RetrofitClient
import com.example.gpsapp.ui.components.ScaffoldWithDrawer
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

fun getAddressFromLocation(context: Context, lat: Double, lon: Double): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()?.getAddressLine(0) ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}

fun calculateBearing(from: GeoPoint, to: GeoPoint): Float {
    val lat1 = Math.toRadians(from.latitude)
    val lon1 = Math.toRadians(from.longitude)
    val lat2 = Math.toRadians(to.latitude)
    val lon2 = Math.toRadians(to.longitude)
    val dLon = lon2 - lon1
    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
    val bearing = Math.toDegrees(atan2(y, x))
    return ((bearing + 360) % 360).toFloat()
}

fun getCarIcon(context: Context, bearing: Float): BitmapDrawable {
    // Normalize bearing to closest 22-degree interval
    val normalized = (bearing / 22.5f).roundToInt() * 22
    val assetName = "car_${normalized % 360}.png"

    return try {
        val inputStream = context.assets.open(assetName)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        BitmapDrawable(context.resources, bitmap)
    } catch (e: Exception) {
        // Fallback if asset not found
        val fallback = BitmapFactory.decodeResource(context.resources, R.drawable.car_0)
        BitmapDrawable(context.resources, fallback)
    }
}

fun getRotatedBitmapDrawableLive(context: Context, angle: Float): BitmapDrawable {
    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.car)
    val matrix = Matrix().apply { postRotate(angle) }
    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    return BitmapDrawable(context.resources, rotated)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealerLiveMapScreen(navController: NavController, role: String) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var selectedVehicle by remember { mutableStateOf<LiveVehicle?>(null) }
    val mapInitialized = remember { mutableStateOf(false) }
    val vehicleMarkers = remember { mutableStateMapOf<String, Marker>() }
    var isSidebarVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val liveVehicles = remember { mutableStateListOf<LiveVehicle>() }



    suspend fun fetchLiveVehicles() {
        try {
            val response = RetrofitClient.apiService.getLiveVehicles()
            if (!response.isSuccessful) {
                Toast.makeText(context, "API error ${response.code()}", Toast.LENGTH_SHORT).show()
                return
            }

            response.body()?.let { list ->
                liveVehicles.clear()
                liveVehicles.addAll(list)

                list.forEach { vehicle ->
                    val geoPoint = GeoPoint(vehicle.latitude, vehicle.longitude)
                    val address = getAddressFromLocation(context, vehicle.latitude, vehicle.longitude)
                    val marker = vehicleMarkers[vehicle.deviceId]
                    val previous = marker?.position

                    if (marker == null) {
                        val newMarker = Marker(mapView).apply {
                            position = geoPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            setInfoWindowAnchor(Marker.ANCHOR_CENTER, -2.5f)
                            isFlat = true
                            icon = if (vehicle.liveStatus.equals("RUNNING", true)) {
                                getRotatedBitmapDrawableLive(context, 0f)
                            } else {
                                BitmapDrawable(
                                    context.resources, BitmapFactory.decodeResource(context.resources,
                                        when (vehicle.liveStatus.uppercase()) {
                                            "IDLE", "PARKED" -> R.drawable.caryellow
                                            "OFFLINE" -> R.drawable.carred
                                            else -> R.drawable.car
                                        }
                                    )
                                )
                            }
                            title = vehicle.deviceId
                            snippet = """
                                Vehicle: ${vehicle.deviceId}
                                Status: ${vehicle.liveStatus}
                                Speed: ${vehicle.speed} km/h
                                Time: ${vehicle.timestamp}
                                Addr: $address
                            """.trimIndent()
                            relatedObject = vehicle

                            setOnMarkerClickListener { m, _ ->
                                (m.relatedObject as? LiveVehicle)?.let {
                                    selectedVehicle = it
                                }
                                true
                            }
                        }
                        vehicleMarkers[vehicle.deviceId] = newMarker
                        mapView.overlays.add(newMarker)
                    } else {
                        if (previous != null && (previous.latitude != geoPoint.latitude || previous.longitude != geoPoint.longitude)) {
                            val bearing = calculateBearing(previous, geoPoint)
                            val rotatedIcon = getRotatedBitmapDrawableLive(context, bearing)
                            marker.icon = rotatedIcon
                        }
                        marker.position = geoPoint
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        mapView.invalidate()


                        val oldStatus = (marker.relatedObject as? LiveVehicle)?.liveStatus
                        val statusChanged = oldStatus != vehicle.liveStatus

                        if (vehicle.liveStatus.equals("RUNNING", true)) {
                            val bearing = previous?.let { calculateBearing(it, geoPoint) } ?: (vehicle.course ?: 0f)
                            val rotatedIcon = getRotatedBitmapDrawableLive(context, bearing)
                            marker.icon = rotatedIcon
                        }

                        else if (statusChanged) {
                            marker.icon = BitmapDrawable(
                                context.resources,
                                BitmapFactory.decodeResource(
                                    context.resources,
                                    when (vehicle.liveStatus.uppercase()) {
                                        "IDLE", "PARKED" -> R.drawable.caryellow
                                        "OFFLINE" -> R.drawable.carred
                                        else -> R.drawable.car
                                    }
                                )
                            )
                        }

                        marker.relatedObject = vehicle

                        marker.snippet = """
    Vehicle: ${vehicle.deviceId}
    Status: ${vehicle.liveStatus}
    Speed: ${vehicle.speed} km/h
    Time: ${vehicle.timestamp}
    Addr: $address
""".trimIndent()
                    }
                }
                mapView.invalidate()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Fetch failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
        mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(true)
            controller.setZoom(6.0)
            controller.setCenter(GeoPoint(20.5937, 78.9629))
        }

        mapView.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                selectedVehicle = null
            }
            false
        }

        mapInitialized.value = true

        while (true) {
            fetchLiveVehicles()
            delay(1000)
        }
    }


    ScaffoldWithDrawer(
        navController = navController,
        screenTitle = "Live Map",
        role = "superadmin",
        disableGestures = true
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
        {
            if (mapInitialized.value) {
                AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
            }

            FloatingActionButton(
                onClick = {
                    mapView.controller.setZoom(6.0)
                    mapView.controller.setCenter(GeoPoint(20.5937, 78.9629))
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = "Recenter")
            }

            IconButton(
                onClick = { isSidebarVisible = !isSidebarVisible },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }

            AnimatedVisibility(
                visible = isSidebarVisible,
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxHeight()
                    .width(300.dp)
                    .background(Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text("✖", Modifier.clickable { isSidebarVisible = false })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val filtered = liveVehicles.filter {
                        it.deviceId.contains(searchQuery.text, ignoreCase = true)
                    }

                    SummaryGroup(filtered)

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search Device ID") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Live Vehicles", style = MaterialTheme.typography.titleMedium)
                    LazyColumn {
                        items(filtered) { v ->
                            VehicleRow(
                                vehicle = v,
                                mapView = mapView,
                                onLocate = { isSidebarVisible = false }
                            )
                        }
                    }
                }
            }
            if (selectedVehicle != null) {
                VehiclePopupDialog(
                    vehicle = selectedVehicle!!,
                    navController = navController,
                    onDismiss = { selectedVehicle = null }
                )
            }
        }
    }
}

@Composable
private fun SummaryGroup(vehicles: List<LiveVehicle>) {
    Column {
        SummaryRow("Total", vehicles.size, true)
        SummaryRow("Running", vehicles.count { it.liveStatus.equals("RUNNING", true) })
        SummaryRow("Idle", vehicles.count { it.liveStatus.equals("IDLE", true) })
        SummaryRow("Parked", vehicles.count { it.liveStatus.equals("PARKED", true) })
        SummaryRow("Offline", vehicles.count { it.liveStatus.equals("OFFLINE", true) })
    }
}

@Composable
private fun SummaryRow(label: String, count: Int, isBold: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text("$count", fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun VehicleRow(
    vehicle: LiveVehicle,
    mapView: MapView,
    onLocate: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(vehicle.liveStatus, Modifier.weight(1f), color = getStatusColor(vehicle.liveStatus))
        Text(vehicle.deviceId, Modifier.weight(2f))
        Text(vehicle.timestamp?.takeLast(8) ?: "--:--:--", Modifier.weight(1f))
        IconButton(
            onClick = {
                mapView.controller.setZoom(18.0)
                mapView.controller.setCenter(GeoPoint(vehicle.latitude, vehicle.longitude))
                onLocate()
            },
            modifier = Modifier.weight(0.5f)
        ) {
            Icon(Icons.Filled.Place, contentDescription = "Locate")
        }
    }
}

private fun getStatusColor(status: String): Color = when (status.uppercase(Locale.getDefault())) {
    "RUNNING", "MOVING" -> Color(0xFF4CAF50)
    "IDLE" -> Color(0xFFFFA000)
    "PARKED" -> Color(0xFF1976D2)
    "OFFLINE" -> Color.Gray
    else -> Color.Black
}

@Composable
fun VehiclePopupDialog(vehicle: LiveVehicle, navController: NavController, onDismiss: () -> Unit)
{
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // ❌ Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "● Last Received: ${vehicle.timestamp ?: "--"}",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "✖",
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(start = 8.dp),
                        color = Color.Gray
                    )
                }

                Spacer(Modifier.height(8.dp))
                Divider(color = Color.LightGray)

                Spacer(Modifier.height(8.dp))

                InfoRow(Icons.Default.DirectionsCar, "Vehicle No", vehicle.deviceId)
                InfoRow(Icons.Default.AccessTime, "Time Interval", "002,010,010")
                InfoRow(Icons.Default.LocationOn, "Latitude", vehicle.latitude.toString())
                InfoRow(Icons.Default.LocationOn, "Longitude", vehicle.longitude.toString())
                InfoRow(Icons.Default.Speed, "Speed", "${vehicle.speed} km/h")
                InfoRow(
                    Icons.Default.Power,
                    "Ignition",
                    if (vehicle.ignition.equals("IGON", true)) "ON" else "OFF",
                    valueColor = if (vehicle.ignition.equals("IGON", true)) Color(0xFF4CAF50) else Color.Red
                )
                InfoRow(Icons.Default.CheckBox, "Status", vehicle.liveStatus)
                InfoRow(Icons.Default.SignalCellularAlt, "GSM", "Perfect (46)", valueColor = Color(0xFF4CAF50))

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActionButton("Playback") {
                        onDismiss()
                        navController.navigate("playback_map/{deviceId}")
                    }
                }
            }
        }
    }
}
@Composable
fun InfoRow(icon: ImageVector, label: String, value: String, valueColor: Color = Color.Black) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = Color.Black, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label: ", fontWeight = FontWeight.Bold)
        Text(text = value, color = valueColor)
    }
}

@Composable
fun ActionButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(Color(0xFF001F54), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = label, color = Color.White)
    }
}