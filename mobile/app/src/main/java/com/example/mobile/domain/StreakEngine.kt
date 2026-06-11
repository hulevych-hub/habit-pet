package com.example.mobile.domain

import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.reward.RewardQueue
import kotlinx.coroutines.flow.firstOrNull

class StreakEngine(
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository,
    private val statisticsRepository: StatisticsRepository,
    private val rewardQueue: RewardQueue,
    private val inventoryItemRepository: InventoryItemRepository
) {

    private data class StreakChestReward(
        val event: RewardUiEvent.ChestReward,
        val summary: List<String>
    )

    companion object {
        private val STREAK_MILESTONES = listOf(7, 14, 30, 60, 100)

        private fun getChestTypeForMilestone(streak: Int): ChestType = when (streak) {
            14 -> ChestType.RARE
            30, 60 -> ChestType.EPIC
            100 -> ChestType.LEGENDARY
            else -> ChestType.NORMAL
        }

        private fun formatChestLabel(chestType: ChestType): String = when (chestType) {
            ChestType.NORMAL -> "Normal"
            ChestType.RARE -> "Rare"
            ChestType.EPIC -> "Epic"
            ChestType.LEGENDARY -> "Legendary"
        }
    }

    /**
     * Called after completing a habit
     */
    suspend fun evaluateTodayStreak(today: Long) {

        val normalizedToday = normalize(today)

        if (statisticsRepository.isStreakAlreadyCountedToday()) return

        val habits = habitRepository.getAllHabits().firstOrNull().orEmpty()

        if (habits.isEmpty()) return

        val allCompleted = habits.all { habit ->
            habitCompletionRepository
                .getCompletionForHabitOnDate(habit.id, normalizedToday)
                .firstOrNull() != null
        }

        if (allCompleted) {
            statisticsRepository.incrementStreak()
            statisticsRepository.markStreakUpdatedToday()

            val stats = statisticsRepository.getStatistics().firstOrNull()
            val currentStreak = stats?.currentStreak ?: 0
            val lastStreakAwardedAt = stats?.lastStreakAwardedAt ?: 0
            val milestone = STREAK_MILESTONES
                .filter { currentStreak >= it && it > lastStreakAwardedAt }
                .lastOrNull()

            if (milestone != null) {
                val streakChestReward = buildStreakChestReward(milestone)

                rewardQueue.addReward(
                    RewardUiEvent.StreakReward(
                        streak = currentStreak,
                        coins = 0,
                        rewardSummary = streakChestReward.summary
                    )
                )
                rewardQueue.addReward(streakChestReward.event)

                statisticsRepository.updateStatistics(
                    stats?.copy(lastStreakAwardedAt = milestone)
                        ?: StatisticsEntity().copy(lastStreakAwardedAt = milestone)
                )
            }
        }
    }

    /**
     * Called after DELETE or correction
     * Recomputes state safely
     */
    suspend fun recalculateTodayStreak(today: Long) {

        val normalizedToday = normalize(today)

        val habits = habitRepository.getAllHabits().firstOrNull().orEmpty()

        if (habits.isEmpty()) return

        val allCompleted = habits.all { habit ->
            habitCompletionRepository
                .getCompletionForHabitOnDate(habit.id, normalizedToday)
                .firstOrNull() != null
        }

        val alreadyCounted = statisticsRepository.isStreakAlreadyCountedToday()

        when {
            allCompleted && !alreadyCounted -> evaluateTodayStreak(today)
            !allCompleted && alreadyCounted -> {
                // optional safety rollback if you ever support it
            }
        }
    }

    private suspend fun buildStreakChestReward(streak: Int): StreakChestReward {
        val chestType = getChestTypeForMilestone(streak)
        val config = ChestRewardConfigProvider.getConfig(chestType)

        var coinAmount = config.getRandomCoins()
        var expAmount = config.getRandomExp()
        var customizationId: Long? = null

        if (config.customizationRarity != null && Math.random() < config.customizationDropChance) {
            val unownedItems = inventoryItemRepository.getUnownedItemsByRarity(config.customizationRarity)
                .firstOrNull()?.toList() ?: emptyList()

            if (unownedItems.isNotEmpty()) {
                val selectedItem = unownedItems.random()
                if (inventoryItemRepository.grantItem(selectedItem.id) == 1) {
                    customizationId = selectedItem.id
                }
            }
        }

        val rewardType = "global_streak_${streak}_${chestType.name.lowercase()}"
        val event = RewardUiEvent.ChestReward(
            rewardType = rewardType,
            amount = coinAmount,
            expAmount = expAmount,
            customizationId = customizationId
        )

        val summary = buildRewardSummary(
            streak = streak,
            chestType = chestType,
            coinAmount = coinAmount,
            expAmount = expAmount,
            customizationId = customizationId,
            hasCustomizationChance = config.customizationRarity != null
        )

        return StreakChestReward(event, summary)
    }

    private fun buildRewardSummary(
        streak: Int,
        chestType: ChestType,
        coinAmount: Int,
        expAmount: Int,
        customizationId: Long?,
        hasCustomizationChance: Boolean
    ): List<String> {
        val summary = mutableListOf(
            "$streak Day Streak Milestone",
            "${formatChestLabel(chestType)} Reward Chest"
        )

        if (coinAmount > 0) {
            summary.add("+$coinAmount coins")
        }

        if (expAmount > 0) {
            summary.add("+$expAmount EXP")
        }

        if (customizationId != null) {
            summary.add("Customization unlocked")
        } else if (hasCustomizationChance) {
            summary.add("Customization chance")
        }

        return summary
    }

    private fun normalize(time: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = time
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
