package com.example.mobile.data.local.database

import android.content.Context
import com.example.mobile.data.local.database.AppDatabase
import com.example.mobile.data.local.dao.AchievementDao
import com.example.mobile.data.local.entities.AchievementEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Initializes the database with default achievements on first launch.
 */
class AchievementDatabaseInitializer(private val database: AppDatabase) {

    suspend fun initializeAchievements() {
        val achievementDao = database.achievementDao()

        // Check if achievements are already populated
        val existingCount = achievementDao.getAllAchievements().first().size

        if (existingCount == 0) {
            // Insert default achievements
            val defaultAchievements = listOf(
                AchievementEntity(
                    name = "First Habit",
                    description = "Create your first habit",
                    icon = "first_habit",
                    targetValue = 1,
                    rewardCoins = 50,
                    isUnlocked = false
                ),
                AchievementEntity(
                    name = "7 Day Streak",
                    description = "Maintain a 7-day streak",
                    icon = "streak_7",
                    targetValue = 7,
                    rewardCoins = 100,
                    isUnlocked = false
                ),
                AchievementEntity(
                    name = "30 Day Streak",
                    description = "Maintain a 30-day streak",
                    icon = "streak_30",
                    targetValue = 30,
                    rewardCoins = 250,
                    isUnlocked = false
                ),
                AchievementEntity(
                    name = "100 Completions",
                    description = "Complete 100 habits",
                    icon = "completions_100",
                    targetValue = 100,
                    rewardCoins = 200,
                    isUnlocked = false
                ),
                AchievementEntity(
                    name = "1000 XP",
                    description = "Earn 1000 XP",
                    icon = "xp_1000",
                    targetValue = 1000,
                    rewardCoins = 150,
                    isUnlocked = false
                ),
                AchievementEntity(
                    name = "Level 10",
                    description = "Reach level 10",
                    icon = "level_10",
                    targetValue = 10,
                    rewardCoins = 300,
                    isUnlocked = false
                ),
                AchievementEntity(
                    name = "Level 25",
                    description = "Reach level 25",
                    icon = "level_25",
                    targetValue = 25,
                    rewardCoins = 500,
                    isUnlocked = false
                )
            )

            for (achievement in defaultAchievements) {
                achievementDao.insertAchievement(achievement)
            }
        }
    }

    fun initializeAchievementsAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            initializeAchievements()
        }
    }
}