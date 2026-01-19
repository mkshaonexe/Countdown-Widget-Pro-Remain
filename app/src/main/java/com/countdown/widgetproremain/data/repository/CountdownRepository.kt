package com.countdown.widgetproremain.data.repository

import com.countdown.widgetproremain.data.local.CountdownDao
import com.countdown.widgetproremain.data.model.CountdownEvent
import kotlinx.coroutines.flow.Flow

class CountdownRepository(private val countdownDao: CountdownDao) {
    val allEvents: Flow<List<CountdownEvent>> = countdownDao.getAllEvents()

    fun getEvent(id: Int): Flow<CountdownEvent> = countdownDao.getEventById(id)

    suspend fun insertEvent(event: CountdownEvent) = countdownDao.insertEvent(event)

    suspend fun updateEvent(event: CountdownEvent) = countdownDao.updateEvent(event)

    suspend fun deleteEvent(event: CountdownEvent) = countdownDao.deleteEvent(event)
    
    /**
     * Export all events for backup
     * @return List of all events in the database
     */
    suspend fun exportAllEvents(): List<CountdownEvent> {
        return countdownDao.getAllEventsSync()
    }
    
    /**
     * Import events from backup
     * @param events List of events to import
     * @param clearExisting If true, deletes all existing events before import
     * @return Number of events successfully imported
     */
    suspend fun importEvents(events: List<CountdownEvent>, clearExisting: Boolean = false): Int {
        return if (clearExisting) {
            countdownDao.deleteAllEvents()
            countdownDao.insertAll(events)
            events.size
        } else {
            // Insert without clearing - Room will handle conflicts with REPLACE strategy
            countdownDao.insertAll(events)
            events.size
        }
    }
}

