package com.example.mobile.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper class for managing notification preferences
 */
object NotificationPrefs {

    private const val PREFS_NAME = "habit_pet_notification_prefs"
    private const val ACTIVITY_TIMELINE_PREFS_NAME = "activity_timeline_engine"
    private const val KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled"
    private const val KEY_STREAK_REMINDER_ENABLED = "streak_reminder_enabled"
    private const val KEY_PET_REMINDER_ENABLED = "pet_reminder_enabled"
    private const val KEY_LAST_STREAK = "last_streak"
    private const val KEY_LAST_NOTIFICATION_AT = "last_notification_at"
    private const val KEY_LAST_ACTIVE_SESSION_TIMESTAMP = "last_active_session_timestamp"
    private const val DEFAULT_ENABLED = true
    private const val DEFAULT_LAST_STREAK = 0
    private const val DEFAULT_LAST_NOTIFICATION_AT = 0L
    private const val DEFAULT_MIN_NOTIFICATION_INTERVAL_HOURS = 3

    /**
     * Initialize default preferences if they don't exist
     */
    fun initializeDefaults(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        if (!prefs.contains(KEY_DAILY_REMINDER_ENABLED)) {
            editor.putBoolean(KEY_DAILY_REMINDER_ENABLED, DEFAULT_ENABLED)
        }
        if (!prefs.contains(KEY_STREAK_REMINDER_ENABLED)) {
            editor.putBoolean(KEY_STREAK_REMINDER_ENABLED, DEFAULT_ENABLED)
        }
        if (!prefs.contains(KEY_PET_REMINDER_ENABLED)) {
            editor.putBoolean(KEY_PET_REMINDER_ENABLED, DEFAULT_ENABLED)
        }
        if (!prefs.contains(KEY_LAST_STREAK)) {
            editor.putInt(KEY_LAST_STREAK, DEFAULT_LAST_STREAK)
        }
        if (!prefs.contains(KEY_LAST_NOTIFICATION_AT)) {
            editor.putLong(KEY_LAST_NOTIFICATION_AT, DEFAULT_LAST_NOTIFICATION_AT)
        }

        editor.apply()
    }

    fun isDailyReminderEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DAILY_REMINDER_ENABLED, DEFAULT_ENABLED)

    fun setDailyReminderEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_DAILY_REMINDER_ENABLED, enabled).apply()
    }

    fun isStreakReminderEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_STREAK_REMINDER_ENABLED, DEFAULT_ENABLED)

    fun setStreakReminderEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_STREAK_REMINDER_ENABLED, enabled).apply()
    }

    fun isPetReminderEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_PET_REMINDER_ENABLED, DEFAULT_ENABLED)

    fun setPetReminderEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_PET_REMINDER_ENABLED, enabled).apply()
    }

    fun recordStreak(context: Context, streak: Int) {
        prefs(context).edit()
            .putInt(KEY_LAST_STREAK, streak.coerceAtLeast(0))
            .apply()
    }

    fun getLastStreak(context: Context): Int =
        prefs(context).getInt(KEY_LAST_STREAK, DEFAULT_LAST_STREAK).coerceAtLeast(0)

    fun recordLastActiveSession(context: Context, timestamp: Long = System.currentTimeMillis()) {
        prefs(context).edit()
            .putLong(KEY_LAST_ACTIVE_SESSION_TIMESTAMP, timestamp)
            .apply()
    }

    fun getLastActiveSessionTimestamp(context: Context): Long {
        val localTimestamp = prefs(context).getLong(KEY_LAST_ACTIVE_SESSION_TIMESTAMP, 0L)
        if (localTimestamp > 0L) return localTimestamp

        return context
            .getSharedPreferences(ACTIVITY_TIMELINE_PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_ACTIVE_SESSION_TIMESTAMP, 0L)
    }

    fun markNotificationShown(context: Context, timestamp: Long = System.currentTimeMillis()) {
        prefs(context).edit()
            .putLong(KEY_LAST_NOTIFICATION_AT, timestamp)
            .apply()
    }

    fun shouldShowNotification(
        context: Context,
        minIntervalHours: Int = DEFAULT_MIN_NOTIFICATION_INTERVAL_HOURS
    ): Boolean {
        val lastNotificationAt = prefs(context).getLong(KEY_LAST_NOTIFICATION_AT, DEFAULT_LAST_NOTIFICATION_AT)
        if (lastNotificationAt <= 0L) return true

        val minIntervalMillis = minIntervalHours.coerceAtLeast(1) * 60L * 60L * 1000L
        return System.currentTimeMillis() - lastNotificationAt >= minIntervalMillis
    }

    /**
     * Clear all notification preferences (for testing/reset)
     */
    fun clearAllPreferences(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

}
