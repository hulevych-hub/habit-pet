package com.example.mobile.domain

/**
 * Centralized configuration for EXP and level progression.
 * This is the single source of truth for all EXP-related calculations.
 */
object ExpConfig {

    // =========================
    // XP REWARDS
    // =========================

    /** XP earned from completing a checkbox habit */
    const val CHECKBOX_HABIT_XP: Long = 100

    /** Base XP earned from completing a timer habit session */
    const val TIMER_HABIT_BASE_XP: Long = 10

    /** Additional XP per minute for timer habits */
    const val TIMER_HABIT_XP_PER_MINUTE: Long = 5

    // =========================
    // COMBO / MOMENTUM
    // =========================

    /** Short window where consecutive habit completions keep momentum alive */
    const val COMBO_INACTIVITY_WINDOW_MS: Long = 2L * 60L * 60L * 1000L

    /** Small additive XP bonus per consecutive completion after the first */
    const val COMBO_BONUS_XP_PER_CONSECUTIVE_COMPLETION: Long = 5

    /** Maximum combo bonus XP awarded to a single habit completion */
    const val COMBO_MAX_BONUS_XP: Long = 20

    /** Combo milestones that are meaningful enough to record in the activity timeline */
    val COMBO_MILESTONES = listOf(3, 5, 10)

    fun isComboActive(lastHabitCompletionTimestamp: Long, now: Long): Boolean {
        return lastHabitCompletionTimestamp > 0L && now - lastHabitCompletionTimestamp <= COMBO_INACTIVITY_WINDOW_MS
    }

    fun comboBonusXp(combo: Int): Long {
        if (combo <= 1) return 0L
        return ((combo - 1) * COMBO_BONUS_XP_PER_CONSECUTIVE_COMPLETION)
            .coerceAtMost(COMBO_MAX_BONUS_XP)
    }

    fun comboMultiplier(combo: Int): Float {
        val bonus = comboBonusXp(combo)
        return 1f + (bonus.toFloat() / CHECKBOX_HABIT_XP.toFloat())
    }

    fun comboMilestoneReached(combo: Int): Boolean = COMBO_MILESTONES.contains(combo)

    // =========================
    // DAILY GOALS
    // =========================

    /** XP target for the daily goal; three checkbox habits reach this goal */
    const val DAILY_XP_GOAL: Long = 300

    /** Bonus XP awarded when the daily goal is completed */
    const val DAILY_GOAL_BONUS_XP: Long = 25

    // =========================
    // LEVEL PROGRESSION
    // =========================

    /** XP required to reach level 1 from level 0 */
    const val BASE_XP_FOR_LEVEL_1: Long = 100

    /** Additional XP required per level (linear growth) */
    const val XP_PER_LEVEL_INCREMENT: Long = 50

    /**
     * Calculates XP required to reach a specific level from the previous level.
     * Level 1: 100 XP
     * Level 2: 150 XP
     * Level 3: 200 XP
     * ...
     * Level N: BASE_XP_FOR_LEVEL_1 + (N-1) * XP_PER_LEVEL_INCREMENT
     */
    fun xpRequiredForLevel(level: Int): Long {
        require(level >= 1) { "Level must be >= 1" }
        return BASE_XP_FOR_LEVEL_1 + (level - 1) * XP_PER_LEVEL_INCREMENT
    }

    /**
     * Calculates total XP required to reach a specific level from level 0.
     * This is the sum of XP required for all levels up to and including the target level.
     */
    fun totalXpRequiredForLevel(level: Int): Long {
        if (level <= 0) return 0
        var total = 0L
        for (i in 1..level) {
            total += xpRequiredForLevel(i)
        }
        return total
    }

    /**
     * Calculates current level from total XP.
     * Returns the highest level fully completed.
     */
    fun calculateLevelFromXp(totalXp: Long): Int {
        var level = 0
        var remaining = totalXp

        while (true) {
            val nextLevel = level + 1
            val required = xpRequiredForLevel(nextLevel)
            if (remaining >= required) {
                remaining -= required
                level = nextLevel
            } else {
                break
            }
        }
        return level
    }

    /**
     * Calculates XP progress within current level (0 to XP required for next level).
     */
    fun xpProgressInCurrentLevel(totalXp: Long): Long {
        val level = calculateLevelFromXp(totalXp)
        val totalForCurrentLevel = totalXpRequiredForLevel(level)
        return totalXp - totalForCurrentLevel
    }

    /**
     * Calculates XP required to reach next level from current total XP.
     */
    fun xpRequiredForNextLevel(totalXp: Long): Long {
        val currentLevel = calculateLevelFromXp(totalXp)
        val nextLevel = currentLevel + 1
        val totalForNextLevel = totalXpRequiredForLevel(nextLevel)
        return totalForNextLevel - totalXp
    }

    // =========================
    // EVOLUTION STAGES
    // =========================

    /** Evolution stage thresholds based on total XP */
    private val EVOLUTION_THRESHOLDS = longArrayOf(
        0,      // Stage 0: Egg (0-499 XP)
        500,    // Stage 1: Hatchling (500-1499 XP)
        1500,   // Stage 2: Young Dragon (1500-2999 XP)
        3000,   // Stage 3: Adult Dragon (3000-5999 XP)
        6000    // Stage 4: Ancient Dragon (6000+ XP)
    )

    /** Names for each evolution stage */
    val EVOLUTION_STAGE_NAMES = arrayOf(
        "Egg",
        "Hatchling",
        "Young Dragon",
        "Adult Dragon",
        "Ancient Dragon"
    )

    /**
     * Calculates evolution stage from total XP.
     * This is the SINGLE SOURCE OF TRUTH for evolution stage calculation.
     */
    fun calculateEvolutionStageFromXp(totalXp: Long): Int {
        var stage = 0
        for (i in EVOLUTION_THRESHOLDS.indices) {
            if (totalXp >= EVOLUTION_THRESHOLDS[i]) {
                stage = i
            } else {
                break
            }
        }
        return stage.coerceAtMost(EVOLUTION_THRESHOLDS.lastIndex)
    }

    /**
     * Gets the XP threshold for a specific evolution stage.
     */
    fun xpThresholdForStage(stage: Int): Long {
        return if (stage in EVOLUTION_THRESHOLDS.indices) {
            EVOLUTION_THRESHOLDS[stage]
        } else {
            EVOLUTION_THRESHOLDS.last()
        }
    }

    /**
     * Gets the name of an evolution stage.
     */
    fun evolutionStageName(stage: Int): String {
        return if (stage in EVOLUTION_STAGE_NAMES.indices) {
            EVOLUTION_STAGE_NAMES[stage]
        } else {
            "Unknown"
        }
    }

    // =========================
    // LEVEL-UP REWARDS
    // =========================

    /** Base coins awarded on level up (level * LEVEL_UP_COIN_MULTIPLIER) */
    const val LEVEL_UP_COIN_MULTIPLIER: Int = 10

    /** Bonus coins from level-up chest */
    const val LEVEL_UP_CHEST_BONUS_COINS: Int = 20

    /**
     * Calculates coins awarded for leveling up (excluding chest bonus).
     */
    fun levelUpCoins(newLevel: Int): Int {
        return newLevel * LEVEL_UP_COIN_MULTIPLIER
    }
}
