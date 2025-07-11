package com.example.gpsapp.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpsapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _statusData = MutableStateFlow<VehicleStatusResponse?>(null)
    val statusData: StateFlow<VehicleStatusResponse?> = _statusData

    init {
        fetchVehicleStatus()
    }

    private fun fetchVehicleStatus() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getVehicleStatus()
                if (response.isSuccessful) {
                    val body = response.body()
                    println("✅ Vehicle status: $body")
                    _statusData.value = body
                } else {
                    println("❌ Vehicle status fetch failed: ${response.code()}")
                }
            } catch (e: Exception) {
                println("❌ Vehicle status fetch error: ${e.localizedMessage}")
            }
        }
    }

}