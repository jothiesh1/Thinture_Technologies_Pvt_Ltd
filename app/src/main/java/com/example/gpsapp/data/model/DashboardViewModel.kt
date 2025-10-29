package com.example.gpsapp.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpsapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _statusData = MutableStateFlow<VehicleStatusResponse?>(null)
    val statusData: StateFlow<VehicleStatusResponse?> = _statusData.asStateFlow()

    init {
        fetchVehicleStatus()
    }

    fun fetchVehicleStatus() {
        viewModelScope.launch {
            try {
                println("🔄 Fetching vehicle status from: api/mobile/vehicles/status")
                val response = RetrofitClient.apiService.getVehicleStatus()

                println("📊 Response code: ${response.code()}")
                println("📊 Response successful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null) {
                        println("✅ API Response received:")
                        println("   - Total: ${body.total}")
                        println("   - Online: ${body.online}")
                        println("   - Moving: ${body.moving}")
                        println("   - Idle: ${body.idle}")
                        println("   - Parked: ${body.parked}")
                        println("   - Offline: ${body.offline}")
                        println("   - Timestamp: ${body.timestamp}")

                        // Explicitly set the value to trigger updates
                        _statusData.value = body

                        println("✅ StateFlow value updated successfully")
                        println("✅ Current StateFlow value: ${_statusData.value}")
                    } else {
                        println("⚠️ Response body is null")
                        _statusData.value = VehicleStatusResponse(
                            total = 0,
                            online = 0,
                            moving = 0,
                            idle = 0,
                            parked = 0,
                            offline = 0,
                            timestamp = ""
                        )
                    }
                } else {
                    println("❌ Vehicle status fetch failed: ${response.code()}")
                    println("❌ Error body: ${response.errorBody()?.string()}")

                    _statusData.value = VehicleStatusResponse(
                        total = 0,
                        online = 0,
                        moving = 0,
                        idle = 0,
                        parked = 0,
                        offline = 0,
                        timestamp = ""
                    )
                }
            } catch (e: Exception) {
                println("❌ Vehicle status fetch error: ${e.message}")
                println("❌ Exception type: ${e::class.simpleName}")
                e.printStackTrace()

                _statusData.value = VehicleStatusResponse(
                    total = 0,
                    online = 0,
                    moving = 0,
                    idle = 0,
                    parked = 0,
                    offline = 0,
                    timestamp = ""
                )
            }
        }
    }
}