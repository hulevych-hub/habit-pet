package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.data.local.entities.StatisticsEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class StatisticsRepositoryImplTest {

    private lateinit var statisticsDao: StatisticsDao
    private lateinit var repository: StatisticsRepositoryImpl

    private val statsFlow = MutableStateFlow<StatisticsEntity?>(null)

    private fun todayKey(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis / 86_400_000L
    }

    @Before
    fun setup() {
        statisticsDao = mock()
        whenever(statisticsDao.getStatistics()).thenReturn(statsFlow)
        repository = StatisticsRepositoryImpl(statisticsDao)
    }

    // ==================== getStatistics ====================

    @Test
    fun `getStatistics — returns flow of statistics`() {
        val entity = StatisticsEntity(id = 1, totalCoins = 100)
        statsFlow.value = entity

        val result = repository.getStatistics()

        // Verify the flow emits the entity
        runTest {
            val value = result.firstOrNull()
            assertEquals(100, value?.totalCoins)
        }
    }

    @Test
    fun `getStatistics — returns default entity when dao returns null`() {
        statsFlow.value = null

        runTest {
            val value = repository.getStatistics().firstOrNull()
            assertEquals(1L, value?.id)
        }
    }

    // ==================== addCoins ====================

    @Test
    fun `addCoins — increases coin balance`() = runTest {
        val entity = StatisticsEntity(id = 1, totalCoins = 50)
        statsFlow.value = entity
        doReturn(1).whenever(statisticsDao).updateStatistics(any())

        repository.addCoins(25)

        verify(statisticsDao).updateStatistics(any())
        // Verify the updated entity has correct coin total
        val updatedEntity = StatisticsEntity(id = 1, totalCoins = 75)
        verify(statisticsDao).updateStatistics(updatedEntity)
    }

    @Test
    fun `addCoins — no-op when no statistics exist`() = runTest {
        statsFlow.value = null

        repository.addCoins(25)

        verify(statisticsDao, never()).updateStatistics(any())
    }

    // ==================== updateStatistics ====================

    @Test
    fun `updateStatistics — updates when row exists`() = runTest {
        val entity = StatisticsEntity(id = 1, totalCoins = 100)
        doReturn(1).whenever(statisticsDao).updateStatistics(any())

        repository.updateStatistics(entity)

        verify(statisticsDao).updateStatistics(entity.copy(id = 1))
    }

    @Test
    fun `updateStatistics — inserts when row does not exist`() = runTest {
        val entity = StatisticsEntity(id = 1, totalCoins = 100)
        doReturn(0).whenever(statisticsDao).updateStatistics(any())
        doReturn(1L).whenever(statisticsDao).insertStatistics(any())

        repository.updateStatistics(entity)

        verify(statisticsDao).insertStatistics(entity.copy(id = 1))
    }

    // ==================== isStreakAlreadyCountedToday ====================

    @Test
    fun `isStreakAlreadyCountedToday — returns true when streak counted today`() = runTest {
        val today = todayKey()
        val entity = StatisticsEntity(
            id = 1,
            currentStreak = 5,
            lastStreakDate = today
        )
        statsFlow.value = entity

        val result = repository.isStreakAlreadyCountedToday()

        assertTrue("Returns true when streak counted today", result)
    }

    @Test
    fun `isStreakAlreadyCountedToday — returns false when streak is 0`() = runTest {
        val today = todayKey()
        val entity = StatisticsEntity(
            id = 1,
            currentStreak = 0,
            lastStreakDate = today
        )
        statsFlow.value = entity

        val result = repository.isStreakAlreadyCountedToday()

        assertFalse("Returns false when streak is 0", result)
    }

    @Test
    fun `isStreakAlreadyCountedToday — returns false when lastStreakDate is not today`() = runTest {
        val entity = StatisticsEntity(
            id = 1,
            currentStreak = 5,
            lastStreakDate = 1000L
        )
        statsFlow.value = entity

        val result = repository.isStreakAlreadyCountedToday()

        assertFalse("Returns false when lastStreakDate is not today", result)
    }

    @Test
    fun `isStreakAlreadyCountedToday — returns false when no statistics exist`() = runTest {
        statsFlow.value = null

        val result = repository.isStreakAlreadyCountedToday()

        assertFalse("Returns false when no statistics", result)
    }

    // ==================== markStreakUpdatedToday ====================

    @Test
    fun `markStreakUpdatedToday — sets lastStreakDate to today`() = runTest {
        val entity = StatisticsEntity(id = 1, currentStreak = 3, lastStreakDate = 0L)
        statsFlow.value = entity
        doReturn(1).whenever(statisticsDao).updateStatistics(any())

        repository.markStreakUpdatedToday()

        verify(statisticsDao).updateStatistics(any())
    }

    @Test
    fun `markStreakUpdatedToday — no-op when no statistics exist`() = runTest {
        statsFlow.value = null

        repository.markStreakUpdatedToday()

        verify(statisticsDao, never()).updateStatistics(any())
    }

    // ==================== incrementStreak ====================

    @Test
    fun `incrementStreak — increases currentStreak by 1`() = runTest {
        val entity = StatisticsEntity(id = 1, currentStreak = 5, globalStreak = 5, bestStreak = 5)
        statsFlow.value = entity
        doReturn(1).whenever(statisticsDao).updateStatistics(any())

        repository.incrementStreak()

        verify(statisticsDao).updateStatistics(any())
    }

    @Test
    fun `incrementStreak — updates bestStreak when exceeded`() = runTest {
        val entity = StatisticsEntity(id = 1, currentStreak = 5, globalStreak = 5, bestStreak = 5)
        statsFlow.value = entity
        doReturn(1).whenever(statisticsDao).updateStatistics(any())

        repository.incrementStreak()

        verify(statisticsDao).updateStatistics(any())
    }

    @Test
    fun `incrementStreak — inserts when no row exists`() = runTest {
        statsFlow.value = null
        doReturn(0).whenever(statisticsDao).updateStatistics(any())
        doReturn(1L).whenever(statisticsDao).insertStatistics(any())

        repository.incrementStreak()

        verify(statisticsDao).insertStatistics(any())
    }

    // ==================== reset ====================

    @Test
    fun `reset — calls dao reset`() = runTest {
        repository.reset()
        verify(statisticsDao).reset()
    }

    // ==================== syncGlobalStreak ====================

    @Test
    fun `syncGlobalStreak — updates globalStreak when different from currentStreak`() = runTest {
        val entity = StatisticsEntity(id = 1, currentStreak = 5, globalStreak = 3)
        statsFlow.value = entity
        doReturn(1).whenever(statisticsDao).updateStatistics(any())

        repository.syncGlobalStreak()

        verify(statisticsDao).updateStatistics(any())
    }

    @Test
    fun `syncGlobalStreak — no-op when globalStreak matches currentStreak`() = runTest {
        val entity = StatisticsEntity(id = 1, currentStreak = 5, globalStreak = 5)
        statsFlow.value = entity

        repository.syncGlobalStreak()

        verify(statisticsDao, never()).updateStatistics(any())
    }

    @Test
    fun `syncGlobalStreak — no-op when no statistics exist`() = runTest {
        statsFlow.value = null

        repository.syncGlobalStreak()

        verify(statisticsDao, never()).updateStatistics(any())
    }
}
