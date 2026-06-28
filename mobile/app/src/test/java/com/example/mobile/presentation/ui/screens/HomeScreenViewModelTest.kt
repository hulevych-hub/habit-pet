package com.example.mobile.presentation.ui.screens

import com.example.mobile.domain.AvatarFrameConfig
import com.example.mobile.domain.ChallengeEngine
import com.example.mobile.domain.DailyLoginStreakEngine
import com.example.mobile.domain.StreakEngine
import com.example.mobile.domain.repository.AchievementRepository
import com.example.mobile.domain.repository.ChallengeUiState
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@kotlin.OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HomeScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var habitRepository: HabitRepository
    private lateinit var petRepository: PetRepository
    private lateinit var habitCompletionRepository: HabitCompletionRepository
    private lateinit var achievementRepository: AchievementRepository
    private lateinit var challengeEngine: ChallengeEngine
    private lateinit var streakEngine: StreakEngine
    private lateinit var dailyLoginStreakEngine: DailyLoginStreakEngine
    private lateinit var inventoryItemRepository: InventoryItemRepository
    private lateinit var viewModel: HomeScreenViewModel

    private val statsFlow = MutableStateFlow(com.example.mobile.data.local.entities.StatisticsEntity())
    private val petFlow = MutableStateFlow(com.example.mobile.data.local.entities.PetEntity(id = 1))
    private val challengeStateFlow = MutableStateFlow(ChallengeUiState.empty())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        statisticsRepository = mock()
        habitRepository = mock()
        petRepository = mock()
        habitCompletionRepository = mock()
        achievementRepository = mock()
        challengeEngine = mock()
        streakEngine = mock()
        dailyLoginStreakEngine = mock()
        inventoryItemRepository = mock()

        whenever(statisticsRepository.getStatistics()).thenReturn(statsFlow)
        whenever(habitRepository.getAllHabits()).thenReturn(flowOf(emptyList()))
        whenever(petRepository.getPet()).thenReturn(petFlow)
        whenever(challengeEngine.getActiveChallengeUiState()).thenReturn(challengeStateFlow)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init — globalStreakCompletedToday is false by default`() = runTest {
        viewModel = HomeScreenViewModel(
            statisticsRepository, habitRepository, petRepository,
            habitCompletionRepository, achievementRepository,
            challengeEngine, streakEngine, dailyLoginStreakEngine, inventoryItemRepository
        )

        val job = launch {
            viewModel.uiState.collect { }
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        job.cancel()

        assertEquals(0, uiState.globalStreak)
    }

    @Test
    fun `resetAllGameData — calls all reset methods`() = runTest {
        viewModel = HomeScreenViewModel(
            statisticsRepository, habitRepository, petRepository,
            habitCompletionRepository, achievementRepository,
            challengeEngine, streakEngine, dailyLoginStreakEngine, inventoryItemRepository
        )

        viewModel.resetAllGameData()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(habitRepository).deleteAll()
        verify(habitCompletionRepository).deleteAll()
        verify(petRepository).resetPet()
        verify(statisticsRepository).reset()
        verify(achievementRepository).reset()
        verify(challengeEngine).reset()
    }

    @Test
    fun `claimChallenge — delegates to engine`() = runTest {
        viewModel = HomeScreenViewModel(
            statisticsRepository, habitRepository, petRepository,
            habitCompletionRepository, achievementRepository,
            challengeEngine, streakEngine, dailyLoginStreakEngine, inventoryItemRepository
        )

        viewModel.claimChallenge()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(challengeEngine).claimActiveChallenge()
    }

    @Test
    fun `renamePet — updates pet name`() = runTest {
        whenever(petRepository.updatePet(any())).thenReturn(1)

        viewModel = HomeScreenViewModel(
            statisticsRepository, habitRepository, petRepository,
            habitCompletionRepository, achievementRepository,
            challengeEngine, streakEngine, dailyLoginStreakEngine, inventoryItemRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.renamePet("Dragon")
        testDispatcher.scheduler.advanceUntilIdle()

        verify(petRepository).updatePet(any())
    }

    @Test
    fun `equipFrame — updates pet equippedFrame`() = runTest {
        whenever(petRepository.updatePet(any())).thenReturn(1)

        viewModel = HomeScreenViewModel(
            statisticsRepository, habitRepository, petRepository,
            habitCompletionRepository, achievementRepository,
            challengeEngine, streakEngine, dailyLoginStreakEngine, inventoryItemRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.equipFrame(AvatarFrameConfig.GOLDEN)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(petRepository).updatePet(any())
    }

    @Test
    fun `usePendingStreakFreeze — calls engine and clears prompt on success`() = runTest {
        whenever(streakEngine.useStreakFreeze(any())).thenReturn(true)

        viewModel = HomeScreenViewModel(
            statisticsRepository, habitRepository, petRepository,
            habitCompletionRepository, achievementRepository,
            challengeEngine, streakEngine, dailyLoginStreakEngine, inventoryItemRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.usePendingStreakFreeze()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(streakEngine).useStreakFreeze(any())
        assertNull("Streak prompt should be null after use", viewModel.streakFreezePrompt.value)
    }

    @Test
    fun `dismissStreakFreezePrompt — resets broken streak and clears prompt`() = runTest {
        viewModel = HomeScreenViewModel(
            statisticsRepository, habitRepository, petRepository,
            habitCompletionRepository, achievementRepository,
            challengeEngine, streakEngine, dailyLoginStreakEngine, inventoryItemRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.dismissStreakFreezePrompt()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(streakEngine).resetBrokenStreak(any())
        assertNull("Streak prompt should be null after dismiss", viewModel.streakFreezePrompt.value)
    }

    @Test
    fun `checkPendingStreakFreeze — updates prompt from engine`() = runTest {
        val prompt: StreakEngine.StreakFreezePrompt? = null
        whenever(streakEngine.checkPendingStreakFreeze(any())).thenReturn(prompt)

        viewModel = HomeScreenViewModel(
            statisticsRepository, habitRepository, petRepository,
            habitCompletionRepository, achievementRepository,
            challengeEngine, streakEngine, dailyLoginStreakEngine, inventoryItemRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.checkPendingStreakFreeze()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(streakEngine, atLeastOnce()).checkPendingStreakFreeze(any())
        assertNull("Prompt should match engine result", viewModel.streakFreezePrompt.value)
    }
}
