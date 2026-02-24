package com.example.myapplication.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val cityName: String = "Ubicación desconocida",
    val countryName: String = ""
)

class LocationHelper(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    /**
     * Verifica si los permisos de ubicación están otorgados
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || 
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Obtiene la ubicación actual del dispositivo
     */
    suspend fun getCurrentLocation(): LocationData? {
        if (!hasLocationPermission()) {
            return null
        }
        
        return try {
            val location = suspendCancellableCoroutine<Location?> { continuation ->
                val cancellationTokenSource = CancellationTokenSource()
                
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    continuation.resume(location)
                }.addOnFailureListener {
                    continuation.resume(null)
                }
                
                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }
            
            location?.let {
                val cityName = getCityName(it.latitude, it.longitude)
                LocationData(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    cityName = cityName.first,
                    countryName = cityName.second
                )
            }
        } catch (e: SecurityException) {
            null
        }
    }
    
    /**
     * Obtiene el nombre de la ciudad usando Geocoding
     */
    private fun getCityName(latitude: Double, longitude: Double): Pair<String, String> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Ubicación"
                val country = address.countryName ?: ""
                Pair(city, country)
            } else {
                Pair("Ubicación desconocida", "")
            }
        } catch (e: Exception) {
            Pair("Ubicación desconocida", "")
        }
    }
}

/**
 * LocationHelper: Helper que usa Google Play Services (FusedLocationProviderClient) para obtener
 * coordenadas actuales, valida permisos y convierte coordenadas a nombres de ciudad mediante Geocoder.
 */
