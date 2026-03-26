package com.example.myapplication.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.Provider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.*

// ==========================================================================================
// --- MODELOS DE DATOS ---
// ==========================================================================================

data class ProviderWithDistance(
    val provider: Provider,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    // Coordenadas para el futuro uso con Firebase GeoPoint
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

// ==========================================================================================
// --- VIEWMODEL: LÓGICA DE NEGOCIO Y DISTANCIAS (PREPARADO PARA FIREBASE) ---
// ==========================================================================================

@HiltViewModel
class FastViewModel @Inject constructor(
    // TODO: FIREBASE - Inyectar repositorio de búsqueda geoespacial
) : ViewModel() {

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchFinished = MutableStateFlow(false)
    val searchFinished: StateFlow<Boolean> = _searchFinished.asStateFlow()

    private val _searchResults = MutableStateFlow<List<ProviderWithDistance>>(emptyList())
    val searchResults: StateFlow<List<ProviderWithDistance>> = _searchResults.asStateFlow()

    /**
     * Inicia la búsqueda de emergencia (Sistema Fast).
     * [ACTUALIZADO] Ahora maneja la lista de categorías y los nuevos campos de dirección.
     */
    fun startEmergencySearch(category: CategoryEntity?, allProviders: List<Provider>, userLat: Double, userLon: Double) {
        if (category == null) return

        viewModelScope.launch {
            _isSearching.value = true
            _searchFinished.value = false
            _searchResults.value = emptyList()

            // Simulamos latencia de red/consulta a Firestore
            delay(4000)

            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isNightTime = currentHour >= 21 || currentHour < 6

            val filteredList = allProviders
                .filter { provider ->
                    // 🔥 CORRECCIÓN: Ahora buscamos si la categoría solicitada está en la lista del prestador
                    provider.categories.any { it.equals(category.name, ignoreCase = true) }
                }
                .filter { provider ->
                    // 🔥 CORRECCIÓN: Los booleanos ahora están en la raíz del Provider
                    // Priorizamos el servicio 24hs o la disponibilidad online
                    if (isNightTime) {
                        provider.works24h && provider.isSubscribed
                    } else {
                        (provider.isOnline || provider.works24h) && provider.isSubscribed
                    }
                }
                .map { provider ->
                    // 🔥 LÓGICA DE UBICACIÓN:
                    // Si el prestador tiene coordenadas en su dirección, las usamos.
                    // Si no (por ser mock), generamos una cercana aleatoria para la demo.
                    val providerLat = provider.address?.latitude ?: (userLat + kotlin.random.Random.nextDouble(-0.03, 0.03))
                    val providerLon = provider.address?.longitude ?: (userLon + kotlin.random.Random.nextDouble(-0.03, 0.03))

                    val distance = calculateDistanceHaversine(userLat, userLon, providerLat, providerLon)
                    val time = (distance * 4.0).toInt().coerceAtLeast(2) // Aprox 4 min por km

                    ProviderWithDistance(provider, distance, time, providerLat, providerLon)
                }
                .sortedBy { it.distanceKm }
                .take(5) // Mostramos los 5 más cercanos en el radar

            _searchResults.value = filteredList
            _isSearching.value = false
            _searchFinished.value = true
        }
    }

    fun resetSearch() {
        _isSearching.value = false
        _searchFinished.value = false
        _searchResults.value = emptyList()
    }

    /**
     * Fórmula de Haversine para cálculo de distancias reales en km.
     */
    private fun calculateDistanceHaversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radio de la Tierra en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}



/**
package com.example.myapplication.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.model.fake.CategorySampleDataFalso.categories
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import perfetto.protos.MetatraceCategories
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.*

// ==========================================================================================
// --- MODELOS DE DATOS ---
// ==========================================================================================

data class ProviderWithDistance(
    val provider: Provider,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    // Coordenadas para el futuro uso con Firebase GeoPoint
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

// ==========================================================================================
// --- VIEWMODEL: LÓGICA DE NEGOCIO Y DISTANCIAS (PREPARADO PARA FIREBASE) ---
// ==========================================================================================

@HiltViewModel
class FastViewModel @Inject constructor(
    // TODO: FIREBASE - Aquí inyectaremos el repositorio (Ej: FastRepository)
    // que se encargará de hacer la consulta a Firestore GeoQueries.
) : ViewModel() {

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchFinished = MutableStateFlow(false)
    val searchFinished: StateFlow<Boolean> = _searchFinished.asStateFlow()

    private val _searchResults = MutableStateFlow<List<ProviderWithDistance>>(emptyList())
    val searchResults: StateFlow<List<ProviderWithDistance>> = _searchResults.asStateFlow()

    /**
     * Inicia la búsqueda de emergencia.
     * En el futuro, userLat y userLon vendrán del GPS del dispositivo y
     * se enviarán a Firebase para buscar prestadores cercanos.
     */
    fun startEmergencySearch(categories: CategoryEntity?, allProviders: List<Provider>, userLat: Double, userLon: Double) {
        if (categories == null) return

        viewModelScope.launch {
            _isSearching.value = true
            _searchFinished.value = false
            _searchResults.value = emptyList()

            // Simulamos el tiempo de consulta a la nube (Google Ads se mostrará aquí)
            // TODO: FIREBASE - Aquí harás el await() a tu llamada a Firestore.
            delay(4000)

            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isNightTime = currentHour >= 21 || currentHour < 6

            // TODO: FIREBASE - En lugar de filtrar 'allProviders' localmente,
            // usarás GeoFirestore para pedir a la base de datos SOLO los prestadores
            // dentro de un radio de X kilómetros, lo que ahorra ancho de banda.
            val filteredList = allProviders
                .filter { it.categories.equals(categories.name, ignoreCase = true) }
                .filter { provider ->
                    val company = provider.companies.firstOrNull()
                    val works24h = company?.works24h ?: false

                    if (isNightTime) {
                        works24h && provider.isSubscribed // Regla Nocturna
                    } else {
                        provider.isOnline && provider.isSubscribed // Regla Diurna
                    }
                }
                .map { provider ->
                    // TODO: FIREBASE - Aquí extraerás el GeoPoint del documento del Provider.
                    // Ej: val providerLat = provider.geoPoint.latitude
                    val providerLat = userLat + kotlin.random.Random.nextDouble(-0.05, 0.05)
                    val providerLon = userLon + kotlin.random.Random.nextDouble(-0.05, 0.05)

                    // Calculamos la distancia real matemática
                    val distance = calculateDistanceHaversine(userLat, userLon, providerLat, providerLon)
                    val time = (distance * 3.5).toInt() // Aprox 3.5 min por km de tránsito

                    ProviderWithDistance(provider, distance, time, providerLat, providerLon)
                }
                .sortedBy { it.distanceKm } // Ordenamos por el más cercano real
                .take(4) // Tomamos los 4 más cercanos para mostrar en el radar

            _searchResults.value = filteredList
            _isSearching.value = false
            _searchFinished.value = true
        }
    }

    fun resetSearch() {
        _isSearching.value = false
        _searchFinished.value = false
        _searchResults.value = emptyList()
    }

    /**
     * Fórmula real de Haversine para cálculo de distancias en la Tierra.
     * Funciona perfectamente con las coordenadas de Firebase.
     */
    private fun calculateDistanceHaversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radio de la Tierra en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
**/