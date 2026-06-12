package com.example.mobile.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mobile.MainActivity
import com.example.mobile.R
import java.util.Calendar

/**
 * Helper class for managing notifications in the app
 */
class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "habit_pet_reminders"
        private const val CHANNEL_NAME = "Habit Pet Reminders"
        private const val CHANNEL_DESCRIPTION = "Reminders for habits, streaks, and pet care"
        private const val DAILY_REMINDER_REQUEST_CODE = 1001
        private const val STREAK_REMINDER_REQUEST_CODE = 1002
        private const val PET_REMINDER_REQUEST_CODE = 1003
        private const val STREAK_HIGH_THRESHOLD = 7
        private const val MILLISECONDS_PER_HOUR = 60L * 60L * 1000L

        private data class NotificationMessage(
            val title: String,
            val message: String
        )

        private fun dailyReminderMessage(context: Context): NotificationMessage {
            val behavior = if (NotificationPrefs.getLastStreak(context) >= STREAK_HIGH_THRESHOLD) {
                ReinforcementMessageProvider.UserBehavior.STREAK
            } else {
                ReinforcementMessageProvider.behavior(context)
            }
            val message = ReinforcementMessageProvider.notification(context, behavior)

            return NotificationMessage(message.title, message.message)
        }

        private fun streakReminderMessage(context: Context): NotificationMessage {
            val message = ReinforcementMessageProvider.notification(
                context = context,
                behavior = ReinforcementMessageProvider.UserBehavior.STREAK
            )

            return NotificationMessage(message.title, message.message)
        }

        private fun petReminderMessage(context: Context): NotificationMessage {
            val behavior = if (hoursSinceLastActive(context) > 48L) {
                ReinforcementMessageProvider.UserBehavior.INACTIVE
            } else {
                ReinforcementMessageProvider.behavior(context)
            }
            val message = ReinforcementMessageProvider.notification(context, behavior)

            return NotificationMessage(message.title, message.message)
        }

        private fun hoursSinceLastActive(context: Context): Long {
            val lastActiveTimestamp = NotificationPrefs.getLastActiveSessionTimestamp(context)
            if (lastActiveTimestamp <= 0L) return 0L

            return (System.currentTimeMillis() - lastActiveTimestamp).coerceAtLeast(0L) / MILLISECONDS_PER_HOUR
        }
    }

    /**
     * Create the notification channel for Android O+ devices
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Schedule daily reminder notification
     */
    fun scheduleDailyReminder(hourOfDay: Int = 9, minute: Int = 0) {
        scheduleReminder(
            requestCode = DAILY_REMINDER_REQUEST_CODE,
            action = "DAILY_REMINDER",
            hourOfDay = hourOfDay,
            minute = minute,
            message = dailyReminderMessage(context)
        )
    }

    /**
     * Schedule streak reminder notification
     */
    fun scheduleStreakReminder(hourOfDay: Int = 20, minute: Int = 0) {
        scheduleReminder(
            requestCode = STREAK_REMINDER_REQUEST_CODE,
            action = "STREAK_REMINDER",
            hourOfDay = hourOfDay,
            minute = minute,
            message = streakReminderMessage(context)
        )
    }

    /**
     * Schedule pet reminder notification
     */
    fun schedulePetReminder(hourOfDay: Int = 12, minute: Int = 0) {
        scheduleReminder(
            requestCode = PET_REMINDER_REQUEST_CODE,
            action = "PET_REMINDER",
            hourOfDay = hourOfDay,
            minute = minute,
            message = petReminderMessage(context)
        )
    }

    /**
     * Cancel a specific reminder
     */
    fun cancelReminder(requestCode: Int) {
        val intent = Intent(context, NotificationPublisher::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Cancel all reminders
     */
    fun cancelAllReminders() {
        cancelReminder(DAILY_REMINDER_REQUEST_CODE)
        cancelReminder(STREAK_REMINDER_REQUEST_CODE)
        cancelReminder(PET_REMINDER_REQUEST_CODE)
    }

    /**
     * Show a notification immediately
     */
    fun showNotification(title: String, message: String) {
        createNotificationChannel()

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun scheduleReminder(
        requestCode: Int,
        action: String,
        hourOfDay: Int,
        minute: Int,
        message: NotificationMessage
    ) {
        cancelReminder(requestCode)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, NotificationPublisher::class.java).apply {
            this.action = action
            putExtra("title", message.title)
            putExtra("message", message.message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}

/**
 * BroadcastReceiver to handle publishing notifications from alarms
 */
class NotificationPublisher : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val safeContext = context ?: return
        if (!NotificationPrefs.shouldShowNotification(safeContext)) return

        NotificationPrefs.markNotificationShown(safeContext)

        val fallbackMessage = ReinforcementMessageProvider.notification(safeContext)
        val title = intent?.getStringExtra("title") ?: fallbackMessage.title
        val message = intent?.getStringExtra("message") ?: fallbackMessage.message

        NotificationHelper(safeContext).showNotification(title, message)
    }
}
