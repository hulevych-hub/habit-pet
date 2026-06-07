package com.example.mobile.domain.repository

import com.example.mobile.data.local.entities.HabitProgressEntity
import kotlinx.coroutines.flow.Flow

interface HabitProgressRepository {
    fun getProgress(habitId: Long, date: Long): Flow<HabitProgressEntity?>
    suspend fun updateProgress(entity: HabitProgressEntity)
    suspend fun reset(habitId: Long)

    suspend fun deleteAll()
}