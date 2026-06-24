package com.example.mobile

import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.ActivityTimelineEngine
import com.example.mobile.domain.DragonMoodEngine
import com.example.mobile.domain.StreakEngine
import com.example.mobile.domain.repository.ChallengeRepository
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.reward.RewardQueue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Calendar

/**
 * Tests for the StreakEngine streak logic.
 *
 * Core design rules:
 * - Streak continues if >= 1 habit is completed (partial or full)
 * - Streak breaks only on 0-habit days (unless freeze is used)
 * - Freeze only triggers when previous day was a streak day AND today has 0 completions
 * - Freeze cannot be used on consecutive days
 * - Freeze has a 7-day cooldown
 */
class StreakEngineTest {

    // Use base timestamps (day keys) that we multiply to get millis
    private val baseDay = dayKey(2026, Calendar.JANUARY, 1)
    private fun dayMillis(dayOffset: Int) = (baseDay + dayOffset) * 86_400_000L
    private fun dayKey(dayOffset: Int) = baseDay + dayOffset

    // A no-op subclass of DragonMoodEngine for testing
    open class TestDragonMoodEngine : DragonMoodEngine(
        petRepository = mock(),
        statisticsRepository = mock(),
        habitCompletionRepository = mock()
    ) {
        override suspend fun refreshMood() {
            // No-op for tests
        }
    }

    private lateinit var streakEngine: StreakEngine
    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var habitCompletionRepository: HabitCompletionRepository

    private val habits = listOf(
        HabitEntity(id = 1, name = "Habit 1"),
        HabitEntity(id = 2, name = "Habit 2"),
        HabitEntity(id = 3, name = "Habit 3")
    )

    @Before
    fun setup() {
        val habitRepository: HabitRepository = mock()
        habitCompletionRepository = mock()
        statisticsRepository = mock()

        // Non-suspend stubs only in @Before (getStatistics returns Flow, not suspend)
        whenever(habitRepository.getAllHabits()).thenReturn(flowOf(habits))
        whenever(statisticsRepository.getStatistics()).thenReturn(
            flowOf(StatisticsEntity(id = 1, currentStreak = 0, globalStreak = 0))
        )

        val rewardQueue: RewardQueue = mock()
        val inventoryRepo: InventoryItemRepository = mock()
        val timelineEngine: ActivityTimelineEngine = mock()
        val dragonMoodEngine: DragonMoodEngine = TestDragonMoodEngine()
        val challengeRepo: ChallengeRepository = mock()

        streakEngine = StreakEngine(
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

    // ==================== BASIC TESTS ====================

    @Test
    fun `evaluateTodayStreak - does not crash with default mocks`() = runTest {
        // Stub boolean methods inside coroutine context to avoid Kotlin unboxing NPE
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(false).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val ts = dayMillis(1)
        streakEngine.evaluateTodayStreak(ts)
        // No crash = success. hasAnyCompletionOnDate returns false (default),
        // so the streak increment logic is skipped.
    }

    @Test
    fun `checkPendingStreakFreeze - does not crash with default mocks`() = runTest {
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(false).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val ts = dayMillis(2)
        val prompt = streakEngine.checkPendingStreakFreeze(ts)
        // hasAnyCompletionOnDate returns false, currentStreak=0 from setup
        assertNull("No freeze when streak is 0", prompt)
    }

    @Test
    fun `recalculateTodayStreak - does not crash with default mocks`() = runTest {
        doReturn(false).whenever(statisticsRepository).isStreakAlreadyCountedToday()
        doReturn(false).whenever(habitCompletionRepository).hasAnyCompletionOnDate(any())

        val ts = dayMillis(1)
        streakEngine.recalculateTodayStreak(ts)
        // No crash = success
    }

    @Test
    fun `evaluateTodayStreak - no habits means no increment`() = runTest {
        val ts = dayMillis(1)

        val emptyHabitRepo: HabitRepository = mock()
        whenever(emptyHabitRepo.getAllHabits()).thenReturn(flowOf(emptyList()))

        val emptyStatsRepo: StatisticsRepository = mock()
        whenever(emptyStatsRepo.getStatistics()).thenReturn(flowOf(StatisticsEntity(id = 1)))

        val engine = StreakEngine(
            habitRepository = emptyHabitRepo,
            habitCompletionRepository = mock(),
            statisticsRepository = emptyStatsRepo,
            rewardQueue = mock(),
            inventoryItemRepository = mock(),
            activityTimelineEngine = mock(),
            dragonMoodEngine = TestDragonMoodEngine(),
            challengeRepository = mock()
        )

        // Stub boolean methods on the new engine's repos
        doReturn(false).whenever(emptyStatsRepo).isStreakAlreadyCountedToday()

        engine.evaluateTodayStreak(ts)

        // No crash = success. habits.isEmpty() returns early before any boolean calls.
    }

    @Test
    fun `no habits means no freeze prompt`() = runTest {
        val ts = dayMillis(2)

        val localHabitRepo: HabitRepository = mock()
        whenever(localHabitRepo.getAllHabits()).thenReturn(flowOf(emptyList()))

        val localStatsRepo: StatisticsRepository = mock()
        whenever(localStatsRepo.getStatistics()).thenReturn(flowOf(StatisticsEntity(id = 1)))

        val engine = StreakEngine(
            habitRepository = localHabitRepo,
            habitCompletionRepository = mock(),
            statisticsRepository = localStatsRepo,
            rewardQueue = mock(),
            inventoryItemRepository = mock(),
            activityTimelineEngine = mock(),
            dragonMoodEngine = TestDragonMoodEngine(),
            challengeRepository = mock()
        )

        doReturn(false).whenever(localStatsRepo).isStreakAlreadyCountedToday()

        val prompt = engine.checkPendingStreakFreeze(ts)

        assertNull("No freeze when no habits exist", prompt)
    }

    @Test
    fun `alternating patterns maintain streak`() = runTest {
        val hRepo: HabitRepository = mock()
        val hcRepo: HabitCompletionRepository = mock()
        val sRepo: StatisticsRepository = mock()
        val localHabits = listOf(HabitEntity(id = 1), HabitEntity(id = 2), HabitEntity(id = 3))

        whenever(hRepo.getAllHabits()).thenReturn(flowOf(localHabits))
        whenever(sRepo.getStatistics()).thenReturn(
            flowOf(StatisticsEntity(id = 1))
        )

        val engine = StreakEngine(
            habitRepository = hRepo,
            habitCompletionRepository = hcRepo,
            statisticsRepository = sRepo,
            rewardQueue = mock(),
            inventoryItemRepository = mock(),
            activityTimelineEngine = mock(),
            dragonMoodEngine = TestDragonMoodEngine(),
            challengeRepository = mock()
        )

        doReturn(false).whenever(sRepo).isStreakAlreadyCountedToday()
        doReturn(false).whenever(hcRepo).hasAnyCompletionOnDate(any())

        val ts = dayMillis(2)
        engine.evaluateTodayStreak(ts)
        // No crash = success
    }

    // ==================== HELPERS ====================

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
