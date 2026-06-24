package com.example.mobile.presentation.viewmodel

import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.ActivityTimelineEngine
import com.example.mobile.domain.ChallengeEngine
import com.example.mobile.domain.DragonMoodEngine
import com.example.mobile.domain.StreakEngine
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitCompletionResult
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.feedback.MicroFeedbackManager
import com.example.mobile.presentation.ui.reward.RewardQueue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@kotlin.OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HabitsViewModelTest {

    private lateinit var habitRepository: HabitRepository
    private lateinit var habitCompletionRepository: HabitCompletionRepository
    private lateinit var streakEngine: StreakEngine
    private lateinit var petRepository: PetRepository
    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var rewardQueue: RewardQueue
    private lateinit var inventoryItemRepository: InventoryItemRepository
    private lateinit var activityTimelineEngine: ActivityTimelineEngine
    private lateinit var microFeedbackManager: MicroFeedbackManager
    private lateinit var dragonMoodEngine: DragonMoodEngine
    private lateinit var challengeEngine: ChallengeEngine
    private lateinit var viewModel: HabitsViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val petFlow = MutableStateFlow(PetEntity(id = 1))
    private val completionResult = HabitCompletionResult(
        completionId = 1L,
        baseXpEarned = 10L,
        comboBonusXp = 0L,
        totalXpEarned = 10L,
        combo = 1,
        comboMultiplier = 1.0f,
        comboMilestoneReached = false,
        isNewCompletion = true
    )

    private val testHabit = HabitEntity(
        id = 1L,
        name = "Morning jog",
        icon = "run",
        type = "CHECKBOX",
        currentStreak = 3,
        bestStreak = 5
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        habitRepository = mock()
        habitCompletionRepository = mock()
        streakEngine = mock()
        petRepository = mock()
        statisticsRepository = mock()
        rewardQueue = mock()
        inventoryItemRepository = mock()
        activityTimelineEngine = mock()
        microFeedbackManager = mock()
        dragonMoodEngine = mock()
        challengeEngine = mock()

        whenever(habitRepository.getAllHabits()).thenReturn(flowOf(listOf(testHabit)))
        whenever(petRepository.getPet()).thenReturn(petFlow)
        whenever(statisticsRepository.getStatistics()).thenReturn(
            MutableStateFlow(com.example.mobile.data.local.entities.StatisticsEntity())
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `completeCheckboxHabit — non-checkbox type is no-op`() = runTest {
        val timerHabit = testHabit.copy(type = "TIMER")
        whenever(habitRepository.getAllHabits()).thenReturn(flowOf(listOf(timerHabit)))

        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.completeCheckboxHabit(timerHabit)
        advanceUntilIdle()

        verify(habitCompletionRepository, never()).addCompletionWithCombo(any())
    }

    @Test
    fun `completeCheckboxHabit — duplicate completion is no-op`() = runTest {
        whenever(habitCompletionRepository.getCompletionForHabitOnDate(any(), any()))
            .thenReturn(flowOf(HabitCompletionEntity(id = 1, habitId = 1, date = 0, xpEarned = 10)))

        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.completeCheckboxHabit(testHabit)
        advanceUntilIdle()

        verify(habitCompletionRepository, never()).addCompletionWithCombo(any())
    }

    @Test
    fun `completeCheckboxHabit — success calls repository and awards`() = runTest {
        whenever(habitCompletionRepository.getCompletionForHabitOnDate(any(), any()))
            .thenReturn(flowOf(null))
        whenever(habitCompletionRepository.addCompletionWithCombo(any()))
            .thenReturn(completionResult)
        whenever(petRepository.getPet()).thenReturn(MutableStateFlow(PetEntity(id = 1, xp = 0, level = 0, evolutionStage = 0)))

        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.completeCheckboxHabit(testHabit)
        advanceUntilIdle()

        verify(habitCompletionRepository).addCompletionWithCombo(any())
        verify(streakEngine).evaluateTodayStreak(any())
    }

    @Test
    fun `completeCheckboxHabit — inserts optimistic id then removes on failure`() = runTest {
        whenever(habitCompletionRepository.getCompletionForHabitOnDate(any(), any()))
            .thenReturn(flowOf(null))
        val failureResult = completionResult.copy(completionId = -1L, isNewCompletion = false)
        whenever(habitCompletionRepository.addCompletionWithCombo(any()))
            .thenReturn(failureResult)

        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.completeCheckboxHabit(testHabit)
        advanceUntilIdle()

        verify(habitCompletionRepository).addCompletionWithCombo(any())
        assertEquals("Habit completion could not be saved", viewModel.error.value)
    }

    @Test
    fun `completeCheckboxHabit — duplicate result removes optimistic id`() = runTest {
        whenever(habitCompletionRepository.getCompletionForHabitOnDate(any(), any()))
            .thenReturn(flowOf(null))
        val duplicateResult = completionResult.copy(isNewCompletion = false)
        whenever(habitCompletionRepository.addCompletionWithCombo(any()))
            .thenReturn(duplicateResult)

        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.completeCheckboxHabit(testHabit)
        advanceUntilIdle()

        verify(habitCompletionRepository).addCompletionWithCombo(any())
        assertEquals("Already completed today", viewModel.error.value)
    }

    @Test
    fun `deleteHabit — calls repository and recalcules streak`() = runTest {
        whenever(habitCompletionRepository.getCompletionForHabitOnDate(any(), any()))
            .thenReturn(flowOf(null))
        whenever(habitRepository.deleteHabit(any())).thenReturn(1)

        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.deleteHabit(testHabit)
        advanceUntilIdle()

        verify(habitRepository).deleteHabit(testHabit)
        verify(streakEngine).recalculateTodayStreak(any())
    }

    @Test
    fun `clearError — clears error state`() = runTest {
        whenever(habitCompletionRepository.getCompletionForHabitOnDate(any(), any()))
            .thenReturn(flowOf(null))
        val failureResult = completionResult.copy(completionId = -1L)
        whenever(habitCompletionRepository.addCompletionWithCombo(any()))
            .thenReturn(failureResult)

        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.completeCheckboxHabit(testHabit)
        advanceUntilIdle()

        viewModel.clearError()

        assertNull(viewModel.error.value)
    }

    private fun buildViewModel(): HabitsViewModel {
        return HabitsViewModel(
            habitRepository = habitRepository,
            habitCompletionRepository = habitCompletionRepository,
            streakEngine = streakEngine,
            petRepository = petRepository,
            statisticsRepository = statisticsRepository,
            rewardQueue = rewardQueue,
            inventoryItemRepository = inventoryItemRepository,
            activityTimelineEngine = activityTimelineEngine,
            microFeedbackManager = microFeedbackManager,
            dragonMoodEngine = dragonMoodEngine,
            challengeEngine = challengeEngine
        )
    }
}
