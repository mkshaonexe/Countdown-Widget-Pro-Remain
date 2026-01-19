package com.countdown.widgetproremain.data.repository

import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class SystemCalendarEvent(
    val id: Long,
    val title: String,
    val dtStart: Long,
    val allDay: Boolean
)

class CalendarRepository(private val context: Context) {

    suspend fun getUpcomingEvents(): List<SystemCalendarEvent> = withContext(Dispatchers.IO) {
        val events = mutableListOf<SystemCalendarEvent>()
        
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.ALL_DAY
        )

        // Filter: Future events only
        val selection = "${CalendarContract.Events.DTSTART} >= ?"
        val selectionArgs = arrayOf(System.currentTimeMillis().toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        val cursor: Cursor? = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(CalendarContract.Events._ID)
            val titleIndex = it.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
            val dtStartIndex = it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
            val allDayIndex = it.getColumnIndexOrThrow(CalendarContract.Events.ALL_DAY)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val title = it.getString(titleIndex) ?: "No Title"
                val dtStart = it.getLong(dtStartIndex)
                val allDay = it.getInt(allDayIndex) == 1

                events.add(SystemCalendarEvent(id, title, dtStart, allDay))
            }
        }
        events
    }
}
