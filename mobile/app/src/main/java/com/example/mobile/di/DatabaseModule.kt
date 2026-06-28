package com.example.mobile.di

import android.content.Context
import androidx.room.Room
import com.example.mobile.data.local.database.AppDatabase
import com.example.mobile.data.local.database.ChallengeDatabaseInitializer
import com.example.mobile.data.local.database.InventoryItemDatabaseInitializer
import com.example.mobile.data.local.database.MIGRATION_12_13
import com.example.mobile.data.local.database.MIGRATION_13_14
import com.example.mobile.data.local.database.MIGRATION_14_15
import com.example.mobile.data.local.database.MIGRATION_17_18
import com.example.mobile.data.local.database.MIGRATION_18_19
import com.example.mobile.data.local.database.MIGRATION_19_20
import com.example.mobile.data.local.database.MIGRATION_20_21
import com.example.mobile.data.local.database.MIGRATION_21_22
import com.example.mobile.data.local.database.StatisticsDatabaseInitializer
import com.example.mobile.domain.CollectionSetEngine
import com.example.mobile.domain.DailyLoginStreakEngine
import com.example.mobile.domain.DragonMoodEngine
import com.example.mobile.domain.ChallengeEngine
import com.example.mobile.domain.StreakEngine
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.ChallengeRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.feedback.MicroFeedbackManager
import com.example.mobile.presentation.ui.reward.RewardEventBus
import com.example.mobile.presentation.ui.reward.RewardManager
import com.example.mobile.presentation.ui.reward.RewardQueue
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
        .addMigrations(MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22)
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
    fun provideGameEventDao(database: AppDatabase): com.example.mobile.data.local.dao.GameEventDao {
        return database.gameEventDao()
    }

    @Provides
    @Singleton
    fun provideStatisticsDao(database: AppDatabase): com.example.mobile.data.local.dao.StatisticsDao {
        return database.statisticsDao()
    }

    @Provides
    @Singleton
    fun provideHabitProgressDao(database: AppDatabase): com.example.mobile.data.local.dao.HabitProgressDao {
        return database.habitProgressDao()
    }

    @Provides
    @Singleton
    fun provideChallengeDao(database: AppDatabase): com.example.mobile.data.local.dao.ChallengeDao {
        return database.challengeDao()
    }

    @Provides
    @Singleton
    fun provideRewardEventBus(): RewardEventBus {
        return RewardEventBus()
    }

    @Provides
    @Singleton
    fun provideMicroFeedbackManager(): MicroFeedbackManager {
        return MicroFeedbackManager()
    }

    @Provides
    @Singleton
    fun provideAchievementInitializer(database: AppDatabase): com.example.mobile.data.local.database.AchievementDatabaseInitializer {
        return com.example.mobile.data.local.database.AchievementDatabaseInitializer(database)
    }

    @Provides
    @Singleton
    fun provideStatisticsDatabaseInitializer(
        database: AppDatabase
    ): StatisticsDatabaseInitializer {
        return StatisticsDatabaseInitializer(database.statisticsDao())
    }

    @Provides
    @Singleton
    fun provideInventoryItemDatabaseInitializer(
        database: AppDatabase,
        inventoryItemRepository: InventoryItemRepository
    ): InventoryItemDatabaseInitializer {
        return InventoryItemDatabaseInitializer(database, inventoryItemRepository)
    }

    @Provides
    @Singleton
    fun provideChallengeDatabaseInitializer(
        database: AppDatabase
    ): ChallengeDatabaseInitializer {
        return ChallengeDatabaseInitializer(database.challengeDao())
    }

    @Provides
    @Singleton
    fun provideAchievementEngine(
        achievementRepository: com.example.mobile.domain.repository.AchievementRepository,
        habitRepository: com.example.mobile.domain.repository.HabitRepository,
        petRepository: com.example.mobile.domain.repository.PetRepository,
        statisticsRepository: com.example.mobile.domain.repository.StatisticsRepository,
        inventoryItemRepository: com.example.mobile.domain.repository.InventoryItemRepository,
        achievementRewardProcessor: com.example.mobile.domain.AchievementRewardProcessor,
        activityTimelineEngine: com.example.mobile.domain.ActivityTimelineEngine,
        gameEventRepository: com.example.mobile.domain.repository.GameEventRepository
    ): com.example.mobile.domain.AchievementEngine {
        return com.example.mobile.domain.AchievementEngine(
            achievementRepository,
            habitRepository,
            petRepository,
            statisticsRepository,
            inventoryItemRepository,
            achievementRewardProcessor,
            activityTimelineEngine,
            gameEventRepository
        )
    }

    @Provides
    @Singleton
    fun provideDragonMoodEngine(
        petRepository: PetRepository,
        statisticsRepository: StatisticsRepository,
        habitCompletionRepository: HabitCompletionRepository
    ): DragonMoodEngine {
        return DragonMoodEngine(
            petRepository,
            statisticsRepository,
            habitCompletionRepository
        )
    }

    @Provides
    @Singleton
    fun provideStreakEngine(
        habitRepository: HabitRepository,
        habitCompletionRepository: HabitCompletionRepository,
        statisticsRepository: StatisticsRepository,
        rewardQueue: RewardQueue,
        inventoryItemRepository: InventoryItemRepository,
        activityTimelineEngine: com.example.mobile.domain.ActivityTimelineEngine,
        dragonMoodEngine: DragonMoodEngine,
        challengeRepository: ChallengeRepository
    ): StreakEngine {
        return StreakEngine(
            habitRepository,
            habitCompletionRepository,
            statisticsRepository,
            rewardQueue,
            inventoryItemRepository,
            activityTimelineEngine,
            dragonMoodEngine,
            challengeRepository
        )
    }

    @Provides
    @Singleton
    fun provideDailyLoginStreakEngine(
        statisticsRepository: StatisticsRepository,
        rewardQueue: RewardQueue
    ): DailyLoginStreakEngine {
        return DailyLoginStreakEngine(
            statisticsRepository,
            rewardQueue
        )
    }

    @Provides
    @Singleton
    fun provideCollectionSetEngine(
        inventoryItemRepository: InventoryItemRepository,
        petRepository: PetRepository,
        statisticsRepository: StatisticsRepository,
        rewardQueue: RewardQueue
    ): CollectionSetEngine {
        return CollectionSetEngine(
            inventoryItemRepository,
            petRepository,
            statisticsRepository,
            rewardQueue
        )
    }

    @Provides
    @Singleton
    fun provideChallengeEngine(
        challengeRepository: ChallengeRepository,
        inventoryItemRepository: InventoryItemRepository,
        activityTimelineEngine: com.example.mobile.domain.ActivityTimelineEngine,
        rewardQueue: RewardQueue
    ): ChallengeEngine {
        return ChallengeEngine(
            challengeRepository,
            inventoryItemRepository,
            activityTimelineEngine,
            rewardQueue
        )
    }

    @Provides
    @Singleton
    fun provideActivityTimelineEngine(
        gameEventRepository: com.example.mobile.domain.repository.GameEventRepository,
        rewardEventBus: RewardEventBus,
        petRepository: PetRepository,
        statisticsRepository: StatisticsRepository,
        challengeRepository: ChallengeRepository,
        @ApplicationContext appContext: Context
    ): com.example.mobile.domain.ActivityTimelineEngine {
        return com.example.mobile.domain.ActivityTimelineEngine(
            gameEventRepository,
            rewardEventBus,
            petRepository,
            statisticsRepository,
            challengeRepository,
            appContext
        )
    }

    @Provides
    @Singleton
    fun provideRewardQueue(): RewardQueue {
        return RewardQueue()
    }

    @Provides
    @Singleton
    fun provideRewardManager(
        rewardQueue: RewardQueue,
        statisticsRepository: StatisticsRepository,
        petRepository: PetRepository,
        inventoryItemRepository: InventoryItemRepository,
        rewardEventBus: RewardEventBus,
        activityTimelineEngine: com.example.mobile.domain.ActivityTimelineEngine,
        microFeedbackManager: MicroFeedbackManager,
        challengeRepository: ChallengeRepository
    ): RewardManager {
        return RewardManager(
            rewardQueue,
            statisticsRepository,
            petRepository,
            inventoryItemRepository,
            rewardEventBus,
            activityTimelineEngine,
            microFeedbackManager,
            challengeRepository
        )
    }
}