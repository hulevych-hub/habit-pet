package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.HabitProgressDao
import com.example.mobile.data.local.entities.HabitProgressEntity
import com.example.mobile.domain.repository.HabitProgressRepository
import javax.inject.Inject

class HabitProgressRepositoryImpl @Inject constructor(
    private val dao: HabitProgressDao
) : HabitProgressRepository {

    override fun getProgress(habitId: Long, date: Long) = dao.getProgress(habitId, date)

    override suspend fun updateProgress(entity: HabitProgressEntity) {
        dao.upsert(entity)
    }

    override suspend fun reset(habitId: Long) {
        dao.clear(habitId)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }
}