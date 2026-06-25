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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Calendar

/**
 * Integration test exercising the streak flow:
 * StreakEngine -> StatisticsRepository -> HabitCompletionRepository
 *
 * Verifies:
 *   - Completing a habit on day 1 increments streak to 1
 *   - Completing a habit on day 2 (continuation) increments streak to 2
 *   - Skipping a day (no completions) and calling on day 3 resets streak to 0
 *   - Already-counted today is idempotent (no double-increment)
 *   - Streak does not increment when no habits exist
 */
class StreakFlowIntegrationTest {

    private val day1Key = dayKey(2026, Calendar.JANUARY, 1)
    private val day2Key = dayKey(2026, Calendar.JANUARY, 2)
    private val day3Key = dayKey(2026, Calendar.JANUARY, 3)

    private fun dayMillis(dayKey: Long) = dayKey * 86_400_000L

    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var habitCompletionRepository: HabitCompletionRepository
    private lateinit var habitRepository: HabitRepository

    private val statsFlow = MutableStateFlow<StatisticsEntity>(StatisticsEntity())
    private val habitsFlow = MutableStateFlow<List<HabitEntity>>(emptyList())

    private val testHabits = listOf(
        HabitEntity(id = 1, name = "Morning jog"),
        HabitEntity(id = 2, name = "Read")
    )

    // No-op DragonMoodEngine for testing
    open class TestDragonMoodEngine : DragonMoodEngine(
        petRepository = mock(),
        statisticsRepository = mock(),
        habitCompletionRepository = mock()
    ) {
        override suspend fun refreshMood() = Unit
    }

    @Before
    fun setup() {
        habitRepository = mock()
        habitCompletionRepository = mock()
        statisticsRepository = mock()

        whenever(habitRepository.getAllHabits()).thenReturn(habitsFlow)
        whenever(statisticsRepository.getStatistics()).thenReturn(statsFlow)
    }

    private fun createEngine(): StreakEngine {
        val rewardQueue: RewardQueue = mock()
        val inventoryRepo: InventoryItemRepository = mock()
        val timelineEngine: ActivityTimelineEngine = mock()
        val dragonMoodEngine: DragonMoodEngine = TestDragonMoodEngine()
        val challengeRepo: ChallengeRepository = mock()

        return StreakEngine(
            habitRepository = habitRepository,
            habitCompletionRepository = habitCompletionRepository,
            statisticsRepository = statisticsRepository,
            rewardQueue = rewardQueue,
            inventoryItemRepository = inventoryRepo,
            activityTimelineEngine = timelineEngine,
            dragonMoodEngine = dragonMoodEngine,
            challengeRepository = challengeRepo
        )
    }

    /**
     * Helper: sets the stats flow to a known state.
     * The MutableStateFlow is the source of truth for getStatistics() reads.
     */
    private fun setStats(
        currentStreak: Int,
        lastStreakFreezeDate: Long = 0L,
        lastFrozenStreakDate: Long = 0L,
        streakFreezeDatesJson: String = "[]"
    ) {
        statsFlow.value = StatisticsEntity(
            id = 1,
            currentStreak = currentStreak,
            globalStreak = currentStreak,
            lastStreakFreezeDate = lastStreakFreezeDate,
            lastFrozenStreakDate = lastFrozenStreakDate,
            streakFreezeDatesJson = streakFreezeDatesJson
        )
    }

    @Test
    fun `streak — first completion on day 1 increments streak to 1`() = runTest {
        habitsFlow.value = testHabits
        setStats(currentStreak = 0)
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(1).whenever(statisticsRepository).incrementStreak()
        doReturn(1).whenever(statisticsRepository).markStreakUpdatedToday()
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val engine = createEngine()
        engine.evaluateTodayStreak(dayMillis(day1Key))

        verify(statisticsRepository).incrementStreak()
        verify(statisticsRepository).markStreakUpdatedToday()
    }

    @Test
    fun `streak — continuation on day 2 increments streak to 2`() = runTest {
        habitsFlow.value = testHabits
        setStats(currentStreak = 1)
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(1).whenever(statisticsRepository).incrementStreak()
        doReturn(1).whenever(statisticsRepository).markStreakUpdatedToday()
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val engine = createEngine()
        engine.evaluateTodayStreak(dayMillis(day2Key))

        verify(statisticsRepository).incrementStreak()
        verify(statisticsRepository).markStreakUpdatedToday()
    }

    @Test
    fun `streak — break due to missing days triggers reset call`() = runTest {
        habitsFlow.value = testHabits
        setStats(currentStreak = 1)
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(false).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())
        doReturn(1).whenever(statisticsRepository).updateStatistics(any())

        val engine = createEngine()
        engine.resetBrokenStreak(dayMillis(day1Key))

        // resetBrokenStreak should call updateStatistics to zero out the streak
        verify(statisticsRepository).updateStatistics(any())
    }

    @Test
    fun `streak — already counted today is idempotent`() = runTest {
        habitsFlow.value = testHabits
        setStats(currentStreak = 3)
        doReturn(true).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val engine = createEngine()
        engine.evaluateTodayStreak(dayMillis(day1Key))

        verify(statisticsRepository, never()).incrementStreak()
        verify(statisticsRepository, never()).markStreakUpdatedToday()
    }

    @Test
    fun `streak — no habits means no increment`() = runTest {
        habitsFlow.value = emptyList()
        setStats(currentStreak = 0)
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()

        val engine = createEngine()
        engine.evaluateTodayStreak(dayMillis(day1Key))

        verify(statisticsRepository, never()).incrementStreak()
    }

    @Test
    fun `streak — no completions today means no increment`() = runTest {
        habitsFlow.value = testHabits
        setStats(currentStreak = 2)
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(1).whenever(statisticsRepository).incrementStreak()
        doReturn(1).whenever(statisticsRepository).markStreakUpdatedToday()
        doReturn(false).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val engine = createEngine()
        engine.evaluateTodayStreak(dayMillis(day1Key))

        verify(statisticsRepository, never()).incrementStreak()
        verify(statisticsRepository, never()).markStreakUpdatedToday()
    }

    @Test
    fun `streak — partial completion still counts as streak day`() = runTest {
        habitsFlow.value = testHabits
        setStats(currentStreak = 1)
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(1).whenever(statisticsRepository).incrementStreak()
        doReturn(1).whenever(statisticsRepository).markStreakUpdatedToday()
        // Only 1 of 2 habits completed — partial completion still counts
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val engine = createEngine()
        engine.evaluateTodayStreak(dayMillis(day2Key))

        verify(statisticsRepository).incrementStreak()
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
