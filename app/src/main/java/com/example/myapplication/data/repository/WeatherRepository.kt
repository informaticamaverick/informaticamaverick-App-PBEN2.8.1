package com.example.myapplication.data.repository

import com.example.myapplication.data.remote.WeatherApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherRepository {
    private val weatherApi: WeatherApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherApi = retrofit.create(WeatherApiService::class.java)
    }

    suspend fun getCurrentWeather(lat: Double, lon: Double): WeatherData? {
        return try {
            android.util.Log.d("WeatherAPI", "Calling Open-Meteo with lat=$lat, lon=$lon")
            val response = weatherApi.getCurrentWeather(lat, lon)

            if (response.isSuccessful) {
                val weather = response.body()
                weather?.let { data ->
                    val cityName = "Lat: ${String.format("%.2f", lat)}"
                    WeatherData(
                        temperature = "${data.current.temperature_2m.toInt()}°C",
                        weatherEmoji = getWeatherEmoji(data.current.weathercode),
                        weatherDescription = getWeatherDescription(data.current.weathercode),
                        windSpeed = "${data.current.windspeed_10m.toInt()} km/h",
                        humidity = "${data.current.relativehumidity_2m}%",
                        cityName = cityName
                    )
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- FUNCIÓN CORREGIDA ---
    suspend fun getForecast(lat: Double, lon: Double): List<ForecastDay> {
        return try {
            val response = weatherApi.getCurrentWeather(lat, lon)
            if (response.isSuccessful) {
                val forecast = response.body()
                forecast?.daily?.let { daily ->
                    // Tomar los próximos 5 días (saltando hoy, índice 0)
                    daily.time.drop(1).take(5).mapIndexed { index, dateStr ->
                        val i = index + 1

                        // AQUÍ ESTABA EL ERROR: Ahora usamos los parámetros correctos
                        ForecastDay(
                            date = dateStr,                // Pasamos la fecha "2026-02-05"
                            maxTemp = daily.temperature_2m_max[i], // Pasamos el número Double directo
                            minTemp = daily.temperature_2m_min[i], // Pasamos el número Double directo
                            weatherCode = daily.weathercode[i]     // Pasamos el código int
                        )
                    }
                } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            android.util.Log.e("WeatherAPI", "Forecast error: ${e.message}", e)
            emptyList()
        }
    }

    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Despejado"
            1, 2, 3 -> "Parcialmente nublado"
            45, 48 -> "Niebla"
            51, 53, 55 -> "Llovizna"
            61, 63, 65 -> "Lluvia"
            71, 73, 75 -> "Nieve"
            77 -> "Granizo"
            80, 81, 82 -> "Chubascos"
            85, 86 -> "Nieve moderada"
            95 -> "Tormenta"
            96, 99 -> "Tormenta con granizo"
            else -> "Desconocido"
        }
    }

    private fun getWeatherEmoji(code: Int): String {
        return when (code) {
            0 -> "☀️"
            1, 2 -> "🌤️"
            3 -> "⛅"
            45, 48 -> "🌫️"
            51, 53, 55, 56, 57 -> "🌦️"
            61, 63, 65, 66, 67 -> "🌧️"
            71, 73, 75, 77 -> "❄️"
            80, 81, 82 -> "🌧️"
            85, 86 -> "🌨️"
            95, 96, 99 -> "⛈️"
            else -> "🌈"
        }
    }

    // (Opcional) Esta función ya no se usa dentro del repositorio,
    // pero la puedes dejar por si la necesitas en otro lado.
    private fun getDayName(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEE", Locale("es", "ES"))
            val date = inputFormat.parse(dateStr)
            val dayName = date?.let { outputFormat.format(it) } ?: "---"
            dayName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } catch (e: Exception) {
            "---"
        }
    }
}

// Modelos de datos
data class WeatherData(
    val temperature: String,
    val weatherEmoji: String,
    val weatherDescription: String,
    val windSpeed: String,
    val humidity: String,
    val cityName: String
)

// CLASE DE DATOS CORRECTA (Debe ser única)
data class ForecastDay(
    val date: String,
    val maxTemp: Double,
    val minTemp: Double,
    val weatherCode: Int
)