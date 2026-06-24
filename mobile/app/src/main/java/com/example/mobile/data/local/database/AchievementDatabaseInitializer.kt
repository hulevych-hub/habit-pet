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
        val existingIds = database.achievementDao().getAllRaw().map { it.id }.toSet()

        AchievementsConfig.achievements
            .filterNot { it.id in existingIds }
            .forEach { definition ->
                database.achievementDao().insertAchievement(AchievementsConfig.toEntity(definition))
            }
    }
}
