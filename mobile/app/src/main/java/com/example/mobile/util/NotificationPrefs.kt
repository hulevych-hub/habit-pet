package com.example.mobile.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper class for managing notification preferences
 */
object NotificationPrefs {

    private const val PREFS_NAME = "habit_pet_notification_prefs"
    private const val KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled"
    private const val KEY_STREAK_REMINDER_ENABLED = "streak_reminder_enabled"
    private const val KEY_PET_REMINDER_ENABLED = "pet_reminder_enabled"
    private const val DEFAULT_ENABLED = true

    /**
     * Initialize default preferences if they don't exist
     */
    fun initializeDefaults(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Set default values if not already set
        if (!prefs.contains(KEY_DAILY_REMINDER_ENABLED)) {
            editor.putBoolean(KEY_DAILY_REMINDER_ENABLED, DEFAULT_ENABLED)
        }
        if (!prefs.contains(KEY_STREAK_REMINDER_ENABLED)) {
            editor.putBoolean(KEY_STREAK_REMINDER_ENABLED, DEFAULT_ENABLED)
        }
        if (!prefs.contains(KEY_PET_REMINDER_ENABLED)) {
            editor.putBoolean(KEY_PET_REMINDER_ENABLED, DEFAULT_ENABLED)
        }

        editor.apply()
    }

    /**
     * Get daily reminder enabled status
     */
    fun isDailyReminderEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DAILY_REMINDER_ENABLED, DEFAULT_ENABLED)
    }

    /**
     * Set daily reminder enabled status
     */
    fun setDailyReminderEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(KEY_DAILY_REMINDER_ENABLED, enabled)
        editor.apply()
    }

    /**
     * Get streak reminder enabled status
     */
    fun isStreakReminderEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_STREAK_REMINDER_ENABLED, DEFAULT_ENABLED)
    }

    /**
     * Set streak reminder enabled status
     */
    fun setStreakReminderEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(KEY_STREAK_REMINDER_ENABLED, enabled)
        editor.apply()
    }

    /**
     * Get pet reminder enabled status
     */
    fun isPetReminderEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PET_REMINDER_ENABLED, DEFAULT_ENABLED)
    }

    /**
     * Set pet reminder enabled status
     */
    fun setPetReminderEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(KEY_PET_REMINDER_ENABLED, enabled)
        editor.apply()
    }

    /**
     * Clear all notification preferences (for testing/reset)
     */
    fun clearAllPreferences(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}