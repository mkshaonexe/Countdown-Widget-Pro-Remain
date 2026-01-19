package com.countdown.widgetproremain.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.countdown.widgetproremain.CountdownApplication
import com.countdown.widgetproremain.data.model.CountdownEvent
import com.countdown.widgetproremain.util.DateUtils
import com.countdown.widgetproremain.util.NotificationHelper
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

class CountdownWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val application = applicationContext as CountdownApplication
        val repository = application.repository

        // Fetch all events
        val events = repository.allEvents.firstOrNull() ?: return Result.success()

        if (events.isEmpty()) {
            NotificationHelper.updateStickyNotification(applicationContext, null)
            return Result.success()
        }

        // 1. Handle Sticky Notification (Most Urgent Event)
        val sortedEvents = events.filter { !it.isCountUp }
            .sortedBy { it.targetDate }
            .filter { it.targetDate > System.currentTimeMillis() }
        
        val mostUrgentEvent = sortedEvents.firstOrNull()
        NotificationHelper.updateStickyNotification(applicationContext, mostUrgentEvent)

        // 2. Handle Milestone Alerts
        checkMilestones(events)

        return Result.success()
    }

    private fun checkMilestones(events: List<CountdownEvent>) {
        val now = System.currentTimeMillis()
        val prefs = applicationContext.getSharedPreferences("milestone_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        events.forEach { event ->
            if (event.isCountUp) return@forEach // Skip count-ups for alerts for now

            val diff = event.targetDate - now
            if (diff < 0) return@forEach // Expired

            val days = TimeUnit.MILLISECONDS.toDays(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)

            // Define milestones keys: "event_id_milestone_name"
            
            // 30 Days
            if (days == 30L && !prefs.getBoolean("${event.id}_30d", false)) {
                NotificationHelper.showMilestoneNotification(applicationContext, event, "30 Days")
                editor.putBoolean("${event.id}_30d", true).apply()
            }
            
            // 7 Days
            if (days == 7L && !prefs.getBoolean("${event.id}_7d", false)) {
                NotificationHelper.showMilestoneNotification(applicationContext, event, "1 Week")
                editor.putBoolean("${event.id}_7d", true).apply()
            }

            // 1 Day
            if (days == 0L && hours < 24 && hours >= 23 && !prefs.getBoolean("${event.id}_1d", false)) {
                 // Trigger exactly when entering the last 24h window? 
                 // Or rather check if days == 1. Logic is tricky with exact match.
                 // Let's rely on range. "Within 24 hours" but not triggered yet.
                 // Actually, simpler logic: 
                 // if diff in [23h..24h] -> 1 Day alert
            }
            // Let's refine simple logic:
            // "1 Day" -> roughly 24 hours left.
            if (hours in 23..24 && !prefs.getBoolean("${event.id}_1d", false)) {
                 NotificationHelper.showMilestoneNotification(applicationContext, event, "1 Day")
                 editor.putBoolean("${event.id}_1d", true).apply()
            }

            // 1 Hour
            if (minutes in 55..65 && !prefs.getBoolean("${event.id}_1h", false)) {
                NotificationHelper.showMilestoneNotification(applicationContext, event, "1 Hour")
                editor.putBoolean("${event.id}_1h", true).apply()
            }
            
            // "The Moment" (0-5 mins)
            if (minutes < 5 && diff > 0 && !prefs.getBoolean("${event.id}_now", false)) {
                NotificationHelper.showMilestoneNotification(applicationContext, event, "It's Time!")
                editor.putBoolean("${event.id}_now", true).apply()
            }
        }
    }
}
