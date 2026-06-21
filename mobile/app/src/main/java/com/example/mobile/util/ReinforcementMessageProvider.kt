package com.example.mobile.util

import android.content.Context
import android.content.SharedPreferences
import com.example.mobile.data.local.entities.GameEventEntity
import com.example.mobile.domain.GameEventRarity
import com.example.mobile.domain.GameEventType
import com.example.mobile.presentation.ui.events.RewardUiEvent

object ReinforcementMessageProvider {

    enum class UserBehavior {
        CONSISTENT,
        INACTIVE,
        STREAK,
        FRESH
    }

    data class ReinforcementMessage(
        val title: String,
        val message: String
    )

    fun notification(context: Context, behavior: UserBehavior = behavior(context)): ReinforcementMessage =
        ReinforcementMessage(
            title = titleForBehavior(behavior),
            message = nextMessage(context, behavior)
        )

    fun timelineMessage(context: Context, event: GameEventEntity): String {
        val behavior = when {
            event.type == GameEventType.FIRST_DAILY_LOGIN.name -> {
                if (isInactiveUser(context)) UserBehavior.INACTIVE else UserBehavior.CONSISTENT
            }
            event.type == GameEventType.STREAK_MILESTONE.name -> UserBehavior.STREAK
            event.type == GameEventType.CHALLENGE_COMPLETED.name -> {
                if (isStreakUser(context)) UserBehavior.STREAK else UserBehavior.CONSISTENT
            }
            event.type == GameEventType.LEVEL_UP.name ||
                event.type == GameEventType.DRAGON_EVOLUTION.name ||
                event.type == GameEventType.ACHIEVEMENT_UNLOCKED.name -> UserBehavior.STREAK
            event.rarity == GameEventRarity.EPIC.name || event.rarity == GameEventRarity.LEGENDARY.name -> UserBehavior.STREAK
            else -> behavior(context)
        }

        return nextMessage(context, behavior)
    }

    fun rewardMessage(context: Context, reward: RewardUiEvent): String {
        val behavior = when (reward) {
            is RewardUiEvent.StreakReward,
            is RewardUiEvent.LevelUpReward,
            is RewardUiEvent.DragonEvolutionReward,
            is RewardUiEvent.ExpReward,
            is RewardUiEvent.CustomizationReward -> UserBehavior.STREAK
            is RewardUiEvent.ChestReward -> if (isStreakUser(context)) UserBehavior.STREAK else UserBehavior.CONSISTENT
            is RewardUiEvent.CoinReward -> UserBehavior.CONSISTENT
        }

        return nextMessage(context, behavior)
    }

    fun behavior(context: Context): UserBehavior = when {
        isInactiveUser(context) -> UserBehavior.INACTIVE
        isStreakUser(context) -> UserBehavior.STREAK
        NotificationPrefs.getLastActiveSessionTimestamp(context) > 0L -> UserBehavior.CONSISTENT
        else -> UserBehavior.FRESH
    }

    private fun titleForBehavior(behavior: UserBehavior): String = when (behavior) {
        UserBehavior.INACTIVE -> "Your dragon is still here"
        UserBehavior.STREAK -> "Your consistency is rare"
        UserBehavior.CONSISTENT -> "You’re building something strong"
        UserBehavior.FRESH -> "Your next small win is ready"
    }

    private fun nextMessage(context: Context, behavior: UserBehavior): String {
        val pool = messagePool(behavior)
        val prefs = prefs(context)
        val lastIndex = prefs.getInt(indexKey(behavior), -1)
        val nextIndex = if (lastIndex < 0 || lastIndex >= pool.lastIndex) 0 else lastIndex + 1

        prefs.edit().putInt(indexKey(behavior), nextIndex).apply()
        return pool[nextIndex]
    }

    private fun messagePool(behavior: UserBehavior): List<String> = when (behavior) {
        UserBehavior.INACTIVE -> listOf(
            "Your dragon is still waiting for you.",
            "No pressure, just a soft place to begin again.",
            "Your dragon kept the hearth warm while you were away.",
            "A small return is enough to wake the rhythm."
        )
        UserBehavior.STREAK -> listOf(
            "Your consistency is rare.",
            "You're building a rhythm your dragon can feel.",
            "This streak has weight. Your dragon notices.",
            "The rhythm you're keeping is becoming something strong."
        )
        UserBehavior.CONSISTENT -> listOf(
            "You’re building something strong.",
            "That small step matters more than it looks.",
            "Your dragon is learning the shape of your effort.",
            "One honest habit at a time, the bond grows."
        )
        UserBehavior.FRESH -> listOf(
            "Your dragon is ready when you are.",
            "The first step can be tiny and still count.",
            "There is room here for a gentle beginning.",
            "Your dragon is happy to start beside you."
        )
    }

    private fun indexKey(behavior: UserBehavior): String = "last_${behavior.name.lowercase()}_message_index"

    private fun isStreakUser(context: Context): Boolean =
        NotificationPrefs.getLastStreak(context) >= STREAK_THRESHOLD

    private fun isInactiveUser(context: Context): Boolean {
        val lastActiveTimestamp = NotificationPrefs.getLastActiveSessionTimestamp(context)
        if (lastActiveTimestamp <= 0L) return false

        return (System.currentTimeMillis() - lastActiveTimestamp) >= INACTIVE_THRESHOLD_MILLIS
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private const val PREFS_NAME = "reinforcement_message_prefs"
    private const val STREAK_THRESHOLD = 3
    private const val INACTIVE_THRESHOLD_MILLIS = 48L * 60L * 60L * 1000L
}
