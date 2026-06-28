package com.example.mobile.domain

import android.util.Log
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.GameEventType
import com.example.mobile.domain.repository.AchievementRepository
import com.example.mobile.domain.repository.GameEventRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
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
    private val activityTimelineEngine: ActivityTimelineEngine,
    private val gameEventRepository: GameEventRepository,
    private val scopeDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) {

    private val scope = CoroutineScope(SupervisorJob() + scopeDispatcher)
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
        observeBestStreak()
        observeBestCombo()
        observeDaysActive()
        observePetAge()
        observeTotalCoins()
        observeFreezesUsed()
        observeEventCounts()
        observeAchievementsClaimed()
        observeTitlesUnlocked()
        observeFramesUnlocked()
        observeSetsCompleted()
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

    private fun observeBestStreak() {
        scope.launch {
            statisticsRepository.getStatistics()
                .map { it.bestStreak }
                .distinctUntilChanged()
                .collectLatest { bestStreak ->
                    Log.d("AchievementEngine", "Best streak = $bestStreak")
                    updateAchievementProgress(AchievementProgressSource.BEST_STREAK, bestStreak)
                }
        }
    }

    private fun observeBestCombo() {
        scope.launch {
            statisticsRepository.getStatistics()
                .map { it.bestCombo }
                .distinctUntilChanged()
                .collectLatest { bestCombo ->
                    Log.d("AchievementEngine", "Best combo = $bestCombo")
                    updateAchievementProgress(AchievementProgressSource.BEST_COMBO, bestCombo)
                }
        }
    }

    private fun observeDaysActive() {
        scope.launch {
            statisticsRepository.getStatistics()
                .map { it.daysActive }
                .distinctUntilChanged()
                .collectLatest { days ->
                    Log.d("AchievementEngine", "Days active = $days")
                    updateAchievementProgress(AchievementProgressSource.DAYS_ACTIVE, days)
                }
        }
    }

    private fun observePetAge() {
        scope.launch {
            statisticsRepository.getStatistics()
                .map { it.petAgeDays }
                .distinctUntilChanged()
                .collectLatest { age ->
                    Log.d("AchievementEngine", "Pet age days = $age")
                    updateAchievementProgress(AchievementProgressSource.PET_AGE_DAYS, age)
                }
        }
    }

    private fun observeTotalCoins() {
        scope.launch {
            statisticsRepository.getStatistics()
                .map { it.totalCoins }
                .distinctUntilChanged()
                .collectLatest { coins ->
                    Log.d("AchievementEngine", "Total coins = $coins")
                    updateAchievementProgress(AchievementProgressSource.TOTAL_COINS, coins)
                }
        }
    }

    private fun observeFreezesUsed() {
        scope.launch {
            statisticsRepository.getStatistics()
                .map { StatisticsEntity.parseFreezeDates(it.streakFreezeDatesJson).size }
                .distinctUntilChanged()
                .collectLatest { freezes ->
                    Log.d("AchievementEngine", "Freezes used = $freezes")
                    updateAchievementProgress(AchievementProgressSource.FREEZES_USED, freezes)
                }
        }
    }

    private fun observeEventCounts() {
        scope.launch {
            while (true) {
                refreshEventCount(GameEventType.CHALLENGE_COMPLETED.name, AchievementProgressSource.CHALLENGES_COMPLETED)
                refreshEventCount(GameEventType.CHEST_OPENED.name, AchievementProgressSource.CHESTS_OPENED)
                refreshEventCount(GameEventType.FIRST_DAILY_LOGIN.name, AchievementProgressSource.DAILY_LOGINS)
                refreshEventCount(GameEventType.DRAGON_EVOLUTION.name, AchievementProgressSource.EVOLUTIONS)
                delay(5_000)
            }
        }
    }

    private suspend fun refreshEventCount(type: String, source: AchievementProgressSource) {
        val count = gameEventRepository.countByType(type)
        Log.d("AchievementEngine", "Event count $type = $count")
        updateAchievementProgress(source, count)
    }

    private fun observeAchievementsClaimed() {
        scope.launch {
            while (true) {
                val claimed = achievementRepository.countClaimed()
                Log.d("AchievementEngine", "Achievements claimed = $claimed")
                updateAchievementProgress(AchievementProgressSource.ACHIEVEMENTS_CLAIMED, claimed)
                delay(5_000)
            }
        }
    }

    private fun observeTitlesUnlocked() {
        scope.launch {
            petRepository.getPet()
                .map { PetEntity.parseUnlockedIds(it.unlockedTitleIdsJson).size }
                .distinctUntilChanged()
                .collectLatest { count ->
                    Log.d("AchievementEngine", "Titles unlocked = $count")
                    updateAchievementProgress(AchievementProgressSource.TITLES_UNLOCKED, count)
                }
        }
    }

    private fun observeFramesUnlocked() {
        scope.launch {
            petRepository.getPet()
                .map { PetEntity.parseUnlockedIds(it.unlockedFramesJson).size }
                .distinctUntilChanged()
                .collectLatest { count ->
                    Log.d("AchievementEngine", "Frames unlocked = $count")
                    updateAchievementProgress(AchievementProgressSource.FRAMES_UNLOCKED, count)
                }
        }
    }

    private fun observeSetsCompleted() {
        scope.launch {
            petRepository.getPet()
                .map { PetEntity.parseUnlockedIds(it.completedSetsJson).size }
                .distinctUntilChanged()
                .collectLatest { count ->
                    Log.d("AchievementEngine", "Sets completed = $count")
                    updateAchievementProgress(AchievementProgressSource.SETS_COMPLETED, count)
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

    suspend fun claimAchievement(achievementId: String): Boolean = claimMutex.withLock {
        val achievement = achievementRepository.getAchievementById(achievementId).firstOrNull() ?: return@withLock false
        val definition = AchievementsConfig.achievementById(achievement.id) ?: return@withLock false

        val targetReached = definition.targetValue?.let { achievement.progress >= it } ?: achievement.isUnlocked
        if (!achievement.isUnlocked || !targetReached || achievement.isClaimed) return@withLock false

        val processed = achievementRewardProcessor.process(
            definition = definition,
            achievementId = achievement.id
        )
        if (!processed) return@withLock false

        Log.d("ACHIEVEMENT", "Claimed: ${definition.name}")
        true
    }
}
