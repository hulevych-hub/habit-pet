package com.example.mobile.di

import com.example.mobile.domain.repository.HabitProgressRepository
import com.example.mobile.data.repository.HabitProgressRepositoryImpl
import com.example.mobile.data.repository.AchievementRepositoryImpl
import com.example.mobile.data.repository.HabitCompletionRepositoryImpl
import com.example.mobile.data.repository.HabitRepositoryImpl
import com.example.mobile.data.repository.InventoryItemRepositoryImpl
import com.example.mobile.data.repository.JournalEntryRepositoryImpl
import com.example.mobile.data.repository.PetRepositoryImpl
import com.example.mobile.data.repository.StatisticsRepositoryImpl
import com.example.mobile.domain.repository.AchievementRepository
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.JournalEntryRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindHabitRepository(repository: HabitRepositoryImpl): HabitRepository

    @Binds
    abstract fun bindHabitCompletionRepository(repository: HabitCompletionRepositoryImpl): HabitCompletionRepository

    @Binds
    abstract fun bindAchievementRepository(repository: AchievementRepositoryImpl): AchievementRepository

    @Binds
    abstract fun bindInventoryItemRepository(repository: InventoryItemRepositoryImpl): InventoryItemRepository

    @Binds
    abstract fun bindJournalEntryRepository(repository: JournalEntryRepositoryImpl): JournalEntryRepository

    @Binds
    abstract fun bindPetRepository(repository: PetRepositoryImpl): PetRepository

    @Binds
    abstract fun bindStatisticsRepository(repository: StatisticsRepositoryImpl): StatisticsRepository

    @Binds
    abstract fun bindHabitProgressRepository(
        repository: HabitProgressRepositoryImpl
    ): HabitProgressRepository
}
