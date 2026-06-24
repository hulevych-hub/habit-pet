package com.example.mobile.presentation.viewmodel

import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.domain.StreakEngine
import com.example.mobile.domain.repository.HabitRepository
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@kotlin.OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HabitEditViewModelTest {

    private lateinit var habitRepository: HabitRepository
    private lateinit var streakEngine: StreakEngine
    private lateinit var viewModel: HabitEditViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testHabit = HabitEntity(
        id = 1,
        name = "Morning jog",
        icon = "run",
        type = "CHECKBOX",
        currentStreak = 3,
        bestStreak = 5
    )

    private val habitFlow = MutableStateFlow(testHabit)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        habitRepository = mock()
        streakEngine = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialize — loads habit data`() = runTest {
        whenever(habitRepository.getHabitById(1L)).thenReturn(habitFlow)

        viewModel = HabitEditViewModel(habitRepository, streakEngine)
        viewModel.initialize(1L)
        advanceUntilIdle()

        assertEquals("Morning jog", viewModel.name.value)
        assertEquals("run", viewModel.icon.value)
        assertEquals("CHECKBOX", viewModel.type.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `initialize — handles null habit`() = runTest {
        whenever(habitRepository.getHabitById(99L)).thenReturn(flowOf(null))

        viewModel = HabitEditViewModel(habitRepository, streakEngine)
        viewModel.initialize(99L)
        advanceUntilIdle()

        assertEquals("", viewModel.name.value)
        assertEquals("", viewModel.icon.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `onNameChanged — updates name`() = runTest {
        viewModel = HabitEditViewModel(habitRepository, streakEngine)

        viewModel.onNameChanged("Evening walk")

        assertEquals("Evening walk", viewModel.name.value)
    }

    @Test
    fun `onTypeSelected — switching to CHECKBOX resets duration`() = runTest {
        viewModel = HabitEditViewModel(habitRepository, streakEngine)

        viewModel.onTypeSelected("TIMER")
        viewModel.onMinimumDurationChanged(10)
        assertEquals(10, viewModel.minimumDuration.value)

        viewModel.onTypeSelected("CHECKBOX")
        assertEquals(0, viewModel.minimumDuration.value)
    }

    @Test
    fun `updateHabit — blank name shows error`() = runTest {
        whenever(habitRepository.getHabitById(1L)).thenReturn(habitFlow)
        doReturn(1).whenever(habitRepository).updateHabit(any())

        viewModel = HabitEditViewModel(habitRepository, streakEngine)
        viewModel.initialize(1L)
        advanceUntilIdle()

        viewModel.onNameChanged("   ")
        viewModel.updateHabit()
        advanceUntilIdle()

        assertEquals("Habit name is required", viewModel.error.value)
    }

    @Test
    fun `updateHabit — blank icon shows error`() = runTest {
        whenever(habitRepository.getHabitById(1L)).thenReturn(habitFlow)
        doReturn(1).whenever(habitRepository).updateHabit(any())

        viewModel = HabitEditViewModel(habitRepository, streakEngine)
        viewModel.initialize(1L)
        advanceUntilIdle()

        viewModel.onIconSelected("")
        viewModel.updateHabit()
        advanceUntilIdle()

        assertEquals("Please select an icon", viewModel.error.value)
    }

    @Test
    fun `updateHabit — TIMER with zero duration shows error`() = runTest {
        whenever(habitRepository.getHabitById(1L)).thenReturn(habitFlow)
        doReturn(1).whenever(habitRepository).updateHabit(any())

        viewModel = HabitEditViewModel(habitRepository, streakEngine)
        viewModel.initialize(1L)
        advanceUntilIdle()

        viewModel.onTypeSelected("TIMER")
        viewModel.onMinimumDurationChanged(0)
        viewModel.updateHabit()
        advanceUntilIdle()

        assertEquals("Minimum duration must be greater than 0 for timer habits", viewModel.error.value)
    }

    @Test
    fun `updateHabit — no habit loaded is no-op`() = runTest {
        viewModel = HabitEditViewModel(habitRepository, streakEngine)
        // Don't initialize

        viewModel.updateHabit()
        advanceUntilIdle()

        verify(habitRepository, never()).updateHabit(any())
    }

    @Test
    fun `updateHabit — success calls repository`() = runTest {
        whenever(habitRepository.getHabitById(1L)).thenReturn(habitFlow)
        doReturn(1).whenever(habitRepository).updateHabit(any())

        viewModel = HabitEditViewModel(habitRepository, streakEngine)
        viewModel.initialize(1L)
        advanceUntilIdle()

        viewModel.updateHabit()
        advanceUntilIdle()

        verify(habitRepository).updateHabit(any())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `deleteHabit — no habit loaded is no-op`() = runTest {
        viewModel = HabitEditViewModel(habitRepository, streakEngine)

        viewModel.deleteHabit()
        advanceUntilIdle()

        verify(habitRepository, never()).deleteHabit(any())
    }

    @Test
    fun `deleteHabit — success calls repository and streak recalculation`() = runTest {
        whenever(habitRepository.getHabitById(1L)).thenReturn(habitFlow)
        doReturn(1).whenever(habitRepository).deleteHabit(any())

        viewModel = HabitEditViewModel(habitRepository, streakEngine)
        viewModel.initialize(1L)
        advanceUntilIdle()

        viewModel.deleteHabit()
        advanceUntilIdle()

        verify(habitRepository).deleteHabit(any())
        verify(streakEngine).recalculateTodayStreak(any())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `resetForm — clears all fields`() = runTest {
        viewModel = HabitEditViewModel(habitRepository, streakEngine)
        viewModel.onNameChanged("Test")
        viewModel.onIconSelected("star")
        viewModel.onTypeSelected("TIMER")
        viewModel.onMinimumDurationChanged(10)

        viewModel.resetForm()

        assertEquals("", viewModel.name.value)
        assertEquals("", viewModel.icon.value)
        assertEquals("CHECKBOX", viewModel.type.value)
        assertEquals(0, viewModel.minimumDuration.value)
        assertNull(viewModel.error.value)
    }
}
