package com.example.mobile.domain.repository

import com.example.mobile.data.local.entities.AchievementEntity
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun getAllAchievements(): Flow<List<AchievementEntity>>
    fun getAchievementById(achievementId: Long): Flow<AchievementEntity?>
    suspend fun addAchievement(achievement: AchievementEntity): Long
    suspend fun updateAchievement(achievement: AchievementEntity): Int
}