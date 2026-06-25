package com.example.mobile.domain

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.example.mobile.domain.repository.ChallengeRepository
import com.example.mobile.domain.repository.GameEventRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.reward.RewardEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for ActivityTimelineEngine.
 *
 * The engine's default scope uses Dispatchers.IO, which would require real
 * threads. We inject a CoroutineScope backed by UnconfinedTestDispatcher so
 * launched coroutines run eagerly up to their first suspension point. This
 * avoids the need for advanceUntilIdle() in most tests.
 */
@kotlin.OptIn(ExperimentalCoroutinesApi::class)
class ActivityTimelineEngineTest {

    private lateinit var gameEventRepository: GameEventRepository
    private lateinit var rewardEventBus: RewardEventBus
    private lateinit var petRepository: PetRepository
    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var challengeRepository: ChallengeRepository
    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: Editor
    private lateinit var engine: ActivityTimelineEngine

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher)
    private val statsFlow = MutableStateFlow(com.example.mobile.data.local.entities.StatisticsEntity())

    @Before
    fun setup() {
        gameEventRepository = mock()
        rewardEventBus = mock()
        petRepository = mock()
        statisticsRepository = mock()
        challengeRepository = mock()
        context = mock()
        prefs = mock()
        editor = mock()

        whenever(context.getSharedPreferences(any(), any())).thenReturn(prefs)
        whenever(prefs.getString(any(), any())).thenReturn(null)
        whenever(prefs.getLong(any(), any())).thenReturn(0L)
        whenever(prefs.getBoolean(any(), any())).thenReturn(false)
        whenever(prefs.edit()).thenReturn(editor)
        whenever(editor.putString(any(), any())).thenReturn(editor)
        whenever(editor.putLong(any(), any())).thenReturn(editor)
        whenever(editor.putBoolean(any(), any())).thenReturn(editor)
        whenever(statisticsRepository.getStatistics()).thenReturn(statsFlow)
        // Prevent NPE in observeRewardEvents which collects from rewardEvents flow
        val rewardEventsFlow = MutableSharedFlow<RewardUiEvent>()
        whenever(rewardEventBus.rewardEvents).thenReturn(rewardEventsFlow)

        engine = ActivityTimelineEngine(
            gameEventRepository = gameEventRepository,
            rewardEventBus = rewardEventBus,
            petRepository = petRepository,
            statisticsRepository = statisticsRepository,
            challengeRepository = challengeRepository,
            context = context,
            scopeCoroutineContext = testDispatcher
        )
    }

    // ==================== logHabitCompleted ====================

    @Test
    fun `logHabitCompleted — delegates to repository`() = runTest {
        engine.logHabitCompleted(
            habitName = "Morning jog",
            xpEarned = 15L,
            coinsEarned = 10
        )

        verify(gameEventRepository).logEvent(any())
    }

    // ==================== logComboMilestone ====================

    @Test
    fun `logComboMilestone — delegates to repository`() = runTest {
        engine.logComboMilestone(combo = 5, bonusXp = 4L, multiplier = 1.4f)

        verify(gameEventRepository).logEvent(any())
    }

    // ==================== logAchievementUnlocked ====================

    @Test
    fun `logAchievementUnlocked — delegates to repository`() = runTest {
        engine.logAchievementUnlocked(achievementName = "First Habit")

        verify(gameEventRepository).logEvent(any())
    }

    // ==================== logLevelUp ====================

    @Test
    fun `logLevelUp — delegates to repository`() = runTest {
        engine.logLevelUp(level = 5, coins = 50)

        verify(gameEventRepository).logEvent(any())
    }

    // ==================== logDragonEvolution ====================

    @Test
    fun `logDragonEvolution — delegates to repository`() = runTest {
        engine.logDragonEvolution(fromStage = 0, toStage = 1)

        verify(gameEventRepository).logEvent(any())
    }

    // ==================== logStreakMilestone ====================

    @Test
    fun `logStreakMilestone — delegates to repository`() = runTest {
        engine.logStreakMilestone(streak = 7, chestType = ChestType.NORMAL)

        verify(gameEventRepository).logEvent(any())
    }

    // ==================== logSurpriseReward ====================

    @Test
    fun `logSurpriseReward — delegates to repository`() = runTest {
        engine.logSurpriseReward(
            coins = 30,
            xp = 20L,
            chestType = "RARE",
            hasCustomization = true
        )

        verify(gameEventRepository).logEvent(any())
    }

    // ==================== logChallengeCompleted ====================

    @Test
    fun `logChallengeCompleted — delegates to repository`() = runTest {
        engine.logChallengeCompleted(
            challengeName = "Three habit rhythm",
            rewards = listOf(ChallengeRewardDefinition.CoinReward(25))
        )

        verify(gameEventRepository).logEvent(any())
    }

    // ==================== ensureFirstDailyLoginEvent ====================

    @Test
    fun `start — logs first daily login on first visit`() = runTest {
        engine.start()

        verify(gameEventRepository).logEvent(any())
    }

    @Test
    fun `ensureFirstDailyLoginEvent — does not log twice on same day`() = runTest {
        // Capture the dayKey that the engine writes via putString during first start
        val dayKeyCapture = org.mockito.kotlin.argumentCaptor<String>()

        // First run: trigger a login event so the engine writes today's dayKey
        engine.start()

        // Capture the dayKey that was written via putString
        org.mockito.kotlin.verify(editor).putString(
            org.mockito.kotlin.eq("last_login_day"),
            dayKeyCapture.capture()
        )
        val actualDayKey = dayKeyCapture.firstValue

        // Now set up logEvent tracking — count calls from the second start
        var logCallCount = 0
        org.mockito.kotlin.doAnswer { _ ->
            logCallCount++
            1L
        }.whenever(gameEventRepository).logEvent(any())

        // Make getString return the actual dayKey the engine produced
        whenever(prefs.getString(any(), any())).thenReturn(actualDayKey)

        // Second run: since lastLoginDay == today, no login event should be logged
        engine.start()

        assertEquals("logEvent should not be called when already logged today", 0, logCallCount)
    }

    @Test
    fun `start — logs again when day changes`() = runTest {
        // Set lastLoginDay to a different day
        whenever(prefs.getString(any(), any())).thenReturn("2020-1-1")

        engine.start()

        verify(gameEventRepository).logEvent(any())
    }

    // ==================== logEvolutionMilestoneNearing ====================

    @Test
    fun `logEvolutionMilestoneNearing — logs when progress above 80 percent`() = runTest {
        // XP = 70, stage 0, next stage 1 (threshold 75)
        // Progress = (70 - 0) / (75 - 0) = 0.933... > 0.8
        engine.logEvolutionMilestoneNearing(toStage = 1, xp = 70L)

        verify(gameEventRepository).logEvent(any())
    }

    @Test
    fun `logEvolutionMilestoneNearing — does not log when already logged`() = runTest {
        // First, set the pref to already logged
        whenever(prefs.getBoolean(any(), any())).thenReturn(true)

        engine.logEvolutionMilestoneNearing(toStage = 1, xp = 70L)

        verify(gameEventRepository, never()).logEvent(any())
    }

    @Test
    fun `logEvolutionMilestoneNearing — does not log when progress below threshold`() = runTest {
        // XP = 10, stage 0, next stage 1 (threshold 75)
        // Progress = 10/75 = 0.133... < 0.8
        engine.logEvolutionMilestoneNearing(toStage = 1, xp = 10L)

        verify(gameEventRepository, never()).logEvent(any())
    }

    // ==================== formatLastSessionDifference ====================

    @Test
    fun `formatLastSessionDifference — first visit message`() {
        val method = engine.javaClass.getDeclaredMethod("formatLastSessionDifference", Long::class.java)
        method.isAccessible = true
        val result = method.invoke(engine, 0L) as String
        assertEquals("This is your first visit.", result)
    }

    @Test
    fun `formatLastSessionDifference — minutes ago`() {
        val method = engine.javaClass.getDeclaredMethod("formatLastSessionDifference", Long::class.java)
        method.isAccessible = true
        val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000L)
        val result = method.invoke(engine, fiveMinutesAgo) as String
        assertTrue("Should contain minutes", result.contains("m ago"))
    }

    @Test
    fun `formatLastSessionDifference — hours ago`() {
        val method = engine.javaClass.getDeclaredMethod("formatLastSessionDifference", Long::class.java)
        method.isAccessible = true
        val threeHoursAgo = System.currentTimeMillis() - (3 * 60 * 60 * 1000L)
        val result = method.invoke(engine, threeHoursAgo) as String
        assertTrue("Should contain hours", result.contains("h ago"))
    }

    @Test
    fun `formatLastSessionDifference — days ago`() {
        val method = engine.javaClass.getDeclaredMethod("formatLastSessionDifference", Long::class.java)
        method.isAccessible = true
        val twoDaysAgo = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L)
        val result = method.invoke(engine, twoDaysAgo) as String
        assertTrue("Should contain days", result.contains("d ago"))
    }

    // ==================== motivationalMessageFor ====================

    @Test
    fun `motivationalMessageFor — first visit`() {
        val method = engine.javaClass.getDeclaredMethod("motivationalMessageFor", Long::class.java)
        method.isAccessible = true
        val result = method.invoke(engine, 0L) as String
        assertEquals("A fresh page is ready for your dragon.", result)
    }

    @Test
    fun `motivationalMessageFor — within a day`() {
        val method = engine.javaClass.getDeclaredMethod("motivationalMessageFor", Long::class.java)
        method.isAccessible = true
        val fourHoursAgo = System.currentTimeMillis() - (4 * 60 * 60 * 1000L)
        val result = method.invoke(engine, fourHoursAgo) as String
        assertEquals("You kept the rhythm alive.", result)
    }

    @Test
    fun `motivationalMessageFor — 1-3 days`() {
        val method = engine.javaClass.getDeclaredMethod("motivationalMessageFor", Long::class.java)
        method.isAccessible = true
        val twoDaysAgo = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L)
        val result = method.invoke(engine, twoDaysAgo) as String
        assertEquals("Your dragon is happy you came back.", result)
    }

    @Test
    fun `motivationalMessageFor — 3-7 days`() {
        val method = engine.javaClass.getDeclaredMethod("motivationalMessageFor", Long::class.java)
        method.isAccessible = true
        val fiveDaysAgo = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000L)
        val result = method.invoke(engine, fiveDaysAgo) as String
        assertEquals("A small return is still a win.", result)
    }

    @Test
    fun `motivationalMessageFor — over 7 days`() {
        val method = engine.javaClass.getDeclaredMethod("motivationalMessageFor", Long::class.java)
        method.isAccessible = true
        val tenDaysAgo = System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000L)
        val result = method.invoke(engine, tenDaysAgo) as String
        assertEquals("Your dragon has been patiently waiting for you.", result)
    }

    // ==================== evolutionProgressFraction ====================

    @Test
    fun `evolutionProgressFraction — at stage start returns 0`() {
        val method = engine.javaClass.getDeclaredMethod("evolutionProgressFraction", Long::class.java, Int::class.java)
        method.isAccessible = true
        // Stage 0 starts at XP 0, next stage 1 at XP 75
        val result = method.invoke(engine, 0L, 0) as Float
        assertEquals(0f, result, 0.01f)
    }

    @Test
    fun `evolutionProgressFraction — at stage end returns 1`() {
        val method = engine.javaClass.getDeclaredMethod("evolutionProgressFraction", Long::class.java, Int::class.java)
        method.isAccessible = true
        // Stage 0 starts at XP 0, next stage 1 at XP 75
        val result = method.invoke(engine, 75L, 0) as Float
        assertEquals(1f, result, 0.01f)
    }

    @Test
    fun `evolutionProgressFraction — midpoint returns approximately 0_5`() {
        val method = engine.javaClass.getDeclaredMethod("evolutionProgressFraction", Long::class.java, Int::class.java)
        method.isAccessible = true
        // Stage 0 starts at XP 0, next stage 1 at XP 75, midpoint = 37.5
        val result = method.invoke(engine, 37L, 0) as Float
        assertTrue("Should be around 0.5, was $result", result in 0.4f..0.6f)
    }

    // ==================== start ====================

    @Test
    fun `start — only runs once`() = runTest {
        // First call sets started=true, second call returns early
        engine.start()
        engine.start()

        // The engine's started flag prevents double-start, but since lastLoginDay is null,
        // the first start will log the event. The second start is a no-op.
        verify(gameEventRepository).logEvent(any())
    }
}
