package com.example.mobile.data.local.database

import com.example.mobile.data.local.dao.AchievementDao
import com.example.mobile.data.local.entities.AchievementEntity
import com.example.mobile.domain.ChestType
import com.example.mobile.domain.EconomyConfig
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

        val existingAchievements = achievementDao.getAllRaw()
        val existingNames = existingAchievements.map { it.name }.toSet()
        val defaultAchievements = buildDefaultAchievements()

        defaultAchievements
            .filterNot { it.name in existingNames }
            .forEach { achievementDao.insertAchievement(it) }
    }

    fun initializeAchievementsAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            initializeAchievements()
        }
    }

    private fun buildDefaultAchievements(): List<AchievementEntity> {
        return listOf(
            AchievementEntity(
                name = "First Habit",
                description = "Create your first habit",
                icon = "first_habit",
                targetValue = 1,
                rewardCoins = EconomyConfig.ACHIEVEMENT_FIRST_HABIT_COINS,
                isUnlocked = false
            ),
            AchievementEntity(
                name = "3 Habit Builder",
                description = "Create 3 habits",
                icon = "habit_builder_3",
                targetValue = 3,
                rewardCoins = 100,
                isUnlocked = false
            ),
            AchievementEntity(
                name = "7 Day Streak",
                description = "Maintain a 7-day streak",
                icon = "streak_7",
                targetValue = 7,
                rewardCoins = EconomyConfig.ACHIEVEMENT_7_DAY_STREAK_COINS,
                isUnlocked = false
            ),
            AchievementEntity(
                name = "30 Day Streak",
                description = "Maintain a 30-day streak",
                icon = "streak_30",
                targetValue = 30,
                rewardCoins = EconomyConfig.ACHIEVEMENT_30_DAY_STREAK_COINS,
                isUnlocked = false
            ),
            AchievementEntity(
                name = "100 Completions",
                description = "Complete 100 habits",
                icon = "completions_100",
                targetValue = 100,
                rewardCoins = EconomyConfig.ACHIEVEMENT_100_COMPLETIONS_COINS,
                isUnlocked = false
            ),
            AchievementEntity(
                name = "1000 XP",
                description = "Earn 1000 XP",
                icon = "xp_1000",
                targetValue = 1000,
                rewardCoins = EconomyConfig.ACHIEVEMENT_1000_XP_COINS,
                isUnlocked = false
            ),
            AchievementEntity(
                name = "5000 XP",
                description = "Earn 5000 XP",
                icon = "xp_5000",
                targetValue = 5000,
                rewardExp = 300,
                isUnlocked = false
            ),
            AchievementEntity(
                name = "Level 10",
                description = "Reach level 10",
                icon = "level_10",
                targetValue = 10,
                rewardCoins = EconomyConfig.ACHIEVEMENT_LEVEL_10_COINS,
                isUnlocked = false
            ),
            AchievementEntity(
                name = "Level 25",
                description = "Reach level 25",
                icon = "level_25",
                targetValue = 25,
                rewardCoins = EconomyConfig.ACHIEVEMENT_LEVEL_25_COINS,
                isUnlocked = false
            ),
            AchievementEntity(
                name = "First Customization",
                description = "Unlock your first customization item",
                icon = "customization_first",
                targetValue = 1,
                rewardCoins = 75,
                isUnlocked = false
            ),
            AchievementEntity(
                name = "Customization Collector",
                description = "Unlock 5 customization items",
                icon = "customization_collector",
                targetValue = 5,
                rewardChestType = ChestType.RARE.name.lowercase(),
                isUnlocked = false
            )
        )
    }
}