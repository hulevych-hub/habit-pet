package com.example.mobile.data.local.database

import com.example.mobile.domain.AchievementsConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Syncs config-driven achievement definitions with persisted progress rows.
 */
class AchievementDatabaseInitializer @Inject constructor(private val database: AppDatabase) {

    suspend fun initializeAchievements() {
        achievementRepositorySync()
    }

    fun initializeAchievementsAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            initializeAchievements()
        }
    }

    private suspend fun achievementRepositorySync() {
        val dao = database.achievementDao()
        val configIds = AchievementsConfig.achievements.map { it.id }

        // Remove stale achievement rows that are no longer defined in AchievementsConfig
        dao.deleteStaleAchievements(configIds)

        // Add any new achievements that don't have a persisted row yet
        val existingIds = dao.getAllRaw().map { it.id }.toSet()
        AchievementsConfig.achievements
            .filterNot { it.id in existingIds }
            .forEach { definition ->
                dao.insertAchievement(AchievementsConfig.toEntity(definition))
            }
    }
}
