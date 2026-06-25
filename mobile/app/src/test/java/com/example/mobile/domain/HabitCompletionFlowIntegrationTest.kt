package com.example.mobile.domain

import com.example.mobile.data.local.dao.HabitCompletionDao
import com.example.mobile.data.local.dao.HabitDao
import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.data.repository.HabitCompletionRepositoryImpl
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitProgressRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.StatisticsRepository
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

/**
 * Integration test that exercises the full habit completion flow:
 * HabitCompletionRepository -> StatisticsRepository -> HabitDao
 *
 * This test verifies that completing a habit correctly:
 * 1. Inserts a completion record
 * 2. Updates statistics (coins, completions)
 * 3. Applies combo bonus when applicable
 * 4. Handles duplicate completions (idempotent)
 */
class HabitCompletionFlowIntegrationTest {

    private lateinit var habitCompletionDao: HabitCompletionDao
    private lateinit var habitDao: HabitDao
    private lateinit var habitProgressRepository: HabitProgressRepository
    private lateinit var statisticsDao: StatisticsDao
    private lateinit var habitRepository: HabitRepository
    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var habitCompletionRepository: HabitCompletionRepository

    private val statsFlow = MutableStateFlow<StatisticsEntity>(StatisticsEntity())
    private val habitsFlow = MutableStateFlow<List<HabitEntity>>(emptyList())

    private val testHabit = HabitEntity(
        id = 1,
        name = "Morning jog",
        icon = "run",
        type = "CHECKBOX",
        currentStreak = 0,
        bestStreak = 0
    )

    @Before
    fun setup() {
        habitCompletionDao = mock()
        habitDao = mock()
        habitProgressRepository = mock()
        statisticsDao = mock()

        whenever(statisticsDao.getStatistics()).thenReturn(statsFlow)
        whenever(habitDao.getAllHabits()).thenReturn(habitsFlow)
        whenever(habitDao.getHabitById(1L)).thenReturn(flowOf(testHabit))

        habitRepository = mock()
        whenever(habitRepository.getAllHabits()).thenReturn(habitsFlow)
        whenever(habitRepository.getHabitById(1L)).thenReturn(flowOf(testHabit))

        statisticsRepository = mock()
        whenever(statisticsRepository.getStatistics()).thenReturn(statsFlow)

        habitCompletionRepository = HabitCompletionRepositoryImpl(
            habitCompletionDao = habitCompletionDao,
            habitDao = habitDao,
            habitProgressRepository = habitProgressRepository,
            statisticsDao = statisticsDao
        )
    }

    @Test
    fun `completeCheckboxHabit — first completion awards XP and coins`() = runTest {
        val today = System.currentTimeMillis()
        val completion = HabitCompletionEntity(
            id = 0,
            habitId = 1,
            date = today,
            xpEarned = 10
        )

        // No existing completion for today
        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, today)

        // No combo (first completion)
        statsFlow.value = StatisticsEntity(
            id = 1,
            lastHabitCompletionTimestamp = 0L,
            currentCombo = 0,
            totalCoins = 0,
            totalCompletions = 0
        )

        doReturn(1L).whenever(habitCompletionDao).insertCompletion(any())
        doReturn(1).whenever(habitDao).updateHabit(any())
        doReturn(0).whenever(habitCompletionDao).getActiveDayCount()
        doReturn(1).whenever(statisticsDao).updateStatistics(any())

        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals(1L, result.completionId)
        assertEquals(1, result.combo) // First completion = combo 1
        assertEquals(0L, result.comboBonusXp) // No combo bonus on first completion

        verify(habitCompletionDao).insertCompletion(any())
        verify(statisticsDao).updateStatistics(any())
    }

    @Test
    fun `completeCheckboxHabit — duplicate completion is idempotent`() = runTest {
        val today = System.currentTimeMillis()
        val completion = HabitCompletionEntity(
            id = 0,
            habitId = 1,
            date = today,
            xpEarned = 10
        )

        // Existing completion for today
        val existingCompletion = HabitCompletionEntity(
            id = 5,
            habitId = 1,
            date = today,
            xpEarned = 10
        )
        doReturn(existingCompletion).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, today)

        statsFlow.value = StatisticsEntity(
            id = 1,
            totalCoins = 10,
            totalCompletions = 1
        )

        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertFalse("Should NOT be new completion", result.isNewCompletion)
        assertEquals(5L, result.completionId) // Returns existing completion ID
        assertEquals(10L, result.totalXpEarned) // Returns existing XP

        verify(habitCompletionDao, never()).insertCompletion(any())
        verify(statisticsDao, never()).updateStatistics(any())
    }

    @Test
    fun `completeCheckboxHabit — combo bonus applied within 2 hours`() = runTest {
        val now = System.currentTimeMillis()
        val twoHoursMs = 2L * 60L * 60L * 1000L
        val completion = HabitCompletionEntity(
            id = 0,
            habitId = 1,
            date = now,
            xpEarned = 10
        )

        // No existing completion for today
        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, now)

        // Combo is active (last completion was 1 hour ago)
        statsFlow.value = StatisticsEntity(
            id = 1,
            lastHabitCompletionTimestamp = now - twoHoursMs / 2, // 1 hour ago
            currentCombo = 2,
            totalCoins = 20,
            totalCompletions = 2
        )

        doReturn(1L).whenever(habitCompletionDao).insertCompletion(any())
        doReturn(1).whenever(habitDao).updateHabit(any())
        doReturn(0).whenever(habitCompletionDao).getActiveDayCount()
        doReturn(1).whenever(statisticsDao).updateStatistics(any())

        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals(3, result.combo) // Combo incremented
        assertTrue("Combo bonus should be > 0", result.comboBonusXp > 0)
        assertEquals(
            completion.xpEarned + result.comboBonusXp,
            result.totalXpEarned
        )
    }

    @Test
    fun `completeCheckboxHabit — combo resets after 2 hours`() = runTest {
        val now = System.currentTimeMillis()
        val twoHoursMs = 2L * 60L * 60L * 1000L
        val completion = HabitCompletionEntity(
            id = 0,
            habitId = 1,
            date = now,
            xpEarned = 10
        )

        // No existing completion for today
        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, now)

        // Combo was active but last completion was 3 hours ago (expired)
        statsFlow.value = StatisticsEntity(
            id = 1,
            lastHabitCompletionTimestamp = now - twoHoursMs * 2, // 4 hours ago
            currentCombo = 5,
            totalCoins = 50,
            totalCompletions = 5
        )

        doReturn(1L).whenever(habitCompletionDao).insertCompletion(any())
        doReturn(1).whenever(habitDao).updateHabit(any())
        doReturn(0).whenever(habitCompletionDao).getActiveDayCount()
        doReturn(1).whenever(statisticsDao).updateStatistics(any())

        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals(1, result.combo) // Combo reset to 1
        assertEquals(0L, result.comboBonusXp) // No combo bonus
    }

    @Test
    fun `completeCheckboxHabit — creates default statistics when none exist`() = runTest {
        val today = System.currentTimeMillis()
        val completion = HabitCompletionEntity(
            id = 0,
            habitId = 1,
            date = today,
            xpEarned = 10
        )

        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, today)

        // No existing statistics - use a fresh entity
        statsFlow.value = StatisticsEntity()

        doReturn(1L).whenever(habitCompletionDao).insertCompletion(any())
        doReturn(1).whenever(habitDao).updateHabit(any())
        doReturn(0).whenever(habitCompletionDao).getActiveDayCount()
        doReturn(0).whenever(statisticsDao).updateStatistics(any())
        doReturn(1L).whenever(statisticsDao).insertStatistics(any())

        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        verify(statisticsDao).insertStatistics(any()) // Creates default stats
    }

    @Test
    fun `completeCheckboxHabit — combo milestone at 5`() = runTest {
        val now = System.currentTimeMillis()
        val twoHoursMs = 2L * 60L * 60L * 1000L
        val completion = HabitCompletionEntity(
            id = 0,
            habitId = 1,
            date = now,
            xpEarned = 10
        )

        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, now)

        // Combo at 4, next completion will hit milestone at 5
        statsFlow.value = StatisticsEntity(
            id = 1,
            lastHabitCompletionTimestamp = now - twoHoursMs / 2,
            currentCombo = 4,
            totalCoins = 40,
            totalCompletions = 4
        )

        doReturn(1L).whenever(habitCompletionDao).insertCompletion(any())
        doReturn(1).whenever(habitDao).updateHabit(any())
        doReturn(0).whenever(habitCompletionDao).getActiveDayCount()
        doReturn(1).whenever(statisticsDao).updateStatistics(any())

        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals(5, result.combo)
        assertTrue("Combo milestone should be reached at 5", result.comboMilestoneReached)
    }
}
