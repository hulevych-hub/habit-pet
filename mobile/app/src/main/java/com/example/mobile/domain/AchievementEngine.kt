package com.example.mobile.domain

import com.example.mobile.data.local.entities.AchievementEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.AchievementRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Engine responsible for checking and unlocking achievements based on game events.
 * Observes various repositories and updates achievement status when conditions are met.
 */
@Singleton
class AchievementEngine @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository,
    private val petRepository: PetRepository,
    private val statisticsRepository: StatisticsRepository
) {

    // Private coroutine scope for observing changes
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    init {
        startObserving()
    }

    private fun startObserving() {
        // Observe habit count for "First Habit" achievement
        observeHabitCount()

        // Observe streaks for streak-based achievements
        observeStreaks()

        // Observe total completions for completion-based achievements
        observeTotalCompletions()

        // Observe XP for XP-based achievements
        observeXp()

        // Observe level for level-based achievements
        observeLevel()
    }

    private fun observeHabitCount() {
        scope.launch {
            habitRepository.getAllHabits()
                .map { it.size }
                .distinctUntilChanged()
                .collect { count ->
                    if (count >= 1) {
                        unlockAchievementByName("First Habit")
                    }
                }
        }
    }

    private fun observeStreaks() {
        scope.launch {
            // We need to calculate streaks based on completion data
            // For simplicity, we'll check global streak from statistics
            statisticsRepository.getStatistics()
                .map { it.currentStreak }
                .distinctUntilChanged()
                .collect { streak ->
                    if (streak >= 7) {
                        unlockAchievementByName("7 Day Streak")
                    }
                    if (streak >= 30) {
                        unlockAchievementByName("30 Day Streak")
                    }
                }
        }
    }

    private fun observeTotalCompletions() {
        scope.launch {
            statisticsRepository.getStatistics()
                .map { it.totalCompletions }
                .distinctUntilChanged()
                .collect { completions ->
                    if (completions >= 100) {
                        unlockAchievementByName("100 Completions")
                    }
                }
        }
    }

    private fun observeXp() {
        scope.launch {
            petRepository.getPet()
                .map { it.xp }
                .distinctUntilChanged()
                .collect { xp ->
                    if (xp >= 1000) {
                        unlockAchievementByName("1000 XP")
                    }
                }
        }
    }

    private fun observeLevel() {
        scope.launch {
            petRepository.getPet()
                .map { it.level }
                .distinctUntilChanged()
                .collect { level ->
                    if (level >= 10) {
                        unlockAchievementByName("Level 10")
                    }
                    if (level >= 25) {
                        unlockAchievementByName("Level 25")
                    }
                }
        }
    }

    private suspend fun unlockAchievementByName(name: String) {
        achievementRepository.getAllAchievements()
            .first() // Get the first emission
            .firstOrNull { it.name == name } // Find achievement by name
            ?.let { achievement ->
                if (!achievement.isUnlocked) {
                    val updatedAchievement = achievement.copy(
                        isUnlocked = true,
                        unlockedDate = System.currentTimeMillis()
                    )
                    achievementRepository.updateAchievement(updatedAchievement)
                }
            }
    }
}