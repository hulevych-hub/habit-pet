package com.example.mobile.domain

import android.util.Log
import com.example.mobile.domain.repository.AchievementRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementEngine @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val habitRepository: HabitRepository,
    private val petRepository: PetRepository,
    private val statisticsRepository: StatisticsRepository,
    private val inventoryItemRepository: InventoryItemRepository,
    private val achievementRewardProcessor: AchievementRewardProcessor,
    private val activityTimelineEngine: ActivityTimelineEngine
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val claimMutex = Mutex()

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
                    updateAchievementProgress(AchievementProgressSource.HABIT_COUNT, count)
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
                    updateAchievementProgress(AchievementProgressSource.CURRENT_STREAK, streak)
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
                    updateAchievementProgress(AchievementProgressSource.TOTAL_COMPLETIONS, total)
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
                    updateAchievementProgress(AchievementProgressSource.TOTAL_XP, xp.toInt())
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
                    updateAchievementProgress(AchievementProgressSource.PET_LEVEL, level)
                }
        }
    }

    private fun observeCollectionAchievements() {
        scope.launch {
            combine(
                inventoryItemRepository.getItemsByType(CustomizationTypes.OUTFIT),
                inventoryItemRepository.getItemsByType(CustomizationTypes.BACKGROUND),
                inventoryItemRepository.getItemsByType(CustomizationTypes.AURA)
            ) { outfits, backgrounds, auras ->
                outfits.count { it.isPurchased } +
                    backgrounds.count { it.isPurchased } +
                    auras.count { it.isPurchased }
            }
                .distinctUntilChanged()
                .collectLatest { ownedCount ->
                    Log.d("AchievementEngine", "Owned customization items = $ownedCount")
                    updateAchievementProgress(AchievementProgressSource.OWNED_CUSTOMIZATIONS, ownedCount)
                }
        }
    }

    private suspend fun updateAchievementProgress(
        source: AchievementProgressSource,
        progress: Int
    ) {
        val safeProgress = progress.coerceAtLeast(0)

        AchievementsConfig.achievements
            .filter { it.progressSource == source }
            .forEach { definition ->
                val currentAchievement = achievementRepository
                    .getAchievementById(definition.id)
                    .firstOrNull()

                val shouldUnlock = currentAchievement?.isUnlocked == true ||
                    (definition.targetValue?.let { safeProgress >= it } ?: true)

                achievementRepository.updateProgress(
                    achievementId = definition.id,
                    progress = safeProgress,
                    isUnlocked = shouldUnlock
                )

                if (currentAchievement != null && !currentAchievement.isUnlocked && shouldUnlock) {
                    activityTimelineEngine.logAchievementUnlocked(definition.name)
                }
            }
    }

    suspend fun claimAchievement(achievementId: String) {
        claimMutex.withLock {
            val achievement = achievementRepository.getAchievementById(achievementId).first() ?: return
            val definition = AchievementsConfig.achievementById(achievement.id) ?: return

            val targetReached = definition.targetValue?.let { achievement.progress >= it } ?: achievement.isUnlocked
            if (!achievement.isUnlocked || !targetReached || achievement.isClaimed) return

            val processed = achievementRewardProcessor.process(
                definition = definition,
                achievementId = achievement.id
            )
            if (!processed) return

            Log.d("ACHIEVEMENT", "Claimed: ${definition.name}")
        }
    }
}
