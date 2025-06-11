package com.example.gpsapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpsapp.network.PlaybackPointDto
import com.example.gpsapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaybackMapViewModel : ViewModel() {
    private val _playbackPoints = MutableStateFlow<List<PlaybackPointDto>>(emptyList())
    val playbackPoints: StateFlow<List<PlaybackPointDto>> = _playbackPoints

    fun fetchPlaybackData(deviceId: String, from: String, to: String) {
        viewModelScope.launch {
            try {
                val result = RetrofitClient.playbackApiService.getPlaybackData(deviceId, from, to)
                _playbackPoints.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
