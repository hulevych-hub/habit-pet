package com.example.mobile.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mobile.data.local.dao.HabitProgressDao
import com.example.mobile.data.local.dao.ChallengeDao
import com.example.mobile.data.local.entities.AchievementEntity
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.GameEventEntity
import com.example.mobile.data.local.entities.HabitProgressEntity
import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.ChallengeEntity
import com.example.mobile.data.local.entities.JournalEntryEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity

@Database(
    entities = [
        HabitEntity::class,
        HabitCompletionEntity::class,
        PetEntity::class,
        InventoryItemEntity::class,
        AchievementEntity::class,
        GameEventEntity::class,
        JournalEntryEntity::class,
        StatisticsEntity::class,
        HabitProgressEntity::class,
        ChallengeEntity::class
    ],
    version = 19,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): com.example.mobile.data.local.dao.HabitDao
    abstract fun habitCompletionDao(): com.example.mobile.data.local.dao.HabitCompletionDao
    abstract fun petDao(): com.example.mobile.data.local.dao.PetDao
    abstract fun inventoryItemDao(): com.example.mobile.data.local.dao.InventoryItemDao
    abstract fun achievementDao(): com.example.mobile.data.local.dao.AchievementDao
    abstract fun gameEventDao(): com.example.mobile.data.local.dao.GameEventDao
    abstract fun statisticsDao(): com.example.mobile.data.local.dao.StatisticsDao
    abstract fun habitProgressDao(): HabitProgressDao
    abstract fun challengeDao(): ChallengeDao
}
