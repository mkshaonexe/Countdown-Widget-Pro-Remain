package com.countdown.widgetproremain.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Schedule periodic widget updates (every 15 minutes) and midnight update
        scheduleWidgetUpdates(context)
        scheduleMidnightUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Cancel scheduled updates when all widgets are removed
        WorkManager.getInstance(context).cancelUniqueWork(WIDGET_UPDATE_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(MIDNIGHT_UPDATE_WORK_NAME)
    }

    companion object {
        private const val WIDGET_UPDATE_WORK_NAME = "countdown_widget_update"
        private const val MIDNIGHT_UPDATE_WORK_NAME = "countdown_widget_midnight_update"

        fun scheduleWidgetUpdates(context: Context) {
            val updateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WIDGET_UPDATE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                updateRequest
            )
        }

        fun scheduleMidnightUpdate(context: Context) {
            // Schedule daily midnight update
            val now = LocalDateTime.now()
            val midnight = LocalDateTime.of(now.toLocalDate().plusDays(1), LocalTime.MIDNIGHT)
            val initialDelay = Duration.between(now, midnight).toMinutes()

            val midnightRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                1, TimeUnit.DAYS
            ).setInitialDelay(initialDelay, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                MIDNIGHT_UPDATE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                midnightRequest
            )
        }
    }
}

/**
 * WorkManager worker to update widgets in the background
 */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val manager = GlanceAppWidgetManager(applicationContext)
            val widget = CountdownWidget()
            val glanceIds = manager.getGlanceIds(widget.javaClass)
            
            glanceIds.forEach { glanceId ->
                widget.update(applicationContext, glanceId)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
