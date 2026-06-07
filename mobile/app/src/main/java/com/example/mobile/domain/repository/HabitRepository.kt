package com.example.mobile.domain.repository

import com.example.mobile.data.local.entities.HabitEntity
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabits(): Flow<List<HabitEntity>>
    fun getHabitById(habitId: Long): Flow<HabitEntity?>
    suspend fun addHabit(habit: HabitEntity): Long
    suspend fun updateHabit(habit: HabitEntity): Int
    suspend fun deleteHabit(habit: HabitEntity): Int

    suspend fun deleteAll()
}