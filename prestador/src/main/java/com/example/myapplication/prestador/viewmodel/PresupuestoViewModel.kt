package com.example.myapplication.prestador.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.PresupuestoEntity
import com.example.myapplication.prestador.data.repository.PresupuestoRepository
import com.example.myapplication.prestador.utils.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PresupuestoViewModel @Inject constructor(
    private val repository: PresupuestoRepository
) : ViewModel() {

    val presupuestos: StateFlow<List<PresupuestoEntity>> =
        repository.getAllPresupuestos()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun insertPresupuesto(presupuesto: PresupuestoEntity) {
        viewModelScope.launch { repository.insertPresupuesto(presupuesto) }
    }

    fun insertCliente(cliente: com.example.myapplication.prestador.data.local.entity.ClienteEntity) {
        viewModelScope.launch { repository.insertCliente(cliente) }
    }

    fun updatePresupuesto(presupuesto: PresupuestoEntity) {
        viewModelScope.launch { repository.updatePresupuesto(presupuesto) }
    }

    fun updateEstado(id: String, estado: String) {
        viewModelScope.launch { repository.updateEstado(id, estado) }
    }

    fun deletePresupuesto(presupuesto: PresupuestoEntity) {
        viewModelScope.launch { repository.deletePresupuesto(presupuesto) }
    }

    suspend fun getClienteById(id: String) = repository.getClienteById(id)

    suspend fun getPresupuestosForAppointment(appointmentId: String) =
        repository.getLatestPresupuestoForAppointment(appointmentId)

    suspend fun getTotalGanancias(prestadorId: String): Double =
        repository.getTotalGanancias(prestadorId)

    suspend fun getGananciasSemanales(prestadorId: String): Double {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val fromDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(cal.time)
        return repository.getGananciasDesde(prestadorId, fromDate)
    }

    fun simulateClientResponse(context: Context, presupuestoId: String, clienteName: String, total: Double) {
        viewModelScope.launch {
            delay(8_000L)
            repository.updateEstado(presupuestoId, "Aceptado")
            NotificationHelper(context).showPresupuestoAceptadoNotification(clienteName, total)
        }
    }
}
