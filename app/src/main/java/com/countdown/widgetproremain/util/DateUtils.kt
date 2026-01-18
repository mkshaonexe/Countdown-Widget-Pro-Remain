package com.countdown.widgetproremain.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {
    fun formatTargetDate(millis: Long): String {
        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        return date.format(formatter)
    }

    fun getTimeRemaining(targetMillis: Long): String {
        val now = LocalDateTime.now()
        val target = Instant.ofEpochMilli(targetMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()

        if (now.isAfter(target)) {
            val days = ChronoUnit.DAYS.between(target, now)
            return "$days days since"
        }

        val days = ChronoUnit.DAYS.between(now, target)
        if (days > 0) return "$days days"
        
        val hours = ChronoUnit.HOURS.between(now, target)
        if (hours > 0) return "$hours hours"

        val minutes = ChronoUnit.MINUTES.between(now, target)
        return "$minutes minutes"
    }
}
