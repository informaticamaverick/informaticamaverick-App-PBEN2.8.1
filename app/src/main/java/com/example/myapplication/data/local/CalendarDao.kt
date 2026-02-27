package com.example.myapplication.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE LA AGENDA TÉCNICA ---
 * Controla todas las operaciones directas con la tabla de eventos.
 */
@Dao
interface CalendarDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEvents(events: List<CalendarEventEntity>)

    /**
     * Obtiene TODOS los eventos.
     * Al usar 'Flow', la interfaz del calendario se actualizará sola
     * en cuanto haya un cambio en la base de datos.
     */
    @Query("SELECT * FROM calendar_events ORDER BY date ASC, time ASC")
    fun getAllEvents(): Flow<List<CalendarEventEntity>>

    /**
     * Actualiza solo el estado (útil para cuando el cliente cancela un turno)
     */
    @Query("UPDATE calendar_events SET status = :status WHERE id = :eventId")
    suspend fun updateEventStatus(eventId: String, status: VisitStatus)

    @Query("DELETE FROM calendar_events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: String)
}