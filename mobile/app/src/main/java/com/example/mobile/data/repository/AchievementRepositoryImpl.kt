package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.AchievementDao
import com.example.mobile.data.local.entities.AchievementEntity
import com.example.mobile.domain.AchievementsConfig
import com.example.mobile.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AchievementRepositoryImpl @Inject constructor(
    private val achievementDao: AchievementDao
) : AchievementRepository {
    override fun getAllAchievements(): Flow<List<AchievementEntity>> =
        achievementDao.getAllAchievements()

    override fun getAchievementById(achievementId: String): Flow<AchievementEntity?> =
        achievementDao.getAchievementById(achievementId)

    override suspend fun addAchievement(achievement: AchievementEntity): Long =
        achievementDao.insertAchievement(achievement)

    override suspend fun updateAchievement(achievement: AchievementEntity): Int =
        achievementDao.updateAchievement(achievement)

    override suspend fun updateProgress(
        achievementId: String,
        progress: Int,
        isUnlocked: Boolean
    ): Int = achievementDao.updateProgress(
        achievementId = achievementId,
        progress = progress,
        isUnlocked = isUnlocked
    )

    override suspend fun markClaimed(achievementId: String): Int =
        achievementDao.markClaimed(achievementId)

    override suspend fun syncAchievements(definitions: List<AchievementsConfig.AchievementDefinition>) {
        val existingIds = achievementDao.getAllRaw().map { it.id }.toSet()

        definitions
            .filterNot { it.id in existingIds }
            .forEach { definition ->
                achievementDao.insertAchievement(AchievementsConfig.toEntity(definition))
            }
    }

    override suspend fun reset() {
        achievementDao.resetAll()
    }
}
