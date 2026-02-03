package com.example.myapplication.Data.Repository

import com.example.myapplication.Data.Remote.WeatherApiService
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
            android.util.Log.d("WeatherAPI", "Response code: ${response.code()}, successful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val weather = response.body()
                android.util.Log.d("WeatherAPI", "Response body: $weather")
                
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
                android.util.Log.e("WeatherAPI", "Error response: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("WeatherAPI", "Exception: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }
    
    suspend fun getForecast(lat: Double, lon: Double): List<ForecastDay> {
        return try {
            val response = weatherApi.getCurrentWeather(lat, lon)
            if (response.isSuccessful) {
                val forecast = response.body()
                forecast?.daily?.let { daily ->
                    // Tomar los próximos 5 días (saltando hoy)
                    daily.time.drop(1).take(5).mapIndexed { index, dateStr ->
                        val i = index + 1 // +1 porque saltamos el día 0 (hoy)
                        ForecastDay(
                            day = getDayName(dateStr),
                            emoji = getWeatherEmoji(daily.weathercode[i]),
                            temp = "${daily.temperature_2m_max[i].toInt()}°/${daily.temperature_2m_min[i].toInt()}°"
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
            0 -> "☀️" // Despejado
            1, 2 -> "🌤️" // Parcialmente nublado
            3 -> "⛅" // Nublado
            45, 48 -> "🌫️" // Niebla
            51, 53, 55, 56, 57 -> "🌦️" // Llovizna
            61, 63, 65, 66, 67 -> "🌧️" // Lluvia
            71, 73, 75, 77 -> "❄️" // Nieve
            80, 81, 82 -> "🌧️" // Chubascos
            85, 86 -> "🌨️" // Nieve
            95, 96, 99 -> "⛈️" // Tormenta
            else -> "🌈"
        }
    }
    
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

data class WeatherData(
    val temperature: String,
    val weatherEmoji: String,
    val weatherDescription: String,
    val windSpeed: String,
    val humidity: String,
    val cityName: String
)

data class ForecastDay(
    val day: String,
    val emoji: String,
    val temp: String
)
