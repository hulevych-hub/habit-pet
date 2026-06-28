package com.example.mobile.domain

import com.example.mobile.data.local.entities.AchievementEntity

object AchievementsConfig {

    const val FIRST_HABIT = "first_habit"
    const val FIRST_CUSTOMIZATION = "first_customization"
    const val FIRST_AURA_GLOW = "first_aura_glow"
    const val THREE_HABIT_BUILDER = "three_habit_builder"
    const val COZY_OUTFIT = "cozy_outfit"
    const val FIVE_HABIT_BUILDER = "five_habit_builder"
    const val FOREST_BACKGROUND = "forest_background"
    const val THREE_CUSTOMIZATIONS = "three_customizations"
    const val TWENTY_FIVE_COMPLETIONS = "twenty_five_completions"
    const val LEVEL_FIVE = "level_five"
    const val SEVEN_DAY_STREAK = "seven_day_streak"
    const val FOURTEEN_DAY_STREAK = "fourteen_day_streak"
    const val TWO_THOUSAND_FIVE_HUNDRED_XP = "two_thousand_five_hundred_xp"
    const val ONE_HUNDRED_COMPLETIONS = "one_hundred_completions"
    const val ONE_THOUSAND_XP = "one_thousand_xp"
    const val TEN_HABIT_BUILDER = "ten_habit_builder"
    const val FIFTEEN_HABIT_BUILDER = "fifteen_habit_builder"
    const val TWO_HUNDRED_FIFTY_COMPLETIONS = "two_hundred_fifty_completions"
    const val LEVEL_TEN = "level_ten"
    const val LEVEL_FIFTEEN = "level_fifteen"
    const val THREE_THOUSAND_XP = "three_thousand_xp"
    const val THIRTY_DAY_STREAK = "thirty_day_streak"
    const val CRYSTAL_AURA = "crystal_aura"
    const val CUSTOMIZATION_COLLECTOR = "customization_collector"
    const val CRYSTAL_CROWN = "crystal_crown"
    const val CRYSTAL_CAVE = "crystal_cave"
    const val FIVE_THOUSAND_XP = "five_thousand_xp"
    const val TWENTY_HABIT_BUILDER = "twenty_habit_builder"
    const val FIVE_HUNDRED_COMPLETIONS = "five_hundred_completions"
    const val LEVEL_TWENTY_FIVE = "level_twenty_five"
    const val LEVEL_FORTY = "level_forty"
    const val LEVEL_FIFTY = "level_fifty"
    const val SEVEN_THOUSAND_FIVE_HUNDRED_XP = "seven_thousand_five_hundred_xp"
    const val TEN_THOUSAND_XP = "ten_thousand_xp"
    const val FIFTEEN_THOUSAND_XP = "fifteen_thousand_xp"
    const val SIXTY_DAY_STREAK = "sixty_day_streak"
    const val DRAGONFIRE_AURA = "dragonfire_aura"
    const val MYSTIC_CLOAK = "mystic_cloak"
    const val COLLECTION_HALFWAY = "collection_halfway"
    const val ONE_THOUSAND_COMPLETIONS = "one_thousand_completions"
    const val TWENTY_FIVE_THOUSAND_XP = "twenty_five_thousand_xp"
    const val ONE_HUNDRED_DAY_STREAK = "one_hundred_day_streak"
    const val STARLIGHT_ARMOR = "starlight_armor"
    const val CELESTIAL_AURA = "celestial_aura"
    const val LEVEL_SIXTY = "level_sixty"

    // New achievements
    const val BEST_STREAK_14 = "best_streak_14"
    const val BEST_STREAK_30 = "best_streak_30"
    const val BEST_STREAK_60 = "best_streak_60"
    const val BEST_STREAK_100 = "best_streak_100"
    const val BEST_COMBO_5 = "best_combo_5"
    const val BEST_COMBO_10 = "best_combo_10"
    const val DAYS_ACTIVE_7 = "days_active_7"
    const val DAYS_ACTIVE_30 = "days_active_30"
    const val DAYS_ACTIVE_100 = "days_active_100"
    const val PET_AGE_30 = "pet_age_30"
    const val PET_AGE_365 = "pet_age_365"
    const val COINS_1000 = "coins_1000"
    const val COINS_10000 = "coins_10000"
    const val FREEZES_1 = "freezes_1"
    const val FREEZES_5 = "freezes_5"
    const val CHALLENGES_1 = "challenges_1"
    const val CHALLENGES_10 = "challenges_10"
    const val CHALLENGES_50 = "challenges_50"
    const val CHESTS_10 = "chests_10"
    const val CHESTS_50 = "chests_50"
    const val CHESTS_100 = "chests_100"
    const val LOGINS_7 = "logins_7"
    const val LOGINS_30 = "logins_30"
    const val EVOLUTIONS_1 = "evolutions_1"
    const val EVOLUTIONS_3 = "evolutions_3"
    const val EVOLUTIONS_ALL = "evolutions_all"
    const val ACHIEVEMENTS_CLAIMED_10 = "achievements_claimed_10"
    const val ACHIEVEMENTS_CLAIMED_25 = "achievements_claimed_25"
    const val ACHIEVEMENTS_CLAIMED_50 = "achievements_claimed_50"
    const val LEVEL_75 = "level_75"
    const val LEVEL_100 = "level_100"
    const val XP_50000 = "xp_50000"
    const val COMPLETIONS_2500 = "completions_2500"
    const val STREAK_365 = "streak_365"

    data class AchievementDefinition(
        val id: String,
        val difficultyRank: Int = 0,
        val name: String,
        val description: String,
        val icon: String,
        val progressSource: AchievementProgressSource,
        val targetValue: Int?,
        val rewards: List<AchievementReward>
    )

    private val difficultyRanks = mapOf(
        // --- Core progression (1-20) ---
        FIRST_HABIT to 1,
        FIRST_CUSTOMIZATION to 2,
        THREE_HABIT_BUILDER to 3,
        FIRST_AURA_GLOW to 4,
        COZY_OUTFIT to 5,
        FIVE_HABIT_BUILDER to 6,
        THREE_CUSTOMIZATIONS to 7,
        TWENTY_FIVE_COMPLETIONS to 8,
        LEVEL_FIVE to 9,
        SEVEN_DAY_STREAK to 10,
        DAYS_ACTIVE_7 to 11,
        LOGINS_7 to 12,
        ONE_THOUSAND_XP to 13,
        FOURTEEN_DAY_STREAK to 14,
        BEST_STREAK_14 to 15,
        BEST_COMBO_5 to 16,
        CHALLENGES_1 to 17,
        CHESTS_10 to 18,
        EVOLUTIONS_1 to 19,
        TEN_HABIT_BUILDER to 20,
        // --- Mid game (21-45) ---
        ONE_HUNDRED_COMPLETIONS to 21,
        TWO_THOUSAND_FIVE_HUNDRED_XP to 22,
        FOREST_BACKGROUND to 23,
        FIFTEEN_HABIT_BUILDER to 24,
        LEVEL_TEN to 25,
        LEVEL_FIFTEEN to 26,
        TWO_HUNDRED_FIFTY_COMPLETIONS to 27,
        THIRTY_DAY_STREAK to 29,
        BEST_STREAK_30 to 30,
        PET_AGE_30 to 31,
        DAYS_ACTIVE_30 to 32,
        LOGINS_30 to 33,
        CRYSTAL_AURA to 34,
        CUSTOMIZATION_COLLECTOR to 35,
        CRYSTAL_CROWN to 36,
        CRYSTAL_CAVE to 37,
        THREE_THOUSAND_XP to 38,
        COLLECTION_HALFWAY to 39,
        CHALLENGES_10 to 40,
        CHESTS_50 to 41,
        EVOLUTIONS_3 to 42,
        COINS_1000 to 43,
        FREEZES_1 to 44,
        ACHIEVEMENTS_CLAIMED_10 to 45,
        // --- Late game (46-70) ---
        FIVE_THOUSAND_XP to 46,
        TWENTY_HABIT_BUILDER to 47,
        FIVE_HUNDRED_COMPLETIONS to 48,
        LEVEL_TWENTY_FIVE to 49,
        LEVEL_FORTY to 50,
        LEVEL_FIFTY to 51,
        SEVEN_THOUSAND_FIVE_HUNDRED_XP to 52,
        TEN_THOUSAND_XP to 53,
        FIFTEEN_THOUSAND_XP to 54,
        SIXTY_DAY_STREAK to 55,
        BEST_STREAK_60 to 56,
        BEST_COMBO_10 to 57,
        DRAGONFIRE_AURA to 58,
        MYSTIC_CLOAK to 59,
        STARLIGHT_ARMOR to 60,
        ONE_THOUSAND_COMPLETIONS to 61,
        TWENTY_FIVE_THOUSAND_XP to 62,
        BEST_STREAK_100 to 63,
        DAYS_ACTIVE_100 to 64,
        CHALLENGES_50 to 65,
        CHESTS_100 to 66,
        EVOLUTIONS_ALL to 67,
        COINS_10000 to 68,
        FREEZES_5 to 69,
        ACHIEVEMENTS_CLAIMED_25 to 70,
        // --- Endgame (71-96) ---
        LEVEL_SIXTY to 71,
        LEVEL_75 to 72,
        LEVEL_100 to 73,
        XP_50000 to 74,
        COMPLETIONS_2500 to 75,
        STREAK_365 to 76,
        PET_AGE_365 to 77,
        CELESTIAL_AURA to 78,
        ACHIEVEMENTS_CLAIMED_50 to 79
    )

    val achievements: List<AchievementDefinition> = listOf(
        // ============================
        // CORE PROGRESSION (1-20)
        // ============================
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
            id = FIRST_CUSTOMIZATION,
            name = "First Customization",
            description = "Unlock your first customization item",
            icon = "customization_first",
            progressSource = AchievementProgressSource.OWNED_CUSTOMIZATIONS,
            targetValue = 1,
            rewards = listOf(AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_FIRST_CUSTOMIZATION_COINS))
        ),
        AchievementDefinition(
            id = THREE_HABIT_BUILDER,
            name = "3 Habit Builder",
            description = "Create 3 habits",
            icon = "habit_builder_3",
            progressSource = AchievementProgressSource.HABIT_COUNT,
            targetValue = 3,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_3_HABIT_BUILDER_COINS)
            )
        ),
        AchievementDefinition(
            id = FIRST_AURA_GLOW,
            name = "First Aura Glow",
            description = "Complete 10 habits to unlock the Sakura Aura",
            icon = "aura_sakura",
            progressSource = AchievementProgressSource.TOTAL_COMPLETIONS,
            targetValue = 10,
            rewards = listOf(
                AchievementReward.CustomizationReward(EquipableConfig.SAKURA_AURA, CustomizationTypes.AURA)
            )
        ),
        AchievementDefinition(
            id = COZY_OUTFIT,
            name = "Royal Outfit",
            description = "Complete 25 habits to unlock the Royal Outfit",
            icon = "outfit_royal",
            progressSource = AchievementProgressSource.TOTAL_COMPLETIONS,
            targetValue = 25,
            rewards = listOf(
                AchievementReward.CustomizationReward(EquipableConfig.ROYAL_OUTFIT, CustomizationTypes.OUTFIT)
            )
        ),
        AchievementDefinition(
            id = FIVE_HABIT_BUILDER,
            name = "5 Habit Builder",
            description = "Create 5 habits",
            icon = "habit_builder_5",
            progressSource = AchievementProgressSource.HABIT_COUNT,
            targetValue = 5,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_5_HABIT_BUILDER_COINS)
            )
        ),
        AchievementDefinition(
            id = THREE_CUSTOMIZATIONS,
            name = "3 Customizations",
            description = "Unlock 3 customization items",
            icon = "customization_3",
            progressSource = AchievementProgressSource.OWNED_CUSTOMIZATIONS,
            targetValue = 3,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_3_CUSTOMIZATIONS_COINS)
            )
        ),
        AchievementDefinition(
            id = TWENTY_FIVE_COMPLETIONS,
            name = "25 Completions",
            description = "Complete 25 habits",
            icon = "completions_25",
            progressSource = AchievementProgressSource.TOTAL_COMPLETIONS,
            targetValue = 25,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_25_COMPLETIONS_COINS)
            )
        ),
        AchievementDefinition(
            id = LEVEL_FIVE,
            name = "Level 5",
            description = "Reach level 5",
            icon = "level_5",
            progressSource = AchievementProgressSource.PET_LEVEL,
            targetValue = 5,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_LEVEL_5_COINS)
            )
        ),
        AchievementDefinition(
            id = SEVEN_DAY_STREAK,
            name = "7 Day Streak",
            description = "Maintain a 7-day streak",
            icon = "streak_7",
            progressSource = AchievementProgressSource.CURRENT_STREAK,
            targetValue = 7,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_7_DAY_STREAK_COINS)
            )
        ),
        AchievementDefinition(
            id = DAYS_ACTIVE_7,
            name = "7 Days Active",
            description = "Play the game on 7 different days",
            icon = "days_active_7",
            progressSource = AchievementProgressSource.DAYS_ACTIVE,
            targetValue = 7,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_DAYS_ACTIVE_7_COINS)
            )
        ),
        AchievementDefinition(
            id = LOGINS_7,
            name = "Week of Welcome",
            description = "Log in on 7 different days",
            icon = "logins_7",
            progressSource = AchievementProgressSource.DAILY_LOGINS,
            targetValue = 7,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_LOGINS_7_COINS)
            )
        ),
        AchievementDefinition(
            id = ONE_THOUSAND_XP,
            name = "1000 XP",
            description = "Earn 1000 XP",
            icon = "xp_1000",
            progressSource = AchievementProgressSource.TOTAL_XP,
            targetValue = 1000,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_1000_XP_COINS)
            )
        ),
        AchievementDefinition(
            id = FOURTEEN_DAY_STREAK,
            name = "14 Day Streak",
            description = "Maintain a 14-day streak",
            icon = "streak_14",
            progressSource = AchievementProgressSource.CURRENT_STREAK,
            targetValue = 14,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_14_DAY_STREAK_COINS)
            )
        ),
        AchievementDefinition(
            id = BEST_STREAK_14,
            name = "Best Streak 14",
            description = "Reach a best streak of 14 days",
            icon = "best_streak_14",
            progressSource = AchievementProgressSource.BEST_STREAK,
            targetValue = 14,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_BEST_STREAK_14_COINS)
            )
        ),
        AchievementDefinition(
            id = BEST_COMBO_5,
            name = "Combo Starter",
            description = "Reach a best combo of 5 hits",
            icon = "combo_5",
            progressSource = AchievementProgressSource.BEST_COMBO,
            targetValue = 5,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_BEST_COMBO_5_COINS)
            )
        ),
        AchievementDefinition(
            id = CHALLENGES_1,
            name = "Challenge Accepted",
            description = "Complete your first challenge",
            icon = "challenge_1",
            progressSource = AchievementProgressSource.CHALLENGES_COMPLETED,
            targetValue = 1,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_CHALLENGES_1_COINS)
            )
        ),
        AchievementDefinition(
            id = CHESTS_10,
            name = "Chest Collector",
            description = "Open 10 chests",
            icon = "chest_10",
            progressSource = AchievementProgressSource.CHESTS_OPENED,
            targetValue = 10,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_CHESTS_10_COINS)
            )
        ),
        AchievementDefinition(
            id = EVOLUTIONS_1,
            name = "First Evolution",
            description = "Evolve your dragon for the first time",
            icon = "evolution_1",
            progressSource = AchievementProgressSource.EVOLUTIONS,
            targetValue = 1,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.RARE),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_EVOLUTIONS_1_COINS)
            )
        ),
        AchievementDefinition(
            id = TEN_HABIT_BUILDER,
            name = "10 Habit Builder",
            description = "Create 10 habits",
            icon = "habit_builder_10",
            progressSource = AchievementProgressSource.HABIT_COUNT,
            targetValue = 10,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_10_HABIT_BUILDER_COINS)
            )
        ),

        // ============================
        // MID GAME (21-45)
        // ============================
        AchievementDefinition(
            id = ONE_HUNDRED_COMPLETIONS,
            name = "100 Completions",
            description = "Complete 100 habits",
            icon = "completions_100",
            progressSource = AchievementProgressSource.TOTAL_COMPLETIONS,
            targetValue = 100,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_100_COMPLETIONS_COINS)
            )
        ),
        AchievementDefinition(
            id = TWO_THOUSAND_FIVE_HUNDRED_XP,
            name = "2500 XP",
            description = "Earn 2500 XP",
            icon = "xp_2500",
            progressSource = AchievementProgressSource.TOTAL_XP,
            targetValue = 2500,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_2500_XP_COINS)
            )
        ),
        AchievementDefinition(
            id = FOREST_BACKGROUND,
            name = "Forest Background",
            description = "Earn 2500 XP to unlock the Forest Background",
            icon = "background_forest",
            progressSource = AchievementProgressSource.TOTAL_XP,
            targetValue = 2500,
            rewards = listOf(
                AchievementReward.CustomizationReward(EquipableConfig.BACKGROUND_FOREST, CustomizationTypes.BACKGROUND)
            )
        ),
        AchievementDefinition(
            id = FIFTEEN_HABIT_BUILDER,
            name = "15 Habit Builder",
            description = "Create 15 habits",
            icon = "habit_builder_15",
            progressSource = AchievementProgressSource.HABIT_COUNT,
            targetValue = 15,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_15_HABIT_BUILDER_COINS)
            )
        ),
        AchievementDefinition(
            id = LEVEL_TEN,
            name = "Level 10",
            description = "Reach level 10",
            icon = "level_10",
            progressSource = AchievementProgressSource.PET_LEVEL,
            targetValue = 10,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_LEVEL_10_COINS)
            )
        ),
        AchievementDefinition(
            id = LEVEL_FIFTEEN,
            name = "Level 15",
            description = "Reach level 15",
            icon = "level_15",
            progressSource = AchievementProgressSource.PET_LEVEL,
            targetValue = 15,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_LEVEL_15_COINS)
            )
        ),
        AchievementDefinition(
            id = TWO_HUNDRED_FIFTY_COMPLETIONS,
            name = "250 Completions",
            description = "Complete 250 habits",
            icon = "completions_250",
            progressSource = AchievementProgressSource.TOTAL_COMPLETIONS,
            targetValue = 250,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_250_COMPLETIONS_COINS)
            )
        ),
        AchievementDefinition(
            id = THIRTY_DAY_STREAK,
            name = "30 Day Streak",
            description = "Maintain a 30-day streak",
            icon = "streak_30",
            progressSource = AchievementProgressSource.CURRENT_STREAK,
            targetValue = 30,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.RARE),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_30_DAY_STREAK_COINS)
            )
        ),
        AchievementDefinition(
            id = BEST_STREAK_30,
            name = "Best Streak 30",
            description = "Reach a best streak of 30 days",
            icon = "best_streak_30",
            progressSource = AchievementProgressSource.BEST_STREAK,
            targetValue = 30,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_BEST_STREAK_30_COINS)
            )
        ),
        AchievementDefinition(
            id = PET_AGE_30,
            name = "One Month Old",
            description = "Your dragon is 30 days old",
            icon = "pet_age_30",
            progressSource = AchievementProgressSource.PET_AGE_DAYS,
            targetValue = 30,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_PET_AGE_30_COINS)
            )
        ),
        AchievementDefinition(
            id = DAYS_ACTIVE_30,
            name = "30 Days Active",
            description = "Play the game on 30 different days",
            icon = "days_active_30",
            progressSource = AchievementProgressSource.DAYS_ACTIVE,
            targetValue = 30,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_DAYS_ACTIVE_30_COINS)
            )
        ),
        AchievementDefinition(
            id = LOGINS_30,
            name = "Month of Welcome",
            description = "Log in on 30 different days",
            icon = "logins_30",
            progressSource = AchievementProgressSource.DAILY_LOGINS,
            targetValue = 30,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.RARE),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_LOGINS_30_COINS)
            )
        ),
        AchievementDefinition(
            id = CRYSTAL_AURA,
            name = "Frost Aura",
            description = "Complete 100 habits to unlock the Frost Aura",
            icon = "aura_frost",
            progressSource = AchievementProgressSource.TOTAL_COMPLETIONS,
            targetValue = 100,
            rewards = listOf(
                AchievementReward.CustomizationReward(EquipableConfig.FROST_AURA, CustomizationTypes.AURA)
            )
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
        ),
        AchievementDefinition(
            id = CRYSTAL_CROWN,
            name = "Crystal Crown",
            description = "Collect 7 customization items",
            icon = "outfit_knight",
            progressSource = AchievementProgressSource.OWNED_CUSTOMIZATIONS,
            targetValue = 7,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.EPIC)
            )
        ),
        AchievementDefinition(
            id = CRYSTAL_CAVE,
            name = "Beach Background",
            description = "Reach level 25 to unlock the Beach Background",
            icon = "background_beach",
            progressSource = AchievementProgressSource.PET_LEVEL,
            targetValue = 25,
            rewards = listOf(
                AchievementReward.CustomizationReward(EquipableConfig.BACKGROUND_BEACH, CustomizationTypes.BACKGROUND)
            )
        ),
        AchievementDefinition(
            id = THREE_THOUSAND_XP,
            name = "3000 XP",
            description = "Earn 3000 XP",
            icon = "xp_3000",
            progressSource = AchievementProgressSource.TOTAL_XP,
            targetValue = 3000,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_3000_XP_COINS)
            )
        ),
        AchievementDefinition(
            id = COLLECTION_HALFWAY,
            name = "Halfway Collector",
            description = "Unlock 10 customization items",
            icon = "customization_10",
            progressSource = AchievementProgressSource.OWNED_CUSTOMIZATIONS,
            targetValue = 10,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.EPIC),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_10_CUSTOMIZATIONS_COINS)
            )
        ),
        AchievementDefinition(
            id = CHALLENGES_10,
            name = "Challenge Veteran",
            description = "Complete 10 challenges",
            icon = "challenge_10",
            progressSource = AchievementProgressSource.CHALLENGES_COMPLETED,
            targetValue = 10,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.RARE),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_CHALLENGES_10_COINS)
            )
        ),
        AchievementDefinition(
            id = CHESTS_50,
            name = "Chest Hoarder",
            description = "Open 50 chests",
            icon = "chest_50",
            progressSource = AchievementProgressSource.CHESTS_OPENED,
            targetValue = 50,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_CHESTS_50_COINS)
            )
        ),
        AchievementDefinition(
            id = EVOLUTIONS_3,
            name = "Triple Evolution",
            description = "Evolve your dragon 3 times",
            icon = "evolution_3",
            progressSource = AchievementProgressSource.EVOLUTIONS,
            targetValue = 3,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.EPIC),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_EVOLUTIONS_3_COINS)
            )
        ),
        AchievementDefinition(
            id = COINS_1000,
            name = "Coin Saver",
            description = "Hoard 1000 coins",
            icon = "coins_1000",
            progressSource = AchievementProgressSource.TOTAL_COINS,
            targetValue = 1000,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_COINS_1000_COINS)
            )
        ),
        AchievementDefinition(
            id = FREEZES_1,
            name = "Streak Freeze",
            description = "Use your first streak freeze",
            icon = "freeze_1",
            progressSource = AchievementProgressSource.FREEZES_USED,
            targetValue = 1,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_FREEZES_1_COINS)
            )
        ),
        AchievementDefinition(
            id = ACHIEVEMENTS_CLAIMED_10,
            name = "Achievement Hunter",
            description = "Claim 10 achievements",
            icon = "achievements_claimed_10",
            progressSource = AchievementProgressSource.ACHIEVEMENTS_CLAIMED,
            targetValue = 10,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.RARE),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENTS_CLAIMED_10_COINS)
            )
        ),

        // ============================
        // LATE GAME (46-70)
        // ============================
        AchievementDefinition(
            id = FIVE_THOUSAND_XP,
            name = "5000 XP",
            description = "Earn 5000 XP",
            icon = "xp_5000",
            progressSource = AchievementProgressSource.TOTAL_XP,
            targetValue = 5000,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.RARE),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_5000_XP_COINS)
            )
        ),
        AchievementDefinition(
            id = TWENTY_HABIT_BUILDER,
            name = "20 Habit Builder",
            description = "Create 20 habits",
            icon = "habit_builder_20",
            progressSource = AchievementProgressSource.HABIT_COUNT,
            targetValue = 20,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.RARE),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_20_HABIT_BUILDER_COINS)
            )
        ),
        AchievementDefinition(
            id = FIVE_HUNDRED_COMPLETIONS,
            name = "500 Completions",
            description = "Complete 500 habits",
            icon = "completions_500",
            progressSource = AchievementProgressSource.TOTAL_COMPLETIONS,
            targetValue = 500,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.RARE),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_500_COMPLETIONS_COINS)
            )
        ),
        AchievementDefinition(
            id = LEVEL_TWENTY_FIVE,
            name = "Level 25",
            description = "Reach level 25",
            icon = "level_25",
            progressSource = AchievementProgressSource.PET_LEVEL,
            targetValue = 25,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.RARE),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_LEVEL_25_COINS)
            )
        ),
        AchievementDefinition(
            id = LEVEL_FORTY,
            name = "Level 40",
            description = "Reach level 40",
            icon = "level_40",
            progressSource = AchievementProgressSource.PET_LEVEL,
            targetValue = 40,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.RARE),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_LEVEL_40_COINS)
            )
        ),
        AchievementDefinition(
            id = LEVEL_FIFTY,
            name = "Level 50",
            description = "Reach level 50",
            icon = "level_50",
            progressSource = AchievementProgressSource.PET_LEVEL,
            targetValue = 50,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.RARE),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_LEVEL_50_COINS)
            )
        ),
        AchievementDefinition(
            id = SEVEN_THOUSAND_FIVE_HUNDRED_XP,
            name = "7500 XP",
            description = "Earn 7500 XP",
            icon = "xp_7500",
            progressSource = AchievementProgressSource.TOTAL_XP,
            targetValue = 7500,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.RARE),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_7500_XP_COINS)
            )
        ),
        AchievementDefinition(
            id = TEN_THOUSAND_XP,
            name = "10000 XP",
            description = "Earn 10000 XP",
            icon = "xp_10000",
            progressSource = AchievementProgressSource.TOTAL_XP,
            targetValue = 10000,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.EPIC),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_10000_XP_COINS)
            )
        ),
        AchievementDefinition(
            id = FIFTEEN_THOUSAND_XP,
            name = "15000 XP",
            description = "Earn 15000 XP",
            icon = "xp_15000",
            progressSource = AchievementProgressSource.TOTAL_XP,
            targetValue = 15000,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.EPIC),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_15000_XP_COINS)
            )
        ),
        AchievementDefinition(
            id = SIXTY_DAY_STREAK,
            name = "60 Day Streak",
            description = "Maintain a 60-day streak",
            icon = "streak_60",
            progressSource = AchievementProgressSource.CURRENT_STREAK,
            targetValue = 60,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.EPIC),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_60_DAY_STREAK_COINS)
            )
        ),
        AchievementDefinition(
            id = BEST_STREAK_60,
            name = "Best Streak 60",
            description = "Reach a best streak of 60 days",
            icon = "best_streak_60",
            progressSource = AchievementProgressSource.BEST_STREAK,
            targetValue = 60,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_BEST_STREAK_60_COINS)
            )
        ),
        AchievementDefinition(
            id = BEST_COMBO_10,
            name = "Combo Master",
            description = "Reach a best combo of 10 hits",
            icon = "combo_10",
            progressSource = AchievementProgressSource.BEST_COMBO,
            targetValue = 10,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_BEST_COMBO_10_COINS)
            )
        ),
        AchievementDefinition(
            id = DRAGONFIRE_AURA,
            name = "Fire Aura",
            description = "Reach level 40 to unlock the Fire Aura",
            icon = "aura_fire",
            progressSource = AchievementProgressSource.PET_LEVEL,
            targetValue = 40,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.EPIC),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_10_CUSTOMIZATIONS_COINS)
            )
        ),
        AchievementDefinition(
            id = MYSTIC_CLOAK,
            name = "Ninja Outfit",
            description = "Reach level 50 to unlock the Ninja Outfit",
            icon = "outfit_ninja",
            progressSource = AchievementProgressSource.PET_LEVEL,
            targetValue = 50,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.LEGENDARY),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_LEVEL_50_COINS)
            )
        ),
        AchievementDefinition(
            id = STARLIGHT_ARMOR,
            name = "Adventure Outfit",
            description = "Reach level 40 to unlock the Adventure Outfit",
            icon = "outfit_adventure",
            progressSource = AchievementProgressSource.PET_LEVEL,
            targetValue = 40,
            rewards = listOf(
                AchievementReward.CustomizationReward(EquipableConfig.ADVENTURE_OUTFIT, CustomizationTypes.OUTFIT)
            )
        ),
        AchievementDefinition(
            id = ONE_THOUSAND_COMPLETIONS,
            name = "1000 Completions",
            description = "Complete 1000 habits",
            icon = "completions_1000",
            progressSource = AchievementProgressSource.TOTAL_COMPLETIONS,
            targetValue = 1000,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.EPIC),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_1000_COMPLETIONS_COINS)
            )
        ),
        AchievementDefinition(
            id = TWENTY_FIVE_THOUSAND_XP,
            name = "25000 XP",
            description = "Earn 25000 XP",
            icon = "xp_25000",
            progressSource = AchievementProgressSource.TOTAL_XP,
            targetValue = 25000,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.LEGENDARY),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_25000_XP_COINS)
            )
        ),
        AchievementDefinition(
            id = BEST_STREAK_100,
            name = "Best Streak 100",
            description = "Reach a best streak of 100 days",
            icon = "best_streak_100",
            progressSource = AchievementProgressSource.BEST_STREAK,
            targetValue = 100,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_BEST_STREAK_100_COINS)
            )
        ),
        AchievementDefinition(
            id = DAYS_ACTIVE_100,
            name = "100 Days Active",
            description = "Play the game on 100 different days",
            icon = "days_active_100",
            progressSource = AchievementProgressSource.DAYS_ACTIVE,
            targetValue = 100,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_DAYS_ACTIVE_100_COINS)
            )
        ),
        AchievementDefinition(
            id = CHALLENGES_50,
            name = "Challenge Champion",
            description = "Complete 50 challenges",
            icon = "challenge_50",
            progressSource = AchievementProgressSource.CHALLENGES_COMPLETED,
            targetValue = 50,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.EPIC),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_CHALLENGES_50_COINS)
            )
        ),
        AchievementDefinition(
            id = CHESTS_100,
            name = "Chest Baron",
            description = "Open 100 chests",
            icon = "chest_100",
            progressSource = AchievementProgressSource.CHESTS_OPENED,
            targetValue = 100,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_CHESTS_100_COINS)
            )
        ),
        AchievementDefinition(
            id = EVOLUTIONS_ALL,
            name = "Full Evolution",
            description = "Evolve your dragon 4 times",
            icon = "evolution_4",
            progressSource = AchievementProgressSource.EVOLUTIONS,
            targetValue = 4,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.LEGENDARY),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_EVOLUTIONS_ALL_COINS)
            )
        ),
        AchievementDefinition(
            id = COINS_10000,
            name = "Coin Baron",
            description = "Hoard 10000 coins",
            icon = "coins_10000",
            progressSource = AchievementProgressSource.TOTAL_COINS,
            targetValue = 10000,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_COINS_10000_COINS)
            )
        ),
        AchievementDefinition(
            id = FREEZES_5,
            name = "Freeze Veteran",
            description = "Use 5 streak freezes",
            icon = "freeze_5",
            progressSource = AchievementProgressSource.FREEZES_USED,
            targetValue = 5,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_FREEZES_5_COINS)
            )
        ),
        AchievementDefinition(
            id = ACHIEVEMENTS_CLAIMED_25,
            name = "Achievement Collector",
            description = "Claim 25 achievements",
            icon = "achievements_claimed_25",
            progressSource = AchievementProgressSource.ACHIEVEMENTS_CLAIMED,
            targetValue = 25,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.EPIC),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENTS_CLAIMED_25_COINS)
            )
        ),

        // ============================
        // ENDGAME (71-96)
        // ============================
        AchievementDefinition(
            id = LEVEL_SIXTY,
            name = "Level 60",
            description = "Reach level 60",
            icon = "level_60",
            progressSource = AchievementProgressSource.PET_LEVEL,
            targetValue = 60,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.LEGENDARY),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_LEVEL_60_COINS)
            )
        ),
        AchievementDefinition(
            id = LEVEL_75,
            name = "Level 75",
            description = "Reach level 75",
            icon = "level_75",
            progressSource = AchievementProgressSource.PET_LEVEL,
            targetValue = 75,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.LEGENDARY),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_LEVEL_75_COINS)
            )
        ),
        AchievementDefinition(
            id = LEVEL_100,
            name = "Level 100",
            description = "Reach level 100",
            icon = "level_100",
            progressSource = AchievementProgressSource.PET_LEVEL,
            targetValue = 100,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.LEGENDARY),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_LEVEL_100_COINS)
            )
        ),
        AchievementDefinition(
            id = XP_50000,
            name = "50000 XP",
            description = "Earn 50000 XP",
            icon = "xp_50000",
            progressSource = AchievementProgressSource.TOTAL_XP,
            targetValue = 50000,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.LEGENDARY),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_XP_50000_COINS)
            )
        ),
        AchievementDefinition(
            id = COMPLETIONS_2500,
            name = "2500 Completions",
            description = "Complete 2500 habits",
            icon = "completions_2500",
            progressSource = AchievementProgressSource.TOTAL_COMPLETIONS,
            targetValue = 2500,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.LEGENDARY),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_COMPLETIONS_2500_COINS)
            )
        ),
        AchievementDefinition(
            id = STREAK_365,
            name = "365 Day Streak",
            description = "Maintain a 365-day streak",
            icon = "streak_365",
            progressSource = AchievementProgressSource.CURRENT_STREAK,
            targetValue = 365,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.LEGENDARY),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_STREAK_365_COINS)
            )
        ),
        AchievementDefinition(
            id = PET_AGE_365,
            name = "One Year Old",
            description = "Your dragon is 365 days old",
            icon = "pet_age_365",
            progressSource = AchievementProgressSource.PET_AGE_DAYS,
            targetValue = 365,
            rewards = listOf(
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_PET_AGE_365_COINS)
            )
        ),
        AchievementDefinition(
            id = CELESTIAL_AURA,
            name = "Celestial Finale",
            description = "Reach level 60 to unlock the Celestial Aura",
            icon = "aura_celestial",
            progressSource = AchievementProgressSource.PET_LEVEL,
            targetValue = 60,
            rewards = listOf(
                AchievementReward.CustomizationReward(EquipableConfig.CELESTIAL_AURA, CustomizationTypes.AURA)
            )
        ),
        AchievementDefinition(
            id = ACHIEVEMENTS_CLAIMED_50,
            name = "Achievement Legend",
            description = "Claim 50 achievements",
            icon = "achievements_claimed_50",
            progressSource = AchievementProgressSource.ACHIEVEMENTS_CLAIMED,
            targetValue = 50,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.LEGENDARY),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENTS_CLAIMED_50_COINS)
            )
        )
    ).mapIndexed { index, achievement ->
        achievement.copy(difficultyRank = difficultyRanks[achievement.id] ?: index + 1)
    }

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
