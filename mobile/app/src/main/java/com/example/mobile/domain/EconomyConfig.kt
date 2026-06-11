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
    // COIN REWARDS - ACHIEVEMENTS
    // =========================

    /** Achievement: First Habit */
    const val ACHIEVEMENT_FIRST_HABIT_COINS: Int = 50

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
    // CHEST REWARDS - ACCESSORY DROP CHANCES
    // =========================

    /** Rare chest accessory drop chance */
    const val RARE_CHEST_ACCESSORY_DROP_CHANCE: Double = 0.15  // 15%

    /** Epic chest accessory drop chance */
    const val EPIC_CHEST_ACCESSORY_DROP_CHANCE: Double = 0.30  // 30%

    /** Legendary chest accessory drop chance */
    const val LEGENDARY_CHEST_ACCESSORY_DROP_CHANCE: Double = 0.50  // 50%

    // =========================
    // CHEST TYPE PROBABILITIES
    // =========================

    /** Chest type distribution for level-up and streak chests */
    const val CHEST_NORMAL_PROBABILITY: Double = 0.55   // 55%
    const val CHEST_RARE_PROBABILITY: Double = 0.30     // 30%
    const val CHEST_EPIC_PROBABILITY: Double = 0.12     // 12%
    const val CHEST_LEGENDARY_PROBABILITY: Double = 0.03  // 3%

    // =========================
    // ACCESSORY PRICING
    // =========================

    /** Base price multiplier for accessory rarity */
    const val ACCESSORY_BASE_PRICE: Int = 100

    /** Price multipliers by rarity */
    val RARITY_PRICE_MULTIPLIER = mapOf(
        Rarity.NORMAL to 1.0,
        Rarity.RARE to 3.0,
        Rarity.EPIC to 8.0,
        Rarity.LEGENDARY to 20.0
    )

    /**
     * Calculates the price for an accessory based on its rarity.
     */
    fun accessoryPrice(rarity: Rarity): Int {
        val multiplier = RARITY_PRICE_MULTIPLIER[rarity] ?: 1.0
        return (ACCESSORY_BASE_PRICE * multiplier).toInt()
    }

    // =========================
    // ECONOMY BALANCE TARGETS
    // =========================

    /** Target coins earned per day (active player completing ~3 habits) */
    const val TARGET_DAILY_COINS: Int = 100

    /** Target coins needed to buy one Normal accessory */
    const val TARGET_NORMAL_ACCESSORY_COST: Int = 100

    /** Target coins needed to buy one Rare accessory */
    const val TARGET_RARE_ACCESSORY_COST: Int = 300

    /** Target coins needed to buy one Epic accessory */
    const val TARGET_EPIC_ACCESSORY_COST: Int = 800

    /** Target coins needed to buy one Legendary accessory */
    const val TARGET_LEGENDARY_ACCESSORY_COST: Int = 2000

    /** Approximate days to save for a Normal accessory (no spending) */
    const val DAYS_FOR_NORMAL_ACCESSORY: Int = TARGET_NORMAL_ACCESSORY_COST / TARGET_DAILY_COINS

    /** Approximate days to save for a Rare accessory (no spending) */
    const val DAYS_FOR_RARE_ACCESSORY: Int = TARGET_RARE_ACCESSORY_COST / TARGET_DAILY_COINS

    /** Approximate days to save for an Epic accessory (no spending) */
    const val DAYS_FOR_EPIC_ACCESSORY: Int = TARGET_EPIC_ACCESSORY_COST / TARGET_DAILY_COINS

    /** Approximate days to save for a Legendary accessory (no spending) */
    const val DAYS_FOR_LEGENDARY_ACCESSORY: Int = TARGET_LEGENDARY_ACCESSORY_COST / TARGET_DAILY_COINS
}
