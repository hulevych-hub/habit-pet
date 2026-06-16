package com.example.mobile.domain

import com.example.mobile.data.local.entities.Rarity

/**
 * Centralized configuration for the game economy (coins).
 * This is the single source of truth for all coin-related values.
 */
object EconomyConfig {

    // =========================
    // COIN REWARDS - HABITS
    // =========================

    /** Coins earned from completing a checkbox habit */
    const val CHECKBOX_HABIT_COINS: Int = 10

    /** Base coins earned from completing a timer habit session */
    const val TIMER_HABIT_BASE_COINS: Int = 5

    /** Additional coins per minute for timer habits */
    const val TIMER_HABIT_COINS_PER_MINUTE: Int = 2

    // =========================
    // COIN REWARDS - STREAKS
    // =========================

    /** Coins awarded for each 7-day streak milestone */
    const val STREAK_MILESTONE_COINS: Int = 50

    /** Interval (in days) for streak milestone rewards */
    const val STREAK_MILESTONE_INTERVAL: Int = 7

    // =========================
    // COIN REWARDS - LEVEL UP
    // =========================

    /** Base coin multiplier for level up (level * multiplier) - defined in ExpConfig */
    // See ExpConfig.LEVEL_UP_COIN_MULTIPLIER

    /** Bonus coins from level-up chest */
    const val LEVEL_UP_CHEST_BONUS_COINS: Int = 20

    // =========================
    // COIN REWARDS - SURPRISE BONUSES
    // =========================

    /** Minimum completions before another surprise reward can roll */
    const val SURPRISE_MIN_COMPLETIONS_BEFORE_CHANCE: Int = 3

    /** Chance to show a surprise chest after the cooldown is satisfied */
    const val HABIT_COMPLETION_CHEST_CHANCE: Double = 0.04 // 4%

    /** Bonus XP awarded by a surprise reward */
    const val SURPRISE_BONUS_XP: Long = 5L

    /** Bonus coins awarded by a surprise reward */
    const val SURPRISE_BONUS_COINS: Int = 3

    /** Rare chest probability inside surprise chest rewards */
    const val SURPRISE_RARE_CHEST_PROBABILITY: Double = 0.85

    /** Epic chest probability inside surprise chest rewards */
    const val SURPRISE_EPIC_CHEST_PROBABILITY: Double = 0.13

    // =========================
    // COIN REWARDS - ACHIEVEMENTS
    // =========================

    /** Achievement: First Habit */
    const val ACHIEVEMENT_FIRST_HABIT_COINS: Int = 50

    /** Achievement: First Customization */
    const val ACHIEVEMENT_FIRST_CUSTOMIZATION_COINS: Int = 60

    /** Achievement: 3 Habit Builder */
    const val ACHIEVEMENT_3_HABIT_BUILDER_COINS: Int = 90

    /** Achievement: 5 Habit Builder */
    const val ACHIEVEMENT_5_HABIT_BUILDER_COINS: Int = 120

    /** Achievement: 3 Customizations */
    const val ACHIEVEMENT_3_CUSTOMIZATIONS_COINS: Int = 150

    /** Achievement: 25 Completions */
    const val ACHIEVEMENT_25_COMPLETIONS_COINS: Int = 100

    /** Achievement: Level 5 */
    const val ACHIEVEMENT_LEVEL_5_COINS: Int = 80

    /** Achievement: 7 Day Streak */
    const val ACHIEVEMENT_7_DAY_STREAK_COINS: Int = 100

    /** Achievement: 14 Day Streak */
    const val ACHIEVEMENT_14_DAY_STREAK_COINS: Int = 150

    /** Achievement: 2500 XP */
    const val ACHIEVEMENT_2500_XP_COINS: Int = 200

    /** Achievement: 100 Completions */
    const val ACHIEVEMENT_100_COMPLETIONS_COINS: Int = 200

    /** Achievement: 1000 XP */
    const val ACHIEVEMENT_1000_XP_COINS: Int = 150

    /** Achievement: 10 Habit Builder */
    const val ACHIEVEMENT_10_HABIT_BUILDER_COINS: Int = 250

    /** Achievement: 15 Habit Builder */
    const val ACHIEVEMENT_15_HABIT_BUILDER_COINS: Int = 350

    /** Achievement: 250 Completions */
    const val ACHIEVEMENT_250_COMPLETIONS_COINS: Int = 300

    /** Achievement: Level 10 */
    const val ACHIEVEMENT_LEVEL_10_COINS: Int = 300

    /** Achievement: Level 15 */
    const val ACHIEVEMENT_LEVEL_15_COINS: Int = 350

    /** Achievement: 3000 XP */
    const val ACHIEVEMENT_3000_XP_COINS: Int = 300

    /** Achievement: 30 Day Streak */
    const val ACHIEVEMENT_30_DAY_STREAK_COINS: Int = 400

    /** Achievement: Customization Collector bonus coins */
    const val ACHIEVEMENT_CUSTOMIZATION_COLLECTOR_COINS: Int = 50

    /** Achievement: 8 Customizations */
    const val ACHIEVEMENT_8_CUSTOMIZATIONS_COINS: Int = 200

    /** Achievement: 5000 XP */
    const val ACHIEVEMENT_5000_XP_COINS: Int = 350

    /** Achievement: 20 Habit Builder */
    const val ACHIEVEMENT_20_HABIT_BUILDER_COINS: Int = 500

    /** Achievement: 500 Completions */
    const val ACHIEVEMENT_500_COMPLETIONS_COINS: Int = 500

    /** Achievement: Level 25 */
    const val ACHIEVEMENT_LEVEL_25_COINS: Int = 500

    /** Achievement: Level 40 */
    const val ACHIEVEMENT_LEVEL_40_COINS: Int = 800

    /** Achievement: Level 50 */
    const val ACHIEVEMENT_LEVEL_50_COINS: Int = 700

    /** Achievement: 7500 XP */
    const val ACHIEVEMENT_7500_XP_COINS: Int = 350

    /** Achievement: 10000 XP */
    const val ACHIEVEMENT_10000_XP_COINS: Int = 450

    /** Achievement: 15000 XP */
    const val ACHIEVEMENT_15000_XP_COINS: Int = 600

    /** Achievement: 60 Day Streak */
    const val ACHIEVEMENT_60_DAY_STREAK_COINS: Int = 600

    /** Achievement: 10 Customizations */
    const val ACHIEVEMENT_10_CUSTOMIZATIONS_COINS: Int = 300

    /** Achievement: 11 Customizations */
    const val ACHIEVEMENT_11_CUSTOMIZATIONS_COINS: Int = 250

    /** Achievement: 1000 Completions */
    const val ACHIEVEMENT_1000_COMPLETIONS_COINS: Int = 800

    /** Achievement: 25000 XP */
    const val ACHIEVEMENT_25000_XP_COINS: Int = 700

    /** Achievement: 100 Day Streak */
    const val ACHIEVEMENT_100_DAY_STREAK_COINS: Int = 800

    /** Achievement: final customization collection */
    const val ACHIEVEMENT_12_CUSTOMIZATIONS_COINS: Int = 200

    /** Achievement: Level 60 */
    const val ACHIEVEMENT_LEVEL_60_COINS: Int = 900

    // =========================
    // CHEST REWARDS - COIN RANGES
    // =========================

    /** Normal chest coin range */
    val NORMAL_CHEST_COIN_RANGE = 5..15

    /** Rare chest coin range */
    val RARE_CHEST_COIN_RANGE = 15..35

    /** Epic chest coin range */
    val EPIC_CHEST_COIN_RANGE = 35..75

    /** Legendary chest coin range */
    val LEGENDARY_CHEST_COIN_RANGE = 75..150

    // =========================
    // CHEST REWARDS - EXP RANGES
    // =========================

    /** Normal chest EXP range */
    val NORMAL_CHEST_EXP_RANGE = 0..0

    /** Rare chest EXP range */
    val RARE_CHEST_EXP_RANGE = 8..20

    /** Epic chest EXP range */
    val EPIC_CHEST_EXP_RANGE = 20..50

    /** Legendary chest EXP range */
    val LEGENDARY_CHEST_EXP_RANGE = 50..100

    // =========================
    // CHEST REWARDS - CUSTOMIZATION DROP CHANCES
    // =========================

    /** Rare chest customization drop chance */
    const val RARE_CHEST_CUSTOMIZATION_DROP_CHANCE: Double = 0.08  // 8%

    /** Epic chest customization drop chance */
    const val EPIC_CHEST_CUSTOMIZATION_DROP_CHANCE: Double = 0.18  // 18%

    /** Legendary chest customization drop chance */
    const val LEGENDARY_CHEST_CUSTOMIZATION_DROP_CHANCE: Double = 0.35  // 35%

    // =========================
    // CHEST TYPE PROBABILITIES
    // =========================

    /** Chest type distribution for level-up and achievement-configured chests */
    const val CHEST_NORMAL_PROBABILITY: Double = 0.65   // 65%
    const val CHEST_RARE_PROBABILITY: Double = 0.25     // 25%
    const val CHEST_EPIC_PROBABILITY: Double = 0.08     // 8%
    const val CHEST_LEGENDARY_PROBABILITY: Double = 0.02  // 2%

    // =========================
    // CUSTOMIZATION PRICING
    // =========================

    /** Base price multiplier for customization rarity */
    const val CUSTOMIZATION_BASE_PRICE: Int = 100

    /** Price multipliers by rarity */
    val RARITY_PRICE_MULTIPLIER = mapOf(
        Rarity.NORMAL to 1.2,
        Rarity.RARE to 4.0,
        Rarity.EPIC to 10.0,
        Rarity.LEGENDARY to 30.0
    )

    /**
     * Calculates the price for a customization item based on its rarity.
     */
    fun customizationPrice(rarity: Rarity): Int {
        val multiplier = RARITY_PRICE_MULTIPLIER[rarity] ?: 1.0
        return (CUSTOMIZATION_BASE_PRICE * multiplier).toInt()
    }

    fun shouldTriggerHabitCompletionChest(): Boolean {
        return Math.random() < HABIT_COMPLETION_CHEST_CHANCE
    }

    fun getRandomSurpriseChestType(): ChestType {
        val roll = Math.random()
        return when {
            roll < SURPRISE_RARE_CHEST_PROBABILITY -> ChestType.RARE
            roll < SURPRISE_RARE_CHEST_PROBABILITY + SURPRISE_EPIC_CHEST_PROBABILITY -> ChestType.EPIC
            else -> ChestType.LEGENDARY
        }
    }

    // =========================
    // ECONOMY BALANCE TARGETS
    // =========================

    /** Target coins earned per day for an active player completing ~3 habits plus recurring chest rewards */
    const val TARGET_DAILY_COINS: Int = 75

    /** Target coins needed to buy one Normal customization item */
    const val TARGET_NORMAL_CUSTOMIZATION_COST: Int = 120

    /** Target coins needed to buy one Rare customization item */
    const val TARGET_RARE_CUSTOMIZATION_COST: Int = 400

    /** Target coins needed to buy one Epic customization item */
    const val TARGET_EPIC_CUSTOMIZATION_COST: Int = 1000

    /** Target coins needed to buy one Legendary customization item */
    const val TARGET_LEGENDARY_CUSTOMIZATION_COST: Int = 3000

    /** Approximate days to save for a Normal customization item (no spending) */
    const val DAYS_FOR_NORMAL_CUSTOMIZATION: Int = TARGET_NORMAL_CUSTOMIZATION_COST / TARGET_DAILY_COINS

    /** Approximate days to save for a Rare customization item (no spending) */
    const val DAYS_FOR_RARE_CUSTOMIZATION: Int = TARGET_RARE_CUSTOMIZATION_COST / TARGET_DAILY_COINS

    /** Approximate days to save for an Epic customization item (no spending) */
    const val DAYS_FOR_EPIC_CUSTOMIZATION: Int = TARGET_EPIC_CUSTOMIZATION_COST / TARGET_DAILY_COINS

    /** Approximate days to save for a Legendary customization item (no spending) */
    const val DAYS_FOR_LEGENDARY_CUSTOMIZATION: Int = TARGET_LEGENDARY_CUSTOMIZATION_COST / TARGET_DAILY_COINS
}
