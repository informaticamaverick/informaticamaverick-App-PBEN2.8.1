package com.example.myapplication.prestador.utils

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import android.widget.Toast
import java.util.*

/**
 * Helper para gestionar eventos del calendario
 */
object CalendarHelper {
    
    /**
     * Guarda un evento en el calendario del dispositivo
     * @param context Contexto de la aplicación
     * @param title Título del evento
     * @param description Descripción del evento
     * @param date Fecha en formato "YYYY-MM-DD"
     * @param time Hora en formato "HH:MM"
     * @return true si se guardó exitosamente, false en caso contrario
     */
    fun saveAppointmentToCalendar(
        context: Context,
        title: String,
        description: String,
        date: String,
        time: String
    ): Boolean {
        try {
            // Parsear fecha y hora
            val dateParts = date.split("-") // "2026-01-24"
            val timeParts = time.split(":") // "09:00"
            
            val year = dateParts[0].toInt()
            val month = dateParts[1].toInt() - 1 // Calendar.MONTH es 0-based
            val day = dateParts[2].toInt()
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            
            // Crear fecha de inicio
            val startCalendar = Calendar.getInstance().apply {
                set(year, month, day, hour, minute, 0)
            }
            val startMillis = startCalendar.timeInMillis
            
            // Duración de 1 hora por defecto
            val endMillis = startMillis + (60 * 60 * 1000)
            
            // Obtener el ID del calendario principal
            val calendarId = getCalendarId(context)
            if (calendarId == -1L) {
                Toast.makeText(context, "No se encontró calendario disponible", Toast.LENGTH_SHORT).show()
                return false
            }
            
            // Crear el evento
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.HAS_ALARM, 1) // Agregar recordatorio
            }
            
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            
            if (uri != null) {
                // Agregar recordatorio de 30 minutos antes
                val eventId = uri.lastPathSegment?.toLongOrNull()
                if (eventId != null) {
                    addReminder(context, eventId, 30)
                }
                
                Toast.makeText(context, "Cita guardada en el calendario", Toast.LENGTH_SHORT).show()
                return true
            } else {
                Toast.makeText(context, "Error al guardar en calendario", Toast.LENGTH_SHORT).show()
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
        }
    }
    
    /**
     * Obtiene el ID del primer calendario disponible
     */
    private fun getCalendarId(context: Context): Long {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            null
        )
        
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(0)
            }
        }
        return -1L
    }
    
    /**
     * Agrega un recordatorio al evento
     */
    private fun addReminder(context: Context, eventId: Long, minutesBefore: Int) {
        val reminderValues = ContentValues().apply {
            put(CalendarContract.Reminders.EVENT_ID, eventId)
            put(CalendarContract.Reminders.MINUTES, minutesBefore)
            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        }
        
        context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
    }
}
