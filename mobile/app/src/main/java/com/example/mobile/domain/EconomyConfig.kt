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
    // COIN REWARDS - DAILY GOALS
    // =========================

    /** Coins awarded when the daily XP goal is completed */
    const val DAILY_GOAL_COIN_BONUS: Int = 25

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

    /** Low-probability surprise bonus chance after a successful habit completion */
    const val SURPRISE_REWARD_CHANCE: Double = 0.08 // 8%

    /** Minimum successful habit completions required before another surprise can trigger */
    const val SURPRISE_MIN_COMPLETIONS_BETWEEN: Int = 3

    /** Bonus XP awarded by a surprise reward */
    const val SURPRISE_BONUS_XP: Long = 25L

    /** Bonus coins awarded by a surprise reward */
    const val SURPRISE_BONUS_COINS: Int = 15

    /** Rare chest probability inside surprise chest rewards */
    const val SURPRISE_RARE_CHEST_PROBABILITY: Double = 0.80

    /** Epic chest probability inside surprise chest rewards */
    const val SURPRISE_EPIC_CHEST_PROBABILITY: Double = 0.18

    // =========================
    // COIN REWARDS - ACHIEVEMENTS
    // =========================

    /** Achievement: First Habit */
    const val ACHIEVEMENT_FIRST_HABIT_COINS: Int = 50

    /** Achievement: 3 Habit Builder */
    const val ACHIEVEMENT_3_HABIT_BUILDER_COINS: Int = 100

    /** Achievement: 7 Day Streak */
    const val ACHIEVEMENT_7_DAY_STREAK_COINS: Int = 100

    /** Achievement: 30 Day Streak */
    const val ACHIEVEMENT_30_DAY_STREAK_COINS: Int = 250

    /** Achievement: 100 Completions */
    const val ACHIEVEMENT_100_COMPLETIONS_COINS: Int = 200

    /** Achievement: 1000 XP */
    const val ACHIEVEMENT_1000_XP_COINS: Int = 150

    /** Achievement: Level 10 */
    const val ACHIEVEMENT_LEVEL_10_COINS: Int = 300

    /** Achievement: Level 25 */
    const val ACHIEVEMENT_LEVEL_25_COINS: Int = 500

    /** Achievement: First Customization */
    const val ACHIEVEMENT_FIRST_CUSTOMIZATION_COINS: Int = 75

    /** Achievement: Customization Collector bonus coins */
    const val ACHIEVEMENT_CUSTOMIZATION_COLLECTOR_COINS: Int = 50

    // =========================
    // CHEST REWARDS - COIN RANGES
    // =========================

    /** Normal chest coin range */
    val NORMAL_CHEST_COIN_RANGE = 10..30

    /** Rare chest coin range */
    val RARE_CHEST_COIN_RANGE = 30..80

    /** Epic chest coin range */
    val EPIC_CHEST_COIN_RANGE = 80..180

    /** Legendary chest coin range */
    val LEGENDARY_CHEST_COIN_RANGE = 180..400

    // =========================
    // CHEST REWARDS - EXP RANGES
    // =========================

    /** Normal chest EXP range */
    val NORMAL_CHEST_EXP_RANGE = 0..0

    /** Rare chest EXP range */
    val RARE_CHEST_EXP_RANGE = 50..150

    /** Epic chest EXP range */
    val EPIC_CHEST_EXP_RANGE = 150..350

    /** Legendary chest EXP range */
    val LEGENDARY_CHEST_EXP_RANGE = 350..800

    // =========================
    // CHEST REWARDS - CUSTOMIZATION DROP CHANCES
    // =========================

    /** Rare chest customization drop chance */
    const val RARE_CHEST_CUSTOMIZATION_DROP_CHANCE: Double = 0.15  // 15%

    /** Epic chest customization drop chance */
    const val EPIC_CHEST_CUSTOMIZATION_DROP_CHANCE: Double = 0.30  // 30%

    /** Legendary chest customization drop chance */
    const val LEGENDARY_CHEST_CUSTOMIZATION_DROP_CHANCE: Double = 0.50  // 50%

    // =========================
    // CHEST TYPE PROBABILITIES
    // =========================

    /** Chest type distribution for level-up and streak chests */
    const val CHEST_NORMAL_PROBABILITY: Double = 0.55   // 55%
    const val CHEST_RARE_PROBABILITY: Double = 0.30     // 30%
    const val CHEST_EPIC_PROBABILITY: Double = 0.12     // 12%
    const val CHEST_LEGENDARY_PROBABILITY: Double = 0.03  // 3%

    // =========================
    // CUSTOMIZATION PRICING
    // =========================

    /** Base price multiplier for customization rarity */
    const val CUSTOMIZATION_BASE_PRICE: Int = 100

    /** Price multipliers by rarity */
    val RARITY_PRICE_MULTIPLIER = mapOf(
        Rarity.NORMAL to 1.0,
        Rarity.RARE to 3.0,
        Rarity.EPIC to 8.0,
        Rarity.LEGENDARY to 20.0
    )

    /**
     * Calculates the price for a customization item based on its rarity.
     */
    fun customizationPrice(rarity: Rarity): Int {
        val multiplier = RARITY_PRICE_MULTIPLIER[rarity] ?: 1.0
        return (CUSTOMIZATION_BASE_PRICE * multiplier).toInt()
    }

    fun shouldTriggerSurpriseReward(completionsSinceLastSurprise: Int): Boolean {
        if (completionsSinceLastSurprise < SURPRISE_MIN_COMPLETIONS_BETWEEN) return false
        return Math.random() < SURPRISE_REWARD_CHANCE
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
    const val TARGET_DAILY_COINS: Int = 100

    /** Target coins needed to buy one Normal customization item */
    const val TARGET_NORMAL_CUSTOMIZATION_COST: Int = 100

    /** Target coins needed to buy one Rare customization item */
    const val TARGET_RARE_CUSTOMIZATION_COST: Int = 300

    /** Target coins needed to buy one Epic customization item */
    const val TARGET_EPIC_CUSTOMIZATION_COST: Int = 800

    /** Target coins needed to buy one Legendary customization item */
    const val TARGET_LEGENDARY_CUSTOMIZATION_COST: Int = 2000

    /** Approximate days to save for a Normal customization item (no spending) */
    const val DAYS_FOR_NORMAL_CUSTOMIZATION: Int = TARGET_NORMAL_CUSTOMIZATION_COST / TARGET_DAILY_COINS

    /** Approximate days to save for a Rare customization item (no spending) */
    const val DAYS_FOR_RARE_CUSTOMIZATION: Int = TARGET_RARE_CUSTOMIZATION_COST / TARGET_DAILY_COINS

    /** Approximate days to save for an Epic customization item (no spending) */
    const val DAYS_FOR_EPIC_CUSTOMIZATION: Int = TARGET_EPIC_CUSTOMIZATION_COST / TARGET_DAILY_COINS

    /** Approximate days to save for a Legendary customization item (no spending) */
    const val DAYS_FOR_LEGENDARY_CUSTOMIZATION: Int = TARGET_LEGENDARY_CUSTOMIZATION_COST / TARGET_DAILY_COINS
}
