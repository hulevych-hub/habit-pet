package com.example.mobile.domain

import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.reward.RewardQueue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DailyLoginStreakEngineTest {

    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var rewardQueue: RewardQueue
    private lateinit var engine: DailyLoginStreakEngine

    private val statsFlow = MutableStateFlow(StatisticsEntity())

    // Use a baseTime that is at UTC midnight so Calendar-based day key is deterministic.
    // 2023-11-15 00:00:00 UTC = 1_700_056_000_000L
    private val baseTime = 1_700_056_000_000L
    private val dayMs = 86_400_000L

    @Before
    fun setup() {
        statisticsRepository = mock()
        rewardQueue = mock()
        whenever(statisticsRepository.getStatistics()).thenReturn(statsFlow)
        engine = DailyLoginStreakEngine(statisticsRepository, rewardQueue)
    }

    private suspend fun stubUpdateStatistics() {
        whenever(statisticsRepository.updateStatistics(any())).thenAnswer { invocation ->
            val entity = invocation.getArgument<StatisticsEntity>(0)
            statsFlow.value = entity
            1
        }
    }

    @Test
    fun `first login creates streak of 1`() = runTest {
        stubUpdateStatistics()
        statsFlow.value = StatisticsEntity(dailyLoginStreak = 0, lastDailyLoginDate = 0L)

        val result = engine.checkAndUpdateLoginStreak(baseTime)

        assertEquals(1, result.streak)
        assertFalse(result.isNewMilestone)
    }

    @Test
    fun `consecutive login increments streak`() = runTest {
        stubUpdateStatistics()
        // Log in yesterday first to establish the day key
        val yesterday = baseTime - dayMs
        val firstResult = engine.checkAndUpdateLoginStreak(yesterday)
        assertEquals(1, firstResult.streak)

        // Capture the day key stored by the engine
        val storedDayKey = statsFlow.value.lastDailyLoginDate
        statsFlow.value = statsFlow.value.copy(
            dailyLoginStreak = firstResult.streak,
            lastDailyLoginDate = storedDayKey,
            lastDailyLoginRewardDay = statsFlow.value.lastDailyLoginRewardDay
        )

        val result = engine.checkAndUpdateLoginStreak(baseTime)

        assertEquals(2, result.streak)
    }

    @Test
    fun `same day login does not increment streak`() = runTest {
        stubUpdateStatistics()
        // Log in first time
        engine.checkAndUpdateLoginStreak(baseTime)
        val afterFirst = statsFlow.value

        // Login again same day
        val result = engine.checkAndUpdateLoginStreak(baseTime + 3_600_000L)

        assertEquals(afterFirst.dailyLoginStreak, result.streak)
        verify(rewardQueue, never()).addReward(any())
    }

    @Test
    fun `gap of more than one day resets streak`() = runTest {
        stubUpdateStatistics()
        // Log in first
        engine.checkAndUpdateLoginStreak(baseTime)
        // Simulate 3 days passing by setting lastDailyLoginDate
        val todayKey = statsFlow.value.lastDailyLoginDate
        statsFlow.value = statsFlow.value.copy(
            dailyLoginStreak = 10,
            lastDailyLoginDate = todayKey // still today
        )
        // Now login 3 days later - the stored dayKey should be todayKey
        // but checkAndUpdateLoginStreak called at baseTime + 3*dayMs,
        // its todayKey will be todayKey + 3, so not consecutive
        val result = engine.checkAndUpdateLoginStreak(baseTime + 3 * dayMs)

        assertEquals(1, result.streak)
    }

    @Test
    fun `non-milestone does not trigger reward`() = runTest {
        stubUpdateStatistics()
        // Log in for 2 consecutive days to reach streak 2
        var dayKey = 0L
        for (i in 0 until 2) {
            val time = baseTime + i * dayMs
            engine.checkAndUpdateLoginStreak(time)
            dayKey = statsFlow.value.lastDailyLoginDate
        }

        // Manually set state to streak 4, lastDailyLoginDate = dayKey of 'day 2'
        statsFlow.value = statsFlow.value.copy(
            dailyLoginStreak = 4,
            lastDailyLoginDate = dayKey,
            lastDailyLoginRewardDay = 0
        )

        // Login one more day (consecutive) → streak 5, which is NOT a milestone
        // The stored lastDailyLoginDate corresponds to baseTime + dayMs (day 2),
        // so we need to call with baseTime + 2*dayMs (day 3, consecutive)
        val result = engine.checkAndUpdateLoginStreak(baseTime + 2 * dayMs)

        assertEquals(5, result.streak)
        assertFalse(result.isNewMilestone)
        verify(rewardQueue, never()).addReward(any())
    }

    @Test
    fun `milestone 3 triggers reward`() = runTest {
        stubUpdateStatistics()
        // Log in to establish day key
        engine.checkAndUpdateLoginStreak(baseTime)
        val todayKey = statsFlow.value.lastDailyLoginDate
        // Set up streak = 2 (one more than milestone threshold)
        statsFlow.value = statsFlow.value.copy(
            dailyLoginStreak = 2,
            lastDailyLoginDate = todayKey
        )

        val result = engine.checkAndUpdateLoginStreak(baseTime + dayMs)

        assertEquals(3, result.streak)
        assertTrue(result.isNewMilestone)
        verify(rewardQueue, atLeastOnce()).addReward(any())
    }

    @Test
    fun `milestone 7 triggers reward`() = runTest {
        stubUpdateStatistics()
        engine.checkAndUpdateLoginStreak(baseTime)
        val todayKey = statsFlow.value.lastDailyLoginDate
        statsFlow.value = statsFlow.value.copy(
            dailyLoginStreak = 6,
            lastDailyLoginDate = todayKey
        )

        val result = engine.checkAndUpdateLoginStreak(baseTime + dayMs)

        assertEquals(7, result.streak)
        assertTrue(result.isNewMilestone)
        verify(rewardQueue, atLeastOnce()).addReward(any())
    }

    @Test
    fun `milestone 30 triggers reward`() = runTest {
        stubUpdateStatistics()
        engine.checkAndUpdateLoginStreak(baseTime)
        val todayKey = statsFlow.value.lastDailyLoginDate
        statsFlow.value = statsFlow.value.copy(
            dailyLoginStreak = 29,
            lastDailyLoginDate = todayKey
        )

        val result = engine.checkAndUpdateLoginStreak(baseTime + dayMs)

        assertEquals(30, result.streak)
        assertTrue(result.isNewMilestone)
        verify(rewardQueue, atLeastOnce()).addReward(any())
    }

    @Test
    fun `milestone 60 triggers reward`() = runTest {
        stubUpdateStatistics()
        engine.checkAndUpdateLoginStreak(baseTime)
        val todayKey = statsFlow.value.lastDailyLoginDate
        statsFlow.value = statsFlow.value.copy(
            dailyLoginStreak = 59,
            lastDailyLoginDate = todayKey
        )

        val result = engine.checkAndUpdateLoginStreak(baseTime + dayMs)

        assertEquals(60, result.streak)
        assertTrue(result.isNewMilestone)
        verify(rewardQueue, atLeastOnce()).addReward(any())
    }

    @Test
    fun `milestone 100 triggers reward`() = runTest {
        stubUpdateStatistics()
        engine.checkAndUpdateLoginStreak(baseTime)
        val todayKey = statsFlow.value.lastDailyLoginDate
        statsFlow.value = statsFlow.value.copy(
            dailyLoginStreak = 99,
            lastDailyLoginDate = todayKey
        )

        val result = engine.checkAndUpdateLoginStreak(baseTime + dayMs)

        assertEquals(100, result.streak)
        assertTrue(result.isNewMilestone)
        verify(rewardQueue, atLeastOnce()).addReward(any())
    }
}
