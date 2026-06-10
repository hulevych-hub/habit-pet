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

            // Check for chest rewards (7-day milestone)
            val stats = statisticsRepository.getStatistics().firstOrNull()
            val currentStreak = stats?.currentStreak ?: 0
            val lastStreakAwardedAt = stats?.lastStreakAwardedAt ?: 0

            // Award chest every 7 days
            if (currentStreak >= 7 && currentStreak % 7 == 0 && currentStreak > lastStreakAwardedAt) {
                // Determine chest type (mostly normal, with chances for better chests)
                val chestType = ChestRewardConfigProvider.getRandomChestType()
                val config = ChestRewardConfigProvider.getConfig(chestType)

                // Initialize reward values
                var coinAmount = config.getRandomCoins()
                var expAmount = config.getRandomExp()
                var accessoryId: Long? = null

                // Determine if we should grant an accessory based on drop chance
                if (config.accessoryRarity != null && Math.random() < config.accessoryDropChance) {
                    // Try to get an unowned accessory of the specified rarity
                    val unownedItems = inventoryItemRepository.getUnownedItemsByType(config.accessoryRarity.name)
                        .firstOrNull()?.toList() ?: emptyList()

                    if (unownedItems.isNotEmpty()) {
                        // Select a random unowned accessory
                        val selectedItem = unownedItems.random()
                        // Grant the accessory (mark as purchased)
                        val grantResult = inventoryItemRepository.grantItem(selectedItem.id)
                        if (grantResult == 1) {
                            // Successfully granted, set the accessory ID
                            accessoryId = selectedItem.id
                        } else {
                            // Failed to grant accessory (already owned?), fall back to standard rewards
                            // (coinAmount and expAmount are already set above)
                        }
                    }
                    // If no unowned items available, we fall back to standard rewards
                    // (coinAmount and expAmount are already set above)
                }

                // Award chest reward based on chest type
                val chestEvent = RewardUiEvent.ChestReward(
                    rewardType = "7_day_streak_${chestType.name.lowercase()}",
                    amount = coinAmount,
                    expAmount = expAmount,
                    accessoryId = accessoryId
                )
                rewardQueue.addReward(chestEvent)

                // Update last streak awarded
                statisticsRepository.updateStatistics(
                    stats?.copy(
                        lastStreakAwardedAt = currentStreak
                    ) ?: StatisticsEntity().copy(
                        lastStreakAwardedAt = currentStreak
                    )
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
            allCompleted && !alreadyCounted -> {
                statisticsRepository.incrementStreak()
                statisticsRepository.markStreakUpdatedToday()
            }

            !allCompleted && alreadyCounted -> {
                // optional safety rollback if you ever support it
            }
        }
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