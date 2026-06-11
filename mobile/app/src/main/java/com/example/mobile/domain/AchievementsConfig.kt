package com.example.mobile.domain

import com.example.mobile.data.local.entities.AchievementEntity

object AchievementsConfig {

    const val FIRST_HABIT = "first_habit"
    const val THREE_HABIT_BUILDER = "three_habit_builder"
    const val SEVEN_DAY_STREAK = "seven_day_streak"
    const val THIRTY_DAY_STREAK = "thirty_day_streak"
    const val ONE_HUNDRED_COMPLETIONS = "one_hundred_completions"
    const val ONE_THOUSAND_XP = "one_thousand_xp"
    const val FIVE_THOUSAND_XP = "five_thousand_xp"
    const val LEVEL_TEN = "level_ten"
    const val LEVEL_TWENTY_FIVE = "level_twenty_five"
    const val FIRST_CUSTOMIZATION = "first_customization"
    const val CUSTOMIZATION_COLLECTOR = "customization_collector"

    data class AchievementDefinition(
        val id: String,
        val name: String,
        val description: String,
        val icon: String,
        val progressSource: AchievementProgressSource,
        val targetValue: Int?,
        val rewards: List<AchievementReward>
    )

    val achievements: List<AchievementDefinition> = listOf(
        AchievementDefinition(
            id = FIRST_HABIT,
            name = "First Habit",
            description = "Create your first habit",
            icon = "first_habit",
            progressSource = AchievementProgressSource.HABIT_COUNT,
            targetValue = 1,
            rewards = listOf(AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_FIRST_HABIT_COINS))
        ),
        AchievementDefinition(
            id = THREE_HABIT_BUILDER,
            name = "3 Habit Builder",
            description = "Create 3 habits",
            icon = "habit_builder_3",
            progressSource = AchievementProgressSource.HABIT_COUNT,
            targetValue = 3,
            rewards = listOf(AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_3_HABIT_BUILDER_COINS))
        ),
        AchievementDefinition(
            id = SEVEN_DAY_STREAK,
            name = "7 Day Streak",
            description = "Maintain a 7-day streak",
            icon = "streak_7",
            progressSource = AchievementProgressSource.CURRENT_STREAK,
            targetValue = 7,
            rewards = listOf(AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_7_DAY_STREAK_COINS))
        ),
        AchievementDefinition(
            id = THIRTY_DAY_STREAK,
            name = "30 Day Streak",
            description = "Maintain a 30-day streak",
            icon = "streak_30",
            progressSource = AchievementProgressSource.CURRENT_STREAK,
            targetValue = 30,
            rewards = listOf(AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_30_DAY_STREAK_COINS))
        ),
        AchievementDefinition(
            id = ONE_HUNDRED_COMPLETIONS,
            name = "100 Completions",
            description = "Complete 100 habits",
            icon = "completions_100",
            progressSource = AchievementProgressSource.TOTAL_COMPLETIONS,
            targetValue = 100,
            rewards = listOf(AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_100_COMPLETIONS_COINS))
        ),
        AchievementDefinition(
            id = ONE_THOUSAND_XP,
            name = "1000 XP",
            description = "Earn 1000 XP",
            icon = "xp_1000",
            progressSource = AchievementProgressSource.TOTAL_XP,
            targetValue = 1000,
            rewards = listOf(AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_1000_XP_COINS))
        ),
        AchievementDefinition(
            id = FIVE_THOUSAND_XP,
            name = "5000 XP",
            description = "Earn 5000 XP",
            icon = "xp_5000",
            progressSource = AchievementProgressSource.TOTAL_XP,
            targetValue = 5000,
            rewards = listOf(AchievementReward.ExpReward(300))
        ),
        AchievementDefinition(
            id = LEVEL_TEN,
            name = "Level 10",
            description = "Reach level 10",
            icon = "level_10",
            progressSource = AchievementProgressSource.PET_LEVEL,
            targetValue = 10,
            rewards = listOf(AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_LEVEL_10_COINS))
        ),
        AchievementDefinition(
            id = LEVEL_TWENTY_FIVE,
            name = "Level 25",
            description = "Reach level 25",
            icon = "level_25",
            progressSource = AchievementProgressSource.PET_LEVEL,
            targetValue = 25,
            rewards = listOf(AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_LEVEL_25_COINS))
        ),
        AchievementDefinition(
            id = FIRST_CUSTOMIZATION,
            name = "First Customization",
            description = "Unlock your first customization item",
            icon = "customization_first",
            progressSource = AchievementProgressSource.OWNED_CUSTOMIZATIONS,
            targetValue = 1,
            rewards = listOf(AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_FIRST_CUSTOMIZATION_COINS))
        ),
        AchievementDefinition(
            id = CUSTOMIZATION_COLLECTOR,
            name = "Customization Collector",
            description = "Unlock 5 customization items",
            icon = "customization_collector",
            progressSource = AchievementProgressSource.OWNED_CUSTOMIZATIONS,
            targetValue = 5,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.RARE),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_CUSTOMIZATION_COLLECTOR_COINS)
            )
        )
    )

    fun achievementById(id: String): AchievementDefinition? = achievements.firstOrNull { it.id == id }

    fun toEntity(definition: AchievementDefinition): AchievementEntity {
        val instantUnlock = definition.targetValue == null
        return AchievementEntity(
            id = definition.id,
            progress = 0,
            isUnlocked = instantUnlock,
            isClaimed = false,
            unlockedDate = if (instantUnlock) System.currentTimeMillis() else null
        )
    }
}
