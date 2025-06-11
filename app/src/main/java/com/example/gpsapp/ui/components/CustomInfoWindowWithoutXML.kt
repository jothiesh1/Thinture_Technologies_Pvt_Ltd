package com.example.gpsapp.ui.components

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class CustomInfoWindowWithoutXML(context: Context, mapView: MapView) :
    InfoWindow(View(context), mapView) {

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
        timeText.text = "Time: $time"
    }

    override fun onClose() {}
}
