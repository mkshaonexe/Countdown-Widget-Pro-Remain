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

    fun getDisplayDate(event: com.countdown.widgetproremain.data.model.CountdownEvent): LocalDateTime {
        val target = Instant.ofEpochMilli(event.targetDate).atZone(ZoneId.systemDefault()).toLocalDateTime()
        if (!event.isCountUp && event.recurrence != "NONE") {
            return calculateNextOccurrence(target, event.recurrence)
        }
        return target
    }

    fun calculateNextOccurrence(originalTarget: LocalDateTime, recurrence: String): LocalDateTime {
        val now = LocalDateTime.now()
        if (originalTarget.isAfter(now)) return originalTarget

        var nextDate = originalTarget
        while (nextDate.isBefore(now) || nextDate.isEqual(now)) {
            nextDate = when (recurrence) {
                "DAILY" -> nextDate.plusDays(1)
                "WEEKLY" -> nextDate.plusWeeks(1)
                "MONTHLY" -> nextDate.plusMonths(1)
                "YEARLY" -> nextDate.plusYears(1)
                else -> return originalTarget // Should not happen if recurrence is not NONE
            }
        }
        return nextDate
    }

    fun getTimeRemaining(event: com.countdown.widgetproremain.data.model.CountdownEvent): String {
        val now = LocalDateTime.now()
        val target = getDisplayDate(event)

        if (event.isCountUp) {
            // Count Up: Time SINCE the target (target should be in the past)
             if (now.isBefore(target)) {
                 // Future date for count up? treat as normal countdown until it passes?
                 // Or just show 0? Let's treat as "starts in X"
                 return "Starts in " + getDurationString(now, target)
             }
            return getDurationString(target, now) + " since"
        } else {
            // Count Down
            if (now.isAfter(target)) {
                 return "Done"
            }
            return getDurationString(now, target)
        }
    }

    private fun getDurationString(start: LocalDateTime, end: LocalDateTime): String {
        val days = ChronoUnit.DAYS.between(start, end)
        if (days > 0) return "$days days"

        val hours = ChronoUnit.HOURS.between(start, end)
        if (hours > 0) return "$hours hours"

        val minutes = ChronoUnit.MINUTES.between(start, end)
        return "$minutes minutes"
    }

    fun getDaysOnly(event: com.countdown.widgetproremain.data.model.CountdownEvent): String {
        val now = LocalDateTime.now()
        val target = getDisplayDate(event)
        val days = if (event.isCountUp) {
            ChronoUnit.DAYS.between(target, now)
        } else {
             ChronoUnit.DAYS.between(now, target)
        }
        return days.toString()
    }
    
    fun getHoursMinutes(event: com.countdown.widgetproremain.data.model.CountdownEvent): String {
        val now = LocalDateTime.now()
        val target = getDisplayDate(event)
        
        val start = if (event.isCountUp) target else now
        val end = if (event.isCountUp) now else target
        
        if (start.isAfter(end)) return "0h 0m"
        
        val totalMinutes = ChronoUnit.MINUTES.between(start, end)
        val hours = (totalMinutes % (24 * 60)) / 60
        val minutes = totalMinutes % 60
        
        return "${hours}h ${minutes}m"
    }

    fun getFullBreakdown(event: com.countdown.widgetproremain.data.model.CountdownEvent): Triple<String, String, String> {
        val now = LocalDateTime.now()
        val target = getDisplayDate(event)

        val start = if (event.isCountUp) target else now
        val end = if (event.isCountUp) now else target

        if (start.isAfter(end)) return Triple("0", "0", "0")

        val days = ChronoUnit.DAYS.between(start, end)
        val tempDate = start.plusDays(days)
        val hours = ChronoUnit.HOURS.between(tempDate, end)
        val tempDate2 = tempDate.plusHours(hours)
        val minutes = ChronoUnit.MINUTES.between(tempDate2, end)

        return Triple(days.toString(), hours.toString(), minutes.toString())
    }

    fun getSecondsOnly(event: com.countdown.widgetproremain.data.model.CountdownEvent): String {
        val now = LocalDateTime.now()
        val target = getDisplayDate(event)

        val start = if (event.isCountUp) target else now
        val end = if (event.isCountUp) now else target

        if (start.isAfter(end)) return "0"

        val days = ChronoUnit.DAYS.between(start, end)
        val tempDate = start.plusDays(days)
        val hours = ChronoUnit.HOURS.between(tempDate, end)
        val tempDate2 = tempDate.plusHours(hours)
        val minutes = ChronoUnit.MINUTES.between(tempDate2, end)
        val tempDate3 = tempDate2.plusMinutes(minutes)
        val seconds = ChronoUnit.SECONDS.between(tempDate3, end)
        
        return seconds.toString()
    }

    // Keep the old method for compatibility if needed, or remove it. 
    // The widget uses the old method signature: getTimeRemaining(Long)
    // We should update the widget to use the new one.
    fun getTimeRemaining(targetMillis: Long): String {
        // Fallback for simple calls without event object
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
