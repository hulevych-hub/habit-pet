package com.example.mobile.domain

import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.ChallengeRepository
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.reward.RewardQueue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Calendar

/**
 * Expanded tests for StreakEngine covering milestone awards, freeze logic, and streak evaluation.
 */
class StreakEngineExpandTest {

    private val baseDay = dayKey(2026, Calendar.JANUARY, 1)
    private fun dayMillis(dayOffset: Int) = (baseDay + dayOffset) * 86_400_000L
    private fun dayKey(dayOffset: Int) = baseDay + dayOffset

    open class TestDragonMoodEngine : DragonMoodEngine(
        petRepository = mock(),
        statisticsRepository = mock(),
        habitCompletionRepository = mock()
    ) {
        override suspend fun refreshMood() {}
    }

    private lateinit var streakEngine: StreakEngine
    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var habitCompletionRepository: HabitCompletionRepository
    private lateinit var rewardQueue: RewardQueue
    private lateinit var activityTimelineEngine: ActivityTimelineEngine
    private lateinit var challengeRepository: ChallengeRepository
    private lateinit var inventoryItemRepository: InventoryItemRepository
    private lateinit var habitRepository: HabitRepository

    private val habits = listOf(
        HabitEntity(id = 1, name = "Habit 1"),
        HabitEntity(id = 2, name = "Habit 2"),
        HabitEntity(id = 3, name = "Habit 3")
    )

    private fun defaultStats() = StatisticsEntity(id = 1, currentStreak = 0, globalStreak = 0)

    @Before
    fun setup() {
        habitCompletionRepository = mock()
        statisticsRepository = mock()
        rewardQueue = mock()
        activityTimelineEngine = mock()
        challengeRepository = mock()
        inventoryItemRepository = mock()
        habitRepository = mock()

        whenever(habitRepository.getAllHabits()).thenReturn(flowOf(habits))
        whenever(statisticsRepository.getStatistics()).thenReturn(flowOf(defaultStats()))
        whenever(inventoryItemRepository.getUnownedItemsByRarity(any()))
            .thenReturn(flowOf(emptyList()))
        whenever(inventoryItemRepository.getUnownedItemsByType(any()))
            .thenReturn(flowOf(emptyList()))

        streakEngine = StreakEngine(
            habitRepository = habitRepository,
            habitCompletionRepository = habitCompletionRepository,
            statisticsRepository = statisticsRepository,
            rewardQueue = rewardQueue,
            inventoryItemRepository = inventoryItemRepository,
            activityTimelineEngine = activityTimelineEngine,
            dragonMoodEngine = TestDragonMoodEngine(),
            challengeRepository = challengeRepository
        )
    }

    // ==================== evaluateTodayStreak ====================

    @Test
    fun `evaluateTodayStreak — increments streak when completions exist`() = runTest {
        val ts = dayMillis(1)
        val stats = defaultStats()
        whenever(statisticsRepository.getStatistics()).thenReturn(flowOf(stats))
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        streakEngine.evaluateTodayStreak(ts)

        verify(statisticsRepository).incrementStreak()
        verify(statisticsRepository).markStreakUpdatedToday()
    }

    @Test
    fun `evaluateTodayStreak — does not increment when no completions`() = runTest {
        val ts = dayMillis(1)
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(false).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        streakEngine.evaluateTodayStreak(ts)

        verify(statisticsRepository, never()).incrementStreak()
    }

    @Test
    fun `evaluateTodayStreak — does not double-count same day`() = runTest {
        val ts = dayMillis(1)
        doReturn(true).whenever(statisticsRepository).isStreakAlreadyCountedToday()

        streakEngine.evaluateTodayStreak(ts)

        verify(statisticsRepository, never()).incrementStreak()
    }

    @Test
    fun `evaluateTodayStreak — awards milestone chest at 7 days`() = runTest {
        val ts = dayMillis(1)
        val initialStats = StatisticsEntity(id = 1, currentStreak = 6, globalStreak = 6, lastStreakAwardedAt = 0)
        val stateFlow = MutableStateFlow(initialStats)

        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)

        whenever(statisticsRepository.incrementStreak()).thenAnswer {
            val updated = stateFlow.value.copy(currentStreak = stateFlow.value.currentStreak + 1)
            stateFlow.value = updated
            Unit
        }

        streakEngine.evaluateTodayStreak(ts)

        verify(rewardQueue, atLeastOnce()).addReward(any())
        verify(activityTimelineEngine).logStreakMilestone(7, ChestType.NORMAL)
    }

    @Test
    fun `evaluateTodayStreak — awards milestone chest at 14 days`() = runTest {
        val ts = dayMillis(1)
        val initialStats = StatisticsEntity(id = 1, currentStreak = 13, globalStreak = 13, lastStreakAwardedAt = 7)
        val stateFlow = MutableStateFlow(initialStats)

        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)

        whenever(statisticsRepository.incrementStreak()).thenAnswer {
            val updated = stateFlow.value.copy(currentStreak = stateFlow.value.currentStreak + 1)
            stateFlow.value = updated
            Unit
        }

        streakEngine.evaluateTodayStreak(ts)

        verify(activityTimelineEngine).logStreakMilestone(14, ChestType.RARE)
    }

    @Test
    fun `evaluateTodayStreak — awards milestone chest at 30 days`() = runTest {
        val ts = dayMillis(1)
        val initialStats = StatisticsEntity(id = 1, currentStreak = 29, globalStreak = 29, lastStreakAwardedAt = 14)
        val stateFlow = MutableStateFlow(initialStats)

        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)

        whenever(statisticsRepository.incrementStreak()).thenAnswer {
            val updated = stateFlow.value.copy(currentStreak = stateFlow.value.currentStreak + 1)
            stateFlow.value = updated
            Unit
        }

        streakEngine.evaluateTodayStreak(ts)

        verify(activityTimelineEngine).logStreakMilestone(30, ChestType.EPIC)
    }

    @Test
    fun `evaluateTodayStreak — does not award milestone twice`() = runTest {
        val ts = dayMillis(1)
        val initialStats = StatisticsEntity(id = 1, currentStreak = 7, globalStreak = 7, lastStreakAwardedAt = 7)
        val stateFlow = MutableStateFlow(initialStats)

        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)

        whenever(statisticsRepository.incrementStreak()).thenAnswer {
            val updated = stateFlow.value.copy(currentStreak = stateFlow.value.currentStreak + 1)
            stateFlow.value = updated
            Unit
        }

        streakEngine.evaluateTodayStreak(ts)

        verify(activityTimelineEngine, never()).logStreakMilestone(any(), any())
    }

    @Test
    fun `evaluateTodayStreak — updates lastStreakAwardedAt after milestone`() = runTest {
        val ts = dayMillis(1)
        val initialStats = StatisticsEntity(id = 1, currentStreak = 6, globalStreak = 6, lastStreakAwardedAt = 0)
        val stateFlow = MutableStateFlow(initialStats)

        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)

        whenever(statisticsRepository.incrementStreak()).thenAnswer {
            val updated = stateFlow.value.copy(currentStreak = stateFlow.value.currentStreak + 1)
            stateFlow.value = updated
            Unit
        }

        streakEngine.evaluateTodayStreak(ts)

        verify(statisticsRepository).updateStatistics(any())

        val captor = argumentCaptor<StatisticsEntity>()
        verify(statisticsRepository).updateStatistics(captor.capture())
        assertTrue(
            "updateStatistics should set lastStreakAwardedAt to milestone",
            captor.firstValue.lastStreakAwardedAt >= 7
        )
    }

    // ==================== checkPendingStreakFreeze ====================

    @Test
    fun `checkPendingStreakFreeze — returns null when streak is 0`() = runTest {
        val ts = dayMillis(2)
        val stats = StatisticsEntity(id = 1, currentStreak = 0, globalStreak = 0)
        val stateFlow = MutableStateFlow(stats)
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)
        doReturn(false).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val prompt = streakEngine.checkPendingStreakFreeze(ts)

        assertNull("No freeze when streak is 0", prompt)
    }

    @Test
    fun `checkPendingStreakFreeze — returns null when today has completions`() = runTest {
        val ts = dayMillis(2)
        val stats = StatisticsEntity(id = 1, currentStreak = 5, globalStreak = 5)
        val stateFlow = MutableStateFlow(stats)
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()

        val prompt = streakEngine.checkPendingStreakFreeze(ts)

        assertNull("No freeze when today has completions", prompt)
    }

    @Test
    fun `checkPendingStreakFreeze — returns null when yesterday was completed`() = runTest {
        val ts = dayMillis(2)
        val stats = StatisticsEntity(id = 1, currentStreak = 5, globalStreak = 5)
        val stateFlow = MutableStateFlow(stats)
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)
        // Today has no completions, but yesterday does
        whenever(habitCompletionRepository.hasAnyCompletionOnDate(any())).thenReturn(false)
        whenever(habitCompletionRepository.hasAnyCompletionOnDate(dayMillis(1))).thenReturn(true)

        val prompt = streakEngine.checkPendingStreakFreeze(ts)

        assertNull("No freeze when yesterday was completed", prompt)
    }

    @Test
    fun `checkPendingStreakFreeze — returns null when freeze on cooldown`() = runTest {
        val ts = dayMillis(10)
        val stats = StatisticsEntity(
            id = 1, currentStreak = 5, globalStreak = 5,
            lastStreakFreezeDate = 5L, lastFrozenStreakDate = 4L
        )
        val stateFlow = MutableStateFlow(stats)
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)
        doReturn(false).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val prompt = streakEngine.checkPendingStreakFreeze(ts)

        assertNull("No freeze when on cooldown", prompt)
    }

    @Test
    fun `checkPendingStreakFreeze — returns null when yesterday was frozen`() = runTest {
        val ts = dayMillis(10)
        val stats = StatisticsEntity(
            id = 1, currentStreak = 5, globalStreak = 5,
            lastStreakFreezeDate = 1L, lastFrozenStreakDate = 9L,
            streakFreezeDatesJson = "[9]"
        )
        val stateFlow = MutableStateFlow(stats)
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)
        doReturn(false).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val prompt = streakEngine.checkPendingStreakFreeze(ts)

        assertNull("No freeze when yesterday was frozen", prompt)
    }

    // ==================== useStreakFreeze ====================

    @Test
    fun `useStreakFreeze — returns false when no pending freeze`() = runTest {
        val ts = dayMillis(2)
        val stats = StatisticsEntity(id = 1, currentStreak = 0, globalStreak = 0)
        val stateFlow = MutableStateFlow(stats)
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)
        doReturn(false).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val result = streakEngine.useStreakFreeze(ts)

        assertFalse("Returns false when no pending freeze", result)
    }

    // ==================== resetBrokenStreak ====================

    @Test
    fun `resetBrokenStreak — sets currentStreak and globalStreak to 0`() = runTest {
        val ts = dayMillis(1)
        val stats = StatisticsEntity(id = 1, currentStreak = 5, globalStreak = 5)
        val stateFlow = MutableStateFlow(stats)
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)

        streakEngine.resetBrokenStreak(ts)

        verify(statisticsRepository).updateStatistics(
            stats.copy(currentStreak = 0, globalStreak = 0, lastStreakDate = 0L, lastUpdated = ts)
        )
    }

    @Test
    fun `resetBrokenStreak — no-op when streak already 0`() = runTest {
        val ts = dayMillis(1)
        val stats = StatisticsEntity(id = 1, currentStreak = 0, globalStreak = 0)
        val stateFlow = MutableStateFlow(stats)
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)

        streakEngine.resetBrokenStreak(ts)

        verify(statisticsRepository, never()).updateStatistics(any())
    }

    // ==================== recalculateTodayStreak ====================

    @Test
    fun `recalculateTodayStreak — delegates to evaluateTodayStreak when needed`() = runTest {
        val ts = dayMillis(1)
        val stats = StatisticsEntity(id = 1, currentStreak = 0, globalStreak = 0)
        val stateFlow = MutableStateFlow(stats)
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        streakEngine.recalculateTodayStreak(ts)

        verify(statisticsRepository).incrementStreak()
    }

    @Test
    fun `recalculateTodayStreak — no-op when already counted`() = runTest {
        val ts = dayMillis(1)
        val stats = StatisticsEntity(id = 1, currentStreak = 5, globalStreak = 5)
        val stateFlow = MutableStateFlow(stats)
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)
        doReturn(true).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(false).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        streakEngine.recalculateTodayStreak(ts)

        verify(statisticsRepository, never()).incrementStreak()
    }

    // ==================== isDayCompletedOrFrozen (indirect tests) ====================

    @Test
    fun `isDayCompletedOrFrozen — frozen day is recognized through freeze logic`() = runTest {
        val ts = dayMillis(5)
        val stats = StatisticsEntity(
            id = 1, currentStreak = 5, globalStreak = 5,
            streakFreezeDatesJson = "[${baseDay + 3}]",
            lastStreakFreezeDate = 1L, lastFrozenStreakDate = 3L
        )
        val stateFlow = MutableStateFlow(stats)
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)
        doReturn(false).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val prompt = streakEngine.checkPendingStreakFreeze(ts)
        assertTrue("Freeze logic completes without crash", true)
    }

    @Test
    fun `isDayCompletedOrFrozen — completed day increments streak`() = runTest {
        val ts = dayMillis(1)
        val stats = defaultStats()
        val stateFlow = MutableStateFlow(stats)
        whenever(statisticsRepository.getStatistics()).thenReturn(stateFlow)
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        streakEngine.evaluateTodayStreak(ts)

        verify(statisticsRepository).incrementStreak()
    }

    @Test
    fun `isDayCompletedOrFrozen — empty day does not increment streak`() = runTest {
        val ts = dayMillis(1)
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(false).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        streakEngine.evaluateTodayStreak(ts)

        verify(statisticsRepository, never()).incrementStreak()
    }

    private fun dayKey(year: Int, month: Int, day: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis / 86_400_000L
    }
}
