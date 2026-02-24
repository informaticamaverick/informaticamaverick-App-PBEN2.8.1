package com.example.myapplication.data.remote

import com.example.myapplication.data.model.OpenMeteoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("hourly") hourly: String? = null,
        @Query("daily") daily: String? = null,
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse
}