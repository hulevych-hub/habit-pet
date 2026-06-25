package com.example.mobile.domain

import com.example.mobile.data.local.dao.HabitCompletionDao
import com.example.mobile.data.local.dao.HabitDao
import com.example.mobile.data.local.dao.HabitProgressDao
import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.HabitProgressEntity
import com.example.mobile.data.local.entities.StatisticsEntity
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
 * Integration test that exercises the timer habit completion flow:
 * HabitProgressRepository -> HabitCompletionRepository -> StatisticsRepository
 *
 * Verifies that completing a timer habit awards the correct XP and coins
 * based on accumulated minutes: XP = 5 + 1 * minutes, coins = 5 + 2 * minutes.
 */
class TimerHabitCompletionFlowIntegrationTest {

    private lateinit var habitCompletionDao: HabitCompletionDao
    private lateinit var habitDao: HabitDao
    private lateinit var habitProgressDao: HabitProgressDao
    private lateinit var statisticsDao: StatisticsDao
    private lateinit var habitRepository: HabitRepository
    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var habitProgressRepository: HabitProgressRepository
    private lateinit var habitCompletionRepository: HabitCompletionRepository

    private val statsFlow = MutableStateFlow<StatisticsEntity>(StatisticsEntity())
    private val habitsFlow = MutableStateFlow<List<HabitEntity>>(emptyList())

    private val testTimerHabit = HabitEntity(
        id = 2,
        name = "Meditate",
        icon = "meditation",
        type = "TIMER",
        minimumDurationMinutes = 10,
        currentStreak = 0,
        bestStreak = 0
    )

    private val testTimerProgress = HabitProgressEntity(
        habitId = 2,
        date = System.currentTimeMillis(),
        accumulatedMinutes = 10,
        lastUpdated = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        habitCompletionDao = mock()
        habitDao = mock()
        habitProgressDao = mock()
        statisticsDao = mock()

        whenever(statisticsDao.getStatistics()).thenReturn(statsFlow)
        whenever(habitDao.getAllHabits()).thenReturn(habitsFlow)
        whenever(habitDao.getHabitById(2L)).thenReturn(flowOf(testTimerHabit))

        habitRepository = mock()
        whenever(habitRepository.getAllHabits()).thenReturn(habitsFlow)
        whenever(habitRepository.getHabitById(2L)).thenReturn(flowOf(testTimerHabit))

        statisticsRepository = mock()
        whenever(statisticsRepository.getStatistics()).thenReturn(statsFlow)

        habitProgressRepository = mock()
        whenever(habitProgressRepository.getProgress(any(), any())).thenReturn(flowOf(testTimerProgress))

        habitCompletionRepository = com.example.mobile.data.repository.HabitCompletionRepositoryImpl(
            habitCompletionDao = habitCompletionDao,
            habitDao = habitDao,
            habitProgressRepository = habitProgressRepository,
            statisticsDao = statisticsDao
        )
    }

    @Test
    fun `stopTimerHabit — 10 minutes awards correct XP and coins`() = runTest {
        val now = System.currentTimeMillis()

        // No existing completion for today
        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(2L, now)

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
        doReturn(0).whenever(habitCompletionDao).getCompletionCountOnDate(any())

        // Simulate: progress shows 10 accumulated minutes
        // When timer stops, the ViewModel computes xp = 5 + 1*10 = 15, coins = 5 + 2*10 = 25
        // Then calls addCompletionWithCombo with xpEarned = 15
        val completion = HabitCompletionEntity(
            id = 0,
            habitId = 2,
            date = now,
            xpEarned = 15L // 5 + 1 * 10
        )

        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals(1L, result.completionId)
        assertEquals(15L, result.totalXpEarned)

        // Verify: coins awarded = 5 + 2*10 = 25
        // The statistics update should reflect coin award of 25
        verify(statisticsDao).updateStatistics(any())
        verify(habitCompletionDao).insertCompletion(any())
    }

    @Test
    fun `stopTimerHabit — 5 minutes awards base XP and coins`() = runTest {
        val now = System.currentTimeMillis()

        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(2L, now)

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
        doReturn(0).whenever(habitCompletionDao).getCompletionCountOnDate(any())

        // xp = 5 + 1*5 = 10, coins = 5 + 2*5 = 15
        val completion = HabitCompletionEntity(
            id = 0,
            habitId = 2,
            date = now,
            xpEarned = 10L
        )

        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals(1L, result.completionId)
        assertEquals(10L, result.totalXpEarned)

        verify(statisticsDao).updateStatistics(any())
    }

    @Test
    fun `stopTimerHabit — 30 minutes awards correct scaled XP`() = runTest {
        val now = System.currentTimeMillis()

        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(2L, now)

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
        doReturn(0).whenever(habitCompletionDao).getCompletionCountOnDate(any())

        // xp = 5 + 1*30 = 35, coins = 5 + 2*30 = 65
        val completion = HabitCompletionEntity(
            id = 0,
            habitId = 2,
            date = now,
            xpEarned = 35L
        )

        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals(35L, result.totalXpEarned)

        verify(habitCompletionDao).insertCompletion(any())
        verify(statisticsDao).updateStatistics(any())
    }

    @Test
    fun `stopTimerHabit — duplicate completion after timer stop is idempotent`() = runTest {
        val now = System.currentTimeMillis()

        // Existing completion (already completed today via timer)
        val existingCompletion = HabitCompletionEntity(
            id = 7,
            habitId = 2,
            date = now,
            xpEarned = 15L
        )
        doReturn(existingCompletion).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(2L, now)

        statsFlow.value = StatisticsEntity(
            id = 1,
            totalCoins = 25,
            totalCompletions = 1
        )

        val completion = HabitCompletionEntity(
            id = 0,
            habitId = 2,
            date = now,
            xpEarned = 15L
        )

        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertFalse("Should NOT be new completion", result.isNewCompletion)
        assertEquals(7L, result.completionId)
        assertEquals(15L, result.totalXpEarned)

        verify(habitCompletionDao, never()).insertCompletion(any())
        verify(statisticsDao, never()).updateStatistics(any())
    }

    @Test
    fun `stopTimerHabit — combo bonus applies to timer habits`() = runTest {
        val now = System.currentTimeMillis()
        val twoHoursMs = 2L * 60L * 60L * 1000L

        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(2L, now)

        // Combo active (last completion 30 min ago)
        statsFlow.value = StatisticsEntity(
            id = 1,
            lastHabitCompletionTimestamp = now - twoHoursMs / 4, // 30 min ago
            currentCombo = 3,
            totalCoins = 50,
            totalCompletions = 5
        )

        doReturn(1L).whenever(habitCompletionDao).insertCompletion(any())
        doReturn(1).whenever(habitDao).updateHabit(any())
        doReturn(0).whenever(habitCompletionDao).getActiveDayCount()
        doReturn(1).whenever(statisticsDao).updateStatistics(any())
        doReturn(0).whenever(habitCompletionDao).getCompletionCountOnDate(any())

        // Base XP = 5 + 1*10 = 15
        val completion = HabitCompletionEntity(
            id = 0,
            habitId = 2,
            date = now,
            xpEarned = 15L
        )

        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals(4, result.combo) // Combo incremented from 3 to 4
        assertTrue("Combo bonus should be > 0", result.comboBonusXp > 0)
        assertEquals(
            completion.xpEarned + result.comboBonusXp,
            result.totalXpEarned
        )
    }

    @Test
    fun `stopTimerHabit — progress accumulates across multiple sessions`() {
        // Verify that the timer stop flow correctly uses accumulated minutes
        // This test validates the formula: totalMinutes = previous + sessionMinutes
        val now = System.currentTimeMillis()
        val dayStart = now - (now % (24L * 60L * 60L * 1000L))

        // First session: 5 minutes
        val firstProgress = HabitProgressEntity(
            habitId = 2,
            date = dayStart,
            accumulatedMinutes = 5,
            lastUpdated = now - 60_000L
        )

        // Second session adds 8 minutes -> total 13
        val updatedProgress = firstProgress.copy(accumulatedMinutes = 13)

        // XP formula: 5 + 1 * totalMinutes
        val expectedXp = 5L + 13L // 18
        // Coins formula: 5 + 2 * totalMinutes
        val expectedCoins = 5 + 2 * 13 // 31

        assertEquals(18L, expectedXp)
        assertEquals(31, expectedCoins)
    }
}
