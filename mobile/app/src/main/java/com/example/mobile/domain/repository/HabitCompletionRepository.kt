package com.example.mobile.domain.repository

import com.example.mobile.data.local.entities.HabitCompletionEntity
import kotlinx.coroutines.flow.Flow

data class HabitCompletionResult(
    val completionId: Long,
    val baseXpEarned: Long,
    val comboBonusXp: Long,
    val totalXpEarned: Long,
    val combo: Int,
    val comboMultiplier: Float,
    val comboMilestoneReached: Boolean,
    val isNewCompletion: Boolean = true
)

interface HabitCompletionRepository {

    fun getCompletionsForHabit(
        habitId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<HabitCompletionEntity>>

    fun getCompletionForHabitOnDate(
        habitId: Long,
        date: Long
    ): Flow<HabitCompletionEntity?>

    suspend fun addCompletion(completion: HabitCompletionEntity): Long

    suspend fun addCompletionWithCombo(completion: HabitCompletionEntity): HabitCompletionResult

    suspend fun updateCompletion(completion: HabitCompletionEntity): Int

    suspend fun deleteCompletion(completion: HabitCompletionEntity): Int

    suspend fun deleteAll()

    /**
     * Returns true if ANY habit has a completion on the given day.
     * (Used for global streak evaluation)
     */
    suspend fun hasAnyCompletionOnDate(date: Long): Boolean

    suspend fun areAllHabitsCompletedOnDate(date: Long): Boolean
}