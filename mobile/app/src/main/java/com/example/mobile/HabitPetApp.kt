package com.example.mobile

import android.app.Application
import com.example.mobile.data.local.database.AchievementDatabaseInitializer
import com.example.mobile.domain.AchievementEngine
import com.example.mobile.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HabitPetApp : Application() {

    @Inject
    lateinit var achievementInitializer: AchievementDatabaseInitializer

    // 🔥 ADD THIS
    @Inject
    lateinit var achievementEngine: AchievementEngine

    override fun onCreate() {
        super.onCreate()

        achievementInitializer.initializeAchievementsAsync()

        // 🔥 FORCE ENGINE TO INITIALIZE
        achievementEngine // just accessing it is enough

        NotificationHelper(this).apply {
            createNotificationChannel()
            scheduleDailyReminder()
            scheduleStreakReminder()
            schedulePetReminder()
        }
    }
}