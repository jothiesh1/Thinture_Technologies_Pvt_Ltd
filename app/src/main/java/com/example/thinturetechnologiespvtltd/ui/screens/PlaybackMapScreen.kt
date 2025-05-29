package com.example.thinturetechnologiespvtltd.ui.screens

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
import androidx.navigation.NavController
import com.example.thinturetechnologiespvtltd.R
import com.example.thinturetechnologiespvtltd.ui.components.ScaffoldWithDrawer
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color as AndroidColor

data class PlaybackPoint(
    val lat: Double,
    val lon: Double,
    val speed: Int,
    val timestamp: String,
    val course: Int,
    val device_id: String
)

@Composable
fun PlaybackMapScreen(navController: NavController) {
    ScaffoldWithDrawer(navController = navController, screenTitle = "Playback Map") { innerPadding ->

        val context = LocalContext.current
        var map by remember { mutableStateOf<MapView?>(null) }
        var markerRef by remember { mutableStateOf<Marker?>(null) }

        val allPlaybackPoints = remember {
            val inputStream = context.assets.open("playback_data.json")
            val json = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(json)
            List(jsonArray.length()) { i ->
                val obj = jsonArray.getJSONObject(i)
                PlaybackPoint(
                    lat = obj.getDouble("latitude"),
                    lon = obj.getDouble("longitude"),
                    speed = obj.getInt("speed"),
                    timestamp = obj.getString("timestamp"),
                    course = obj.getInt("course"),
                    device_id = obj.getString("device_id")
                )
            }
        }

        var deviceId by remember { mutableStateOf("") }
        var fromDateTime by remember { mutableStateOf("") }
        var toDateTime by remember { mutableStateOf("") }
        var filteredPlaybackPoints by remember { mutableStateOf(allPlaybackPoints) }

        var playbackIndex by remember { mutableStateOf(0) }
        var isPlaying by remember { mutableStateOf(false) }
        var sidebarVisible by remember { mutableStateOf(false) }
        var playbackSpeed by remember { mutableStateOf(1.0f) }

        fun filterData() {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            filteredPlaybackPoints = allPlaybackPoints.filter {
                (deviceId.isBlank() || it.device_id == deviceId) &&
                        (fromDateTime.isBlank() || format.parse(it.timestamp)!! >= format.parse(fromDateTime)!!) &&
                        (toDateTime.isBlank() || format.parse(it.timestamp)!! <= format.parse(toDateTime)!!)
            }
            playbackIndex = 0
        }

        LaunchedEffect(isPlaying, playbackIndex, playbackSpeed) {
            if (isPlaying && playbackIndex < filteredPlaybackPoints.lastIndex) {
                delay((300L / playbackSpeed).toLong())
                playbackIndex++
            }
        }

        LaunchedEffect(playbackIndex) {
            val point = filteredPlaybackPoints.getOrNull(playbackIndex) ?: return@LaunchedEffect
            val geoPoint = GeoPoint(point.lat, point.lon)
            markerRef?.apply {
                position = geoPoint
                snippet = "Lat: ${point.lat}\nLng: ${point.lon}\nSpeed: ${point.speed} km/h\nTime: ${point.timestamp}"
                rotation = (point.course - 90).toFloat()
                map?.controller?.setCenter(geoPoint)
                map?.invalidate()
                if (isInfoWindowShown) closeInfoWindow()
            }
        }

        if (isPlaying) {
            markerRef?.closeInfoWindow()
        }

        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            AndroidView(
                factory = {
                    Configuration.getInstance().load(it, it.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                    MapView(it).apply {
                        isTilesScaledToDpi = false
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(17.0)
                        val startPoint = GeoPoint(filteredPlaybackPoints[0].lat, filteredPlaybackPoints[0].lon)
                        controller.setCenter(startPoint)
                        map = this

                        val marker = Marker(this)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        val bmp = BitmapFactory.decodeResource(resources, R.drawable.car)
                        marker.icon = BitmapDrawable(resources, Bitmap.createScaledBitmap(bmp, 100, 100, false))
                        marker.position = startPoint
                        marker.snippet = "Lat: ${filteredPlaybackPoints[0].lat}\nLng: ${filteredPlaybackPoints[0].lon}\nSpeed: ${filteredPlaybackPoints[0].speed} km/h\nTime: ${filteredPlaybackPoints[0].timestamp}"

                        val window = CustomInfoWindowWithoutXML(context, this)
                        marker.infoWindow = window

                        marker.setOnMarkerClickListener { m, _ ->
                            if (m.isInfoWindowShown) m.closeInfoWindow()
                            else m.showInfoWindow()
                            true
                        }

                        overlays.add(marker)
                        markerRef = marker
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            AnimatedVisibility(
                visible = sidebarVisible,
                enter = slideInHorizontally(animationSpec = tween(300)) { it },
                exit = slideOutHorizontally(animationSpec = tween(300)) { it },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxHeight()
                    .width(300.dp)
                    .background(ComposeColor.White)
                    .padding(12.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = deviceId,
                        onValueChange = { deviceId = it },
                        label = { Text("Device ID") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DateTimePicker(label = "From", dateTime = fromDateTime) { fromDateTime = it }
                    DateTimePicker(label = "To", dateTime = toDateTime) { toDateTime = it }

                    Button(onClick = { filterData() }, modifier = Modifier.fillMaxWidth()) {
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

@Composable
fun DateTimePicker(label: String, dateTime: String, onDateTimeSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Column {
        OutlinedTextField(
            value = dateTime,
            onValueChange = {},
            readOnly = true,
            label = { Text("$label Timestamp") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )
        Button(onClick = {
            DatePickerDialog(context, { _, year, month, day ->
                TimePickerDialog(context, { _, hour, minute ->
                    calendar.set(year, month, day, hour, minute)
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    onDateTimeSelected(sdf.format(calendar.time))
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }) {
            Text("Select $label Time")
        }
    }
}

class CustomInfoWindowWithoutXML(context: Context, mapView: MapView) :
    InfoWindow(LinearLayout(context), mapView) {

    private val latText = TextView(context).apply { setTextColor(AndroidColor.BLACK); textSize = 16f }
    private val lonText = TextView(context).apply { setTextColor(AndroidColor.BLACK); textSize = 16f }
    private val speedText = TextView(context).apply { setTextColor(AndroidColor.BLACK); textSize = 16f }
    private val dateText = TextView(context).apply { setTextColor(AndroidColor.BLACK); textSize = 16f }
    private val timeText = TextView(context).apply { setTextColor(AndroidColor.BLACK); textSize = 16f }

    init {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 20, 24, 20)
            background = GradientDrawable().apply {
                cornerRadius = 24f
                setColor(AndroidColor.WHITE)
                setStroke(2, AndroidColor.DKGRAY)
            }
            addView(latText)
            addView(lonText)
            addView(speedText)
            addView(dateText)
            addView(timeText)
        }
        mView = layout
    }

    override fun onOpen(item: Any?) {
        val marker = item as? Marker ?: return
        val point = marker.position
        val speed = marker.snippet?.lines()?.getOrNull(2)?.substringAfter(":")?.trim() ?: ""
        val timestamp = marker.snippet?.lines()?.getOrNull(3)?.substringAfter(":")?.trim() ?: ""
        val (date, time) = timestamp.split("T").let {
            val d = it.getOrNull(0) ?: ""
            val t = it.getOrNull(1)?.removeSuffix("Z") ?: ""
            d to t
        }

        latText.text = "Latitude: %.6f".format(point.latitude)
        lonText.text = "Longitude: %.6f".format(point.longitude)
        speedText.text = "Speed: $speed"
        dateText.text = "Date: $date"
        timeText.text = "Time: $time"
    }

    override fun onClose() {}
}
