package com.example.mobile.domain

import com.example.mobile.data.local.entities.GameEventEntity
import java.util.Locale

object GameEventFactory {

    fun habitCompleted(
        habitName: String,
        xpEarned: Long,
        coinsEarned: Int,
        combo: Int = 0,
        comboBonusXp: Long = 0L,
        comboMultiplier: Float = 1f
    ): GameEventEntity {
        val safeHabitName = habitName.ifBlank { "Habit" }
        val comboText = if (combo > 1) {
            " Momentum combo x${formatMultiplier(comboMultiplier)} added +$comboBonusXp XP."
        } else {
            ""
        }

        return GameEventEntity(
            type = GameEventType.HABIT_COMPLETED.name,
            title = "$safeHabitName completed",
            description = "You completed $safeHabitName. Your dragon gained ${xpEarned} XP and $coinsEarned coins.$comboText",
            icon = "habit_completed",
            rarity = if (combo > 1) GameEventRarity.RARE.name else GameEventRarity.COMMON.name,
            payload = payloadOf(
                "habit" to safeHabitName,
                "xp" to xpEarned.toInt(),
                "coins" to coinsEarned,
                "combo" to combo,
                "comboBonusXp" to comboBonusXp,
                "comboMultiplier" to comboMultiplier
            )
        )
    }

    fun achievementUnlocked(achievementName: String): GameEventEntity {
        val safeAchievementName = achievementName.ifBlank { "Achievement" }
        return GameEventEntity(
            type = GameEventType.ACHIEVEMENT_UNLOCKED.name,
            title = "Achievement unlocked",
            description = "$safeAchievementName unlocked. Your dragon is proud of this real-life win.",
            icon = "achievement_unlocked",
            rarity = GameEventRarity.RARE.name,
            payload = payloadOf("achievement" to safeAchievementName)
        )
    }

    fun levelUp(level: Int, coins: Int): GameEventEntity {
        return GameEventEntity(
            type = GameEventType.LEVEL_UP.name,
            title = "Level $level reached",
            description = "Your dragon grew stronger and earned $coins bonus coins.",
            icon = "level_up",
            rarity = GameEventRarity.RARE.name,
            payload = payloadOf(
                "level" to level,
                "coins" to coins
            )
        )
    }

    fun dragonEvolution(
        fromStage: Int,
        toStage: Int,
        fromStageName: String,
        toStageName: String
    ): GameEventEntity {
        return GameEventEntity(
            type = GameEventType.DRAGON_EVOLUTION.name,
            title = "$toStageName",
            description = "Your dragon evolved from $fromStageName to $toStageName.",
            icon = "dragon_evolution",
            rarity = GameEventRarity.EPIC.name,
            payload = payloadOf(
                "fromStage" to fromStage,
                "toStage" to toStage,
                "fromStageName" to fromStageName,
                "toStageName" to toStageName
            )
        )
    }

    fun evolutionMilestoneNearing(
        toStage: Int,
        xp: Long,
        progress: Float
    ): GameEventEntity {
        val stageName = ExpConfig.evolutionStageName(toStage)
        val threshold = ExpConfig.xpThresholdForStage(toStage)
        val percent = (progress * 100f).toInt()

        return GameEventEntity(
            type = GameEventType.DRAGON_EVOLUTION.name,
            title = "$stageName milestone nearing",
            description = "Your dragon is $percent% of the way to $stageName ($xp / $threshold XP).",
            icon = "dragon_evolution",
            rarity = GameEventRarity.EPIC.name,
            payload = payloadOf(
                "toStage" to toStage,
                "stageName" to stageName,
                "threshold" to threshold,
                "xp" to xp,
                "progress" to progress
            )
        )
    }

    fun chestOpened(
        rewardType: String,
        chestType: String,
        coins: Int = 0,
        expAmount: Int = 0,
        hasCustomization: Boolean = false
    ): GameEventEntity {
        val label = chestType.replaceFirstChar { it.uppercase() }
        return GameEventEntity(
            type = GameEventType.CHEST_OPENED.name,
            title = "$label chest opened",
            description = "A $label chest opened with fresh rewards for your dragon.",
            icon = "chest_opened",
            rarity = rarityForChestType(chestType),
            payload = payloadOf(
                "rewardType" to rewardType,
                "chestType" to chestType,
                "coins" to coins,
                "exp" to expAmount,
                "hasCustomization" to hasCustomization
            )
        )
    }

    fun streakMilestone(streak: Int, chestType: String): GameEventEntity {
        val label = chestType.replaceFirstChar { it.uppercase() }
        return GameEventEntity(
            type = GameEventType.STREAK_MILESTONE.name,
            title = "$streak-day streak milestone",
            description = "You kept a $streak-day streak and earned a $label reward chest.",
            icon = "streak_milestone",
            rarity = rarityForChestType(chestType),
            payload = payloadOf(
                "streak" to streak,
                "chestType" to chestType
            )
        )
    }

    fun dailyGoalCompleted(goalXp: Long, bonusCoins: Int, bonusExp: Long): GameEventEntity {
        return GameEventEntity(
            type = GameEventType.DAILY_GOAL_COMPLETED.name,
            title = "Daily goal complete",
            description = "You gathered $goalXp XP today and earned +$bonusExp XP plus $bonusCoins coins for your dragon.",
            icon = "daily_goal_completed",
            rarity = GameEventRarity.RARE.name,
            payload = payloadOf(
                "goalXp" to goalXp,
                "coins" to bonusCoins,
                "xp" to bonusExp
            )
        )
    }

    fun surpriseReward(
        coins: Int,
        xp: Long,
        chestType: String,
        hasCustomization: Boolean
    ): GameEventEntity {
        val label = chestType.replaceFirstChar { it.uppercase() }
        val customizationText = if (hasCustomization) " and a hidden customization item" else ""
        return GameEventEntity(
            type = GameEventType.SURPRISE_REWARD.name,
            title = "Surprise reward",
            description = "Your dragon found a rare bonus: +$xp XP, +$coins coins, and a $label chest$customizationText.",
            icon = "surprise_reward",
            rarity = rarityForChestType(chestType),
            payload = payloadOf(
                "coins" to coins,
                "xp" to xp.toInt(),
                "chestType" to chestType,
                "hasCustomization" to hasCustomization
            )
        )
    }

    fun comboMilestone(combo: Int, bonusXp: Long, multiplier: Float): GameEventEntity {
        return GameEventEntity(
            type = GameEventType.COMBO_MILESTONE.name,
            title = "${combo}-hit momentum",
            description = "Your dragon caught the rhythm: combo x${formatMultiplier(multiplier)} added +$bonusXp bonus XP.",
            icon = "combo_milestone",
            rarity = if (combo >= 5) GameEventRarity.RARE.name else GameEventRarity.COMMON.name,
            payload = payloadOf(
                "combo" to combo,
                "bonusXp" to bonusXp,
                "comboMultiplier" to multiplier
            )
        )
    }

    fun firstDailyLogin(
        streak: Int,
        lastActiveTimestamp: Long,
        lastSessionDifference: String,
        motivationalMessage: String
    ): GameEventEntity {
        val streakLabel = if (streak > 0) "Streak: $streak days." else "Streak: waiting to restart."
        return GameEventEntity(
            type = GameEventType.FIRST_DAILY_LOGIN.name,
            title = "Welcome back",
            description = "Welcome back. Your dragon missed you. $lastSessionDifference $motivationalMessage $streakLabel",
            icon = "daily_welcome",
            rarity = GameEventRarity.COMMON.name,
            payload = payloadOf(
                "streak" to streak,
                "lastActiveTimestamp" to lastActiveTimestamp,
                "lastSessionDifference" to lastSessionDifference,
                "motivationalMessage" to motivationalMessage
            )
        )
    }

    private fun rarityForChestType(chestType: String): String = when (chestType.lowercase()) {
        "legendary" -> GameEventRarity.LEGENDARY.name
        "epic" -> GameEventRarity.EPIC.name
        "rare" -> GameEventRarity.RARE.name
        else -> GameEventRarity.COMMON.name
    }

    private fun formatMultiplier(multiplier: Float): String =
        String.format(Locale.US, "%.2f", multiplier)

    private fun payloadOf(vararg values: Pair<String, Any?>): String {
        return values.joinToString(prefix = "{", postfix = "}") { (key, value) ->
            val safeValue = when (value) {
                null -> "null"
                is Number, is Boolean -> value.toString()
                else -> "\"${value.toString().escapeJson()}\""
            }
            "\"$key\": $safeValue"
        }
    }

    private fun String.escapeJson(): String = buildString {
        forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
    }
}
