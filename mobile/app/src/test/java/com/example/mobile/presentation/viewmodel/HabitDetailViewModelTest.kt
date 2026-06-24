package com.example.mobile.presentation.viewmodel

import com.example.mobile.data.local.entities.HabitCompletionEntity
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.HabitProgressEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.ActivityTimelineEngine
import com.example.mobile.domain.ChallengeEngine
import com.example.mobile.domain.DragonMoodEngine
import com.example.mobile.domain.StreakEngine
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitCompletionResult
import com.example.mobile.domain.repository.HabitProgressRepository
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@kotlin.OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HabitDetailViewModelTest {

    private lateinit var habitRepository: HabitRepository
    private lateinit var habitCompletionRepository: HabitCompletionRepository
    private lateinit var petRepository: PetRepository
    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var habitProgressRepository: HabitProgressRepository
    private lateinit var streakEngine: StreakEngine
    private lateinit var rewardQueue: RewardQueue
    private lateinit var inventoryItemRepository: InventoryItemRepository
    private lateinit var activityTimelineEngine: ActivityTimelineEngine
    private lateinit var microFeedbackManager: MicroFeedbackManager
    private lateinit var dragonMoodEngine: DragonMoodEngine
    private lateinit var challengeEngine: ChallengeEngine
    private lateinit var viewModel: HabitDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val petFlow = MutableStateFlow(PetEntity(id = 1))
    private val habitFlow = MutableStateFlow<HabitEntity?>(null)
    private val statsFlow = MutableStateFlow(com.example.mobile.data.local.entities.StatisticsEntity())

    private val testHabit = HabitEntity(
        id = 1L,
        name = "Morning jog",
        icon = "run",
        type = "CHECKBOX",
        currentStreak = 3,
        bestStreak = 5,
        minimumDurationMinutes = 0
    )

    private val timerHabit = HabitEntity(
        id = 2L,
        name = "Study",
        icon = "book",
        type = "TIMER",
        currentStreak = 0,
        bestStreak = 0,
        minimumDurationMinutes = 30
    )

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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        habitRepository = mock()
        habitCompletionRepository = mock()
        petRepository = mock()
        statisticsRepository = mock()
        habitProgressRepository = mock()
        streakEngine = mock()
        rewardQueue = mock()
        inventoryItemRepository = mock()
        activityTimelineEngine = mock()
        microFeedbackManager = mock()
        dragonMoodEngine = mock()
        challengeEngine = mock()

        whenever(petRepository.getPet()).thenReturn(petFlow)
        whenever(statisticsRepository.getStatistics()).thenReturn(statsFlow)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialize — loads habit and completions`() = runTest {
        habitFlow.value = testHabit
        whenever(habitRepository.getHabitById(1L)).thenReturn(habitFlow)
        whenever(habitCompletionRepository.getCompletionsForHabit(any(), any(), any()))
            .thenReturn(flowOf(listOf(HabitCompletionEntity(id = 1, habitId = 1, date = 0, xpEarned = 10))))

        viewModel = buildViewModel()
        viewModel.initialize(1L)
        advanceUntilIdle()

        assertEquals(testHabit, viewModel.habit.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `initialize — handles null habit`() = runTest {
        whenever(habitRepository.getHabitById(99L)).thenReturn(flowOf(null))
        whenever(habitCompletionRepository.getCompletionsForHabit(any(), any(), any()))
            .thenReturn(flowOf(emptyList()))

        viewModel = buildViewModel()
        viewModel.initialize(99L)
        advanceUntilIdle()

        assertNull(viewModel.habit.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `completeCheckboxHabit — already completed is no-op`() = runTest {
        habitFlow.value = testHabit
        whenever(habitRepository.getHabitById(1L)).thenReturn(habitFlow)
        whenever(habitCompletionRepository.getCompletionsForHabit(any(), any(), any()))
            .thenReturn(flowOf(emptyList()))
        whenever(habitCompletionRepository.getCompletionForHabitOnDate(any(), any()))
            .thenReturn(flowOf(HabitCompletionEntity(id = 1, habitId = 1, date = 0, xpEarned = 10)))

        viewModel = buildViewModel()
        viewModel.initialize(1L)
        advanceUntilIdle()

        viewModel.completeCheckboxHabit(1L)
        advanceUntilIdle()

        verify(habitCompletionRepository, never()).addCompletionWithCombo(any())
    }

    @Test
    fun `completeCheckboxHabit — success calls repository and awards`() = runTest {
        habitFlow.value = testHabit
        whenever(habitRepository.getHabitById(1L)).thenReturn(habitFlow)
        whenever(habitCompletionRepository.getCompletionsForHabit(any(), any(), any()))
            .thenReturn(flowOf(emptyList()))
        whenever(habitCompletionRepository.getCompletionForHabitOnDate(any(), any()))
            .thenReturn(flowOf(null))
        whenever(habitCompletionRepository.addCompletionWithCombo(any()))
            .thenReturn(completionResult)
        whenever(petRepository.getPet()).thenReturn(MutableStateFlow(PetEntity(id = 1, xp = 0, level = 0, evolutionStage = 0)))

        viewModel = buildViewModel()
        viewModel.initialize(1L)
        advanceUntilIdle()

        viewModel.completeCheckboxHabit(1L)
        advanceUntilIdle()

        verify(habitCompletionRepository).addCompletionWithCombo(any())
        verify(streakEngine).evaluateTodayStreak(any())
    }

    @Test
    fun `completeCheckboxHabit — insert failure sets error`() = runTest {
        habitFlow.value = testHabit
        whenever(habitRepository.getHabitById(1L)).thenReturn(habitFlow)
        whenever(habitCompletionRepository.getCompletionsForHabit(any(), any(), any()))
            .thenReturn(flowOf(emptyList()))
        whenever(habitCompletionRepository.getCompletionForHabitOnDate(any(), any()))
            .thenReturn(flowOf(null))
        val failureResult = completionResult.copy(completionId = -1L, isNewCompletion = false)
        whenever(habitCompletionRepository.addCompletionWithCombo(any()))
            .thenReturn(failureResult)

        viewModel = buildViewModel()
        viewModel.initialize(1L)
        advanceUntilIdle()

        viewModel.completeCheckboxHabit(1L)
        advanceUntilIdle()

        assertEquals("Habit completion could not be saved", viewModel.error.value)
    }

    @Test
    fun `completeCheckboxHabit — duplicate sets error`() = runTest {
        habitFlow.value = testHabit
        whenever(habitRepository.getHabitById(1L)).thenReturn(habitFlow)
        whenever(habitCompletionRepository.getCompletionsForHabit(any(), any(), any()))
            .thenReturn(flowOf(emptyList()))
        whenever(habitCompletionRepository.getCompletionForHabitOnDate(any(), any()))
            .thenReturn(flowOf(null))
        val duplicateResult = completionResult.copy(isNewCompletion = false)
        whenever(habitCompletionRepository.addCompletionWithCombo(any()))
            .thenReturn(duplicateResult)

        viewModel = buildViewModel()
        viewModel.initialize(1L)
        advanceUntilIdle()

        viewModel.completeCheckboxHabit(1L)
        advanceUntilIdle()

        assertEquals("Already completed today", viewModel.error.value)
    }

    @Test
    fun `startTimerHabit — sets running state`() = runTest {
        habitFlow.value = timerHabit
        whenever(habitRepository.getHabitById(2L)).thenReturn(habitFlow)
        whenever(habitCompletionRepository.getCompletionsForHabit(any(), any(), any()))
            .thenReturn(flowOf(emptyList()))
        whenever(habitCompletionRepository.getCompletionForHabitOnDate(any(), any()))
            .thenReturn(flowOf(null))

        viewModel = buildViewModel()
        viewModel.initialize(2L)
        advanceUntilIdle()

        viewModel.startTimerHabit(2L)
        advanceUntilIdle()

        assertEquals(true, viewModel.isTimerRunning.value)
    }

    @Test
    fun `stopTimerHabit — below minimum shows message`() = runTest {
        habitFlow.value = timerHabit
        whenever(habitRepository.getHabitById(2L)).thenReturn(habitFlow)
        whenever(habitCompletionRepository.getCompletionsForHabit(any(), any(), any()))
            .thenReturn(flowOf(emptyList()))
        whenever(habitCompletionRepository.getCompletionForHabitOnDate(any(), any()))
            .thenReturn(flowOf(null))
        whenever(habitProgressRepository.getProgress(any(), any()))
            .thenReturn(flowOf(HabitProgressEntity(habitId = 2, date = 0, accumulatedMinutes = 0, lastUpdated = 0)))

        viewModel = buildViewModel()
        viewModel.initialize(2L)
        advanceUntilIdle()

        // Simulate timer running for 10 seconds (0 minutes)
        viewModel.stopTimerHabit(2L)
        advanceUntilIdle()

        assertNotNull(viewModel.message.value)
        assertEquals(false, viewModel.isTimerRunning.value)
    }

    @Test
    fun `resetTimer — clears timer state`() = runTest {
        habitFlow.value = timerHabit
        whenever(habitRepository.getHabitById(2L)).thenReturn(habitFlow)
        whenever(habitCompletionRepository.getCompletionsForHabit(any(), any(), any()))
            .thenReturn(flowOf(emptyList()))
        whenever(habitCompletionRepository.getCompletionForHabitOnDate(any(), any()))
            .thenReturn(flowOf(null))

        viewModel = buildViewModel()
        viewModel.initialize(2L)
        advanceUntilIdle()

        viewModel.startTimerHabit(2L)
        advanceUntilIdle()
        viewModel.resetTimer()

        assertEquals(false, viewModel.isTimerRunning.value)
        assertEquals(0, viewModel.elapsedSeconds.value)
    }

    private fun buildViewModel(): HabitDetailViewModel {
        return HabitDetailViewModel(
            habitRepository = habitRepository,
            habitCompletionRepository = habitCompletionRepository,
            petRepository = petRepository,
            statisticsRepository = statisticsRepository,
            habitProgressRepository = habitProgressRepository,
            streakEngine = streakEngine,
            rewardQueue = rewardQueue,
            inventoryItemRepository = inventoryItemRepository,
            activityTimelineEngine = activityTimelineEngine,
            microFeedbackManager = microFeedbackManager,
            dragonMoodEngine = dragonMoodEngine,
            challengeEngine = challengeEngine
        )
    }
}
