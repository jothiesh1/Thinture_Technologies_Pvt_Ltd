package com.example.gpsapp.ui.screens.admin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gpsapp.R
import com.example.gpsapp.ui.components.DateTimePicker
import com.example.gpsapp.ui.components.ScaffoldWithDrawer
import com.example.gpsapp.ui.screens.PlaybackMapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import android.util.Log
import org.json.JSONArray
import java.net.URL

data class EnhancedGeoPoint(
    val geoPoint: GeoPoint,
    val speed: Double,
    val timestamp: String,
    val rawIndex: Int
)

data class VehicleInfo(
    val speed: String,
    val timestamp: String,
    val address: String,
    val coordinates: String,
    val course: String
)

data class VehicleDevice(
    val deviceId: String,
    val vehicleName: String? = null
)

fun getRotatedBitmapDrawable(context: Context, angle: Float): BitmapDrawable {
    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.car)
    val matrix = Matrix().apply { postRotate(angle) }
    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    return BitmapDrawable(context.resources, rotated)
}

fun getSpeedColor(speed: Double): Int {
    return when {
        speed < 10 -> android.graphics.Color.RED
        speed < 30 -> android.graphics.Color.YELLOW
        speed < 60 -> android.graphics.Color.GREEN
        speed < 80 -> android.graphics.Color.CYAN
        else -> android.graphics.Color.MAGENTA
    }
}

suspend fun fetchDevices(): List<VehicleDevice> {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("http://43.205.58.131:8183/api/mobile/vehicles/live")
            val connection = url.openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            Log.d("PlaybackMap", "API Response: $response")

            val jsonArray = JSONArray(response)
            Log.d("PlaybackMap", "Number of devices in response: ${jsonArray.length()}")

            val devices = mutableListOf<VehicleDevice>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                val deviceId = jsonObject.optString("deviceId", "")
                    .ifEmpty { jsonObject.optString("device_id", "") }
                    .ifEmpty { jsonObject.optString("id", "") }

                val vehicleName = jsonObject.optString("vehicleName", "")
                    .ifEmpty { jsonObject.optString("vehicle_name", "") }
                    .ifEmpty { jsonObject.optString("name", "") }

                Log.d("PlaybackMap", "Device $i - ID: $deviceId, Name: $vehicleName")

                if (deviceId.isNotEmpty() && deviceId != "null") {
                    val finalVehicleName = if (vehicleName.isNotEmpty() && vehicleName != "null") {
                        vehicleName
                    } else {
                        null
                    }
                    devices.add(VehicleDevice(deviceId, finalVehicleName))
                }
            }

            Log.d("PlaybackMap", "Total valid devices: ${devices.size}")
            devices.sortedBy { it.vehicleName ?: it.deviceId }
        } catch (e: Exception) {
            Log.e("PlaybackMap", "Error fetching devices: ${e.message}", e)
            emptyList()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPlaybackMapScreen(navController: NavController, deviceId: String) {
    ScaffoldWithDrawer(
        navController = navController,
        screenTitle = "Playback Map",
        role = "admin"
    ) { innerPadding ->
        val context = LocalContext.current
        val viewModel: PlaybackMapViewModel = viewModel()
        val allRawPoints by viewModel.playbackPoints.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val errorMessage by viewModel.errorMessage.collectAsState()
        val coroutineScope = rememberCoroutineScope()

        var enhancedPoints by remember { mutableStateOf(listOf<EnhancedGeoPoint>()) }
        var map by remember { mutableStateOf<MapView?>(null) }
        var markerRef by remember { mutableStateOf<Marker?>(null) }
        var mapInitialized by remember { mutableStateOf(false) }
        var speedColoredPolylines by remember { mutableStateOf(listOf<Polyline>()) }

        var loadingProgress by remember { mutableStateOf(0f) }
        var isProcessingData by remember { mutableStateOf(false) }
        var loadingMessage by remember { mutableStateOf("Preparing data...") }

        var playbackIndex by remember { mutableStateOf(0) }
        var isPlaying by remember { mutableStateOf(false) }
        var playbackSpeed by remember { mutableStateOf(1.0f) }
        var isSeeking by remember { mutableStateOf(false) }
        var shouldLoadMap by remember { mutableStateOf(false) }

        var deviceIdState by remember { mutableStateOf("") }
        var fromDateTime by remember { mutableStateOf("") }
        var toDateTime by remember { mutableStateOf("") }
        var sidebarVisible by remember { mutableStateOf(false) }
        var show24HourDialog by remember { mutableStateOf(false) }
        var selectedHours by remember { mutableStateOf(0L) }

        var deviceList by remember { mutableStateOf(listOf<VehicleDevice>()) }
        var isLoadingDevices by remember { mutableStateOf(false) }
        var deviceDropdownExpanded by remember { mutableStateOf(false) }

        var isControlsCollapsed by remember { mutableStateOf(false) }
        var showLegend by remember { mutableStateOf(false) }

        var showVehicleInfo by remember { mutableStateOf(false) }
        var currentVehicleInfo by remember { mutableStateOf(VehicleInfo("", "", "", "", "")) }
        var isLoadingAddress by remember { mutableStateOf(false) }

        LaunchedEffect(sidebarVisible) {
            if (sidebarVisible && deviceList.isEmpty()) {
                isLoadingDevices = true
                deviceList = fetchDevices()
                isLoadingDevices = false
            }
        }

        LaunchedEffect(isPlaying) {
            if (isPlaying && enhancedPoints.isNotEmpty() && playbackIndex in enhancedPoints.indices) {
                val currentPoint = enhancedPoints[playbackIndex]
                map?.controller?.apply {
                    setZoom(18.0)
                    animateTo(currentPoint.geoPoint)
                }
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

        fun String?.toDecimalDegrees(): Double? {
            if (this.isNullOrBlank()) return null
            var s = this.trim()
            s = s.replace("\\s+".toRegex(), "")
            s = s.replace(",", ".")

            var sign = 1.0
            if (s.endsWith("N", true) || s.endsWith("E", true)) {
                s = s.substring(0, s.length - 1)
            } else if (s.endsWith("S", true) || s.endsWith("W", true)) {
                s = s.substring(0, s.length - 1)
                sign = -1.0
            } else if (s.startsWith("N", true) || s.startsWith("E", true)) {
                s = s.substring(1)
            } else if (s.startsWith("S", true) || s.startsWith("W", true)) {
                s = s.substring(1)
                sign = -1.0
            }

            val dmsRegex = Regex("""^(\d+)[°\s]+(\d+)[\'\s]+([\d.]+)""")
            val m = dmsRegex.find(s)
            if (m != null) {
                val d = m.groupValues[1].toDoubleOrNull() ?: return null
                val mnt = m.groupValues[2].toDoubleOrNull() ?: 0.0
                val sec = m.groupValues[3].toDoubleOrNull() ?: 0.0
                return sign * (d + mnt / 60.0 + sec / 3600.0)
            }

            val cleaned = s.replace("°", "").replace(Regex("[^0-9+\\-\\.eE]"), "")
            cleaned.toDoubleOrNull()?.let { return it * sign }

            val numRegex = Regex("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?")
            val numMatch = numRegex.find(s)
            return numMatch?.value?.toDoubleOrNull()?.let { it * sign }
        }

        suspend fun getAddressFromCoordinates(lat: Double, lon: Double): String {
            return withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    addresses?.firstOrNull()?.let { address ->
                        buildString {
                            address.featureName?.let { append("$it, ") }
                            address.thoroughfare?.let { append("$it, ") }
                            address.locality?.let { append("$it, ") }
                            address.adminArea?.let { append(it) }
                        }.removeSuffix(", ")
                    } ?: "Address not available"
                } catch (e: Exception) {
                    Log.e("PlaybackMap", "Geocoding error", e)
                    "Address not available"
                }
            }
        }

        suspend fun updateVehicleInfo(index: Int) {
            if (index in enhancedPoints.indices) {
                val point = enhancedPoints[index]
                val previous = enhancedPoints.getOrNull(index - 1)
                val course = if (previous != null) {
                    calculateBearing(previous.geoPoint, point.geoPoint)
                } else 0f

                isLoadingAddress = true
                val address = getAddressFromCoordinates(
                    point.geoPoint.latitude,
                    point.geoPoint.longitude
                )
                isLoadingAddress = false

                currentVehicleInfo = VehicleInfo(
                    speed = "${point.speed.toInt()} km/h",
                    timestamp = point.timestamp,
                    address = address,
                    coordinates = "%.6f, %.6f".format(point.geoPoint.latitude, point.geoPoint.longitude),
                    course = "${course.toInt()}°"
                )
            }
        }

        LaunchedEffect(allRawPoints) {
            if (allRawPoints.isNotEmpty()) {
                isProcessingData = true
                loadingMessage = "Processing GPS data..."
                loadingProgress = 0f

                val validPoints = mutableListOf<EnhancedGeoPoint>()
                val totalPoints = allRawPoints.size

                allRawPoints.forEachIndexed { index, point ->
                    val lat = point.latitude.toDecimalDegrees()
                    val lon = point.longitude.toDecimalDegrees()
                    val speed = point.speed
                    if (lat != null && lon != null) {
                        validPoints.add(
                            EnhancedGeoPoint(
                                geoPoint = GeoPoint(lat, lon),
                                speed = speed,
                                timestamp = point.timestamp,
                                rawIndex = index
                            )
                        )
                    }

                    loadingProgress = (index + 1).toFloat() / totalPoints
                    if (index % 50 == 0) {
                        delay(10)
                    }
                }

                if (validPoints.isNotEmpty()) {
                    enhancedPoints = validPoints
                    playbackIndex = 0
                    shouldLoadMap = true
                    loadingMessage = "Rendering map..."
                } else {
                    Toast.makeText(context, "No valid GPS points found", Toast.LENGTH_SHORT).show()
                }

                delay(500)
                isProcessingData = false
            }
        }

        LaunchedEffect(isPlaying, playbackIndex, playbackSpeed, enhancedPoints) {
            if (isPlaying && playbackIndex < enhancedPoints.lastIndex && !isSeeking) {
                val delayTime = (200L / playbackSpeed).toLong().coerceAtLeast(50L)
                delay(delayTime)
                playbackIndex++
            } else if (isPlaying && playbackIndex >= enhancedPoints.lastIndex) {
                isPlaying = false
                Toast.makeText(context, "Playback completed", Toast.LENGTH_SHORT).show()
            }
        }

        LaunchedEffect(playbackIndex, enhancedPoints) {
            if (enhancedPoints.isNotEmpty() && playbackIndex in enhancedPoints.indices && mapInitialized) {
                val current = enhancedPoints[playbackIndex]
                val previous = enhancedPoints.getOrNull(playbackIndex - 1)

                markerRef?.let { marker ->
                    if (marker.position.latitude != current.geoPoint.latitude ||
                        marker.position.longitude != current.geoPoint.longitude) {

                        marker.position = current.geoPoint

                        if (previous != null) {
                            val course = calculateBearing(previous.geoPoint, current.geoPoint)
                            marker.icon = getRotatedBitmapDrawable(context, course)
                        }

                        map?.controller?.animateTo(current.geoPoint)
                        map?.invalidate()
                    }
                }

                if (showVehicleInfo) {
                    updateVehicleInfo(playbackIndex)
                }
            }
        }

        fun createSpeedColoredPolylines(): List<Polyline> {
            val polylines = mutableListOf<Polyline>()
            if (enhancedPoints.size < 2) return polylines

            for (i in 0 until enhancedPoints.size - 1) {
                val current = enhancedPoints[i]
                val next = enhancedPoints[i + 1]

                val polyline = Polyline().apply {
                    setPoints(listOf(current.geoPoint, next.geoPoint))
                    color = getSpeedColor(current.speed)
                    width = 10f
                    outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                }
                polylines.add(polyline)
            }
            return polylines
        }

        if (show24HourDialog) {
            AlertDialog(
                onDismissRequest = { show24HourDialog = false },
                icon = {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                title = {
                    Text(
                        text = "Time Range Too Long",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "You've selected a time range of $selectedHours hours, which exceeds the 24-hour limit.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "\nFor longer reports, please use our website for better performance.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { show24HourDialog = false }) { Text("Got it") }
                },
                dismissButton = {
                    TextButton(onClick = { show24HourDialog = false }) { Text("Cancel") }
                }
            )
        }

        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            AndroidView(
                factory = { ctx ->
                    Configuration.getInstance().apply {
                        userAgentValue = ctx.packageName
                        osmdroidBasePath = File(ctx.cacheDir, "osmdroid")
                        osmdroidTileCache = File(osmdroidBasePath, "tiles")
                        load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                    }
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        maxZoomLevel = 19.0
                        controller.setZoom(15.0)
                        controller.setCenter(GeoPoint(12.9716, 77.5946))
                        map = this
                    }
                },
                update = { mv ->
                    map = mv
                    if (shouldLoadMap && !mapInitialized && enhancedPoints.isNotEmpty()) {
                        val start = enhancedPoints[0].geoPoint

                        speedColoredPolylines = createSpeedColoredPolylines()

                        val marker = Marker(mv).apply {
                            position = start
                            icon = getRotatedBitmapDrawable(context, 0f)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            isFlat = true
                            setOnMarkerClickListener { _, _ ->
                                showVehicleInfo = true
                                coroutineScope.launch {
                                    updateVehicleInfo(playbackIndex)
                                }
                                true
                            }
                        }

                        mv.overlays.clear()
                        speedColoredPolylines.forEach { polyline ->
                            mv.overlays.add(polyline)
                        }
                        mv.overlays.add(marker)

                        if (enhancedPoints.size > 1) {
                            val boundingBox = org.osmdroid.util.BoundingBox.fromGeoPointsSafe(
                                enhancedPoints.map { it.geoPoint }
                            )
                            mv.zoomToBoundingBox(boundingBox, true, 100)
                        }

                        mv.invalidate()
                        markerRef = marker
                        mapInitialized = true
                        shouldLoadMap = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isProcessingData) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 16.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = loadingMessage,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { loadingProgress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(loadingProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (isLoading && !isProcessingData) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp),
                    strokeWidth = 5.dp
                )
            }

            AnimatedVisibility(
                visible = showVehicleInfo,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .width(340.dp)
                        .shadow(16.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Vehicle Details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { showVehicleInfo = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Speed: ${currentVehicleInfo.speed}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = currentVehicleInfo.timestamp,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Location",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isLoadingAddress) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Loading...",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                } else {
                                    Text(
                                        text = currentVehicleInfo.address,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Text(
                                        text = currentVehicleInfo.coordinates,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Bearing: ${currentVehicleInfo.course}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = sidebarVisible,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxHeight()
                    .width(320.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Playback Options",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { sidebarVisible = false }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Text(
                            text = "Select Vehicle",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        ExposedDropdownMenuBox(
                            expanded = deviceDropdownExpanded,
                            onExpandedChange = {
                                if (!isLoadingDevices) {
                                    deviceDropdownExpanded = !deviceDropdownExpanded
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = if (deviceIdState.isNotEmpty()) {
                                    deviceList.find { it.deviceId == deviceIdState }?.let {
                                        it.vehicleName ?: it.deviceId
                                    } ?: deviceIdState
                                } else {
                                    ""
                                },
                                onValueChange = { },
                                readOnly = true,
                                label = {
                                    Text(
                                        "Select Vehicle",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (isLoadingDevices) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                },
                                placeholder = {
                                    Text(
                                        if (isLoadingDevices) "Loading vehicles..." else "Tap to choose vehicle",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = deviceDropdownExpanded,
                                onDismissRequest = { deviceDropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(14.dp)
                                    )
                            ) {
                                if (deviceList.isEmpty() && !isLoadingDevices) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "No vehicles available",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = { },
                                        enabled = false
                                    )
                                } else {
                                    deviceList.forEach { device ->
                                        val isSelected = deviceIdState == device.deviceId
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        modifier = Modifier.weight(1f),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = if (isSelected)
                                                                MaterialTheme.colorScheme.primaryContainer
                                                            else
                                                                MaterialTheme.colorScheme.surfaceVariant,
                                                            modifier = Modifier.size(40.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = (device.vehicleName?.firstOrNull()
                                                                        ?: device.deviceId.firstOrNull()
                                                                        ?: "?").toString().uppercase(),
                                                                    style = MaterialTheme.typography.titleMedium,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isSelected)
                                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                                    else
                                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = device.vehicleName ?: device.deviceId,
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                                color = if (isSelected)
                                                                    MaterialTheme.colorScheme.primary
                                                                else
                                                                    MaterialTheme.colorScheme.onSurface
                                                            )
                                                            if (device.vehicleName != null) {
                                                                Text(
                                                                    text = "ID: ${device.deviceId}",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                                )
                                                            }
                                                        }
                                                    }
                                                    if (isSelected) {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = MaterialTheme.colorScheme.primaryContainer,
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.PlayArrow,
                                                                contentDescription = "Selected",
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.padding(6.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                            onClick = {
                                                deviceIdState = device.deviceId
                                                deviceDropdownExpanded = false
                                            },
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (isSelected)
                                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                                    else
                                                        Color.Transparent
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Time Range",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        DateTimePicker(label = "From", dateTime = fromDateTime) { fromDateTime = it }
                        DateTimePicker(label = "To", dateTime = toDateTime) { toDateTime = it }

                        Button(
                            onClick = {
                                if (deviceIdState.isBlank() || fromDateTime.isBlank() || toDateTime.isBlank()) {
                                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                try {
                                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                    val fromDate = sdf.parse(fromDateTime)
                                    val toDate = sdf.parse(toDateTime)

                                    if (fromDate == null || toDate == null) {
                                        Toast.makeText(context, "Invalid date format", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    val diffMillis = toDate.time - fromDate.time
                                    if (diffMillis < 0) {
                                        Toast.makeText(context, "'To' must be after 'From'", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis)
                                    if (diffHours > 24) {
                                        selectedHours = diffHours
                                        show24HourDialog = true
                                        return@Button
                                    }

                                    playbackIndex = 0
                                    isPlaying = false
                                    shouldLoadMap = true
                                    mapInitialized = false
                                    viewModel.fetchPlaybackData(deviceIdState, fromDateTime, toDateTime)
                                    sidebarVisible = false
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error parsing dates", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Load Playback",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { sidebarVisible = !sidebarVisible },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(56.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    modifier = Modifier.size(28.dp)
                )
            }

            FloatingActionButton(
                onClick = { showLegend = !showLegend },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(48.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Legend",
                    modifier = Modifier.size(24.dp)
                )
            }

            if (enhancedPoints.isNotEmpty()) {
                val buttonOffset by animateFloatAsState(
                    targetValue = if (isControlsCollapsed) 0f else 1f,
                    label = "buttonOffset"
                )

                FloatingActionButton(
                    onClick = { isControlsCollapsed = !isControlsCollapsed },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            end = 15.dp,
                            bottom = 16.dp + (280.dp.value * buttonOffset).dp
                        ),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(
                        if (isControlsCollapsed) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isControlsCollapsed) "Show Controls" else "Hide Controls",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (enhancedPoints.isNotEmpty()) {
                AnimatedVisibility(
                    visible = !isControlsCollapsed,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .alpha(0.95f),
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 12.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (playbackIndex in enhancedPoints.indices) {
                                val currentPoint = enhancedPoints[playbackIndex]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = currentPoint.timestamp.takeLast(8),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${playbackIndex + 1} / ${enhancedPoints.size}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Speed,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "${currentPoint.speed.toInt()}",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "km/h",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Slider(
                                    value = playbackIndex.toFloat().coerceIn(0f, enhancedPoints.lastIndex.toFloat()),
                                    onValueChange = {
                                        isSeeking = true
                                        playbackIndex = it.toInt().coerceIn(0, enhancedPoints.lastIndex)
                                    },
                                    onValueChangeFinished = {
                                        isSeeking = false
                                    },
                                    valueRange = 0f..enhancedPoints.lastIndex.toFloat(),
                                    steps = max(0, enhancedPoints.size - 2),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = SliderDefaults.colors(
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        thumbColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                if (enhancedPoints.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = enhancedPoints.firstOrNull()?.timestamp?.takeLast(8) ?: "",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = enhancedPoints.lastOrNull()?.timestamp?.takeLast(8) ?: "",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(0.5f to "0.5×", 1f to "1×", 2f to "2×").forEach { (speed, label) ->
                                    Button(
                                        onClick = { playbackSpeed = speed },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (playbackSpeed == speed)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (playbackSpeed == speed)
                                                Color.White
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { isPlaying = !isPlaying },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                            ) {
                                Icon(
                                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isPlaying) "Pause" else "Play",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showLegend,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it }),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 80.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.width(200.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Speed Legend",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { showLegend = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        val speedRanges = listOf(
                            "0-10" to android.graphics.Color.RED,
                            "10-30" to android.graphics.Color.YELLOW,
                            "30-60" to android.graphics.Color.GREEN,
                            "60-80" to android.graphics.Color.CYAN,
                            "80+" to android.graphics.Color.MAGENTA
                        )

                        speedRanges.forEach { (range, color) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(vertical = 3.dp)
                            ) {
                                Canvas(
                                    modifier = Modifier.size(28.dp, 6.dp)
                                ) {
                                    drawLine(
                                        color = Color(color),
                                        start = Offset(0f, size.height / 2),
                                        end = Offset(size.width, size.height / 2),
                                        strokeWidth = size.height,
                                        cap = StrokeCap.Round
                                    )
                                }
                                Text(
                                    text = "$range km/h",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}