package com.example.mobile.domain

import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DragonMoodEngine @Inject constructor(
    private val petRepository: PetRepository,
    private val statisticsRepository: StatisticsRepository,
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository
) {

    suspend fun refreshMood() {
        val stats = statisticsRepository.getStatistics().firstOrNull()
        val currentStreak = stats?.currentStreak ?: 0
        val recentCompletions = recentHabitCompletions()
        val lastActivityTimestamp = recentCompletions.lastActivityTimestamp
        val mood = DragonMood.calculate(
            currentStreak = currentStreak,
            lastActivityTimestamp = lastActivityTimestamp,
            recentHabitCompletions = recentCompletions.count
        )

        val currentPet = petRepository.getPet().firstOrNull() ?: PetEntity(id = 1)
        if (currentPet.mood != mood.value) {
            petRepository.updatePet(currentPet.copy(mood = mood.value))
        }
    }

    private suspend fun recentHabitCompletions(): RecentCompletions {
        val now = System.currentTimeMillis()
        val recentWindowStart = now - RECENT_COMPLETION_WINDOW_MILLIS
        val habits = habitRepository.getAllHabits().firstOrNull().orEmpty()
        var count = 0
        var lastActivityTimestamp = 0L

        habits.forEach { habit ->
            val completions = habitCompletionRepository
                .getCompletionsForHabit(habit.id, recentWindowStart, now)
                .firstOrNull()
                .orEmpty()

            completions.forEach { completion ->
                count++
                if (completion.date > lastActivityTimestamp) {
                    lastActivityTimestamp = completion.date
                }
            }
        }

        return RecentCompletions(
            count = count,
            lastActivityTimestamp = lastActivityTimestamp
        )
    }

    private data class RecentCompletions(
        val count: Int,
        val lastActivityTimestamp: Long
    )

    companion object {
        private const val RECENT_COMPLETION_WINDOW_MILLIS = 72L * 60L * 60L * 1000L
    }
}
