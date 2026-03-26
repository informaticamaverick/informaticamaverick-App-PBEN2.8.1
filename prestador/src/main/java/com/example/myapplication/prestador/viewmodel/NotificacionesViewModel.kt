package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.model.NotificacionItem
import com.example.myapplication.prestador.data.model.TipoNotificacion
import com.example.myapplication.prestador.data.repository.NotificacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificacionesViewModel @Inject constructor(
    private val repo: NotificacionRepository) : ViewModel() {
        private val _filtroTipo = MutableStateFlow<TipoNotificacion?>(null)
    val filtroTipo: StateFlow<TipoNotificacion?> = _filtroTipo.asStateFlow()

    private val _soloNoLeidas = MutableStateFlow(false)
    val soloNoLeidas: StateFlow<Boolean> = _soloNoLeidas.asStateFlow()

    val unreadCount: StateFlow<Int> = repo.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val notificaciones: StateFlow<List<NotificacionItem>> = combine(_filtroTipo, _soloNoLeidas) {
        tipo, soloNoLeidas ->
        Pair(tipo, soloNoLeidas)
    }.flatMapLatest { (tipo, soloNoLeidas) ->
        when{
            tipo != null -> repo.getByTipoFlow(tipo)
            soloNoLeidas -> repo.getUnreadFlow()
            else -> repo.getAllFlow()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            if (repo.getTotalCount() == 0 ) {
                val ahora = System.currentTimeMillis()
                val unDia = 86_400_000L
                listOf(
                    NotificacionItem(
                        tipo = TipoNotificacion.MENSAJE,
                        titulo = "Nuevo mensaje de Carlo",
                        mensaje = "Hola, ¿podés venir el jueves alas 10?",
                        fechaMs = ahora - (unDia * 0),
                        leida = false
                    ),
                    NotificacionItem(
                        tipo = TipoNotificacion.CITA,
                        titulo = "Cita confirmada",
                        mensaje = "Tu cita del viernes 21/03 a las 14:00 fue confirmada.",
                        fechaMs = ahora - (unDia * 1),
                        leida = false
                    ),
                    NotificacionItem(
                        tipo = TipoNotificacion.SOLICITUD,
                        titulo = "Nueva SOlicitud de servicio",
                        mensaje = "Maria lópez solicitó instalación eléctrica.",
                        fechaMs = ahora - (unDia * 2),
                        leida = false
                    ),
                    NotificacionItem(
                        tipo = TipoNotificacion.SISTEMA,
                        titulo = "Bienvenido a Maverick",
                        mensaje = "Tu perfil está activo y listo para recibir solicitudes.",
                        fechaMs = ahora - (unDia * 3),
                        leida = true
                    ),
                    NotificacionItem(
                        tipo = TipoNotificacion.MENSAJE,
                        titulo = "Mensaje de Ana García",
                        mensaje = "Gracias por el trabajo, quedó excelente!",
                        fechaMs = ahora - (unDia * 4),
                        leida = true
                    )
                ).forEach { repo.guardar(it) }
            }
        }
    }

    fun setFiltroTipo(tipo: TipoNotificacion?)
    { _filtroTipo.value = tipo }
    fun toggleSoloNoLeidas() {
        _soloNoLeidas.value = !_soloNoLeidas.value }
    fun marcarLeida(id: Long) =
        viewModelScope.launch { repo.marcarLeida(id) }
    fun marcarTodasLeidas() = viewModelScope.launch {
            repo.marcarTodasLeidas() }
    fun eliminar(id: Long) =
        viewModelScope.launch { repo.eliminar(id) }
    }