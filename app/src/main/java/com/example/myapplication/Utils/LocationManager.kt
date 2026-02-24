package com.example.myapplication.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager as AndroidLocationManager
import android.os.Build
import android.os.Bundle
import java.util.Locale

class LocationManager(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? AndroidLocationManager
    private val geocoder = Geocoder(context, Locale.getDefault())

    @SuppressLint("MissingPermission")
    fun requestLocation(onResult: (String, Double?, Double?) -> Unit) {
        android.util.Log.d("LocationManager", "Solicitando ubicación...")
        
        // Primero intentar la última ubicación conocida
        val lastLocation = locationManager?.getLastKnownLocation(AndroidLocationManager.GPS_PROVIDER)
            ?: locationManager?.getLastKnownLocation(AndroidLocationManager.NETWORK_PROVIDER)
        
        if (lastLocation != null) {
            android.util.Log.d("LocationManager", "Ubicación obtenida: ${lastLocation.latitude}, ${lastLocation.longitude}")
            getLocationName(lastLocation, onResult)
            return
        }
        
        android.util.Log.d("LocationManager", "No hay última ubicación, solicitando actualización...")
        
        // Si no hay última ubicación, solicitar una nueva
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                android.util.Log.d("LocationManager", "Nueva ubicación: ${location.latitude}, ${location.longitude}")
                locationManager?.removeUpdates(this)
                getLocationName(location, onResult)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                android.util.Log.d("LocationManager", "GPS deshabilitado")
                onResult("GPS deshabilitado", null, null)
            }
        }

        try {
            // Solicitar actualizaciones de ubicación
            locationManager?.requestLocationUpdates(
                AndroidLocationManager.GPS_PROVIDER,
                0L,
                0f,
                listener
            )
            
            // Timeout de 5 segundos
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                android.util.Log.d("LocationManager", "Timeout alcanzado, usando ubicación por defecto")
                locationManager?.removeUpdates(listener)
                onResult("Buenos Aires, AR", null, null)
            }, 5000)
        } catch (e: Exception) {
            android.util.Log.e("LocationManager", "Error obteniendo ubicación: ${e.message}")
            onResult("Error obteniendo ubicación", null, null)
        }
    }

    private fun getLocationName(location: Location, onResult: (String, Double?, Double?) -> Unit) {
        android.util.Log.d("LocationManager", "Convirtiendo a nombre: ${location.latitude}, ${location.longitude}")
        
        val lat = location.latitude
        val lon = location.longitude
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                ) { addresses ->
                    val result = formatAddress(addresses.firstOrNull())
                    android.util.Log.d("LocationManager", "Geocoding resultado: $result")
                    onResult(result, lat, lon)
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )
                val result = formatAddress(addresses?.firstOrNull())
                android.util.Log.d("LocationManager", "Geocoding resultado: $result")
                onResult(result, lat, lon)
            }
        } catch (e: Exception) {
            android.util.Log.e("LocationManager", "Error en geocoding: ${e.message}")
            // Si falla el geocoding, mostrar coordenadas
            onResult("${location.latitude.format(2)}, ${location.longitude.format(2)}", lat, lon)
        }
    }

    private fun formatAddress(address: Address?): String {
        if (address == null) return "Ubicación desconocida"
        
        val city = address.locality ?: address.subAdminArea
        val country = address.countryCode
        
        return when {
            city != null && country != null -> "$city, $country"
            city != null -> city
            country != null -> country
            else -> "Ubicación desconocida"
        }
    }

    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
}

/**
 * LocationManager: Obtiene ubicación GPS usando LocationListener con geocodificación inversa
 * para convertir coordenadas a nombre de ciudad, incluye timeout de 5 segundos.
 */
