package com.example.mobile.domain

import com.example.mobile.data.local.entities.AchievementEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.AchievementRepository
import com.example.mobile.domain.repository.GameEventRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import android.util.Log
import kotlinx.coroutines.runBlocking
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.junit.After
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AchievementEngineTest {

    private lateinit var achievementRepository: AchievementRepository
    private lateinit var habitRepository: HabitRepository
    private lateinit var petRepository: PetRepository
    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var inventoryItemRepository: InventoryItemRepository
    private lateinit var achievementRewardProcessor: AchievementRewardProcessor
    private lateinit var activityTimelineEngine: ActivityTimelineEngine
    private lateinit var gameEventRepository: GameEventRepository
    private lateinit var engine: AchievementEngine

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var logMock: MockedStatic<Log>

    private val statsFlow = MutableStateFlow(StatisticsEntity())
    private val petFlow = MutableStateFlow(PetEntity(id = 1))
    private val habitsFlow = MutableStateFlow<List<HabitEntity>>(emptyList())
    private val outfitFlow = MutableStateFlow<List<InventoryItemEntity>>(emptyList())
    private val backgroundFlow = MutableStateFlow<List<InventoryItemEntity>>(emptyList())
    private val auraFlow = MutableStateFlow<List<InventoryItemEntity>>(emptyList())

    private val achievementStates = mutableMapOf(
        "first_habit" to AchievementEntity(id = "first_habit", progress = 0, isUnlocked = false, isClaimed = false),
        "seven_day_streak" to AchievementEntity(id = "seven_day_streak", progress = 0, isUnlocked = false, isClaimed = false)
    )
    private val achievementFlows = mutableMapOf<String, MutableStateFlow<AchievementEntity?>>()

    @Before
    fun setup() {
        logMock = mockStatic(Log::class.java)
        whenever(Log.d(any(), any())).thenReturn(0)
        whenever(Log.e(any(), any(), any())).thenReturn(0)

        achievementRepository = mock()
        habitRepository = mock()
        petRepository = mock()
        statisticsRepository = mock()
        inventoryItemRepository = mock()
        achievementRewardProcessor = mock()
        activityTimelineEngine = mock()
        gameEventRepository = mock()

        whenever(achievementRepository.getAllAchievements()).thenReturn(
            MutableStateFlow(achievementStates.values.toList())
        )
        whenever(achievementRepository.getAchievementById(any())).thenAnswer { invocation ->
            val id = invocation.arguments[0] as String
            achievementFlows.computeIfAbsent(id) { MutableStateFlow(achievementStates[id]) }
        }
        runBlocking {
            whenever(achievementRepository.updateProgress(any(), any(), any())).thenAnswer { invocation ->
                val id = invocation.arguments[0] as String
                val progress = invocation.arguments[1] as Int
                val isUnlocked = invocation.arguments[2] as Boolean
                val updated = AchievementEntity(
                    id = id,
                    progress = progress,
                    isUnlocked = isUnlocked,
                    isClaimed = achievementStates[id]?.isClaimed ?: false
                )
                achievementStates[id] = updated
                achievementFlows[id]?.value = updated
                1
            }
        }
        whenever(statisticsRepository.getStatistics()).thenReturn(statsFlow)
        whenever(petRepository.getPet()).thenReturn(petFlow)
        whenever(habitRepository.getAllHabits()).thenReturn(habitsFlow)
        whenever(inventoryItemRepository.getItemsByType(CustomizationTypes.OUTFIT)).thenReturn(outfitFlow)
        whenever(inventoryItemRepository.getItemsByType(CustomizationTypes.BACKGROUND)).thenReturn(backgroundFlow)
        whenever(inventoryItemRepository.getItemsByType(CustomizationTypes.AURA)).thenReturn(auraFlow)
        runBlocking {
            whenever(gameEventRepository.countByType(any())).thenReturn(0)
            whenever(achievementRepository.countClaimed()).thenReturn(0)
            whenever(achievementRewardProcessor.process(any(), any())).thenReturn(true)
        }
    }

    @After
    fun teardown() {
        logMock.close()
    }

    private fun buildEngine() {
        engine = AchievementEngine(
            achievementRepository,
            habitRepository,
            petRepository,
            statisticsRepository,
            inventoryItemRepository,
            achievementRewardProcessor,
            activityTimelineEngine,
            gameEventRepository,
            testDispatcher
        )
    }

    // ==================== claimAchievement ====================

    @Test
    fun `claimAchievement — unknown id returns false`() = runTest {
        whenever(achievementRepository.getAchievementById("nope")).thenReturn(MutableStateFlow(null))
        buildEngine()

        val result = engine.claimAchievement("nope")

        assertFalse(result)
    }

    @Test
    fun `claimAchievement — locked achievement returns false`() = runTest {
        val entity = AchievementEntity(id = "first_habit", progress = 0, isUnlocked = false, isClaimed = false)
        whenever(achievementRepository.getAchievementById("first_habit")).thenReturn(MutableStateFlow(entity))
        buildEngine()

        val result = engine.claimAchievement("first_habit")

        assertFalse(result)
    }

    @Test
    fun `claimAchievement — target not reached returns false`() = runTest {
        val entity = AchievementEntity(id = "seven_day_streak", progress = 3, isUnlocked = false, isClaimed = false)
        whenever(achievementRepository.getAchievementById("seven_day_streak")).thenReturn(MutableStateFlow(entity))
        buildEngine()

        val result = engine.claimAchievement("seven_day_streak")

        assertFalse(result)
    }

    @Test
    fun `claimAchievement — already claimed returns false`() = runTest {
        val entity = AchievementEntity(id = "first_habit", progress = 1, isUnlocked = true, isClaimed = true)
        whenever(achievementRepository.getAchievementById("first_habit")).thenReturn(MutableStateFlow(entity))
        buildEngine()

        val result = engine.claimAchievement("first_habit")

        assertFalse(result)
    }

    @Test
    fun `claimAchievement — processor failure returns false`() = runTest {
        val entity = AchievementEntity(id = "first_habit", progress = 1, isUnlocked = true, isClaimed = false)
        whenever(achievementRepository.getAchievementById("first_habit")).thenReturn(MutableStateFlow(entity))
        whenever(achievementRewardProcessor.process(any(), any())).thenReturn(false)
        buildEngine()

        val result = engine.claimAchievement("first_habit")

        assertFalse(result)
    }

    @Test
    fun `claimAchievement — success returns true`() = runTest {
        val entity = AchievementEntity(id = "first_habit", progress = 1, isUnlocked = true, isClaimed = false)
        whenever(achievementRepository.getAchievementById("first_habit")).thenReturn(MutableStateFlow(entity))
        buildEngine()

        val result = engine.claimAchievement("first_habit")

        assertTrue(result)
    }

    // ==================== observer wiring ====================

    @Test
    fun `observer — streak progress updates streak achievements`() = runTest {
        buildEngine()
        statsFlow.value = StatisticsEntity(currentStreak = 7)
        advanceUntilIdle()

        verify(achievementRepository, atLeastOnce()).updateProgress(
            achievementId = "seven_day_streak",
            progress = 7,
            isUnlocked = true
        )
    }

    @Test
    fun `observer — best streak uses bestStreak not currentStreak`() = runTest {
        buildEngine()
        statsFlow.value = StatisticsEntity(currentStreak = 2, bestStreak = 14)
        advanceUntilIdle()

        verify(achievementRepository, atLeastOnce()).updateProgress(
            achievementId = "best_streak_14",
            progress = 14,
            isUnlocked = true
        )
    }

    @Test
    fun `observer — combo progress updates combo achievements`() = runTest {
        buildEngine()
        statsFlow.value = StatisticsEntity(bestCombo = 5)
        advanceUntilIdle()

        verify(achievementRepository, atLeastOnce()).updateProgress(
            achievementId = "best_combo_5",
            progress = 5,
            isUnlocked = true
        )
    }

    @Test
    fun `observer — days active updates days active achievements`() = runTest {
        buildEngine()
        statsFlow.value = StatisticsEntity(daysActive = 7)
        advanceUntilIdle()

        verify(achievementRepository, atLeastOnce()).updateProgress(
            achievementId = "days_active_7",
            progress = 7,
            isUnlocked = true
        )
    }

    @Test
    fun `observer — pet age updates pet age achievements`() = runTest {
        buildEngine()
        statsFlow.value = StatisticsEntity(petAgeDays = 30)
        advanceUntilIdle()

        verify(achievementRepository, atLeastOnce()).updateProgress(
            achievementId = "pet_age_30",
            progress = 30,
            isUnlocked = true
        )
    }

    @Test
    fun `observer — total coins updates coin achievements`() = runTest {
        buildEngine()
        statsFlow.value = StatisticsEntity(totalCoins = 1000)
        advanceUntilIdle()

        verify(achievementRepository, atLeastOnce()).updateProgress(
            achievementId = "coins_1000",
            progress = 1000,
            isUnlocked = true
        )
    }

    @Test
    fun `observer — freezes used parses freeze dates json`() = runTest {
        buildEngine()
        statsFlow.value = StatisticsEntity(streakFreezeDatesJson = "[100,200,300]")
        advanceUntilIdle()

        verify(achievementRepository, atLeastOnce()).updateProgress(
            achievementId = "freezes_1",
            progress = 3,
            isUnlocked = true
        )
    }

    @Test
    fun `observer — habit count updates habit achievements`() = runTest {
        buildEngine()
        habitsFlow.value = listOf(HabitEntity(id = 1), HabitEntity(id = 2), HabitEntity(id = 3))
        advanceUntilIdle()

        verify(achievementRepository, atLeastOnce()).updateProgress(
            achievementId = "three_habit_builder",
            progress = 3,
            isUnlocked = true
        )
    }

    @Test
    fun `observer — owned customizations counts only purchased across all types`() = runTest {
        buildEngine()
        outfitFlow.value = listOf(
            InventoryItemEntity(id = 1, itemId = "a", isPurchased = true),
            InventoryItemEntity(id = 2, itemId = "b", isPurchased = false)
        )
        backgroundFlow.value = listOf(InventoryItemEntity(id = 3, itemId = "c", isPurchased = true))
        auraFlow.value = listOf(InventoryItemEntity(id = 4, itemId = "d", isPurchased = false))
        advanceUntilIdle()

        verify(achievementRepository, atLeastOnce()).updateProgress(
            achievementId = "three_customizations",
            progress = 2,
            isUnlocked = false
        )
    }

    @Test
    fun `observer — xp updates xp achievements`() = runTest {
        buildEngine()
        petFlow.value = PetEntity(id = 1, xp = 1000L)
        advanceUntilIdle()

        verify(achievementRepository, atLeastOnce()).updateProgress(
            achievementId = "one_thousand_xp",
            progress = 1000,
            isUnlocked = true
        )
    }

    @Test
    fun `observer — level updates level achievements`() = runTest {
        buildEngine()
        petFlow.value = PetEntity(id = 1, level = 5)
        advanceUntilIdle()

        verify(achievementRepository, atLeastOnce()).updateProgress(
            achievementId = "level_five",
            progress = 5,
            isUnlocked = true
        )
    }

    @Test
    fun `observer — completions updates completion achievements`() = runTest {
        buildEngine()
        statsFlow.value = StatisticsEntity(totalCompletions = 25)
        advanceUntilIdle()

        verify(achievementRepository, atLeastOnce()).updateProgress(
            achievementId = "twenty_five_completions",
            progress = 25,
            isUnlocked = true
        )
    }

    @Test
    fun `observer — distinctUntilChanged suppresses duplicate emissions`() = runTest {
        buildEngine()
        statsFlow.value = StatisticsEntity(currentStreak = 7)
        advanceUntilIdle()
        clearInvocations(achievementRepository)
        statsFlow.value = StatisticsEntity(currentStreak = 7)
        advanceUntilIdle()

        verify(achievementRepository, never()).updateProgress("seven_day_streak", 7, true)
    }

    @Test
    fun `observer — unlock logs timeline event once`() = runTest {
        buildEngine()
        statsFlow.value = StatisticsEntity(currentStreak = 6)
        advanceUntilIdle()
        clearInvocations(activityTimelineEngine)
        statsFlow.value = StatisticsEntity(currentStreak = 7)
        advanceUntilIdle()

        verify(activityTimelineEngine, atLeastOnce()).logAchievementUnlocked("7 Day Streak")
    }

    @Test
    fun `observer — already unlocked does not log timeline event again`() = runTest {
        buildEngine()
        statsFlow.value = StatisticsEntity(currentStreak = 7)
        advanceUntilIdle()
        clearInvocations(activityTimelineEngine)
        statsFlow.value = StatisticsEntity(currentStreak = 8)
        advanceUntilIdle()

        verify(activityTimelineEngine, never()).logAchievementUnlocked("7 Day Streak")
    }
}
