package com.example.myapplication.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.Model.OpenMeteoResponse
import com.example.myapplication.Network.WeatherApi
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

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        
    private val api = retrofit.create(WeatherApi::class.java)

    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val response = api.getWeather(lat = lat, lon = lon)
                _weatherData.value = response
                _temperature.value = "${response.current_weather.temperature.toInt()}°C"
                _weatherEmoji.value = getWeatherEmoji(response.current_weather.weathercode)
            } catch (e: Exception) {
                _temperature.value = "24°C"
                _weatherEmoji.value = "☀️"
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
}