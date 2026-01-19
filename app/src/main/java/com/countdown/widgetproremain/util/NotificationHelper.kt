package com.countdown.widgetproremain.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.countdown.widgetproremain.MainActivity
import com.countdown.widgetproremain.R
import com.countdown.widgetproremain.data.model.CountdownEvent

object NotificationHelper {

    private const val CHANNEL_ID_ALERTS = "channel_alerts"
    private const val CHANNEL_NAME_ALERTS = "Milestone Alerts"
    private const val CHANNEL_ID_STICKY = "channel_sticky"
    private const val CHANNEL_NAME_STICKY = "Ongoing Countdowns"

    private const val NOTIFICATION_ID_STICKY = 1001

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertsChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                CHANNEL_NAME_ALERTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for event milestones (1 day left, etc.)"
            }

            val stickyChannel = NotificationChannel(
                CHANNEL_ID_STICKY,
                CHANNEL_NAME_STICKY,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent notification for the most urgent event"
                setShowBadge(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(alertsChannel)
            notificationManager.createNotificationChannel(stickyChannel)
        }
    }

    fun showMilestoneNotification(context: Context, event: CountdownEvent, milestone: String) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, event.id, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with app icon if available
            .setContentTitle("Countdown Alert: ${event.title}")
            .setContentText("$milestone remaining for ${event.title}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(event.id, builder.build())
    }

    fun updateStickyNotification(context: Context, event: CountdownEvent?) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val notificationManager = NotificationManagerCompat.from(context)

        if (event == null) {
            notificationManager.cancel(NOTIFICATION_ID_STICKY)
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val timeRemaining = DateUtils.getTimeRemaining(event)
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_STICKY)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Replace with better icon
            .setContentTitle(event.title)
            .setContentText(timeRemaining)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)

        notificationManager.notify(NOTIFICATION_ID_STICKY, builder.build())
    }
}
