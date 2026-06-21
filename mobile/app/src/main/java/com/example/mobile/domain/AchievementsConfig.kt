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
    const val EIGHT_CUSTOMIZATIONS = "eight_customizations"
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
    const val TEN_CUSTOMIZATIONS = "ten_customizations"
    const val ELEVEN_CUSTOMIZATIONS = "eleven_customizations"
    const val FLOATING_ISLANDS = "floating_islands"
    const val ONE_THOUSAND_COMPLETIONS = "one_thousand_completions"
    const val TWENTY_FIVE_THOUSAND_XP = "twenty_five_thousand_xp"
    const val ONE_HUNDRED_DAY_STREAK = "one_hundred_day_streak"
    const val TWELVE_CUSTOMIZATIONS = "twelve_customizations"
    const val STARLIGHT_ARMOR = "starlight_armor"
    const val CELESTIAL_REALM = "celestial_realm"
    const val CELESTIAL_AURA = "celestial_aura"
    const val LEVEL_SIXTY = "level_sixty"

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
        FIRST_HABIT to 1,
        FIRST_CUSTOMIZATION to 2,
        FIRST_AURA_GLOW to 3,
        THREE_HABIT_BUILDER to 4,
        FIVE_HABIT_BUILDER to 5,
        THREE_CUSTOMIZATIONS to 6,
        TWENTY_FIVE_COMPLETIONS to 7,
        COZY_OUTFIT to 8,
        LEVEL_FIVE to 9,
        SEVEN_DAY_STREAK to 10,
        FOURTEEN_DAY_STREAK to 11,
        ONE_THOUSAND_XP to 12,
        TWO_THOUSAND_FIVE_HUNDRED_XP to 13,
        FOREST_BACKGROUND to 14,
        TEN_HABIT_BUILDER to 15,
        FIFTEEN_HABIT_BUILDER to 16,
        ONE_HUNDRED_COMPLETIONS to 17,
        CRYSTAL_AURA to 18,
        TWO_HUNDRED_FIFTY_COMPLETIONS to 19,
        LEVEL_TEN to 20,
        LEVEL_FIFTEEN to 21,
        THREE_THOUSAND_XP to 22,
        THIRTY_DAY_STREAK to 23,
        CUSTOMIZATION_COLLECTOR to 24,
        CRYSTAL_CROWN to 25,
        EIGHT_CUSTOMIZATIONS to 26,
        FIVE_THOUSAND_XP to 27,
        TWENTY_HABIT_BUILDER to 28,
        FIVE_HUNDRED_COMPLETIONS to 29,
        CRYSTAL_CAVE to 30,
        LEVEL_TWENTY_FIVE to 31,
        LEVEL_FORTY to 32,
        STARLIGHT_ARMOR to 33,
        LEVEL_FIFTY to 34,
        SEVEN_THOUSAND_FIVE_HUNDRED_XP to 35,
        TEN_THOUSAND_XP to 36,
        FIFTEEN_THOUSAND_XP to 37,
        SIXTY_DAY_STREAK to 38,
        DRAGONFIRE_AURA to 39,
        TEN_CUSTOMIZATIONS to 40,
        MYSTIC_CLOAK to 41,
        ELEVEN_CUSTOMIZATIONS to 42,
        FLOATING_ISLANDS to 43,
        TWELVE_CUSTOMIZATIONS to 44,
        CELESTIAL_REALM to 45,
        CELESTIAL_AURA to 46,
        ONE_THOUSAND_COMPLETIONS to 47,
        TWENTY_FIVE_THOUSAND_XP to 48,
        ONE_HUNDRED_DAY_STREAK to 49,
        LEVEL_SIXTY to 50
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
            id = FIRST_CUSTOMIZATION,
            name = "First Customization",
            description = "Unlock your first customization item",
            icon = "customization_first",
            progressSource = AchievementProgressSource.OWNED_CUSTOMIZATIONS,
            targetValue = 1,
            rewards = listOf(AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_FIRST_CUSTOMIZATION_COINS))
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
            id = FOREST_BACKGROUND,
            name = "Forest Background",
            description = "Earn 2500 XP to unlock the Forest Background",
            icon = "background_forest",
            progressSource = AchievementProgressSource.TOTAL_XP,
            targetValue = 2500,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CustomizationReward(EquipableConfig.BACKGROUND_FOREST, CustomizationTypes.BACKGROUND)
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
                AchievementReward.ChestReward(ChestType.NORMAL),
                AchievementReward.CustomizationReward(EquipableConfig.BACKGROUND_BEACH, CustomizationTypes.BACKGROUND)
            )
        ),
        AchievementDefinition(
            id = EIGHT_CUSTOMIZATIONS,
            name = "8 Customizations",
            description = "Unlock 8 customization items",
            icon = "customization_8",
            progressSource = AchievementProgressSource.OWNED_CUSTOMIZATIONS,
            targetValue = 8,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.RARE),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_8_CUSTOMIZATIONS_COINS)
            )
        ),
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
            id = DRAGONFIRE_AURA,
            name = "10 Customization Spark",
            description = "Unlock 10 customization items",
            icon = "aura_fire",
            progressSource = AchievementProgressSource.OWNED_CUSTOMIZATIONS,
            targetValue = 10,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.RARE)
            )
        ),
        AchievementDefinition(
            id = MYSTIC_CLOAK,
            name = "11 Customization Hoard",
            description = "Unlock 11 customization items",
            icon = "outfit_ninja",
            progressSource = AchievementProgressSource.OWNED_CUSTOMIZATIONS,
            targetValue = 11,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.LEGENDARY)
            )
        ),
        AchievementDefinition(
            id = TEN_CUSTOMIZATIONS,
            name = "10 Customizations",
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
            id = ELEVEN_CUSTOMIZATIONS,
            name = "11 Customizations",
            description = "Unlock 11 customization items",
            icon = "customization_11",
            progressSource = AchievementProgressSource.OWNED_CUSTOMIZATIONS,
            targetValue = 11,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.EPIC),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_11_CUSTOMIZATIONS_COINS)
            )
        ),
        AchievementDefinition(
            id = FLOATING_ISLANDS,
            name = "19 Customization Milestone",
            description = "Unlock all 19 customization items",
            icon = "background_mountains",
            progressSource = AchievementProgressSource.OWNED_CUSTOMIZATIONS,
            targetValue = 19,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.EPIC)
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
            id = ONE_HUNDRED_DAY_STREAK,
            name = "100 Day Streak",
            description = "Maintain a 100-day streak",
            icon = "streak_100",
            progressSource = AchievementProgressSource.CURRENT_STREAK,
            targetValue = 100,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.LEGENDARY),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_100_DAY_STREAK_COINS)
            )
        ),
        AchievementDefinition(
            id = TWELVE_CUSTOMIZATIONS,
            name = "19 Customizations",
            description = "Unlock all 19 customization items",
            icon = "customization_12",
            progressSource = AchievementProgressSource.OWNED_CUSTOMIZATIONS,
            targetValue = 19,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.LEGENDARY),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_19_CUSTOMIZATIONS_COINS)
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
                AchievementReward.ChestReward(ChestType.RARE),
                AchievementReward.CustomizationReward(EquipableConfig.ADVENTURE_OUTFIT, CustomizationTypes.OUTFIT)
            )
        ),
        AchievementDefinition(
            id = CELESTIAL_REALM,
            name = "Celestial Realm",
            description = "Complete the full 19-item customization collection",
            icon = "background_night_sky",
            progressSource = AchievementProgressSource.OWNED_CUSTOMIZATIONS,
            targetValue = 19,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.LEGENDARY)
            )
        ),
        AchievementDefinition(
            id = CELESTIAL_AURA,
            name = "Celestial Finale",
            description = "Complete the full 19-item customization collection",
            icon = "aura_sakura",
            progressSource = AchievementProgressSource.OWNED_CUSTOMIZATIONS,
            targetValue = 19,
            rewards = listOf(
                AchievementReward.ChestReward(ChestType.LEGENDARY),
                AchievementReward.CoinReward(EconomyConfig.ACHIEVEMENT_19_CUSTOMIZATIONS_COINS)
            )
        ),
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
