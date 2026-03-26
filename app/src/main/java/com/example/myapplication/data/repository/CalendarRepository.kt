package com.example.myapplication.data.repository

import com.example.myapplication.data.local.CalendarDao
import com.example.myapplication.data.local.CalendarEventEntity
import com.example.myapplication.data.local.VisitStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepository @Inject constructor(
    private val calendarDao: CalendarDao
) {
    // Flujo en tiempo real de todos los eventos
    val allEvents: Flow<List<CalendarEventEntity>> = calendarDao.getAllEvents()

    suspend fun addEvent(event: CalendarEventEntity) {
        calendarDao.insertEvent(event)
    }

    suspend fun cancelEvent(eventId: String) {
        calendarDao.updateEventStatus(eventId, VisitStatus.CANCELLED)
    }

    suspend fun deleteEvent(eventId: String) {
        calendarDao.deleteEvent(eventId)
    }
}