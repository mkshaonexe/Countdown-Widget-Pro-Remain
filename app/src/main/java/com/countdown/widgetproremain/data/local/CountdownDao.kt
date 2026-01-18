package com.countdown.widgetproremain.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.countdown.widgetproremain.data.model.CountdownEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface CountdownDao {
    @Query("SELECT * FROM countdown_events ORDER BY targetDate ASC")
    fun getAllEvents(): Flow<List<CountdownEvent>>

    @Query("SELECT * FROM countdown_events WHERE id = :id")
    fun getEventById(id: Int): Flow<CountdownEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CountdownEvent)

    @Update
    suspend fun updateEvent(event: CountdownEvent)

    @Delete
    suspend fun deleteEvent(event: CountdownEvent)
}
