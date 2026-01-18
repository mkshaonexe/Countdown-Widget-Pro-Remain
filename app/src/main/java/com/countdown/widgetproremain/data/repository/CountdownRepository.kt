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
}
