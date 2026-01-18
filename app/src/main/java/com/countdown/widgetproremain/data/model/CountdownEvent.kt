package com.countdown.widgetproremain.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "countdown_events")
data class CountdownEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val targetDate: Long, // Epoch millis
    val isAllDay: Boolean = false,
    val color: Int, // ARGB
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
