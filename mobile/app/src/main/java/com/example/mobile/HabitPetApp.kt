package com.example.mobile

import android.app.Application
import com.example.mobile.data.local.database.AchievementDatabaseInitializer
import com.example.mobile.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HabitPetApp : Application() {

    @Inject
    lateinit var achievementInitializer: AchievementDatabaseInitializer

    override fun onCreate() {
        super.onCreate()
        // Initialize achievements on app start
        achievementInitializer.initializeAchievementsAsync()

        // Initialize notification system
        NotificationHelper(this).apply {
            createNotificationChannel()
            // Schedule reminders based on user preferences (defaults to enabled)
            scheduleDailyReminder()
            scheduleStreakReminder()
            schedulePetReminder()
        }
    }
}