package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.HabitDao
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao
) : HabitRepository {
    override fun getAllHabits(): Flow<List<HabitEntity>> = habitDao.getAllHabits()

    override fun getHabitById(habitId: Long): Flow<HabitEntity?> = habitDao.getHabitById(habitId)

    override suspend fun addHabit(habit: HabitEntity): Long = habitDao.insertHabit(habit)

    override suspend fun updateHabit(habit: HabitEntity): Int = habitDao.updateHabit(habit)

    override suspend fun deleteHabit(habit: HabitEntity): Int = habitDao.deleteHabit(habit)
}