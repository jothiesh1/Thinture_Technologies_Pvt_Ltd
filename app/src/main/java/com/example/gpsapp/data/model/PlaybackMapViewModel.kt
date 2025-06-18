package com.example.gpsapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpsapp.network.PlaybackPointDto
import com.example.gpsapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class PlaybackMapViewModel : ViewModel() {
    private val _playbackPoints = MutableStateFlow<List<PlaybackPointDto>>(emptyList())
    val playbackPoints: StateFlow<List<PlaybackPointDto>> = _playbackPoints

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchPlaybackData(deviceId: String, from: String, to: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = RetrofitClient.playbackApiService.getPlaybackData(deviceId, from, to)
                _playbackPoints.value = interpolatePoints(result)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load data: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun interpolatePoints(points: List<PlaybackPointDto>, spacingMeters: Double = 15.0): List<PlaybackPointDto> {
        if (points.size < 2) return points

        val interpolated = mutableListOf<PlaybackPointDto>()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]

            interpolated.add(p1)

            val distance = haversine(p1.latitude, p1.longitude, p2.latitude, p2.longitude)
            val steps = (distance / spacingMeters).toInt()

            if (steps > 1) {
                val time1 = sdf.parse(p1.timestamp)?.time ?: 0
                val time2 = sdf.parse(p2.timestamp)?.time ?: time1
                val deltaTime = (time2 - time1) / steps

                for (j in 1 until steps) {
                    val fraction = j.toDouble() / steps
                    val lat = p1.latitude + (p2.latitude - p1.latitude) * fraction
                    val lon = p1.longitude + (p2.longitude - p1.longitude) * fraction
                    val speed = p1.speed + (p2.speed - p1.speed) * fraction
                    val time = Date(time1 + deltaTime * j)
                    val timeStr = sdf.format(time)

                    interpolated.add(
                        p1.copy(
                            latitude = lat,
                            longitude = lon,
                            speed = speed,
                            timestamp = timeStr
                        )
                    )
                }
            }
        }

        interpolated.add(points.last())
        return interpolated
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}
