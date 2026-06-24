package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.HabitCompletionDao
import com.example.mobile.data.local.dao.HabitDao
import com.example.mobile.data.local.dao.RecentCompletionsStats
import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.HabitProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
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

class HabitCompletionRepositoryImplTest {

    private lateinit var habitCompletionDao: HabitCompletionDao
    private lateinit var habitDao: HabitDao
    private lateinit var habitProgressRepository: HabitProgressRepository
    private lateinit var statisticsDao: StatisticsDao
    private lateinit var repository: HabitCompletionRepositoryImpl

    private val statsFlow = MutableStateFlow<StatisticsEntity?>(null)

    private val testHabit = HabitEntity(
        id = 1, name = "Morning jog", icon = "run",
        type = "CHECKBOX", currentStreak = 3, bestStreak = 5
    )

    private val testCompletion = HabitCompletionEntity(
        id = 0, habitId = 1, date = 100L, xpEarned = 10
    )

    @Before
    fun setup() {
        habitCompletionDao = mock()
        habitDao = mock()
        habitProgressRepository = mock()
        statisticsDao = mock()

        whenever(statisticsDao.getStatistics()).thenReturn(statsFlow)

        repository = HabitCompletionRepositoryImpl(
            habitCompletionDao = habitCompletionDao,
            habitDao = habitDao,
            habitProgressRepository = habitProgressRepository,
            statisticsDao = statisticsDao
        )
    }

    // ==================== getCompletionsForHabit ====================

    @Test
    fun `getCompletionsForHabit — delegates to dao`() {
        val completions = listOf(
            HabitCompletionEntity(id = 1, habitId = 1, date = 100L, xpEarned = 10)
        )
        whenever(habitCompletionDao.getCompletionsForHabit(1L, 100L, 200L))
            .thenReturn(flowOf(completions))

        val result = repository.getCompletionsForHabit(1L, 100L, 200L)

        runTest {
            val value = result.firstOrNull()
            assertEquals(1, value?.size)
            assertEquals(10L, value?.first()?.xpEarned)
        }
    }

    // ==================== getRecentCompletionsStats ====================

    @Test
    fun `getRecentCompletionsStats — delegates to dao`() = runTest {
        val stats = RecentCompletionsStats(count = 5, lastActivityTimestamp = 1000L)
        doReturn(stats).whenever(habitCompletionDao).getRecentCompletionsStats(100L, 200L)

        val result = repository.getRecentCompletionsStats(100L, 200L)

        assertEquals(5, result.count)
        assertEquals(1000L, result.lastActivityTimestamp)
    }

    // ==================== getCompletionForHabitOnDate ====================

    @Test
    fun `getCompletionForHabitOnDate — delegates to dao`() {
        val completion = HabitCompletionEntity(id = 1, habitId = 1, date = 100L, xpEarned = 10)
        whenever(habitCompletionDao.getCompletionForHabitOnDate(1L, 100L))
            .thenReturn(flowOf(completion))

        val result = repository.getCompletionForHabitOnDate(1L, 100L)

        runTest {
            val value = result.firstOrNull()
            assertEquals(10L, value?.xpEarned)
        }
    }

    // ==================== addCompletion ====================

    @Test
    fun `addCompletion — delegates to addCompletionWithCombo and returns id`() = runTest {
        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, 100L)
        statsFlow.value = StatisticsEntity(id = 1, lastHabitCompletionTimestamp = 0L)
        doReturn(1L).whenever(habitCompletionDao).insertCompletion(any())
        whenever(habitDao.getHabitById(1L)).thenReturn(flowOf(testHabit))
        doReturn(1).whenever(habitDao).updateHabit(any())
        doReturn(0).whenever(habitCompletionDao).getActiveDayCount()
        doReturn(1).whenever(statisticsDao).updateStatistics(any())

        val result = repository.addCompletion(testCompletion)

        assertTrue("Result should be positive", result > 0)
        verify(habitCompletionDao).insertCompletion(any())
    }

    // ==================== addCompletionWithCombo ====================

    @Test
    fun `addCompletionWithCombo — returns existing completion when already completed`() = runTest {
        val existing = HabitCompletionEntity(id = 5, habitId = 1, date = 100L, xpEarned = 10)
        doReturn(existing).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, 100L)

        val result = repository.addCompletionWithCombo(testCompletion)

        assertFalse("Should not be new completion", result.isNewCompletion)
        assertEquals(5L, result.completionId)
        assertEquals(10L, result.totalXpEarned)
        verify(habitCompletionDao, never()).insertCompletion(any())
    }

    @Test
    fun `addCompletionWithCombo — inserts new completion when none exists`() = runTest {
        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, 100L)
        statsFlow.value = StatisticsEntity(id = 1, lastHabitCompletionTimestamp = 0L)
        doReturn(1L).whenever(habitCompletionDao).insertCompletion(any())
        whenever(habitDao.getHabitById(1L)).thenReturn(flowOf(testHabit))
        doReturn(1).whenever(habitDao).updateHabit(any())
        doReturn(0).whenever(habitCompletionDao).getActiveDayCount()
        doReturn(1).whenever(statisticsDao).updateStatistics(any())

        val result = repository.addCompletionWithCombo(testCompletion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals(1L, result.completionId)
        verify(habitCompletionDao).insertCompletion(any())
    }

    @Test
    fun `addCompletionWithCombo — applies combo bonus when combo is active`() = runTest {
        val now = System.currentTimeMillis()
        val twoHoursMs = 2L * 60L * 60L * 1000L
        val completion = testCompletion.copy(id = now, date = now)

        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, now)
        statsFlow.value = StatisticsEntity(
            id = 1,
            lastHabitCompletionTimestamp = now - twoHoursMs,
            currentCombo = 2
        )
        doReturn(1L).whenever(habitCompletionDao).insertCompletion(any())
        whenever(habitDao.getHabitById(1L)).thenReturn(flowOf(testHabit))
        doReturn(1).whenever(habitDao).updateHabit(any())
        doReturn(0).whenever(habitCompletionDao).getActiveDayCount()
        doReturn(1).whenever(statisticsDao).updateStatistics(any())

        val result = repository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals(3, result.combo)
        assertTrue("Combo bonus should be > 0", result.comboBonusXp > 0)
        assertEquals(
            completion.xpEarned + result.comboBonusXp,
            result.totalXpEarned
        )
    }

    @Test
    fun `addCompletionWithCombo — resets combo when combo is not active`() = runTest {
        val now = System.currentTimeMillis()
        val completion = testCompletion.copy(id = now, date = now)

        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, now)
        statsFlow.value = StatisticsEntity(
            id = 1,
            lastHabitCompletionTimestamp = 0L,
            currentCombo = 5
        )
        doReturn(1L).whenever(habitCompletionDao).insertCompletion(any())
        whenever(habitDao.getHabitById(1L)).thenReturn(flowOf(testHabit))
        doReturn(1).whenever(habitDao).updateHabit(any())
        doReturn(0).whenever(habitCompletionDao).getActiveDayCount()
        doReturn(1).whenever(statisticsDao).updateStatistics(any())

        val result = repository.addCompletionWithCombo(completion)

        assertEquals(1, result.combo)
        assertEquals(0L, result.comboBonusXp)
    }

    @Test
    fun `addCompletionWithCombo — returns -1 when insert fails`() = runTest {
        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, 100L)
        statsFlow.value = StatisticsEntity(id = 1, lastHabitCompletionTimestamp = 0L)
        doReturn(-1L).whenever(habitCompletionDao).insertCompletion(any())

        val result = repository.addCompletionWithCombo(testCompletion)

        assertFalse("Should not be new completion on failure", result.isNewCompletion)
        assertEquals(-1L, result.completionId)
    }

    @Test
    fun `addCompletionWithCombo — creates default stats when none exist`() = runTest {
        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, 100L)
        statsFlow.value = null
        doReturn(1L).whenever(habitCompletionDao).insertCompletion(any())
        whenever(habitDao.getHabitById(1L)).thenReturn(flowOf(testHabit))
        doReturn(1).whenever(habitDao).updateHabit(any())
        doReturn(0).whenever(habitCompletionDao).getActiveDayCount()
        doReturn(0).whenever(statisticsDao).updateStatistics(any())
        doReturn(1L).whenever(statisticsDao).insertStatistics(any())

        val result = repository.addCompletionWithCombo(testCompletion)

        assertTrue("Should be new completion", result.isNewCompletion)
        verify(statisticsDao).insertStatistics(any())
    }

    @Test
    fun `addCompletionWithCombo — detects combo milestone`() = runTest {
        val now = System.currentTimeMillis()
        val twoHoursMs = 2L * 60L * 60L * 1000L
        val completion = testCompletion.copy(id = now, date = now)

        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, now)
        statsFlow.value = StatisticsEntity(
            id = 1,
            lastHabitCompletionTimestamp = now - twoHoursMs,
            currentCombo = 4
        )
        doReturn(1L).whenever(habitCompletionDao).insertCompletion(any())
        whenever(habitDao.getHabitById(1L)).thenReturn(flowOf(testHabit))
        doReturn(1).whenever(habitDao).updateHabit(any())
        doReturn(0).whenever(habitCompletionDao).getActiveDayCount()
        doReturn(1).whenever(statisticsDao).updateStatistics(any())

        val result = repository.addCompletionWithCombo(completion)

        assertEquals(5, result.combo)
        assertTrue("Combo milestone should be reached at 5", result.comboMilestoneReached)
    }

    // ==================== updateCompletion ====================

    @Test
    fun `updateCompletion — delegates to dao`() = runTest {
        val completion = HabitCompletionEntity(id = 1, habitId = 1, date = 100L, xpEarned = 15)
        doReturn(1).whenever(habitCompletionDao).updateCompletion(any())

        val result = repository.updateCompletion(completion)

        assertEquals(1, result)
        verify(habitCompletionDao).updateCompletion(completion)
    }

    // ==================== deleteCompletion ====================

    @Test
    fun `deleteCompletion — delegates to dao`() = runTest {
        val completion = HabitCompletionEntity(id = 1, habitId = 1, date = 100L, xpEarned = 10)
        doReturn(1).whenever(habitCompletionDao).deleteCompletion(any())

        val result = repository.deleteCompletion(completion)

        assertEquals(1, result)
        verify(habitCompletionDao).deleteCompletion(completion)
    }

    // ==================== deleteAll ====================

    @Test
    fun `deleteAll — delegates to dao`() = runTest {
        repository.deleteAll()
        verify(habitCompletionDao).deleteAll()
    }

    // ==================== hasAnyCompletionOnDate ====================

    @Test
    fun `hasAnyCompletionOnDate — returns true when completions exist`() = runTest {
        doReturn(3).whenever(habitCompletionDao).getCompletionCountOnDate(100L)

        val result = repository.hasAnyCompletionOnDate(100L)

        assertTrue("Should return true when completions exist", result)
    }

    @Test
    fun `hasAnyCompletionOnDate — returns false when no completions`() = runTest {
        doReturn(0).whenever(habitCompletionDao).getCompletionCountOnDate(100L)

        val result = repository.hasAnyCompletionOnDate(100L)

        assertFalse("Should return false when no completions", result)
    }

    // ==================== areAllHabitsCompletedOnDate ====================

    @Test
    fun `areAllHabitsCompletedOnDate — returns true when all habits completed`() = runTest {
        val habits = listOf(
            HabitEntity(id = 1, name = "Habit 1"),
            HabitEntity(id = 2, name = "Habit 2")
        )
        whenever(habitDao.getAllHabits()).thenReturn(flowOf(habits))
        doReturn(HabitCompletionEntity(id = 1, habitId = 1, date = 100L, xpEarned = 10))
            .whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, 100L)
        doReturn(HabitCompletionEntity(id = 2, habitId = 2, date = 100L, xpEarned = 10))
            .whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(2L, 100L)

        val result = repository.areAllHabitsCompletedOnDate(100L)

        assertTrue("All habits completed", result)
    }

    @Test
    fun `areAllHabitsCompletedOnDate — returns false when not all completed`() = runTest {
        val habits = listOf(
            HabitEntity(id = 1, name = "Habit 1"),
            HabitEntity(id = 2, name = "Habit 2")
        )
        whenever(habitDao.getAllHabits()).thenReturn(flowOf(habits))
        doReturn(HabitCompletionEntity(id = 1, habitId = 1, date = 100L, xpEarned = 10))
            .whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, 100L)
        doReturn(null)
            .whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(2L, 100L)

        val result = repository.areAllHabitsCompletedOnDate(100L)

        assertFalse("Not all habits completed", result)
    }

    @Test
    fun `areAllHabitsCompletedOnDate — returns true when no habits exist`() = runTest {
        whenever(habitDao.getAllHabits()).thenReturn(flowOf(emptyList()))

        val result = repository.areAllHabitsCompletedOnDate(100L)

        assertTrue("No habits means vacuously all completed", result)
    }

    @Test
    fun `areAllHabitsCompletedOnDate — returns true when habits flow emits null`() = runTest {
        @Suppress("UNCHECKED_CAST")
        whenever(habitDao.getAllHabits()).thenReturn(
            MutableStateFlow(null) as MutableStateFlow<List<HabitEntity>>
        )

        val result = repository.areAllHabitsCompletedOnDate(100L)

        assertTrue("Null habits means vacuously all completed", result)
    }

    // ==================== isPartialCompletionOnDate ====================

    @Test
    fun `isPartialCompletionOnDate — returns true when some habits completed`() = runTest {
        val habits = listOf(
            HabitEntity(id = 1, name = "Habit 1"),
            HabitEntity(id = 2, name = "Habit 2"),
            HabitEntity(id = 3, name = "Habit 3")
        )
        whenever(habitDao.getAllHabits()).thenReturn(flowOf(habits))
        doReturn(HabitCompletionEntity(id = 1, habitId = 1, date = 100L, xpEarned = 10))
            .whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, 100L)
        doReturn(null)
            .whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(2L, 100L)
        doReturn(null)
            .whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(3L, 100L)

        val result = repository.isPartialCompletionOnDate(100L)

        assertTrue("Partial completion", result)
    }

    @Test
    fun `isPartialCompletionOnDate — returns false when all habits completed`() = runTest {
        val habits = listOf(
            HabitEntity(id = 1, name = "Habit 1"),
            HabitEntity(id = 2, name = "Habit 2")
        )
        whenever(habitDao.getAllHabits()).thenReturn(flowOf(habits))
        doReturn(HabitCompletionEntity(id = 1, habitId = 1, date = 100L, xpEarned = 10))
            .whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, 100L)
        doReturn(HabitCompletionEntity(id = 2, habitId = 2, date = 100L, xpEarned = 10))
            .whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(2L, 100L)

        val result = repository.isPartialCompletionOnDate(100L)

        assertFalse("All completed is not partial", result)
    }

    @Test
    fun `isPartialCompletionOnDate — returns false when no habits completed`() = runTest {
        val habits = listOf(
            HabitEntity(id = 1, name = "Habit 1"),
            HabitEntity(id = 2, name = "Habit 2")
        )
        whenever(habitDao.getAllHabits()).thenReturn(flowOf(habits))
        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, 100L)
        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(2L, 100L)

        val result = repository.isPartialCompletionOnDate(100L)

        assertFalse("None completed is not partial", result)
    }

    @Test
    fun `isPartialCompletionOnDate — returns false when no habits exist`() = runTest {
        whenever(habitDao.getAllHabits()).thenReturn(flowOf(emptyList()))

        val result = repository.isPartialCompletionOnDate(100L)

        assertFalse("No habits means not partial", result)
    }
}
