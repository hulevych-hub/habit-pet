package com.example.mobile.domain

import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.ChestRewardFactory
import com.example.mobile.domain.repository.ChallengeRepository
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
    private val inventoryItemRepository: InventoryItemRepository,
    private val activityTimelineEngine: ActivityTimelineEngine,
    private val dragonMoodEngine: DragonMoodEngine,
    private val challengeRepository: ChallengeRepository
) {

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

        val allCompleted = habitCompletionRepository.areAllHabitsCompletedOnDate(normalizedToday)

        if (allCompleted) {
            statisticsRepository.incrementStreak()
            statisticsRepository.markStreakUpdatedToday()
            dragonMoodEngine.refreshMood()

            val stats = statisticsRepository.getStatistics().firstOrNull()
            val currentStreak = stats?.currentStreak ?: 0
            challengeRepository.recordStreak(currentStreak)
            val lastStreakAwardedAt = stats?.lastStreakAwardedAt ?: 0
            val milestone = STREAK_MILESTONES
                .filter { currentStreak >= it && it > lastStreakAwardedAt }
                .lastOrNull()

            if (milestone != null) {
                val milestoneChestType = getChestTypeForMilestone(milestone)
                val streakChestReward = ChestRewardFactory.buildChestReward(
                    rewardType = "global_streak_${milestone}_${milestoneChestType.name.lowercase()}",
                    chestType = milestoneChestType,
                    inventoryItemRepository = inventoryItemRepository
                )
                val chestConfig = ChestRewardConfigProvider.getConfig(milestoneChestType)
                val streakChestSummary = buildRewardSummary(
                    streak = milestone,
                    chestType = milestoneChestType,
                    coinAmount = (streakChestReward.amount as? Int) ?: 0,
                    expAmount = streakChestReward.expAmount,
                    customizationId = streakChestReward.customizationId,
                    hasCustomizationChance = chestConfig.customizationRarity != null
                )

                rewardQueue.addReward(
                    RewardUiEvent.StreakReward(
                        streak = currentStreak,
                        coins = 0,
                        rewardSummary = streakChestSummary
                    )
                )
                rewardQueue.addReward(streakChestReward)
                activityTimelineEngine.logStreakMilestone(currentStreak, milestoneChestType)

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

        val allCompleted = habitCompletionRepository.areAllHabitsCompletedOnDate(normalizedToday)

        val alreadyCounted = statisticsRepository.isStreakAlreadyCountedToday()

        when {
            allCompleted && !alreadyCounted -> evaluateTodayStreak(today)
            !allCompleted && alreadyCounted -> {
                // optional safety rollback if you ever support it
            }
        }
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
