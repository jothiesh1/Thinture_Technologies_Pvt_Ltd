package com.example.thinturetechnologiespvtltd.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

// Data class for playback points
data class PlaybackPoint(
    val lat: Double,
    val lon: Double,
    val speed: Int,
    val timestamp: String
)

@Composable
fun PlaybackMapScreen(navController: NavController) {
    ScaffoldWithDrawer(navController = navController, screenTitle = "Playback Map") { innerPadding ->

        val context = LocalContext.current
        var map by remember { mutableStateOf<MapView?>(null) }
        var markerRef by remember { mutableStateOf<Marker?>(null) }
        var infoWindow by remember { mutableStateOf<CustomInfoWindowWithoutXML?>(null) }

        // Load dummy JSON from assets
        val playbackPoints = remember {
            val inputStream = context.assets.open("playback_data.json")
            val json = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(json)
            List(jsonArray.length()) { i ->
                val obj = jsonArray.getJSONObject(i)
                PlaybackPoint(
                    lat = obj.getDouble("latitude"),
                    lon = obj.getDouble("longitude"),
                    speed = obj.getInt("speed"),
                    timestamp = obj.getString("timestamp")
                )
            }
        }

        var playbackIndex by remember { mutableStateOf(0) }
        var isPlaying by remember { mutableStateOf(false) }

        // Load osmdroid configuration
        LaunchedEffect(Unit) {
            Configuration.getInstance().load(
                context,
                context.getSharedPreferences("osm_prefs", android.content.Context.MODE_PRIVATE)
            )
        }

        // Fast playback update
        LaunchedEffect(isPlaying, playbackIndex) {
            if (isPlaying && playbackIndex < playbackPoints.lastIndex) {
                delay(200L)
                playbackIndex++
            }
        }

        // Update marker and popup content
        LaunchedEffect(playbackIndex) {
            val point = playbackPoints[playbackIndex]
            val geoPoint = GeoPoint(point.lat, point.lon)
            markerRef?.apply {
                position = geoPoint
                snippet = "Lat: ${point.lat}\nLng: ${point.lon}\nSpeed: ${point.speed} km/h\nTime: ${point.timestamp}"
                map?.controller?.setCenter(geoPoint)
                map?.invalidate()
                if (isInfoWindowShown) {
                    closeInfoWindow()
                    showInfoWindow()
                }
            }
        }

        // UI layout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AndroidView(
                factory = {
                    MapView(it).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(17.0)
                        val startPoint = GeoPoint(playbackPoints[0].lat, playbackPoints[0].lon)
                        controller.setCenter(startPoint)
                        map = this

                        val marker = Marker(this)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        val bmp = BitmapFactory.decodeResource(resources, R.drawable.car)
                        marker.icon = BitmapDrawable(resources, Bitmap.createScaledBitmap(bmp, 100, 100, false))
                        marker.position = startPoint
                        marker.snippet = "Lat: ${playbackPoints[0].lat}\nLng: ${playbackPoints[0].lon}\nSpeed: ${playbackPoints[0].speed} km/h\nTime: ${playbackPoints[0].timestamp}"

                        val window = CustomInfoWindowWithoutXML(context, this)
                        marker.infoWindow = window
                        infoWindow = window

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

            // Play/Pause button
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Button(onClick = { isPlaying = !isPlaying }) {
                    Text(if (isPlaying) "Pause" else "Play")
                }
            }
        }
    }
}

// Custom info window class without XML
class CustomInfoWindowWithoutXML(context: android.content.Context, mapView: MapView) :
    InfoWindow(LinearLayout(context), mapView) {

    private val latText = TextView(context).apply { setTextColor(Color.BLACK); textSize = 16f }
    private val lonText = TextView(context).apply { setTextColor(Color.BLACK); textSize = 16f }
    private val speedText = TextView(context).apply { setTextColor(Color.BLACK); textSize = 16f }
    private val dateText = TextView(context).apply { setTextColor(Color.BLACK); textSize = 16f }
    private val timeText = TextView(context).apply { setTextColor(Color.BLACK); textSize = 16f }

    init {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 20, 24, 20)
            background = GradientDrawable().apply {
                cornerRadius = 24f
                setColor(Color.WHITE)
                setStroke(2, Color.DKGRAY)
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
    }

    override fun onClose() {}
}
