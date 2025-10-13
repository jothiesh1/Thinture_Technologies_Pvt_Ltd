@file:Suppress("DEPRECATION")

package com.example.gpsapp.ui.screens.superadmin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
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
import com.google.gson.annotations.SerializedName




/** Accept string coords, parse safely and return address or "Unknown" */
fun getAddressFromLocation(context: Context, latStr: String?, lonStr: String?): String {
    val lat = latStr?.extractFirstDouble()
    val lon = lonStr?.extractFirstDouble()
    if (lat == null || lon == null) return "Unknown"

    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()?.getAddressLine(0) ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}

// ----------------------- Coordinate parsing helpers -----------------------
/** Extracts the first decimal number from a string (handles "12.345", "12.345N", "12,345"). */
fun String.extractFirstDouble(): Double? {
    val s = this.trim().replace(",", ".")                       // normalize comma -> dot
    val match = Regex("""-?\d+(\.\d+)?""").find(s)              // match first float-like token
    return match?.value?.toDoubleOrNull()
}

/** Safely convert a pair of string coords into a GeoPoint, or null if invalid. */
fun LiveVehicle.toGeoPointOrNull(): GeoPoint? {
    val lat = this.latitude?.extractFirstDouble()
    val lon = this.longitude?.extractFirstDouble()
    return if (lat != null && lon != null) GeoPoint(lat, lon) else null
}

fun String?.formatCoordinateForDisplay(): String {
    val d = this?.extractFirstDouble()
    return d?.let { String.format(Locale.getDefault(), "%.6f", it) } ?: (this ?: "--")
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
fun LiveMapScreen(navController: NavController) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val density = LocalDensity.current

    val mapView = remember { MapView(context) }
    var selectedVehicle by remember { mutableStateOf<LiveVehicle?>(null) }
    val mapInitialized = remember { mutableStateOf(false) }
    val vehicleMarkers = remember { mutableStateMapOf<String, Marker>() }
    var isSidebarVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val liveVehicles = remember { mutableStateListOf<LiveVehicle>() }

    // Bottom sheet state
    var isBottomSheetVisible by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }

    // Calculate bottom sheet height (60% of screen)
    val bottomSheetHeight = screenHeight * 0.6f
    val minBottomSheetHeight = 100.dp // Minimum peek height

    // Animate bottom sheet position - Fixed positioning logic
    val bottomSheetOffset by animateFloatAsState(
        targetValue = if (isBottomSheetVisible) {
            with(density) {
                // When visible: position at bottom of screen minus drag offset
                // 0f means fully visible at bottom, positive values move it down (hidden)
                dragOffset.coerceIn(0f, (bottomSheetHeight - minBottomSheetHeight).toPx())
            }
        } else {
            with(density) {
                // When hidden: move it completely off-screen (positive Y moves down)
                (bottomSheetHeight - minBottomSheetHeight).toPx()
            }
        },
        animationSpec = tween(durationMillis = 300),
        label = "BottomSheet"
    )

    // Replace your fetchLiveVehicles function with this improved version:

    suspend fun fetchLiveVehicles() {
        try {
            val response = RetrofitClient.apiService.getLiveVehicles()

            // Better error handling for response
            if (!response.isSuccessful) {
                val errorMsg = "API error: ${response.code()} - ${response.message()}"
                android.util.Log.e("LiveMapScreen", errorMsg)
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                return
            }

            val vehicleList = response.body()

            // Handle null or empty response body
            if (vehicleList == null) {
                android.util.Log.w("LiveMapScreen", "Response body is null")
                Toast.makeText(context, "No data received from server", Toast.LENGTH_SHORT).show()
                return
            }

            if (vehicleList.isEmpty()) {
                android.util.Log.w("LiveMapScreen", "Response body is empty list")
                // Clear existing vehicles if list is empty
                liveVehicles.clear()
                return
            }

            // Log successful fetch
            android.util.Log.d("LiveMapScreen", "Fetched ${vehicleList.size} vehicles")

            liveVehicles.clear()
            liveVehicles.addAll(vehicleList)

            vehicleList.forEach { vehicle ->
                // Skip vehicles without deviceId (they can't be tracked)
                if (vehicle.deviceId.isNullOrBlank()) {
                    android.util.Log.w("LiveMapScreen", "Skipping vehicle with null/blank deviceId: $vehicle")
                    return@forEach
                }

                // Parse and validate coordinates with better error handling
                val geoPoint = vehicle.toGeoPointOrNull()

                if (geoPoint == null) {
                    // Log but don't show toast for every invalid coordinate
                    android.util.Log.w(
                        "LiveMapScreen",
                        "Invalid coords for device=${vehicle.deviceId}, " +
                                "lat='${vehicle.latitude}', lon='${vehicle.longitude}'"
                    )

                    // Remove marker if it exists (vehicle went offline with no coords)
                    vehicleMarkers[vehicle.deviceId]?.let { marker ->
                        mapView.overlays.remove(marker)
                        vehicleMarkers.remove(vehicle.deviceId)
                        android.util.Log.d("LiveMapScreen", "Removed marker for ${vehicle.deviceId} (no valid coords)")
                    }
                    return@forEach
                }

                // Get address safely
                val address = try {
                    getAddressFromLocation(context, vehicle.latitude, vehicle.longitude)
                } catch (e: Exception) {
                    android.util.Log.e("LiveMapScreen", "Geocoding error for ${vehicle.deviceId}", e)
                    "Address unavailable"
                }

                val marker = vehicleMarkers[vehicle.deviceId]
                val previous = marker?.position

                if (marker == null) {
                    // Create new marker
                    val newMarker = Marker(mapView).apply {
                        position = geoPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        setInfoWindowAnchor(Marker.ANCHOR_CENTER, -2.5f)
                        isFlat = true

                        // Set icon based on status
                        icon = when {
                            vehicle.liveStatus.equals("RUNNING", true) -> {
                                getRotatedBitmapDrawableLive(context, 0f)
                            }
                            vehicle.liveStatus.equals("IDLE", true) ||
                                    vehicle.liveStatus.equals("PARKED", true) -> {
                                BitmapDrawable(
                                    context.resources,
                                    BitmapFactory.decodeResource(context.resources, R.drawable.caryellow)
                                )
                            }
                            vehicle.liveStatus.equals("OFFLINE", true) -> {
                                BitmapDrawable(
                                    context.resources,
                                    BitmapFactory.decodeResource(context.resources, R.drawable.carred)
                                )
                            }
                            else -> {
                                BitmapDrawable(
                                    context.resources,
                                    BitmapFactory.decodeResource(context.resources, R.drawable.car)
                                )
                            }
                        }

                        title = vehicle.deviceId
                        snippet = """
                        Vehicle: ${vehicle.deviceId}
                        Status: ${vehicle.liveStatus ?: "Unknown"}
                        Speed: ${vehicle.speed ?: "0"} km/h
                        Time: ${vehicle.timestamp ?: "No data"}
                        Addr: $address
                    """.trimIndent()
                        relatedObject = vehicle

                        setOnMarkerClickListener { m, _ ->
                            (m.relatedObject as? LiveVehicle)?.let { clickedVehicle ->
                                selectedVehicle = clickedVehicle
                                isBottomSheetVisible = true
                                dragOffset = 0f

                                mapView.controller.setZoom(16.0)

                                val screenHeight = context.resources.displayMetrics.heightPixels
                                val topHalfCenter = screenHeight * 0.7f
                                val mapCenter = screenHeight * 0.5f
                                val offsetY = (topHalfCenter - mapCenter).toInt()

                                val projection = mapView.projection
                                val centerPoint = android.graphics.Point()
                                projection.toPixels(geoPoint, centerPoint)
                                centerPoint.y += offsetY
                                val offsetGeoPoint = projection.fromPixels(
                                    centerPoint.x,
                                    centerPoint.y
                                ) as GeoPoint

                                mapView.controller.setCenter(offsetGeoPoint)
                            }
                            true
                        }
                    }

                    vehicleMarkers[vehicle.deviceId] = newMarker
                    mapView.overlays.add(newMarker)
                    android.util.Log.d("LiveMapScreen", "Created marker for ${vehicle.deviceId}")

                } else {
                    // Update existing marker
                    val positionChanged = previous != null &&
                            (previous.latitude != geoPoint.latitude ||
                                    previous.longitude != geoPoint.longitude)

                    if (positionChanged && previous != null) {
                        val bearing = calculateBearing(previous, geoPoint)

                        if (vehicle.liveStatus.equals("RUNNING", true)) {
                            marker.icon = getRotatedBitmapDrawableLive(context, bearing)
                        }
                    }

                    marker.position = geoPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

                    // Update icon if status changed
                    val oldStatus = (marker.relatedObject as? LiveVehicle)?.liveStatus
                    val statusChanged = oldStatus != vehicle.liveStatus

                    if (statusChanged && !vehicle.liveStatus.equals("RUNNING", true)) {
                        marker.icon = when {
                            vehicle.liveStatus.equals("IDLE", true) ||
                                    vehicle.liveStatus.equals("PARKED", true) -> {
                                BitmapDrawable(
                                    context.resources,
                                    BitmapFactory.decodeResource(context.resources, R.drawable.caryellow)
                                )
                            }
                            vehicle.liveStatus.equals("OFFLINE", true) -> {
                                BitmapDrawable(
                                    context.resources,
                                    BitmapFactory.decodeResource(context.resources, R.drawable.carred)
                                )
                            }
                            else -> {
                                BitmapDrawable(
                                    context.resources,
                                    BitmapFactory.decodeResource(context.resources, R.drawable.car)
                                )
                            }
                        }
                    }

                    marker.relatedObject = vehicle
                    marker.snippet = """
                    Vehicle: ${vehicle.deviceId}
                    Status: ${vehicle.liveStatus ?: "Unknown"}
                    Speed: ${vehicle.speed ?: "0"} km/h
                    Time: ${vehicle.timestamp ?: "No data"}
                    Addr: $address
                """.trimIndent()

                    // Update selected vehicle data for real-time updates
                    if (selectedVehicle?.deviceId == vehicle.deviceId) {
                        selectedVehicle = vehicle
                    }
                }
            }

            mapView.invalidate()

        } catch (e: Exception) {
            // Comprehensive error logging
            val errorMsg = when {
                e.message.isNullOrBlank() -> "Network error: ${e.javaClass.simpleName}"
                else -> "Fetch failed: ${e.message}"
            }

            android.util.Log.e("LiveMapScreen", errorMsg, e)
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }


// Also improve the coordinate extraction function:

    /** Extracts the first decimal number from a string (handles "12.345", "12.345N", "12,345", empty strings). */
    fun String?.extractFirstDouble(): Double? {
        // Handle null or blank strings
        if (this.isNullOrBlank()) return null

        val s = this.trim()

        // Handle empty string after trim
        if (s.isEmpty()) return null

        // Normalize comma to dot
        val normalized = s.replace(",", ".")

        // Match first float-like token
        val match = Regex("""-?\d+(\.\d+)?""").find(normalized)

        return match?.value?.toDoubleOrNull()
    }


// Improve the toGeoPointOrNull extension:

    /** Safely convert a pair of string coords into a GeoPoint, or null if invalid. */
    fun LiveVehicle.toGeoPointOrNull(): GeoPoint? {
        // Extract coordinates
        val lat = this.latitude?.extractFirstDouble()
        val lon = this.longitude?.extractFirstDouble()

        // Validate coordinates are within valid ranges
        return if (lat != null && lon != null &&
            lat >= -90.0 && lat <= 90.0 &&
            lon >= -180.0 && lon <= 180.0) {
            GeoPoint(lat, lon)
        } else {
            null
        }
    }


// Improve the address function:

    /** Accept string coords, parse safely and return address or "Unknown" */
    fun getAddressFromLocation(context: Context, latStr: String?, lonStr: String?): String {
        // Handle null or blank strings
        if (latStr.isNullOrBlank() || lonStr.isNullOrBlank()) {
            return "No location data"
        }

        val lat = latStr.extractFirstDouble()
        val lon = lonStr.extractFirstDouble()

        if (lat == null || lon == null) {
            return "Invalid coordinates"
        }

        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            addresses?.firstOrNull()?.getAddressLine(0) ?: "Address not available"
        } catch (e: Exception) {
            android.util.Log.e("LiveMapScreen", "Geocoding error for $lat,$lon", e)
            "Address unavailable"
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
                // Only hide bottom sheet if clicking on empty map area
                if (isBottomSheetVisible) {
                    // You can add logic here to check if click is on map vs UI elements
                }
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
        ) {
            // Map View - occupies full screen but gets clipped by bottom sheet
            if (mapInitialized.value) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Floating Action Button - Fixed positioning
            FloatingActionButton(
                onClick = {
                    mapView.controller.setZoom(6.0)
                    mapView.controller.setCenter(GeoPoint(20.5937, 78.9629))
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = if (isBottomSheetVisible) {
                                with(density) { -(bottomSheetHeight - minBottomSheetHeight + 16.dp).roundToPx() }
                            } else 0
                        )
                    },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = "Recenter")
            }

            // Menu Button
            IconButton(
                onClick = { isSidebarVisible = !isSidebarVisible },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }

            // Sidebar (unchanged)
            AnimatedVisibility(
                visible = isSidebarVisible,
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxHeight()
                    .width(300.dp)
                    .background(Color.White)
                    .zIndex(2f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text("✖", Modifier.clickable { isSidebarVisible = false })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val filtered = liveVehicles.filter {
                        it.deviceId?.contains(searchQuery.text, ignoreCase = true) == true
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
                                context = context,
                                onLocate = { isSidebarVisible = false }
                            )
                        }
                    }
                }
            }

            // Bottom Sheet - shows vehicle details
            selectedVehicle?.let { vehicle ->
                VehicleBottomSheet(
                    vehicle = vehicle,
                    navController = navController,
                    mapView = mapView,
                    context = context,
                    isVisible = isBottomSheetVisible,
                    offset = bottomSheetOffset,
                    onDismiss = {
                        isBottomSheetVisible = false
                        selectedVehicle = null
                        dragOffset = 0f
                    },
                    onDragChange = { delta ->
                        dragOffset = (dragOffset + delta).coerceAtLeast(0f)
                    },
                    bottomSheetHeight = bottomSheetHeight,
                    minHeight = minBottomSheetHeight
                )
            }
        }
    }
}

@Composable
fun VehicleBottomSheet(
    vehicle: LiveVehicle,
    navController: NavController,
    mapView: MapView,
    context: Context,
    isVisible: Boolean,
    offset: Float,
    onDismiss: () -> Unit,
    onDragChange: (Float) -> Unit,
    bottomSheetHeight: androidx.compose.ui.unit.Dp,
    minHeight: androidx.compose.ui.unit.Dp
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(bottomSheetHeight)
            .offset {
                // Position at bottom of screen and move up based on visibility
                IntOffset(
                    x = 0,
                    y = with(density) {
                        (screenHeight - bottomSheetHeight + offset.toDp()).roundToPx()
                    }
                )
            }
            .zIndex(1f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = Color.White,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            // Positive dragAmount.y means dragging down (should hide sheet)
                            // Negative dragAmount.y means dragging up (should show more sheet)
                            onDragChange(dragAmount.y)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                )

                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // Vehicle Header
                VehicleHeaderSection(vehicle)

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Stats
                VehicleStatsSection(vehicle)

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                Spacer(modifier = Modifier.height(16.dp))

                // Location Information
                Text(
                    text = "Location Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LocationInfoSection(vehicle)

                Spacer(modifier = Modifier.height(16.dp))

                // Technical Information
                Text(
                    text = "Technical Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )

                Spacer(modifier = Modifier.height(12.dp))

                TechnicalInfoSection(vehicle)

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                VehicleActionButtons(
                    vehicle = vehicle,
                    navController = navController,
                    mapView = mapView,
                    context = context,
                    onDismiss = onDismiss
                )

                // Extra padding to ensure buttons are not cropped
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun VehicleHeaderSection(vehicle: LiveVehicle) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vehicle Icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    Color(0xFF1976D2).copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = "Vehicle",
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = vehicle.deviceId ?: "Unknown Device",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Status Indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            if (vehicle.isOnline == true) Color(0xFF4CAF50) else Color(0xFFFF5722),
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (vehicle.isOnline == true) "Live" else "Last Received: ${vehicle.timestamp ?: "--"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
fun VehicleStatsSection(vehicle: LiveVehicle) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickStatCard(
            icon = Icons.Default.Speed,
            label = "Speed",
            value = "${vehicle.speed ?: 0}",
            unit = "km/h",
            color = run {
                val speedValue = vehicle.speed?.toString()?.toFloatOrNull() ?: 0f
                when {
                    speedValue > 60f -> Color(0xFFFF5722)
                    speedValue > 20f -> Color(0xFFFFC107)
                    else -> Color(0xFF4CAF50)
                }
            },
            modifier = Modifier.weight(1f)
        )

        QuickStatCard(
            icon = Icons.Default.CheckBox,
            label = "Status",
            value = vehicle.liveStatus ?: "--",
            unit = "",
            color = getStatusColor(vehicle.liveStatus ?: ""),
            modifier = Modifier.weight(1f)
        )

        QuickStatCard(
            icon = Icons.Default.Power,
            label = "Ignition",
            value = if (vehicle.ignition.equals("IGON", true)) "ON" else "OFF",
            unit = "",
            color = if (vehicle.ignition.equals("IGON", true)) Color(0xFF4CAF50) else Color(0xFFFF5722),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(
                Color(0xFFFAFAFA),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF757575),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = color,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E9E9E),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun VehicleActionButtons(
    vehicle: LiveVehicle,
    navController: NavController,
    mapView: MapView,
    context: Context,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Track Button
        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable {
                    val geoPoint = vehicle.toGeoPointOrNull()
                    if (geoPoint != null) {
                        mapView.controller.setZoom(18.0)
                        mapView.controller.setCenter(geoPoint)
                        Toast.makeText(context, "Tracking ${vehicle.deviceId}", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Invalid location for ${vehicle.deviceId}", Toast.LENGTH_SHORT).show()
                    }
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "Track",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Track Vehicle",
                    color = Color(0xFF1976D2),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Playback Button
        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(Color(0xFF1976D2), Color(0xFF42A5F5))
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable {
                    onDismiss()
                    navController.navigate("playback_map/{deviceId}")
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Playback",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "View Playback",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun LocationInfoSection(vehicle: LiveVehicle) {
    val context = LocalContext.current

    Column {
        ModernInfoRow(
            Icons.Default.LocationOn,
            "Latitude",
            vehicle.latitude.formatCoordinateForDisplay()
        )
        ModernInfoRow(
            Icons.Default.LocationOn,
            "Longitude",
            vehicle.longitude.formatCoordinateForDisplay()
        )

        // Add address if available
        val address = remember(vehicle.latitude, vehicle.longitude) {
            getAddressFromLocation(context, vehicle.latitude, vehicle.longitude)
        }

        if (address != "Unknown") {
            ModernInfoRow(
                Icons.Default.Place,
                "Address",
                address
            )
        }
    }
}

@Composable
fun TechnicalInfoSection(vehicle: LiveVehicle) {
    Column {
        ModernInfoRow(
            Icons.Default.Power,
            "Ignition",
            if (vehicle.ignition.equals("IGON", true)) "ON" else "OFF",
            valueColor = if (vehicle.ignition.equals("IGON", true)) Color(0xFF4CAF50) else Color(0xFFFF5722)
        )
        ModernInfoRow(
            Icons.Default.AccessTime,
            "Last Update",
            vehicle.timestamp ?: "--"
        )
        ModernInfoRow(
            Icons.Default.SignalCellularAlt,
            "GSM Signal",
            "Perfect (46)",
            valueColor = Color(0xFF4CAF50)
        )
    }
}

@Composable
fun ModernInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color(0xFF424242)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(
                    Color(0xFFF8F9FA),
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = Color(0xFF6C757D),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6C757D),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = valueColor,
                fontWeight = FontWeight.SemiBold
            )
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
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
    context: Context,
    onLocate: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(vehicle.liveStatus ?: "", Modifier.weight(1f), color = getStatusColor(vehicle.liveStatus))
        Text(vehicle.deviceId ?: "", Modifier.weight(2f))
        Text(vehicle.timestamp?.takeLast(8) ?: "--:--:--", Modifier.weight(1f))
        IconButton(
            onClick = {
                val gp = vehicle.toGeoPointOrNull()
                if (gp != null) {
                    mapView.controller.setZoom(18.0)
                    mapView.controller.setCenter(gp)
                    onLocate()
                } else {
                    Toast.makeText(context, "Invalid coordinates for ${vehicle.deviceId}", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.weight(0.5f)
        ) {
            Icon(Icons.Filled.Place, contentDescription = "Locate")
        }
    }
}

private fun getStatusColor(status: String?): Color = when (status?.uppercase(Locale.getDefault())) {
    "RUNNING", "MOVING" -> Color(0xFF4CAF50)
    "IDLE" -> Color(0xFFFFA000)
    "PARKED" -> Color(0xFF1976D2)
    "OFFLINE" -> Color.Gray
    else -> Color.Black
}