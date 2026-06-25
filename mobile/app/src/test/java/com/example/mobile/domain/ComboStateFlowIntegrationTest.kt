package com.example.mobile.domain

import com.example.mobile.data.local.dao.HabitCompletionDao
import com.example.mobile.data.local.dao.HabitDao
import com.example.mobile.data.local.dao.HabitProgressDao
import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.HabitProgressEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.HabitProgressRepository
import com.example.mobile.data.repository.HabitCompletionRepositoryImpl
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
 * Integration test exercising the combo state flow:
 * HabitCompletionRepositoryImpl.addCompletionWithCombo -> ExpConfig combo logic
 *
 * Combo rules:
 *   - Window: 2 hours between completions
 *   - First completion: combo = 1, no bonus
 *   - Subsequent within window: combo increments, bonus = (combo - 1) XP capped at 4
 *   - Milestones: combo 3, 5, 10
 *   - Missing window: combo resets to 1
 *
 * Verifies:
 *   - First completion starts combo at 1 with no bonus
 *   - Second completion within 2h increments combo to 2 with +1 bonus
 *   - Third completion reaches milestone at combo 3
 *   - Combo continues to 4 with +3 bonus (not +3, formula is (4-1)*1 = 3)
 *   - Fifth completion reaches milestone at combo 5 with max bonus (+4)
 *   - Completion after 2h window resets combo to 1
 *   - Duplicate completion returns existing combo state
 */
class ComboStateFlowIntegrationTest {

    private lateinit var habitCompletionDao: HabitCompletionDao
    private lateinit var habitDao: HabitDao
    private lateinit var habitProgressDao: HabitProgressDao
    private lateinit var statisticsDao: StatisticsDao
    private lateinit var habitProgressRepository: HabitProgressRepository
    private lateinit var habitCompletionRepository: HabitCompletionRepositoryImpl

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

    private val testTimerProgress = HabitProgressEntity(
        habitId = 1,
        date = System.currentTimeMillis(),
        accumulatedMinutes = 10,
        lastUpdated = System.currentTimeMillis()
    )

    // Track DAO call counts
    private var insertCompletionCallCount = 0
    private var updateStatisticsCallCount = 0
    private var updatePetCallCount = 0

    @Before
    fun setup() {
        habitCompletionDao = mock()
        habitDao = mock()
        habitProgressDao = mock()
        statisticsDao = mock()
        habitProgressRepository = mock()

        insertCompletionCallCount = 0
        updateStatisticsCallCount = 0
        updatePetCallCount = 0

        runTest {
            // getCompletionForHabitOnDateOnce: no existing completion
            whenever(habitCompletionDao.getCompletionForHabitOnDateOnce(any(), any())).thenReturn(null)
            whenever(habitCompletionDao.insertCompletion(any())).thenAnswer {
                insertCompletionCallCount++
                it.getArgument<HabitCompletionEntity>(0).id.takeIf { id -> id > 0L } ?: 1L
            }
            whenever(habitCompletionDao.getActiveDayCount()).thenReturn(1)
            whenever(habitCompletionDao.getCompletionCountOnDate(any())).thenReturn(0)
            whenever(statisticsDao.getStatistics()).thenReturn(statsFlow)
            whenever(statisticsDao.updateStatistics(any())).thenAnswer {
                updateStatisticsCallCount++
                // Update flow to reflect combo state
                val updated = it.getArgument<StatisticsEntity>(0)
                statsFlow.value = updated
                1
            }
        }

        runTest {
            whenever(habitDao.getAllHabits()).thenReturn(habitsFlow)
            whenever(habitDao.getHabitById(1L)).thenReturn(flowOf(testHabit))
            whenever(habitDao.updateHabit(any())).thenReturn(1)
        }
        whenever(habitProgressRepository.getProgress(any(), any())).thenReturn(flowOf(testTimerProgress))

        habitCompletionRepository = HabitCompletionRepositoryImpl(
            habitCompletionDao = habitCompletionDao,
            habitDao = habitDao,
            habitProgressRepository = habitProgressRepository,
            statisticsDao = statisticsDao
        )
    }

    private fun setStats(
        currentCombo: Int = 0,
        lastHabitCompletionTimestamp: Long = 0L,
        bestCombo: Int = 0,
        totalCompletions: Int = 0,
        totalCoins: Int = 0
    ) {
        statsFlow.value = StatisticsEntity(
            id = 1,
            currentCombo = currentCombo,
            bestCombo = bestCombo,
            lastHabitCompletionTimestamp = lastHabitCompletionTimestamp,
            totalCompletions = totalCompletions,
            totalCoins = totalCoins
        )
    }

    private fun makeCompletion(habitId: Long = 1L, timestamp: Long, xpEarned: Long = 10L): HabitCompletionEntity {
        return HabitCompletionEntity(
            id = timestamp,
            habitId = habitId,
            date = timestamp,
            xpEarned = xpEarned
        )
    }

    @Test
    fun `combo — first completion starts combo at 1 with no bonus`() = runTest {
        setStats(lastHabitCompletionTimestamp = 0L, currentCombo = 0)

        val completion = makeCompletion(timestamp = 1000L)
        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals("First combo should be 1", 1, result.combo)
        assertEquals("No bonus for first completion", 0L, result.comboBonusXp)
        assertEquals("Total XP should equal base", 10L, result.totalXpEarned)
        assertEquals("Multiplier should be 1.0", 1f, result.comboMultiplier)
        assertFalse("No milestone at combo 1", result.comboMilestoneReached)
    }

    @Test
    fun `combo — second completion within 2h increments combo to 2`() = runTest {
        val now = System.currentTimeMillis()
        val oneHourMs = 60 * 60 * 1000L

        // Stats reflect the first completion was 1 hour ago
        setStats(
            lastHabitCompletionTimestamp = now - oneHourMs,
            currentCombo = 1,
            bestCombo = 1
        )

        val completion = makeCompletion(timestamp = now)
        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals("Combo should be 2", 2, result.combo)
        assertEquals("Bonus should be 1 XP at combo 2", 1L, result.comboBonusXp)
        assertEquals("Total XP should be 10 + 1 = 11", 11L, result.totalXpEarned)
        assertEquals("Multiplier should be 1.1", 1.1f, result.comboMultiplier, 0.01f)
        assertFalse("No milestone at combo 2", result.comboMilestoneReached)
    }

    @Test
    fun `combo — third completion reaches milestone at combo 3`() = runTest {
        val now = System.currentTimeMillis()
        val oneHourMs = 60 * 60 * 1000L

        setStats(
            lastHabitCompletionTimestamp = now - oneHourMs,
            currentCombo = 2,
            bestCombo = 2
        )

        val completion = makeCompletion(timestamp = now)
        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals("Combo should be 3", 3, result.combo)
        assertEquals("Bonus should be 2 XP at combo 3", 2L, result.comboBonusXp)
        assertEquals("Total XP should be 10 + 2 = 12", 12L, result.totalXpEarned)
        assertTrue("Milestone should be reached at combo 3", result.comboMilestoneReached)
    }

    @Test
    fun `combo — fifth completion reaches milestone and max bonus`() = runTest {
        val now = System.currentTimeMillis()
        val oneHourMs = 60 * 60 * 1000L

        setStats(
            lastHabitCompletionTimestamp = now - oneHourMs,
            currentCombo = 4,
            bestCombo = 4
        )

        val completion = makeCompletion(timestamp = now)
        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals("Combo should be 5", 5, result.combo)
        // Bonus = (5-1) * 1 = 4, capped at 4
        assertEquals("Bonus should be 4 XP (max)", 4L, result.comboBonusXp)
        assertEquals("Total XP should be 10 + 4 = 14", 14L, result.totalXpEarned)
        assertTrue("Milestone should be reached at combo 5", result.comboMilestoneReached)
    }

    @Test
    fun `combo — bonus caps at 4 XP for high combos`() = runTest {
        val now = System.currentTimeMillis()
        val oneHourMs = 60 * 60 * 1000L

        setStats(
            lastHabitCompletionTimestamp = now - oneHourMs,
            currentCombo = 9,
            bestCombo = 9
        )

        val completion = makeCompletion(timestamp = now)
        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertEquals("Combo should be 10", 10, result.combo)
        // Bonus = (10-1) * 1 = 9, but capped at 4
        assertEquals("Bonus should be capped at 4", 4L, result.comboBonusXp)
        assertTrue("Milestone should be reached at combo 10", result.comboMilestoneReached)
    }

    @Test
    fun `combo — completion after 2h window resets combo to 1`() = runTest {
        val now = System.currentTimeMillis()
        val threeHoursMs = 3 * 60 * 60 * 1000L

        // Last completion was 3 hours ago (outside 2h window)
        setStats(
            lastHabitCompletionTimestamp = now - threeHoursMs,
            currentCombo = 5,
            bestCombo = 5
        )

        val completion = makeCompletion(timestamp = now)
        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals("Combo should reset to 1", 1, result.combo)
        assertEquals("No bonus after reset", 0L, result.comboBonusXp)
        assertEquals("Total XP should equal base", 10L, result.totalXpEarned)
    }

    @Test
    fun `combo — best combo is tracked`() = runTest {
        val now = System.currentTimeMillis()
        val oneHourMs = 60 * 60 * 1000L

        // Build up combo to 3
        setStats(
            lastHabitCompletionTimestamp = now - oneHourMs,
            currentCombo = 2,
            bestCombo = 2
        )

        val completion = makeCompletion(timestamp = now)
        habitCompletionRepository.addCompletionWithCombo(completion)

        // After combo 3, stats should reflect bestCombo = 3
        val updatedStats = statsFlow.value
        assertEquals("Best combo should be 3", 3, updatedStats.bestCombo)
        assertEquals("Current combo should be 3", 3, updatedStats.currentCombo)
    }

    @Test
    fun `combo — duplicate completion returns existing state without combo change`() = runTest {
        val now = System.currentTimeMillis()
        val oneHourMs = 60 * 60 * 1000L

        // Set up as if there was a recent completion
        setStats(
            lastHabitCompletionTimestamp = now - oneHourMs,
            currentCombo = 2,
            bestCombo = 2,
            totalCompletions = 2
        )

        // Return an existing completion to trigger the duplicate path
        val existingCompletion = makeCompletion(timestamp = now - oneHourMs)
        doReturn(existingCompletion).whenever(habitCompletionDao)
            .getCompletionForHabitOnDateOnce(1L, now)

        val completion = makeCompletion(timestamp = now)
        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertFalse("Should NOT be new completion", result.isNewCompletion)
        assertEquals("Should return existing completion ID", existingCompletion.id, result.completionId)
        assertEquals("Should return existing XP", result.totalXpEarned, existingCompletion.xpEarned)

        verify(habitCompletionDao, never()).insertCompletion(any())
        verify(statisticsDao, never()).updateStatistics(any())
    }

    @Test
    fun `combo — combo active exactly at 2h boundary`() = runTest {
        val now = System.currentTimeMillis()
        val twoHoursMs = 2L * 60L * 60L * 1000L

        // Last completion exactly 2 hours ago (within window: <= boundary)
        setStats(
            lastHabitCompletionTimestamp = now - twoHoursMs,
            currentCombo = 2,
            bestCombo = 2
        )

        val completion = makeCompletion(timestamp = now)
        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        // isComboActive: now - lastHabitCompletionTimestamp <= COMBO_INACTIVITY_WINDOW_MS
        // At exactly 2h: (now - (now - 2h)) = 2h <= 2h → true
        assertEquals("Should be exactly at 2h boundary", twoHoursMs, now - (now - twoHoursMs))
        assertTrue("Combo should still be active at exactly 2h", result.isNewCompletion)
        assertEquals("Combo should be 3 at boundary", 3, result.combo)
    }

    @Test
    fun `combo — combo inactive just past 2h boundary`() = runTest {
        val now = System.currentTimeMillis()
        val twoHoursMs = 2L * 60L * 60L * 1000L
        val oneMs = 1L

        // Last completion just past 2h window
        setStats(
            lastHabitCompletionTimestamp = now - twoHoursMs - oneMs,
            currentCombo = 2,
            bestCombo = 2
        )

        val completion = makeCompletion(timestamp = now)
        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        // isComboActive: now - lastHabitCompletionTimestamp <= COMBO_INACTIVITY_WINDOW_MS
        // 2h + 1ms > 2h → false
        assertEquals("Combo should reset to 1 just past boundary", 1, result.combo)
        assertEquals("No bonus after reset", 0L, result.comboBonusXp)
    }

    @Test
    fun `combo — every 3rd combo milestone`() = runTest {
        val now = System.currentTimeMillis()
        val oneHourMs = 60 * 60 * 1000L

        // Combo at 9, next increment to 10 (milestone)
        setStats(
            lastHabitCompletionTimestamp = now - oneHourMs,
            currentCombo = 9,
            bestCombo = 9
        )

        val completion = makeCompletion(timestamp = now)
        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertEquals("Combo should be 10", 10, result.combo)
        assertTrue("Combo 10 is a milestone", result.comboMilestoneReached)
        // Combo 4, 6, 7, 8, 9 are NOT milestones
        assertFalse("Combo 4 is not a milestone", ExpConfig.comboMilestoneReached(4))
        assertFalse("Combo 6 is not a milestone", ExpConfig.comboMilestoneReached(6))
        assertTrue("Combo 5 is a milestone", ExpConfig.comboMilestoneReached(5))
        assertTrue("Combo 3 is a milestone", ExpConfig.comboMilestoneReached(3))
    }

    @Test
    fun `combo — full combo build-up from 1 to 5`() = runTest {
        val now = System.currentTimeMillis()
        val oneHourMs = 60 * 60 * 1000L

        var timestamp = now - (5 * oneHourMs)

        // Step 1: First completion (combo 1)
        setStats(lastHabitCompletionTimestamp = 0L, currentCombo = 0, bestCombo = 0)
        var result = habitCompletionRepository.addCompletionWithCombo(makeCompletion(timestamp = timestamp))
        assertEquals(1, result.combo)
        assertEquals(0L, result.comboBonusXp)

        // Step 2: Second completion (combo 2)
        timestamp += oneHourMs
        setStats(lastHabitCompletionTimestamp = timestamp - oneHourMs, currentCombo = 1, bestCombo = 1)
        result = habitCompletionRepository.addCompletionWithCombo(makeCompletion(timestamp = timestamp))
        assertEquals(2, result.combo)
        assertEquals(1L, result.comboBonusXp)

        // Step 3: Third completion (combo 3, milestone)
        timestamp += oneHourMs
        setStats(lastHabitCompletionTimestamp = timestamp - oneHourMs, currentCombo = 2, bestCombo = 2)
        result = habitCompletionRepository.addCompletionWithCombo(makeCompletion(timestamp = timestamp))
        assertEquals(3, result.combo)
        assertEquals(2L, result.comboBonusXp)
        assertTrue("Milestone at 3", result.comboMilestoneReached)

        // Step 4: Fourth completion (combo 4)
        timestamp += oneHourMs
        setStats(lastHabitCompletionTimestamp = timestamp - oneHourMs, currentCombo = 3, bestCombo = 3)
        result = habitCompletionRepository.addCompletionWithCombo(makeCompletion(timestamp = timestamp))
        assertEquals(4, result.combo)
        assertEquals(3L, result.comboBonusXp)
        assertFalse("No milestone at 4", result.comboMilestoneReached)

        // Step 5: Fifth completion (combo 5, milestone, max bonus)
        timestamp += oneHourMs
        setStats(lastHabitCompletionTimestamp = timestamp - oneHourMs, currentCombo = 4, bestCombo = 4)
        result = habitCompletionRepository.addCompletionWithCombo(makeCompletion(timestamp = timestamp))
        assertEquals(5, result.combo)
        assertEquals(4L, result.comboBonusXp)
        assertTrue("Milestone at 5", result.comboMilestoneReached)
    }
}
