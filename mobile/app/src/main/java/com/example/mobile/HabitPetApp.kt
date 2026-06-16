package com.example.mobile

import android.app.Application
import com.example.mobile.data.local.database.AchievementDatabaseInitializer
import com.example.mobile.data.local.database.ChallengeDatabaseInitializer
import com.example.mobile.data.local.database.InventoryItemDatabaseInitializer
import com.example.mobile.data.local.database.StatisticsDatabaseInitializer
import com.example.mobile.domain.AchievementEngine
import com.example.mobile.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HabitPetApp : Application() {

    @Inject
    lateinit var achievementInitializer: AchievementDatabaseInitializer

    @Inject
    lateinit var statisticsInitializer: StatisticsDatabaseInitializer

    @Inject
    lateinit var inventoryItemInitializer: InventoryItemDatabaseInitializer

    @Inject
    lateinit var challengeInitializer: ChallengeDatabaseInitializer

    @Inject
    lateinit var achievementEngine: AchievementEngine

    override fun onCreate() {
        super.onCreate()

        achievementInitializer.initializeAchievementsAsync()

        statisticsInitializer.initializeStatisticsAsync()

        inventoryItemInitializer.initializeCustomizationItemsAsync()

        challengeInitializer.initializeChallengesAsync()

        achievementEngine

        NotificationHelper(this).apply {
            createNotificationChannel()
            scheduleDailyReminder()
            scheduleStreakReminder()
            schedulePetReminder()
        }
    }
}