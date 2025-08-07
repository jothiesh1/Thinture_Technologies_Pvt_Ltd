package com.example.gpsapp.ui.screens.admin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gpsapp.R
import com.example.gpsapp.ui.components.DateTimePicker
import com.example.gpsapp.ui.components.ScaffoldWithDrawer
import com.example.gpsapp.ui.screens.PlaybackMapViewModel
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

fun getRotatedBitmapDrawable(context: Context, angle: Float): BitmapDrawable {
    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.car)
    val matrix = Matrix().apply { postRotate(angle) }
    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    return BitmapDrawable(context.resources, rotated)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPlaybackMapScreen(navController: NavController, deviceId: String){
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

        var interpolatedPoints by remember { mutableStateOf(listOf<GeoPoint>()) }
        var map by remember { mutableStateOf<MapView?>(null) }
        var markerRef by remember { mutableStateOf<Marker?>(null) }
        var mapInitialized by remember { mutableStateOf(false) }

        var playbackIndex by remember { mutableStateOf(0) }
        var isPlaying by remember { mutableStateOf(false) }
        var playbackSpeed by remember { mutableStateOf(1.0f) }
        var isSeeking by remember { mutableStateOf(false) }
        var shouldLoadMap by remember { mutableStateOf(false) }

        var deviceId by remember { mutableStateOf("") }
        var fromDateTime by remember { mutableStateOf("") }
        var toDateTime by remember { mutableStateOf("") }
        var sidebarVisible by remember { mutableStateOf(false) }

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

        fun interpolatePoints(points: List<GeoPoint>, interval: Double = 5.0): List<GeoPoint> {
            val res = mutableListOf<GeoPoint>()
            for (i in 0 until points.size - 1) {
                val start = points[i]
                val end = points[i + 1]
                val steps = max(1, (start.distanceToAsDouble(end) / interval).toInt())
                for (j in 0..steps) {
                    val frac = j / steps.toDouble()
                    res.add(
                        GeoPoint(
                            start.latitude + (end.latitude - start.latitude) * frac,
                            start.longitude + (end.longitude - start.longitude) * frac
                        )
                    )
                }
            }
            if (points.isNotEmpty()) res.add(points.last())
            return res
        }

        LaunchedEffect(isPlaying, playbackIndex, playbackSpeed, interpolatedPoints) {
            if (isPlaying && playbackIndex < interpolatedPoints.lastIndex) {
                delay((33L / playbackSpeed).toLong())
                playbackIndex++
            } else if (isPlaying && playbackIndex >= interpolatedPoints.lastIndex) {
                isPlaying = false
                Toast.makeText(context, "Playback ended.", Toast.LENGTH_SHORT).show()
            }
        }

        LaunchedEffect(playbackIndex, isSeeking) {
            if (!isSeeking && interpolatedPoints.isNotEmpty() && playbackIndex in interpolatedPoints.indices) {
                val current = interpolatedPoints[playbackIndex]
                val previous = interpolatedPoints.getOrNull(playbackIndex - 1) ?: current
                val course = if (previous != current) calculateBearing(previous, current) else null

                markerRef?.apply {
                    position = current
                    course?.let {
                        icon = getRotatedBitmapDrawable(context, it)
                    }
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    map?.controller?.setCenter(current)
                    isFlat = true
                    title = buildString {
                        append("Speed: ${allRawPoints.getOrNull(playbackIndex)?.speed} km/h\n")
                        append("Time: ${allRawPoints.getOrNull(playbackIndex)?.timestamp}\n")
                        append("Course: ${course?.let { "%.0f".format(it) } ?: "—"}°")
                    }
                    closeInfoWindow()
                }

                map?.invalidate()
            }
        }

        LaunchedEffect(allRawPoints) {
            if (allRawPoints.isNotEmpty()) {
                interpolatedPoints = interpolatePoints(
                    allRawPoints.map { GeoPoint(it.latitude, it.longitude) }
                )
                playbackIndex = 0
            }
        }

        LaunchedEffect(errorMessage) {
            errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
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
                        controller.setZoom(17.0)
                        controller.setCenter(GeoPoint(12.9716, 77.5946))
                        map = this
                    }
                },
                update = { mv ->
                    map = mv
                    if (shouldLoadMap && !mapInitialized && interpolatedPoints.isNotEmpty()) {
                        val start = interpolatedPoints[0]
                        val m = Marker(mv).apply {
                            position = start
                            icon = getRotatedBitmapDrawable(context, 0f)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            isFlat = true
                        }
                        val poly = Polyline().apply {
                            setPoints(interpolatedPoints)
                            color = android.graphics.Color.BLUE
                            width = 6f
                        }
                        mv.overlays.clear()
                        mv.overlays.add(poly)
                        mv.overlays.add(m)
                        mv.controller.setCenter(start)
                        mv.invalidate()
                        markerRef = m
                        mapInitialized = true
                        shouldLoadMap = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            AnimatedVisibility(
                visible = sidebarVisible,
                enter = slideInHorizontally(),
                exit = slideOutHorizontally(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxHeight()
                    .width(300.dp)
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = deviceId,
                        onValueChange = { deviceId = it },
                        label = { Text("Device ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DateTimePicker(label = "From", dateTime = fromDateTime) { fromDateTime = it }
                    DateTimePicker(label = "To", dateTime = toDateTime) { toDateTime = it }
                    Button(onClick = {
                        if (deviceId.isNotBlank() && fromDateTime.isNotBlank() && toDateTime.isNotBlank()) {
                            playbackIndex = 0
                            isPlaying = false
                            shouldLoadMap = true
                            mapInitialized = false
                            viewModel.fetchPlaybackData(deviceId, fromDateTime, toDateTime)
                            sidebarVisible = false
                        }
                    }, Modifier.fillMaxWidth()) {
                        Text("Search")
                    }
                }
            }

            IconButton(
                onClick = { sidebarVisible = !sidebarVisible },
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Toggle Sidebar")
            }

            if (interpolatedPoints.isNotEmpty()) {
                Column(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = { isPlaying = !isPlaying }) {
                        Text(if (isPlaying) "Pause" else "Play")
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        listOf(1f, 1.5f, 2f).forEach { s ->
                            Button(
                                onClick = { playbackSpeed = s },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (playbackSpeed == s) Color.DarkGray else MaterialTheme.colorScheme.primary
                                )
                            ) { Text("${s}x", fontSize = 14.sp, color = Color.White) }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Slider(
                            value = playbackIndex.toFloat().coerceIn(0f, interpolatedPoints.lastIndex.toFloat()),
                            onValueChange = {
                                isSeeking = true
                                playbackIndex = it.toInt().coerceIn(0, interpolatedPoints.lastIndex)
                            },
                            onValueChangeFinished = {
                                isSeeking = false
                            },
                            valueRange = 0f..interpolatedPoints.lastIndex.toFloat(),
                            steps = max(0, interpolatedPoints.size - 2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = SliderDefaults.colors(
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                thumbColor = MaterialTheme.colorScheme.primary
                            ),
                            thumb = {
                                Box(
                                    modifier = Modifier
                                        .width(12.dp)
                                        .height(12.dp)
                                        .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(50))
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
