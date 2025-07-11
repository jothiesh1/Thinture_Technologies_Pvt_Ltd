@file:Suppress("DEPRECATION")

package com.example.gpsapp.ui.screens.admin

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.gpsapp.R
import com.example.gpsapp.data.model.LiveVehicle
import com.example.gpsapp.network.RetrofitClient
import com.example.gpsapp.ui.components.CustomInfoWindowWithoutXML
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

fun getRotatedBitmapDrawableLive(context: Context, angle: Float): BitmapDrawable {
    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.car)
    val matrix = Matrix().apply { postRotate(angle) }
    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    return BitmapDrawable(context.resources, rotated)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLiveMapScreen(navController: NavController, role: String){
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val mapInitialized = remember { mutableStateOf(false) }
    val vehicleMarkers = remember { mutableStateMapOf<String, Marker>() }
    var isSidebarVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val liveVehicles = remember { mutableStateListOf<LiveVehicle>() }

    val customInfoWindow = remember {
        CustomInfoWindowWithoutXML(context, mapView, navController)
    }

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
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
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
                            infoWindow = customInfoWindow

                            setOnMarkerClickListener { m, _ ->
                                customInfoWindow.userTappedToOpen = true
                                if (m.isInfoWindowShown) m.closeInfoWindow()
                                else m.showInfoWindow()
                                true
                            }
                        }
                        vehicleMarkers[vehicle.deviceId] = newMarker
                        mapView.overlays.add(newMarker)
                    } else {
                        marker.position = geoPoint
                        marker.relatedObject = vehicle
                        marker.snippet = """
                            Vehicle: ${vehicle.deviceId}
                            Status: ${vehicle.liveStatus}
                            Speed: ${vehicle.speed} km/h
                            Time: ${vehicle.timestamp}
                            Addr: $address
                        """.trimIndent()

                        if (vehicle.liveStatus.equals("RUNNING", true)) {
                            val angle = previous?.let { calculateBearing(it, geoPoint) } ?: 0f
                            marker.icon = getRotatedBitmapDrawableLive(context, angle)
                        } else {
                            marker.icon = BitmapDrawable(
                                context.resources, BitmapFactory.decodeResource(context.resources,
                                    when (vehicle.liveStatus.uppercase()) {
                                        "IDLE", "PARKED" -> R.drawable.caryellow
                                        "OFFLINE" -> R.drawable.carred
                                        else -> R.drawable.car
                                    }
                                )
                            )
                        }

                        customInfoWindow.refresh(marker)
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
        mapInitialized.value = true

        while (true) {
            fetchLiveVehicles()
            delay(2000)
        }
    }

    ScaffoldWithDrawer(
        navController = navController,
        screenTitle = "Live Map",
        role = role,
        disableGestures = true
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clickable {
                    vehicleMarkers.values.forEach { it.closeInfoWindow() }
                }
        ) {
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
                            VehicleRow(v, vehicleMarkers[v.deviceId], mapView, customInfoWindow) {
                                isSidebarVisible = false
                            }
                        }
                    }
                }
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
    marker: Marker?,
    mapView: MapView,
    infoWindow: CustomInfoWindowWithoutXML,
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
        Text(vehicle.timestamp.takeLast(8), Modifier.weight(1f))
        IconButton(onClick = {
            marker?.let {
                mapView.controller.setZoom(18.0)
                mapView.controller.setCenter(it.position)
                infoWindow.userTappedToOpen = true // ✅ Only this line
                it.showInfoWindow()
                onLocate()
            }
        }, Modifier.weight(0.5f)) {
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
