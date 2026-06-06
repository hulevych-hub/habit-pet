package com.example.mobile.util

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
        cancelReminder(DAILY_REMINDER_REQUEST_CODE)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, NotificationPublisher::class.java).apply {
            action = "DAILY_REMINDER"
            putExtra("title", "Daily Habit Reminder")
            putExtra("message", "Time to check your habits for today!")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_REMINDER_REQUEST_CODE,
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

    /**
     * Schedule streak reminder notification
     */
    fun scheduleStreakReminder(hourOfDay: Int = 20, minute: Int = 0) {
        cancelReminder(STREAK_REMINDER_REQUEST_CODE)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, NotificationPublisher::class.java).apply {
            action = "STREAK_REMINDER"
            putExtra("title", "Streak Reminder")
            putExtra("message", "Don't break your streak! Complete your habits today.")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            STREAK_REMINDER_REQUEST_CODE,
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

    /**
     * Schedule pet reminder notification
     */
    fun schedulePetReminder(hourOfDay: Int = 12, minute: Int = 0) {
        cancelReminder(PET_REMINDER_REQUEST_CODE)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, NotificationPublisher::class.java).apply {
            action = "PET_REMINDER"
            putExtra("title", "Pet Care Reminder")
            putExtra("message", "Your pet needs attention today!")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            PET_REMINDER_REQUEST_CODE,
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
}

/**
 * BroadcastReceiver to handle publishing notifications from alarms
 */
class NotificationPublisher : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        val title = intent?.getStringExtra("title") ?: "Reminder"
        val message = intent?.getStringExtra("message") ?: "Time to check your habits!"

        NotificationHelper(context).showNotification(title, message)
    }
}