package com.example.mobile.domain.repository

import com.example.mobile.data.local.entities.AchievementEntity
import com.example.mobile.domain.AchievementsConfig
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun getAllAchievements(): Flow<List<AchievementEntity>>
    fun getAchievementById(achievementId: String): Flow<AchievementEntity?>
    suspend fun addAchievement(achievement: AchievementEntity): Long
    suspend fun updateAchievement(achievement: AchievementEntity): Int
    suspend fun updateProgress(
        achievementId: String,
        progress: Int,
        isUnlocked: Boolean
    ): Int
    suspend fun markClaimed(achievementId: String): Int
    suspend fun syncAchievements(definitions: List<AchievementsConfig.AchievementDefinition>)
    suspend fun reset()
    suspend fun countClaimed(): Int
}
