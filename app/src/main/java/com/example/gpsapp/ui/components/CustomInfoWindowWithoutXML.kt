package com.example.gpsapp.ui.components

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.navigation.NavController
import com.example.gpsapp.data.model.LiveVehicle
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class CustomInfoWindowWithoutXML(
    private val context: Context,
    mapView: MapView,
    private val navController: NavController
) : InfoWindow(LinearLayout(context), mapView) {

    private val scrollView: ScrollView = ScrollView(context)
    private val container: LinearLayout = LinearLayout(context)
    private val deviceIdRow = createRow()
    private val statusRow = createRow()
    private val speedRow = createRow()
    private val addressRow = createRow()
    private val timestampRow = createRow()
    private val gsmRow = createRow()
    private val ignitionRow = createRow()
    val lastReceivedText = TextView(context).apply {
        textSize = 10f
        setTextColor(Color.GRAY)
        gravity = Gravity.END
    }

    var userTappedToOpen = false
    private var lastMarkerPosition: org.osmdroid.util.GeoPoint? = null
    private var lastVehicleSnapshot: LiveVehicle? = null
    private var mSelectedMarker: Marker? = null

    init {
        scrollView.setBackgroundColor(Color.WHITE)
        scrollView.setPadding(16, 16, 16, 16)

        container.orientation = LinearLayout.VERTICAL
        container.setPadding(16, 16, 16, 16)

        listOf(deviceIdRow, statusRow, speedRow, addressRow, timestampRow, gsmRow, ignitionRow).forEach {
            container.addView(it)
        }

        container.addView(lastReceivedText)
        scrollView.addView(container)
        mView = scrollView
    }

    private fun createRow(): LinearLayout {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 8, 0, 8)
        }
        val label = TextView(context).apply {
            textSize = 14f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 16, 0)
        }
        val value = TextView(context).apply {
            textSize = 14f
            setTextColor(Color.DKGRAY)
        }
        layout.addView(label)
        layout.addView(value)
        return layout
    }

    private fun setText(row: LinearLayout, text: String, bold: Boolean = false) {
        val label = row.getChildAt(0) as TextView
        val value = row.getChildAt(1) as TextView
        val parts = text.split(":", limit = 2)
        label.text = parts.getOrNull(0)?.trim()?.plus(": ") ?: ""
        value.text = parts.getOrNull(1)?.trim() ?: ""
        if (bold) {
            label.setTextColor(Color.BLACK)
            value.setTextColor(Color.BLACK)
        }
    }

    override fun onOpen(item: Any?) {
        if (!userTappedToOpen){
            close()
            return
        }

        val marker = item as? Marker ?: return
        mSelectedMarker = marker
        lastMarkerPosition = marker.position

        val vehicle = marker.relatedObject as? LiveVehicle

        lastReceivedText.text = "● Last Received: ${vehicle?.timestamp ?: "--"}"
        setText(deviceIdRow, "Vehicle No: ${vehicle?.deviceId ?: "--"}", bold = true)
        setText(statusRow, "Status: ${vehicle?.liveStatus ?: "--"}")
        setText(speedRow, "Speed: ${vehicle?.speed ?: "--"} km/h")
        setText(addressRow, "Address: ${marker.snippet?.lines()?.getOrNull(4)?.substringAfter(":")?.trim() ?: "--"}")
        setText(timestampRow, "Time: ${vehicle?.timestamp ?: "--"}")
        setText(gsmRow, "GSM: No Signal (0)")

        val ignitionText = if ((vehicle?.ignition ?: "").equals("IGON", true)) "ON" else "OFF"
        val ignitionView = ignitionRow.getChildAt(1) as TextView
        ignitionView.text = "Ignition: $ignitionText"
        ignitionView.setTextColor(if (ignitionText == "OFF") Color.RED else Color.parseColor("#4CAF50"))

        container.startAnimation(
            ScaleAnimation(
                0.8f, 1f, 0.8f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 200
                fillAfter = true
            }
        )

        lastVehicleSnapshot = vehicle?.copy()
    }

    override fun onClose() {
        userTappedToOpen = false
        mSelectedMarker = null
        lastMarkerPosition = null
        lastVehicleSnapshot = null

        container.startAnimation(
            AlphaAnimation(1f, 0f).apply {
                duration = 200
                fillAfter = true
            }
        )
    }

    fun refresh(marker: Marker) {
        if (marker == mSelectedMarker && lastMarkerPosition == marker.position) {
            val newVehicle = marker.relatedObject as? LiveVehicle
            if (newVehicle != null && newVehicle != lastVehicleSnapshot) {
                onOpen(marker)
            }
        }
    }
}
