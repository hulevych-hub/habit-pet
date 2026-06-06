package com.example.mobile.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver to handle device boot completed event.
 * Reschedules all notifications after device reboot.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val notificationHelper = NotificationHelper(context!!)
            // Reschedule all reminders with default times
            notificationHelper.scheduleDailyReminder()
            notificationHelper.scheduleStreakReminder()
            notificationHelper.schedulePetReminder()
        }
    }
}