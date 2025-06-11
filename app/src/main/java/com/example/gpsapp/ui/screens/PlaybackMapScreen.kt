package com.example.gpsapp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gpsapp.R
import com.example.gpsapp.ui.components.CustomInfoWindowWithoutXML
import com.example.gpsapp.ui.components.DateTimePicker
import com.example.gpsapp.ui.components.ScaffoldWithDrawer
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color as AndroidColor

@Composable
fun PlaybackMapScreen(navController: NavController) {
    ScaffoldWithDrawer(navController = navController, screenTitle = "Playback Map") { innerPadding ->
        val context = LocalContext.current
        val viewModel: PlaybackMapViewModel = viewModel()
        val allPlaybackPoints by viewModel.playbackPoints.collectAsState()

        var map by remember { mutableStateOf<MapView?>(null) }
        var markerRef by remember { mutableStateOf<Marker?>(null) }
        var endMarkerRef by remember { mutableStateOf<Marker?>(null) }

        var deviceId by remember { mutableStateOf("") }
        var fromDateTime by remember { mutableStateOf("") }
        var toDateTime by remember { mutableStateOf("") }

        var playbackIndex by remember { mutableStateOf(0) }
        var isPlaying by remember { mutableStateOf(false) }
        var sidebarVisible by remember { mutableStateOf(false) }
        var playbackSpeed by remember { mutableStateOf(1.0f) }
        var shouldRedrawMap by remember { mutableStateOf(false) }

        val filteredPlaybackPoints = remember(allPlaybackPoints, deviceId, fromDateTime, toDateTime) {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            allPlaybackPoints.filter {
                (deviceId.isBlank() || it.device_id == deviceId) &&
                        (fromDateTime.isBlank() || format.parse(it.timestamp)!! >= format.parse(fromDateTime)!!) &&
                        (toDateTime.isBlank() || format.parse(it.timestamp)!! <= format.parse(toDateTime)!!)
            }
        }

        // Playback animation
        LaunchedEffect(isPlaying, playbackIndex, playbackSpeed) {
            if (isPlaying && playbackIndex < filteredPlaybackPoints.lastIndex) {
                delay((300L / playbackSpeed).toLong())
                playbackIndex++
            }
        }

        // Move marker
        LaunchedEffect(playbackIndex) {
            val point = filteredPlaybackPoints.getOrNull(playbackIndex) ?: return@LaunchedEffect
            val geoPoint = GeoPoint(point.latitude, point.longitude)
            markerRef?.apply {
                position = geoPoint
                snippet = "Lat: ${point.latitude}\nLng: ${point.longitude}\nSpeed: ${point.speed} km/h\nTime: ${point.timestamp}"
                rotation = (point.course - 90).toFloat()
                map?.controller?.setCenter(geoPoint)
                map?.invalidate()
                if (isInfoWindowShown) closeInfoWindow()
            }
        }

        // Fetch data
        LaunchedEffect(deviceId, fromDateTime, toDateTime) {
            if (deviceId.isNotBlank() && fromDateTime.isNotBlank() && toDateTime.isNotBlank()) {
                viewModel.fetchPlaybackData(deviceId, fromDateTime, toDateTime)
                playbackIndex = 0
                shouldRedrawMap = true
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
                        controller.setZoom(17.0)
                        controller.setCenter(GeoPoint(12.9716, 77.5946))
                        map = this
                    }
                },
                update = { mapView ->
                    map = mapView

                    if (shouldRedrawMap && filteredPlaybackPoints.isNotEmpty()) {
                        shouldRedrawMap = false
                        mapView.overlays.clear()

                        val startPoint = GeoPoint(filteredPlaybackPoints[0].latitude, filteredPlaybackPoints[0].longitude)
                        val endPoint = GeoPoint(filteredPlaybackPoints.last().latitude, filteredPlaybackPoints.last().longitude)

                        val carBmp = BitmapFactory.decodeResource(mapView.context.resources, R.drawable.car)
                        val carMarker = Marker(mapView).apply {
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            icon = BitmapDrawable(mapView.context.resources, Bitmap.createScaledBitmap(carBmp, 100, 100, false))
                            position = startPoint
                            snippet = "Lat: ${startPoint.latitude}\nLng: ${startPoint.longitude}\nSpeed: ${filteredPlaybackPoints[0].speed} km/h\nTime: ${filteredPlaybackPoints[0].timestamp}"
                            infoWindow = CustomInfoWindowWithoutXML(mapView.context, mapView)
                            setOnMarkerClickListener { m, _ -> if (m.isInfoWindowShown) m.closeInfoWindow() else m.showInfoWindow(); true }
                        }

                        val endMarker = Marker(mapView).apply {
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            position = endPoint
                            icon = mapView.context.getDrawable(R.drawable.custom_marker)
                            snippet = "End: ${endPoint.latitude}, ${endPoint.longitude}"
                        }

                        val polyline = Polyline().apply {
                            setPoints(filteredPlaybackPoints.map { GeoPoint(it.latitude, it.longitude) })
                            color = AndroidColor.BLUE
                            width = 6f
                        }

                        mapView.overlays.add(polyline)
                        mapView.overlays.add(carMarker)
                        mapView.overlays.add(endMarker)
                        markerRef = carMarker
                        endMarkerRef = endMarker
                        mapView.controller.setCenter(startPoint)
                        mapView.invalidate()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Sidebar
            AnimatedVisibility(
                visible = sidebarVisible,
                enter = slideInHorizontally(tween(300)) { it },
                exit = slideOutHorizontally(tween(300)) { it },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxHeight()
                    .width(300.dp)
                    .background(ComposeColor.White)
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

                    Button(
                        onClick = {
                            if (deviceId.isNotBlank() && fromDateTime.isNotBlank() && toDateTime.isNotBlank()) {
                                viewModel.fetchPlaybackData(deviceId, fromDateTime, toDateTime)
                                playbackIndex = 0
                                shouldRedrawMap = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Search")
                    }

                    Button(onClick = {
                        isPlaying = !isPlaying
                        if (isPlaying) {
                            markerRef?.closeInfoWindow()
                            sidebarVisible = false
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (isPlaying) "Pause" else "Play")
                    }

                    Text("Playback Speed", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1f, 1.5f, 2f).forEach { speed ->
                            Button(
                                onClick = { playbackSpeed = speed },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (playbackSpeed == speed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("${speed}x")
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = { sidebarVisible = !sidebarVisible },
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            ) {
                Icon(
                    imageVector = if (sidebarVisible) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Toggle Sidebar"
                )
            }
        }
    }
}
