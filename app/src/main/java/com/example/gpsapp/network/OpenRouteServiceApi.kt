package com.example.gpsapp.network

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call

// ORS API endpoint
private const val BASE_URL = "https://api.openrouteservice.org/"

// Directions API response model (define properly below)
data class RouteRequest(
    val coordinates: List<List<Double>>
)

data class Geometry(
    val coordinates: List<List<Double>>
)

data class Route(
    val geometry: Geometry
)

data class Feature(
    val segments: List<Route>
)

data class DirectionsResponse(
    val routes: List<Route>
)

interface OpenRouteServiceApi {

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json",
        "Authorization: 5b3ce3597851110001cf62484222bc188d8c48d0af0da9d6832a2a30" // Replace with actual key in code later
    )
    @POST("v2/directions/driving-car/geojson")
    fun getRoute(
        @Body body: RouteRequest
    ): Call<DirectionsResponse>

    companion object {
        fun create(): OpenRouteServiceApi {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(OpenRouteServiceApi::class.java)
        }
    }
}
