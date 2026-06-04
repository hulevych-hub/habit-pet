package com.example.mobile.domain.repository

import com.example.mobile.data.local.entities.HabitCompletionEntity
import kotlinx.coroutines.flow.Flow

interface HabitCompletionRepository {
    fun getCompletionsForHabit(habitId: Long, startDate: Long, endDate: Long): Flow<List<HabitCompletionEntity>>
    fun getCompletionForHabitOnDate(habitId: Long, date: Long): Flow<HabitCompletionEntity?>
    suspend fun addCompletion(completion: HabitCompletionEntity): Long
    suspend fun updateCompletion(completion: HabitCompletionEntity): Int
    suspend fun deleteCompletion(completion: HabitCompletionEntity): Int
}