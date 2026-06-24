package com.example.mobile.presentation.viewmodel

import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.domain.repository.HabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@kotlin.OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HabitCreationViewModelTest {

    private lateinit var habitRepository: HabitRepository
    private lateinit var viewModel: HabitCreationViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        habitRepository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init — default state`() = runTest {
        viewModel = HabitCreationViewModel(habitRepository)

        assertEquals("", viewModel.name.value)
        assertEquals("🎯", viewModel.icon.value)
        assertEquals("CHECKBOX", viewModel.type.value)
        assertEquals(0, viewModel.minimumDuration.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `onNameChanged — updates name`() = runTest {
        viewModel = HabitCreationViewModel(habitRepository)

        viewModel.onNameChanged("Morning jog")

        assertEquals("Morning jog", viewModel.name.value)
    }

    @Test
    fun `onIconSelected — updates icon`() = runTest {
        viewModel = HabitCreationViewModel(habitRepository)

        viewModel.onIconSelected("run")

        assertEquals("run", viewModel.icon.value)
    }

    @Test
    fun `onTypeSelected — switches to TIMER`() = runTest {
        viewModel = HabitCreationViewModel(habitRepository)

        viewModel.onTypeSelected("TIMER")

        assertEquals("TIMER", viewModel.type.value)
    }

    @Test
    fun `onTypeSelected — switching to CHECKBOX resets duration`() = runTest {
        viewModel = HabitCreationViewModel(habitRepository)

        viewModel.onTypeSelected("TIMER")
        viewModel.onMinimumDurationChanged(10)
        assertEquals(10, viewModel.minimumDuration.value)

        viewModel.onTypeSelected("CHECKBOX")
        assertEquals(0, viewModel.minimumDuration.value)
    }

    @Test
    fun `onMinimumDurationChanged — updates duration`() = runTest {
        viewModel = HabitCreationViewModel(habitRepository)

        viewModel.onMinimumDurationChanged(15)

        assertEquals(15, viewModel.minimumDuration.value)
    }

    @Test
    fun `createHabit — blank name shows error`() = runTest {
        viewModel = HabitCreationViewModel(habitRepository)
        viewModel.onNameChanged("   ")

        viewModel.createHabit()
        advanceUntilIdle()

        assertEquals("Habit name is required", viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `createHabit — invalid type shows error`() = runTest {
        viewModel = HabitCreationViewModel(habitRepository)
        viewModel.onNameChanged("Test")
        viewModel.onTypeSelected("INVALID")

        viewModel.createHabit()
        advanceUntilIdle()

        assertEquals("Invalid habit type", viewModel.error.value)
    }

    @Test
    fun `createHabit — TIMER with zero duration shows error`() = runTest {
        viewModel = HabitCreationViewModel(habitRepository)
        viewModel.onNameChanged("Test")
        viewModel.onTypeSelected("TIMER")
        viewModel.onMinimumDurationChanged(0)

        viewModel.createHabit()
        advanceUntilIdle()

        assertEquals("Minimum duration must be greater than 0 for timer habits", viewModel.error.value)
    }

    @Test
    fun `createHabit — duplicate name shows error`() = runTest {
        val existing = HabitEntity(id = 1, name = "Morning Jog", icon = "run", type = "CHECKBOX")
        whenever(habitRepository.getAllHabits()).thenReturn(flowOf(listOf(existing)))
        doReturn(1L).whenever(habitRepository).addHabit(any())

        viewModel = HabitCreationViewModel(habitRepository)
        viewModel.onNameChanged("morning jog") // different case, same name

        viewModel.createHabit()
        advanceUntilIdle()

        assertEquals("A habit with this name already exists", viewModel.error.value)
        verify(habitRepository, never()).addHabit(any())
    }

    @Test
    fun `createHabit — success emits habit and resets form`() = runTest {
        whenever(habitRepository.getAllHabits()).thenReturn(flowOf(emptyList()))
        doReturn(5L).whenever(habitRepository).addHabit(any())

        viewModel = HabitCreationViewModel(habitRepository)
        viewModel.onNameChanged("Morning jog")
        viewModel.onIconSelected("run")
        viewModel.onTypeSelected("CHECKBOX")

        viewModel.createHabit()
        advanceUntilIdle()

        verify(habitRepository).addHabit(any())
        assertFalse(viewModel.isLoading.value)
        // Form should be reset
        assertEquals("", viewModel.name.value)
        assertEquals("🎯", viewModel.icon.value)
        assertEquals("CHECKBOX", viewModel.type.value)
    }

    @Test
    fun `createHabit — insert failure shows error`() = runTest {
        whenever(habitRepository.getAllHabits()).thenReturn(flowOf(emptyList()))
        doReturn(-1L).whenever(habitRepository).addHabit(any())

        viewModel = HabitCreationViewModel(habitRepository)
        viewModel.onNameChanged("Test habit")

        viewModel.createHabit()
        advanceUntilIdle()

        assertEquals("Habit could not be saved", viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `resetForm — clears all fields`() = runTest {
        viewModel = HabitCreationViewModel(habitRepository)
        viewModel.onNameChanged("Test")
        viewModel.onIconSelected("star")
        viewModel.onTypeSelected("TIMER")
        viewModel.onMinimumDurationChanged(10)

        viewModel.resetForm()

        assertEquals("", viewModel.name.value)
        assertEquals("🎯", viewModel.icon.value)
        assertEquals("CHECKBOX", viewModel.type.value)
        assertEquals(0, viewModel.minimumDuration.value)
        assertNull(viewModel.error.value)
    }
}
