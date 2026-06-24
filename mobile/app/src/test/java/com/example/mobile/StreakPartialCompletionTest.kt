package com.example.mobile

import com.example.mobile.data.local.dao.HabitCompletionDao
import com.example.mobile.data.local.dao.HabitDao
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.data.repository.HabitCompletionRepositoryImpl
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Calendar

/**
 * Tests for partial completion detection in HabitCompletionRepositoryImpl.
 *
 * Partial completion = at least 1 but not all habits completed on a given day.
 */
class StreakPartialCompletionTest {

    private lateinit var habitCompletionDao: HabitCompletionDao
    private lateinit var habitDao: HabitDao
    private lateinit var repository: HabitCompletionRepositoryImpl

    private val habit1 = HabitEntity(id = 1, name = "Hydration")
    private val habit2 = HabitEntity(id = 2, name = "Reading")
    private val habit3 = HabitEntity(id = 3, name = "Meditation")

    private fun dayStart(year: Int, month: Int, day: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Before
    fun setup() {
        habitCompletionDao = mock()
        habitDao = mock()
        repository = HabitCompletionRepositoryImpl(
            habitCompletionDao = habitCompletionDao,
            habitDao = habitDao,
            habitProgressRepository = mock(),
            statisticsDao = mock()
        )
    }

    @Test
    fun `partial completion - 1 of 3 habits done`() = runTest {
        val date = dayStart(2026, Calendar.JUNE, 15)
        whenever(habitDao.getAllHabits()).thenReturn(flowOf(listOf(habit1, habit2, habit3)))
        whenever(habitCompletionDao.getCompletionForHabitOnDateOnce(1, date)).thenReturn(mock())
        whenever(habitCompletionDao.getCompletionForHabitOnDateOnce(2, date)).thenReturn(null)
        whenever(habitCompletionDao.getCompletionForHabitOnDateOnce(3, date)).thenReturn(null)

        assertTrue("1 of 3 habits should be partial", repository.isPartialCompletionOnDate(date))
    }

    @Test
    fun `partial completion - 2 of 3 habits done`() = runTest {
        val date = dayStart(2026, Calendar.JUNE, 15)
        whenever(habitDao.getAllHabits()).thenReturn(flowOf(listOf(habit1, habit2, habit3)))
        whenever(habitCompletionDao.getCompletionForHabitOnDateOnce(1, date)).thenReturn(mock())
        whenever(habitCompletionDao.getCompletionForHabitOnDateOnce(2, date)).thenReturn(mock())
        whenever(habitCompletionDao.getCompletionForHabitOnDateOnce(3, date)).thenReturn(null)

        assertTrue("2 of 3 habits should be partial", repository.isPartialCompletionOnDate(date))
    }

    @Test
    fun `not partial - 0 habits done`() = runTest {
        val date = dayStart(2026, Calendar.JUNE, 15)
        whenever(habitDao.getAllHabits()).thenReturn(flowOf(listOf(habit1, habit2, habit3)))
        whenever(habitCompletionDao.getCompletionForHabitOnDateOnce(1, date)).thenReturn(null)
        whenever(habitCompletionDao.getCompletionForHabitOnDateOnce(2, date)).thenReturn(null)
        whenever(habitCompletionDao.getCompletionForHabitOnDateOnce(3, date)).thenReturn(null)

        assertFalse("0 habits should not be partial", repository.isPartialCompletionOnDate(date))
    }

    @Test
    fun `not partial - all 3 habits done`() = runTest {
        val date = dayStart(2026, Calendar.JUNE, 15)
        whenever(habitDao.getAllHabits()).thenReturn(flowOf(listOf(habit1, habit2, habit3)))
        whenever(habitCompletionDao.getCompletionForHabitOnDateOnce(1, date)).thenReturn(mock())
        whenever(habitCompletionDao.getCompletionForHabitOnDateOnce(2, date)).thenReturn(mock())
        whenever(habitCompletionDao.getCompletionForHabitOnDateOnce(3, date)).thenReturn(mock())

        assertFalse("all habits done should not be partial", repository.isPartialCompletionOnDate(date))
    }

    @Test
    fun `not partial - no habits exist`() = runTest {
        val date = dayStart(2026, Calendar.JUNE, 15)
        whenever(habitDao.getAllHabits()).thenReturn(flowOf(emptyList()))

        assertFalse("no habits exist should not be partial", repository.isPartialCompletionOnDate(date))
    }

    @Test
    fun `partial vs all - areAllHabitsCompletedOnDate agrees`() = runTest {
        val date = dayStart(2026, Calendar.JUNE, 15)

        // Full completion
        whenever(habitDao.getAllHabits()).thenReturn(flowOf(listOf(habit1, habit2)))
        whenever(habitCompletionDao.getCompletionForHabitOnDateOnce(1, date)).thenReturn(mock())
        whenever(habitCompletionDao.getCompletionForHabitOnDateOnce(2, date)).thenReturn(mock())

        assertTrue("all habits completed", repository.areAllHabitsCompletedOnDate(date))
        assertFalse("not partial when all completed", repository.isPartialCompletionOnDate(date))
    }

    @Test
    fun `hasAnyCompletionOnDate - partial returns true`() = runTest {
        val date = dayStart(2026, Calendar.JUNE, 15)
        whenever(habitCompletionDao.getCompletionCountOnDate(date)).thenReturn(2)

        assertTrue("any completion should be true for partial", repository.hasAnyCompletionOnDate(date))
    }

    @Test
    fun `hasAnyCompletionOnDate - no completion returns false`() = runTest {
        val date = dayStart(2026, Calendar.JUNE, 15)
        whenever(habitCompletionDao.getCompletionCountOnDate(date)).thenReturn(0)

        assertFalse("no completion should return false", repository.hasAnyCompletionOnDate(date))
    }

    @Test
    fun `single habit - completion is full not partial`() = runTest {
        val date = dayStart(2026, Calendar.JUNE, 15)
        whenever(habitDao.getAllHabits()).thenReturn(flowOf(listOf(habit1)))
        whenever(habitCompletionDao.getCompletionForHabitOnDateOnce(1, date)).thenReturn(mock())

        assertTrue("single habit completed is all", repository.areAllHabitsCompletedOnDate(date))
        assertFalse("single habit completed is not partial", repository.isPartialCompletionOnDate(date))
    }

    @Test
    fun `StatisticsEntity - freeze dates parsing roundtrip`() {
        val dates = setOf(1001L, 1005L, 1010L)
        val json = StatisticsEntity.freezeDatesToJson(dates)
        val parsed = StatisticsEntity.parseFreezeDates(json)

        assertEquals(dates, parsed)
    }

    @Test
    fun `StatisticsEntity - freeze dates parsing empty`() {
        val json = "[]"
        val parsed = StatisticsEntity.parseFreezeDates(json)

        assertTrue("empty json should parse to empty set", parsed.isEmpty())
    }

    @Test
    fun `StatisticsEntity - freeze dates to json empty`() {
        val json = StatisticsEntity.freezeDatesToJson(emptySet())

        assertEquals("[]", json)
    }
}
