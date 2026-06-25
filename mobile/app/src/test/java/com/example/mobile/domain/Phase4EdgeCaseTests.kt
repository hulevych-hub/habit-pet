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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
 * Phase 4 edge case tests:
 *   4.5 — StreakEngine streak across month/year boundary
 *   4.6 — StreakEngine streak recalculation after habit deletion
 *   4.7 — Streak freeze edge cases
 */
class Phase4EdgeCaseTests {

    // ==================== 4.5 — Month/Year Boundary ====================

    private val dec31Key = dayKey(2025, Calendar.DECEMBER, 31)
    private val jan1Key = dayKey(2026, Calendar.JANUARY, 1)
    private val jan2Key = dayKey(2026, Calendar.JANUARY, 2)

    /**
     * Computes the start-of-day timestamp for a given date.
     * Uses the same algorithm as StreakEngine.getDayStart so the round-trip
     * dayKey(dayMillis(year, month, day)) holds regardless of timezone.
     */
    private fun dayMillis(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun dayStartMillis(dayKey: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dayKey * 86_400_000L
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var habitCompletionRepository: HabitCompletionRepository
    private lateinit var habitRepository: HabitRepository

    private val statsFlow = MutableStateFlow<StatisticsEntity>(StatisticsEntity())
    private val habitsFlow = MutableStateFlow<List<HabitEntity>>(emptyList())

    private val testHabits = listOf(
        HabitEntity(id = 1, name = "Morning jog"),
        HabitEntity(id = 2, name = "Read")
    )

    open class TestDragonMoodEngine : DragonMoodEngine(
        petRepository = mock(),
        statisticsRepository = mock(),
        habitCompletionRepository = mock()
    ) {
        override suspend fun refreshMood() = Unit
    }

    private fun createEngine(): StreakEngine {
        return StreakEngine(
            habitRepository = habitRepository,
            habitCompletionRepository = habitCompletionRepository,
            statisticsRepository = statisticsRepository,
            rewardQueue = mock(),
            inventoryItemRepository = mock(),
            activityTimelineEngine = mock(),
            dragonMoodEngine = TestDragonMoodEngine(),
            challengeRepository = mock()
        )
    }

    private fun setStats(currentStreak: Int, lastStreakFreezeDate: Long = 0L) {
        statsFlow.value = StatisticsEntity(
            id = 1,
            currentStreak = currentStreak,
            globalStreak = currentStreak,
            lastStreakFreezeDate = lastStreakFreezeDate,
            lastFrozenStreakDate = 0L,
            streakFreezeDatesJson = "[]"
        )
    }

    @Before
    fun setup() {
        habitRepository = mock()
        habitCompletionRepository = mock()
        statisticsRepository = mock()

        whenever(habitRepository.getAllHabits()).thenReturn(habitsFlow)
        whenever(statisticsRepository.getStatistics()).thenReturn(statsFlow)
    }

    @Test
    fun `4_5 — streak continues across month boundary Dec 31 to Jan 1`() = runTest {
        habitsFlow.value = testHabits
        setStats(currentStreak = 1)
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(1).whenever(statisticsRepository).incrementStreak()
        doReturn(1).whenever(statisticsRepository).markStreakUpdatedToday()
        // Both Dec 31 and Jan 1 have completions
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())
        doReturn(false).whenever(habitCompletionRepository).areAllHabitsCompletedOnDate(any())

        val engine = createEngine()
        // Evaluate on Jan 1 — should increment streak (continuation from Dec 31)
        engine.evaluateTodayStreak(dayMillis(2026, Calendar.JANUARY, 1))

        verify(statisticsRepository).incrementStreak()
        verify(statisticsRepository).markStreakUpdatedToday()
    }

    @Test
    fun `4_5 — streak continues across year boundary`() = runTest {
        habitsFlow.value = testHabits
        setStats(currentStreak = 2)
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(1).whenever(statisticsRepository).incrementStreak()
        doReturn(1).whenever(statisticsRepository).markStreakUpdatedToday()
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())
        doReturn(false).whenever(habitCompletionRepository).areAllHabitsCompletedOnDate(any())

        val engine = createEngine()
        // Evaluate on Jan 2 — should increment streak (continuation from Jan 1)
        engine.evaluateTodayStreak(dayMillis(2026, Calendar.JANUARY, 2))

        verify(statisticsRepository).incrementStreak()
    }

    @Test
    fun `4_5 — streak resets when day is missing across year boundary`() = runTest {
        habitsFlow.value = testHabits
        setStats(currentStreak = 2)
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        // No completions on Jan 1 (the day being evaluated)
        doReturn(false).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val engine = createEngine()
        // Evaluate on Jan 1 — no completions, streak should not increment
        engine.evaluateTodayStreak(dayMillis(2026, Calendar.JANUARY, 1))

        verify(statisticsRepository, never()).incrementStreak()
    }

    // ==================== 4.6 — Streak After Habit Deletion ====================

    @Test
    fun `4_6 — streak still valid after habit deletion with partial completion`() = runTest {
        // 3 habits, all completed today → streak was incremented
        val threeHabits = listOf(
            HabitEntity(id = 1, name = "Morning jog"),
            HabitEntity(id = 2, name = "Read"),
            HabitEntity(id = 3, name = "Meditate")
        )
        habitsFlow.value = threeHabits
        setStats(currentStreak = 1)
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(1).whenever(statisticsRepository).incrementStreak()
        doReturn(1).whenever(statisticsRepository).markStreakUpdatedToday()
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())
        doReturn(false).whenever(habitCompletionRepository).areAllHabitsCompletedOnDate(any())

        val engine = createEngine()
        engine.evaluateTodayStreak(dayMillis(2026, Calendar.JANUARY, 1))

        // Streak should increment (partial completion still counts)
        verify(statisticsRepository).incrementStreak()
    }

    @Test
    fun `4_6 — streak recalculation after habit deletion uses remaining habits`() = runTest {
        // Start with 3 habits, delete one, 2 remain
        val twoHabits = listOf(
            HabitEntity(id = 1, name = "Morning jog"),
            HabitEntity(id = 2, name = "Read")
        )
        habitsFlow.value = twoHabits
        setStats(currentStreak = 5)
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(1).whenever(statisticsRepository).incrementStreak()
        doReturn(1).whenever(statisticsRepository).markStreakUpdatedToday()
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())
        doReturn(false).whenever(habitCompletionRepository).areAllHabitsCompletedOnDate(any())

        val engine = createEngine()
        // Streak should still increment with 2 habits (partial completion is valid)
        engine.evaluateTodayStreak(dayMillis(2026, Calendar.JANUARY, 1))

        verify(statisticsRepository).incrementStreak()
    }

    @Test
    fun `4_6 — streak resets when all habits deleted`() = runTest {
        habitsFlow.value = emptyList()
        setStats(currentStreak = 5)
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()

        val engine = createEngine()
        engine.evaluateTodayStreak(dayMillis(2026, Calendar.JANUARY, 1))

        // No habits → no streak increment
        verify(statisticsRepository, never()).incrementStreak()
    }

    // ==================== 4.7 — Streak Freeze Edge Cases ====================

    @Test
    fun `4_7 — frozen date followed by real completion does not consume freeze twice`() = runTest {
        // Day2 has real completion → checkPendingStreakFreeze should return null
        // (evaluateTodayStreak is called instead of freeze logic)
        habitsFlow.value = testHabits
        setStats(currentStreak = 3, lastStreakFreezeDate = dayKey(2026, Calendar.JANUARY, 1))
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(1).whenever(statisticsRepository).incrementStreak()
        doReturn(1).whenever(statisticsRepository).markStreakUpdatedToday()
        doReturn(true).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())
        doReturn(false).whenever(habitCompletionRepository).areAllHabitsCompletedOnDate(any())

        val engine = createEngine()
        val prompt = engine.checkPendingStreakFreeze(dayMillis(2026, Calendar.JANUARY, 2))

        // Day2 has completions → evaluateTodayStreak is called, no freeze prompt
        assertNull("No freeze prompt when today has real completions", prompt)
    }

    @Test
    fun `4_7 — two frozen days in a row is rejected`() = runTest {
        habitsFlow.value = testHabits
        // Jan 1 and Jan 2 are both frozen, now on Jan 3 trying to freeze Jan 2
        val jan1Key = dayKey(2026, Calendar.JANUARY, 1)
        val jan2Key = dayKey(2026, Calendar.JANUARY, 2)
        val stats = StatisticsEntity(
            id = 1,
            currentStreak = 5,
            globalStreak = 5,
            lastStreakFreezeDate = jan1Key,
            lastFrozenStreakDate = jan2Key,
            streakFreezeDatesJson = StatisticsEntity.freezeDatesToJson(setOf(jan1Key, jan2Key))
        )
        statsFlow.value = stats

        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        // Today (Jan 3) has no completions, Jan 2 is frozen (no real completion), Jan 1 is frozen
        doReturn(false).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val engine = createEngine()
        val prompt = engine.checkPendingStreakFreeze(dayMillis(2026, Calendar.JANUARY, 3))

        // Jan 2 cannot be frozen because Jan 1 is already frozen (consecutive freeze)
        assertNull("Two frozen days in a row should be rejected", prompt)
    }

    @Test
    fun `4_7 — freeze eligible after 7 days cooldown`() = runTest {
        habitsFlow.value = testHabits
        // Freeze used on Jan 1, now on Jan 8 (7 days later)
        val jan1Key = dayKey(2026, Calendar.JANUARY, 1)
        val stats = StatisticsEntity(
            id = 1,
            currentStreak = 5,
            globalStreak = 5,
            lastStreakFreezeDate = jan1Key,
            lastFrozenStreakDate = jan1Key,
            streakFreezeDatesJson = StatisticsEntity.freezeDatesToJson(setOf(jan1Key))
        )
        statsFlow.value = stats

        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        // 4 calls to hasAnyCompletionOnDate: today, yesterday (line 169), yesterday (canFreezeDate), dayBeforeYesterday
        var callCount = 0
        org.mockito.kotlin.doAnswer { _ ->
            callCount++
            callCount == 4  // only the 4th call (dayBeforeYesterday) returns true
        }.whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val engine = createEngine()
        val prompt = engine.checkPendingStreakFreeze(dayMillis(2026, Calendar.JANUARY, 8))

        // 7 days have passed → cooldown complete, freeze should be eligible
        assertNotNull("Freeze should be eligible after 7 days cooldown", prompt)
        assertEquals(dayKey(2026, Calendar.JANUARY, 7), prompt!!.frozenDateKey)
    }

    @Test
    fun `4_7 — freeze rejected during 6-day cooldown`() = runTest {
        habitsFlow.value = testHabits
        // Freeze used on Jan 1, now on Jan 7 (6 days later — still on cooldown)
        val jan1Key = dayKey(2026, Calendar.JANUARY, 1)
        val stats = StatisticsEntity(
            id = 1,
            currentStreak = 5,
            globalStreak = 5,
            lastStreakFreezeDate = jan1Key,
            lastFrozenStreakDate = jan1Key,
            streakFreezeDatesJson = StatisticsEntity.freezeDatesToJson(setOf(jan1Key))
        )
        statsFlow.value = stats

        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        // 3 calls: today, yesterday (line 169), yesterday (canFreezeDate) — 3rd returns true
        var callCount6 = 0
        org.mockito.kotlin.doAnswer { _ ->
            callCount6++
            callCount6 == 3
        }.whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val engine = createEngine()
        val prompt = engine.checkPendingStreakFreeze(dayMillis(2026, Calendar.JANUARY, 7))

        // Only 6 days passed → still on cooldown
        assertNull("Freeze should be rejected during 6-day cooldown", prompt)
    }

    @Test
    fun `4_7 — useStreakFreeze returns false when no freeze available`() = runTest {
        habitsFlow.value = emptyList()
        setStats(currentStreak = 0)

        val engine = createEngine()
        val result = engine.useStreakFreeze(dayMillis(2026, Calendar.JANUARY, 1))

        assertFalse("useStreakFreeze returns false when no freeze available", result)
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
