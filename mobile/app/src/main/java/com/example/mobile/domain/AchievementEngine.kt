package com.example.mobile.domain

import android.util.Log
import com.example.mobile.domain.repository.AchievementRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Observes game state and unlocks achievements automatically.
 */
@Singleton
class AchievementEngine @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val habitRepository: HabitRepository,
    private val petRepository: PetRepository,
    private val statisticsRepository: StatisticsRepository,
) {

    // IMPORTANT: structured coroutine scope
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        Log.d("AchievementEngine", "Engine initialized 🚀")
        startObserving()
    }

    private fun startObserving() {
        observeHabitCount()
        observeStreaks()
        observeCompletions()
        observeXp()
        observeLevel()
    }

    // -------------------------
    // HABITS
    // -------------------------
    private fun observeHabitCount() {
        scope.launch {
            habitRepository.getAllHabits()
                .map { it.size }
                .distinctUntilChanged()
                .collectLatest { count ->

                    Log.d("AchievementEngine", "Habit count = $count")

                    if (count >= 1) {
                        unlockAchievement("First Habit")
                    }
                }
        }
    }

    // -------------------------
    // STREAKS
    // -------------------------
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

    // -------------------------
    // COMPLETIONS
    // -------------------------
    private fun observeCompletions() {
        scope.launch {
            statisticsRepository.getStatistics()
                .map { it.totalCompletions }
                .distinctUntilChanged()
                .collectLatest { total ->

                    Log.d("AchievementEngine", "Completions = $total")

                    if (total >= 100) {
                        unlockAchievement("100 Completions")
                    }
                }
        }
    }

    // -------------------------
    // XP
    // -------------------------
    private fun observeXp() {
        scope.launch {
            petRepository.getPet()
                .map { it.xp }
                .distinctUntilChanged()
                .collectLatest { xp ->

                    Log.d("AchievementEngine", "XP = $xp")

                    if (xp >= 1000) {
                        unlockAchievement("1000 XP")
                    }
                }
        }
    }

    // -------------------------
    // LEVEL
    // -------------------------
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

    // -------------------------
    // UNLOCK LOGIC
    // -------------------------
    private suspend fun unlockAchievement(name: String): RewardUiEvent? {
        val achievement = achievementRepository.getAllAchievements()
            .first()
            .firstOrNull { it.name == name }

        if (achievement == null) {
            Log.d("AchievementEngine", "Achievement not found: $name")
            return null
        }

        if (achievement.isUnlocked) {
            return null
        }

        Log.d("AchievementEngine", "Unlocking: $name")

        achievementRepository.updateAchievement(
            achievement.copy(
                isUnlocked = true,
                unlockedDate = System.currentTimeMillis()
            )
        )

        return RewardUiEvent.AchievementReward(name, achievement.rewardCoins)
    }
}