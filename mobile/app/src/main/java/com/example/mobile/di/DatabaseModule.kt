package com.example.mobile.di

import android.content.Context
import androidx.room.Room
import com.example.mobile.data.local.database.AppDatabase
import com.example.mobile.data.local.dao.AchievementDao
import com.example.mobile.data.local.dao.InventoryItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "habit_pet_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideHabitDao(database: AppDatabase): com.example.mobile.data.local.dao.HabitDao {
        return database.habitDao()
    }

    @Provides
    @Singleton
    fun provideHabitCompletionDao(database: AppDatabase): com.example.mobile.data.local.dao.HabitCompletionDao {
        return database.habitCompletionDao()
    }

    @Provides
    @Singleton
    fun providePetDao(database: AppDatabase): com.example.mobile.data.local.dao.PetDao {
        return database.petDao()
    }

    @Provides
    @Singleton
    fun provideInventoryItemDao(database: AppDatabase): com.example.mobile.data.local.dao.InventoryItemDao {
        return database.inventoryItemDao()
    }

    @Provides
    @Singleton
    fun provideAchievementDao(database: AppDatabase): com.example.mobile.data.local.dao.AchievementDao {
        return database.achievementDao()
    }


    @Provides
    @Singleton
    fun provideJournalEntryDao(database: AppDatabase): com.example.mobile.data.local.dao.JournalEntryDao {
        return database.journalEntryDao()
    }

    @Provides
    @Singleton
    fun provideStatisticsDao(database: AppDatabase): com.example.mobile.data.local.dao.StatisticsDao {
        return database.statisticsDao()
    }
}