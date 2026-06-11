package com.example.mobile.domain

import android.util.Log
import com.example.mobile.domain.repository.AchievementRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.reward.RewardQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementEngine @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val habitRepository: HabitRepository,
    private val petRepository: PetRepository,
    private val statisticsRepository: StatisticsRepository,
    private val inventoryItemRepository: InventoryItemRepository,
    private val rewardQueue: RewardQueue
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        Log.d("AchievementEngine", "Engine initialized")
        startObserving()
    }

    private fun startObserving() {
        observeHabitCount()
        observeStreaks()
        observeCompletions()
        observeXp()
        observeLevel()
        observeCollectionAchievements()
    }

    private fun observeHabitCount() {
        scope.launch {
            habitRepository.getAllHabits()
                .map { it.size }
                .distinctUntilChanged()
                .collectLatest { count ->
                    Log.d("AchievementEngine", "Habit count = $count")

                    if (count >= 1) unlockAchievement("First Habit")
                    if (count >= 3) unlockAchievement("3 Habit Builder")
                }
        }
    }

    private fun observeStreaks() {
        scope.launch {
            statisticsRepository.getStatistics()
                .map { it.currentStreak }
                .distinctUntilChanged()
                .collectLatest { streak ->
                    Log.d("AchievementEngine", "Streak = $streak")

                    if (streak >= 7) unlockAchievement("7 Day Streak")
                    if (streak >= 30) unlockAchievement("30 Day Streak")
                }
        }
    }

    private fun observeCompletions() {
        scope.launch {
            statisticsRepository.getStatistics()
                .map { it.totalCompletions }
                .distinctUntilChanged()
                .collectLatest { total ->
                    Log.d("AchievementEngine", "Completions = $total")

                    if (total >= 100) unlockAchievement("100 Completions")
                }
        }
    }

    private fun observeXp() {
        scope.launch {
            petRepository.getPet()
                .map { it.xp }
                .distinctUntilChanged()
                .collectLatest { xp ->
                    Log.d("AchievementEngine", "XP = $xp")

                    if (xp >= 1000) unlockAchievement("1000 XP")
                    if (xp >= 5000) unlockAchievement("5000 XP")
                }
        }
    }

    private fun observeLevel() {
        scope.launch {
            petRepository.getPet()
                .map { it.level }
                .distinctUntilChanged()
                .collectLatest { level ->
                    Log.d("AchievementEngine", "Level = $level")

                    if (level >= 10) unlockAchievement("Level 10")
                    if (level >= 25) unlockAchievement("Level 25")
                }
        }
    }

    private fun observeCollectionAchievements() {
        scope.launch {
            combine(
                inventoryItemRepository.getItemsByType("HAT"),
                inventoryItemRepository.getItemsByType("GLASSES"),
                inventoryItemRepository.getItemsByType("SCARF"),
                inventoryItemRepository.getItemsByType("BACKGROUND")
            ) { hats, glasses, scarves, backgrounds ->
                hats.count { it.isPurchased } +
                    glasses.count { it.isPurchased } +
                    scarves.count { it.isPurchased } +
                    backgrounds.count { it.isPurchased }
            }
                .distinctUntilChanged()
                .collectLatest { ownedCount ->
                    Log.d("AchievementEngine", "Owned accessories = $ownedCount")

                    if (ownedCount >= 1) unlockAchievement("First Accessory")
                    if (ownedCount >= 5) unlockAchievement("Accessory Collector")
                }
        }
    }

    suspend fun claimAchievement(achievementId: Long) {
        val achievement = achievementRepository.getAchievementById(achievementId).first() ?: return

        if (!achievement.isUnlocked || achievement.isClaimed) return

        achievementRepository.updateAchievement(achievement.copy(isClaimed = true))

        rewardQueue.addReward(
            RewardUiEvent.AchievementReward(
                achievementName = achievement.name,
                coins = achievement.rewardCoins,
                expAmount = achievement.rewardExp,
                chestType = achievement.rewardChestType
            )
        )
    }

    private suspend fun unlockAchievement(name: String) {
        val achievement = achievementRepository.getAllAchievements()
            .first()
            .firstOrNull { it.name == name } ?: return

        if (achievement.isUnlocked) return

        achievementRepository.updateAchievement(
            achievement.copy(
                isUnlocked = true,
                isClaimed = false,
                unlockedDate = System.currentTimeMillis()
            )
        )

        Log.d("ACHIEVEMENT", "Unlocked: $name")
    }
}
