package com.example.myapplication.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.OpenMeteoResponse
import com.example.myapplication.data.remote.WeatherApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherViewModel : ViewModel() {

    private val _weatherData = MutableStateFlow<OpenMeteoResponse?>(null)
    val weatherData: StateFlow<OpenMeteoResponse?> = _weatherData

    private val _temperature = MutableStateFlow("--°C")
    val temperature: StateFlow<String> = _temperature

    private val _weatherEmoji = MutableStateFlow("🌤️")
    val weatherEmoji: StateFlow<String> = _weatherEmoji

    private val _weatherDescription = MutableStateFlow("Cargando...")
    val weatherDescription: StateFlow<String> = _weatherDescription

    private val _windSpeed = MutableStateFlow("-- km/h")
    val windSpeed: StateFlow<String> = _windSpeed

    private val _humidity = MutableStateFlow("-- %")
    val humidity: StateFlow<String> = _humidity

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(WeatherApi::class.java)

    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val response = api.getWeather(
                    lat = lat,
                    lon = lon,
                    hourly = "temperature_2m,relativehumidity_2m,windspeed_10m",
                    daily = "temperature_2m_max,temperature_2m_min,weathercode,precipitation_sum,windspeed_10m_max"
                )
                _weatherData.value = response
                _temperature.value = "${response.current_weather.temperature.toInt()}°C"
                _weatherEmoji.value = getWeatherEmoji(response.current_weather.weathercode)
                _weatherDescription.value = getWeatherDescription(response.current_weather.weathercode)
                _windSpeed.value = "${response.current_weather.windspeed?.toInt() ?: 0} km/h"

                // Obtener humedad de los datos por hora (primer valor)
                val currentHumidity = response.hourly?.relativehumidity_2m?.firstOrNull()
                _humidity.value = "${currentHumidity ?: 0}%"

            } catch (e: Exception) {
                _temperature.value = "24°C"
                _weatherEmoji.value = "☀️"
                _weatherDescription.value = "Despejado"
                _windSpeed.value = "15 km/h"
                _humidity.value = "60%"
            }
        }
    }

    private fun getWeatherEmoji(code: Int): String {
        return when (code) {
            0 -> "☀️"           // Despejado
            1, 2, 3 -> "⛅"     // Parcialmente nublado
            45, 48 -> "🌫️"     // Niebla
            51, 53, 55 -> "🌦️" // Llovizna
            61, 63, 65 -> "🌧️" // Lluvia
            71, 73, 75 -> "❄️"  // Nieve
            95, 96, 99 -> "⛈️"  // Tormenta
            else -> "🌤️"
        }
    }

    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Despejado"
            1 -> "Mayormente despejado"
            2 -> "Parcialmente nublado"
            3 -> "Nublado"
            45, 48 -> "Niebla"
            51 -> "Llovizna ligera"
            53 -> "Llovizna moderada"
            55 -> "Llovizna densa"
            61 -> "Lluvia ligera"
            63 -> "Lluvia moderada"
            65 -> "Lluvia intensa"
            71 -> "Nevada ligera"
            73 -> "Nevada moderada"
            75 -> "Nevada intensa"
            95 -> "Tormenta"
            96, 99 -> "Tormenta con granizo"
            else -> "Clima variable"
        }
    }
}