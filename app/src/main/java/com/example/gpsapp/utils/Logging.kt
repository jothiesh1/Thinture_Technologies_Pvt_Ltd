package com.example.gpsapp.utils

import com.example.gpsapp.data.model.EventReportItem
import com.example.gpsapp.data.model.LoginRequest
import com.example.gpsapp.data.model.LoginResponse
import com.example.gpsapp.network.PlaybackPointDto
import retrofit2.Response
import timber.log.Timber

object Logging {

    fun logLoginRequest(request: LoginRequest) {
        Timber.d("🔐 Sending login request: $request")
    }

    fun logLoginResponse(response: Response<LoginResponse>) {
        if (response.isSuccessful) {
            Timber.i("✅ Login success: ${response.body()}")
        } else {
            Timber.e("❌ Login failed - Code: ${response.code()}, Error: ${response.errorBody()?.string()}")
        }
    }

    fun logLoginException(e: Exception) {
        Timber.e(e, "🚨 Exception during login")
    }

    fun logEventReportRequest(deviceId: String, from: String, to: String) {
        Timber.d("📄 Fetching event report for $deviceId from $from to $to")
    }

    fun logEventReportResponse(response: Response<List<EventReportItem>>) {
        if (response.isSuccessful) {
            Timber.i("✅ Event report success: ${response.body()?.size ?: 0} items")
        } else {
            Timber.e("❌ Event report failed - Code: ${response.code()}")
        }
    }

    fun logEventReportException(e: Exception) {
        Timber.e(e, "🔥 Exception fetching event report")
    }

    fun logPlaybackRequest(deviceId: String, from: String, to: String) {
        Timber.d("🎞️ Fetching playback data for $deviceId from $from to $to")
    }

    fun logPlaybackResponse(result: List<PlaybackPointDto>) {
        Timber.i("✅ Playback data received: ${result.size} points")
    }

    fun logPlaybackException(e: Exception) {
        Timber.e(e, "💥 Exception fetching playback data")
    }
}
