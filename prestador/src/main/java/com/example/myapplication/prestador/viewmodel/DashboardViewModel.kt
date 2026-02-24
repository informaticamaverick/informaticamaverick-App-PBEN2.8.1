package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.AppointmentEntity
import com.example.myapplication.prestador.data.repository.AppointmentRepository
import com.example.myapplication.prestador.data.repository.PresupuestoRepository
import com.example.myapplication.prestador.data.repository.ProviderRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class DashboardUiState(
    val saludo: String = "",
    val proximaCita: AppointmentEntity? = null,
    val citasHoy: Int = 0,
    val gananciasSemanales: Double = 0.0,
    val serviceType: String = "",
    val solicitudesRecientes: List<AppointmentEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val presupuestoRepository: PresupuestoRepository,
    private val providerRepository: ProviderRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        calcularSaludo()
        cargarServiceType()
    }

    private fun calcularSaludo() {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val saludo = when {
            hora < 12 -> "Buenos días"
            hora < 18 -> "Buenas tardes"
            else -> "Buenas noches"

        }

        _uiState.update { it.copy(saludo = saludo) }
    }

    private fun cargarServiceType() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            providerRepository.getProviderById(uid).collect { provider ->
                val tipo = provider?.serviceType ?: "TECHNICAL"
                _uiState.update {
                    it.copy(serviceType = tipo)
                }
                cargarDatos(tipo)
            }
        }
    }

    private fun cargarDatos(serviceType: String) {
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Calendar.getInstance().time)
        viewModelScope.launch {
            appointmentRepository.getAllAppointments()
                .collect { citas ->
                    val citasHoy = citas.count {
                        it.date == hoy && it.status != "cancelado" && it.status != "cancelled"
                    }
                    val proximaCita =
                        citas.filter { it.date >= hoy && (it.status == "confirmado" || it.status == "confirmed" || it.status == "pending") }

                            .sortedWith(compareBy({ it.date }, { it.time }))
                            .firstOrNull()
                    _uiState.update {
                        it.copy(
                            citasHoy = citasHoy,
                            proximaCita = proximaCita,
                            solicitudesRecientes = citas
                                .filter { c -> c.status == "pending"}
                                .sortedByDescending { c -> c.date + c.time }
                                .take(3),
                            isLoading = false
                        )
                    }
                }
        }

        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            val fromDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            val ganancias = presupuestoRepository.getGananciasDesde("prestador_demo", fromDate)
            _uiState.update { it.copy(gananciasSemanales = ganancias) }
        }
    }
}