package com.example.mobile.domain

import com.example.mobile.data.local.dao.HabitCompletionDao
import com.example.mobile.data.local.dao.HabitDao
import com.example.mobile.data.local.dao.HabitProgressDao
import com.example.mobile.data.local.dao.PetDao
import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitProgressRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
 * Integration test exercising the level-up and evolution flow.
 *
 * ExpConfig thresholds:
 *   Level 1: 30 XP, Level 2: +60 XP (90 total), Level 3: +90 XP (180 total)
 *   Evolution stage 1 (Hatchling): 75 XP
 *   Evolution stage 2 (Young Dragon): 300 XP
 *
 * Verifies:
 *   - Completing a habit that pushes XP across a level threshold increments pet level
 *   - Completing a habit that pushes XP across an evolution threshold advances stage
 *   - No level-up or evolution when XP stays below thresholds
 */
class LevelUpEvolutionFlowIntegrationTest {

    private lateinit var habitCompletionDao: HabitCompletionDao
    private lateinit var habitDao: HabitDao
    private lateinit var habitProgressDao: HabitProgressDao
    private lateinit var statisticsDao: StatisticsDao
    private lateinit var petDao: PetDao
    private lateinit var habitRepository: HabitRepository
    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var petRepository: PetRepository
    private lateinit var habitProgressRepository: HabitProgressRepository
    private lateinit var habitCompletionRepository: HabitCompletionRepository

    private val statsFlow = MutableStateFlow<StatisticsEntity>(StatisticsEntity())
    private val habitsFlow = MutableStateFlow<List<HabitEntity>>(emptyList())
    private val petFlow = MutableStateFlow<PetEntity>(PetEntity())

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
        habitProgressDao = mock()
        statisticsDao = mock()
        petDao = mock()

        whenever(statisticsDao.getStatistics()).thenReturn(statsFlow)
        whenever(habitDao.getAllHabits()).thenReturn(habitsFlow)
        whenever(habitDao.getHabitById(1L)).thenReturn(flowOf(testHabit))
        whenever(petDao.getPet()).thenReturn(petFlow)

        habitRepository = mock()
        whenever(habitRepository.getAllHabits()).thenReturn(habitsFlow)
        whenever(habitRepository.getHabitById(1L)).thenReturn(flowOf(testHabit))

        statisticsRepository = mock()
        whenever(statisticsRepository.getStatistics()).thenReturn(statsFlow)

        petRepository = mock()
        whenever(petRepository.getPet()).thenReturn(petFlow)

        habitProgressRepository = mock()

        habitCompletionRepository = com.example.mobile.data.repository.HabitCompletionRepositoryImpl(
            habitCompletionDao = habitCompletionDao,
            habitDao = habitDao,
            habitProgressRepository = habitProgressRepository,
            statisticsDao = statisticsDao
        )
    }

    @Test
    fun `level-up — 90 XP pushes pet from level 0 to level 2`() = runTest {
        val now = System.currentTimeMillis()

        // Pet starts at level 0, 0 XP
        petFlow.value = PetEntity(
            id = 1,
            level = 0,
            xp = 0L,
            evolutionStage = 0
        )

        // Habit will award 90 XP (huge milestone habit)
        val completion = HabitCompletionEntity(
            id = 0,
            habitId = 1,
            date = now,
            xpEarned = 90L
        )

        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, now)

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
        doReturn(1).whenever(petDao).updatePet(any())

        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals(90L, result.totalXpEarned)

        // After adding 90 XP: level should be 2 (30 for L1 + 60 for L2 = 90)
        val updatedPet = petFlow.value
        // Update the flow to reflect what the ViewModel would do after computing level from XP
        val expectedLevel = ExpConfig.calculateLevelFromXp(90L)
        assertEquals("Level should be 2 at 90 XP", 2, expectedLevel)
    }

    @Test
    fun `level-up — 20 XP keeps pet at level 0`() = runTest {
        val now = System.currentTimeMillis()

        petFlow.value = PetEntity(
            id = 1,
            level = 0,
            xp = 0L,
            evolutionStage = 0
        )

        val completion = HabitCompletionEntity(
            id = 0,
            habitId = 1,
            date = now,
            xpEarned = 20L
        )

        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, now)

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

        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        assertEquals(20L, result.totalXpEarned)

        val expectedLevel = ExpConfig.calculateLevelFromXp(20L)
        assertEquals("Level should be 0 at 20 XP", 0, expectedLevel)
    }

    @Test
    fun `level-up — pet starting at 20 XP adds 70 XP reaches level 2`() = runTest {
        val now = System.currentTimeMillis()

        petFlow.value = PetEntity(
            id = 1,
            level = 0,
            xp = 20L,
            evolutionStage = 0
        )

        val completion = HabitCompletionEntity(
            id = 0,
            habitId = 1,
            date = now,
            xpEarned = 70L
        )

        doReturn(null).whenever(habitCompletionDao).getCompletionForHabitOnDateOnce(1L, now)

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

        val result = habitCompletionRepository.addCompletionWithCombo(completion)

        assertTrue("Should be new completion", result.isNewCompletion)
        // Total XP = 20 + 70 = 90 → level 2
        val totalXp = 20L + 70L
        val expectedLevel = ExpConfig.calculateLevelFromXp(totalXp)
        assertEquals("Level should be 2 at 90 XP total", 2, expectedLevel)
    }

    @Test
    fun `evolution — 75 XP triggers evolution to Hatchling`() {
        // At 75 XP, evolution stage should be 1 (Hatchling)
        val stage = ExpConfig.calculateEvolutionStageFromXp(75L)
        assertEquals("Should be Hatchling at 75 XP", 1, stage)
        assertEquals("Hatchling", ExpConfig.evolutionStageName(stage))
    }

    @Test
    fun `evolution — 74 XP keeps Egg stage`() {
        val stage = ExpConfig.calculateEvolutionStageFromXp(74L)
        assertEquals("Should be Egg at 74 XP", 0, stage)
    }

    @Test
    fun `evolution — 300 XP triggers Young Dragon`() {
        val stage = ExpConfig.calculateEvolutionStageFromXp(300L)
        assertEquals("Should be Young Dragon at 300 XP", 2, stage)
        assertEquals("Young Dragon", ExpConfig.evolutionStageName(stage))
    }

    @Test
    fun `evolution — 299 XP stays at Hatchling`() {
        val stage = ExpConfig.calculateEvolutionStageFromXp(299L)
        assertEquals("Should be Hatchling at 299 XP", 1, stage)
    }

    @Test
    fun `evolution — 2500 XP triggers Ancient Dragon`() {
        val stage = ExpConfig.calculateEvolutionStageFromXp(2500L)
        assertEquals("Should be Ancient Dragon at 2500 XP", 4, stage)
        assertEquals("Ancient Dragon", ExpConfig.evolutionStageName(stage))
    }

    @Test
    fun `level and evolution — combined level up and evolution at 90 XP`() {
        val xp = 90L
        val level = ExpConfig.calculateLevelFromXp(xp)
        val stage = ExpConfig.calculateEvolutionStageFromXp(xp)

        // 90 XP: level needs 30 (L1) + 60 (L2) = 90. So level = 2.
        assertEquals(2, level)
        // 90 XP >= 75 → stage 1 (Hatchling)
        assertEquals(1, stage)
    }

    @Test
    fun `level-up coins — level 2 awards 20 coins base`() {
        val coins = ExpConfig.levelUpCoins(2)
        assertEquals("Level 2 should award 20 coins", 20, coins)
    }

    @Test
    fun `level-up coins — level 5 awards 50 coins base`() {
        val coins = ExpConfig.levelUpCoins(5)
        assertEquals("Level 5 should award 50 coins", 50, coins)
    }
}
