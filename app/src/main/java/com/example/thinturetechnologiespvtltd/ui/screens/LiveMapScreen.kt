package com.example.thinturetechnologiespvtltd.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.thinturetechnologiespvtltd.R
import com.example.thinturetechnologiespvtltd.ui.components.ScaffoldWithDrawer
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun LiveMapScreen(navController: NavController) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val coroutineScope = rememberCoroutineScope()
    var userGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var mapInitialized by remember { mutableStateOf(false) }

    // Initialize OSMDroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.setBuiltInZoomControls(true)
        mapView.setBackgroundColor(android.graphics.Color.WHITE)
        mapView.controller.setZoom(18.0)
        mapView.controller.setCenter(GeoPoint(37.7749, -122.4194)) // Fallback center
        mapInitialized = true
        checkPermissionsAndStartLocationUpdates(context, mapView) { geoPoint ->
            userGeoPoint = geoPoint
        }
    }

    ScaffoldWithDrawer(navController = navController, screenTitle = "Live Map") { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            if (mapInitialized) {
                AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
            }

            FloatingActionButton(
                onClick = {
                    userGeoPoint?.let {
                        mapView.controller.setCenter(it)
                        mapView.controller.setZoom(20.0)
                    } ?: Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = "Recenter", tint = Color.White)
            }
        }
    }
}

private fun checkPermissionsAndStartLocationUpdates(
    context: Context,
    map: MapView,
    onLocationAvailable: (GeoPoint) -> Unit
) {
    val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION

    if (ContextCompat.checkSelfPermission(context, locationPermission) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
            context as android.app.Activity,
            arrayOf(locationPermission),
            1
        )
        Toast.makeText(context, "Please grant location permission", Toast.LENGTH_SHORT).show()
    } else {
        startLocationUpdates(context, map, onLocationAvailable)
    }
}

private fun startLocationUpdates(
    context: Context,
    map: MapView,
    onLocationAvailable: (GeoPoint) -> Unit
) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
    val providers = listOf(
        android.location.LocationManager.GPS_PROVIDER,
        android.location.LocationManager.NETWORK_PROVIDER,
        android.location.LocationManager.PASSIVE_PROVIDER
    )

    var bestLocation: android.location.Location? = null

    for (provider in providers) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val location = locationManager.getLastKnownLocation(provider)
            if (location != null && (bestLocation == null || location.accuracy < bestLocation.accuracy)) {
                bestLocation = location
            }
        }
    }

    if (bestLocation != null) {
        val userGeoPoint = GeoPoint(bestLocation.latitude, bestLocation.longitude)
        map.controller.setCenter(userGeoPoint)
        addMarkerAtLocation(context, map, userGeoPoint)
        onLocationAvailable(userGeoPoint)
    } else {
        Toast.makeText(context, "Location not available. Try outdoors.", Toast.LENGTH_LONG).show()
    }
}

private fun addMarkerAtLocation(context: Context, map: MapView, location: GeoPoint) {
    val marker = Marker(map).apply {
        position = location
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.custom_marker)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, false)
        icon = BitmapDrawable(context.resources, scaledBitmap)
        setOnMarkerClickListener { _, _ ->
            Toast.makeText(context, "You are here", Toast.LENGTH_SHORT).show()
            true
        }
    }
    map.overlays.add(marker)
}
