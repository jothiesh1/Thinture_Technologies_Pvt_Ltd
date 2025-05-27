package com.example.thinturetechnologiespvtltd.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.thinturetechnologiespvtltd.R
import com.example.thinturetechnologiespvtltd.ui.components.ScaffoldWithDrawer
import org.osmdroid.views.MapView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

@Composable
fun PlaybackMapScreen(navController: NavController) {
    ScaffoldWithDrawer(navController = navController, screenTitle = "Playback Map") { innerPadding ->

        val context = LocalContext.current

        var map by remember { mutableStateOf<MapView?>(null) }
        val playbackLocations = listOf(
            GeoPoint(37.7749, -122.4194),
            GeoPoint(34.0522, -118.2437),
            GeoPoint(40.7128, -74.0060)
        )
        var playbackIndex by remember { mutableStateOf(0) }
        var userGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }

        LaunchedEffect(Unit) {
            Configuration.getInstance().load(
                context,
                context.getSharedPreferences("osm_prefs", android.content.Context.MODE_PRIVATE)
            )
        }

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
                        setBuiltInZoomControls(true)
                        controller.setZoom(15.0)
                        controller.setCenter(playbackLocations[0])
                        setBackgroundColor(android.graphics.Color.WHITE)
                        map = this

                        val marker = Marker(this)
                        marker.position = playbackLocations[0]
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.custom_marker)
                        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, false)
                        marker.icon = BitmapDrawable(resources, scaledBitmap)

                        overlays.add(marker)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            FloatingActionButton(
                onClick = {
                    userGeoPoint?.let {
                        map?.controller?.setCenter(it)
                        map?.controller?.setZoom(15.0)
                    } ?: Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = "Recenter", tint = Color.Black)
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            ) {
                IconButton(onClick = {
                    if (playbackIndex > 0) {
                        playbackIndex--
                        val prev = playbackLocations[playbackIndex]
                        map?.controller?.setCenter(prev)
                    } else {
                        Toast.makeText(context, "Beginning of playback", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Previous")
                }

                IconButton(onClick = {
                    if (playbackIndex < playbackLocations.lastIndex) {
                        playbackIndex++
                        val next = playbackLocations[playbackIndex]
                        map?.controller?.setCenter(next)
                    } else {
                        Toast.makeText(context, "End of playback", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "Next")
                }
            }
        }
    }
}
