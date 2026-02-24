package com.example.myapplication.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.utils.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationViewModel(private val context: Context) : ViewModel() {
    private val locationManager = LocationManager(context)
    
    private val _locationName = MutableStateFlow("Buenos Aires, AR")
    val locationName: StateFlow<String> = _locationName
    
    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude: StateFlow<Double?> = _latitude
    
    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude: StateFlow<Double?> = _longitude
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    init {
        fetchLocation()
    }
    
    fun fetchLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Verificar permisos
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                
                if (!hasPermission) {
                    _locationName.value = "Permiso de ubicación requerido"
                    _isLoading.value = false
                    return@launch
                }
                
                locationManager.requestLocation { name, lat, lon ->
                    _locationName.value = name
                    _latitude.value = lat
                    _longitude.value = lon
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _locationName.value = "Buenos Aires, AR"
                _isLoading.value = false
            }
        }
    }
}

/**
 * LocationViewModel: Obtiene ubicación actual del dispositivo usando LocationManager,
 * expone nombre de ubicación, latitud, longitud y estado de carga mediante StateFlows.
 */
