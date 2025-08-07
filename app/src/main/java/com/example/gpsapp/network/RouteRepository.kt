package com.example.gpsapp.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RouteRepository(private val api: OpenRouteServiceApi) {

    suspend fun getRoadPolyline(coordinates: List<List<Double>>): List<List<Double>>? {
        return withContext(Dispatchers.IO) {
            try {
                val request = RouteRequest(coordinates)
                val response = api.getRoute(request).execute()

                if (response.isSuccessful) {
                    val route = response.body()?.routes?.firstOrNull()
                    route?.geometry?.coordinates
                } else {
                    Log.e("RouteRepository", "ORS API Error: ${response.code()} ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("RouteRepository", "Exception: ${e.message}")
                null
            }
        }
    }
}
