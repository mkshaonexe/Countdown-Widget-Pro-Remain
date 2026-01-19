package com.countdown.widgetproremain

import android.app.Application
import com.countdown.widgetproremain.data.local.CountdownDatabase
import com.countdown.widgetproremain.data.repository.CountdownRepository

class CountdownApplication : Application() {
    val database by lazy { CountdownDatabase.getDatabase(this) }
    val repository by lazy { CountdownRepository(database.countdownDao()) }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Notification Channels
        com.countdown.widgetproremain.util.NotificationHelper.createNotificationChannels(this)

        // Schedule Periodic Work
        setupPeriodicWork()
    }

    private fun setupPeriodicWork() {
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.countdown.widgetproremain.worker.CountdownWorker>(
            15, java.util.concurrent.TimeUnit.MINUTES // Minimum interval is 15 mins
        )
        .build()

        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "CountdownWork",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP, // Keep existing if running
            workRequest
        )
    }
}
