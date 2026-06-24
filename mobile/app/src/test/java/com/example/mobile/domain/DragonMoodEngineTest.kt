package com.example.mobile.domain

import com.example.mobile.data.local.dao.RecentCompletionsStats
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DragonMoodEngineTest {

    private lateinit var dragonMoodEngine: DragonMoodEngine
    private lateinit var petRepository: PetRepository
    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var habitCompletionRepository: HabitCompletionRepository

    private val statsFlow = MutableStateFlow(StatisticsEntity(id = 1, currentStreak = 0))
    private val petFlow = MutableStateFlow(PetEntity(id = 1, mood = DragonMood.CALM.value))

    @Before
    fun setup() = runBlocking {
        petRepository = mock()
        statisticsRepository = mock()
        habitCompletionRepository = mock()

        whenever(petRepository.getPet()).thenReturn(petFlow)
        whenever(statisticsRepository.getStatistics()).thenReturn(statsFlow)
        doReturn(RecentCompletionsStats(count = 0, lastActivityTimestamp = 0L))
            .whenever(habitCompletionRepository)
            .getRecentCompletionsStats(any(), any())

        dragonMoodEngine = DragonMoodEngine(
            petRepository = petRepository,
            statisticsRepository = statisticsRepository,
            habitCompletionRepository = habitCompletionRepository
        )
    }

    @Test
    fun `refreshMood — updates pet mood when mood changes`() = runTest {
        statsFlow.value = StatisticsEntity(id = 1, currentStreak = 3)
        petFlow.value = PetEntity(id = 1, mood = DragonMood.CALM.value)

        dragonMoodEngine.refreshMood()

        verify(petRepository).updatePet(any())
        val captor = argumentCaptor<PetEntity>()
        verify(petRepository).updatePet(captor.capture())
        assertEquals(DragonMood.HAPPY.value, captor.firstValue.mood)
    }

    @Test
    fun `refreshMood — does not update when mood unchanged`() = runTest {
        statsFlow.value = StatisticsEntity(id = 1, currentStreak = 0)
        petFlow.value = PetEntity(id = 1, mood = DragonMood.CALM.value)

        dragonMoodEngine.refreshMood()

        verify(petRepository, never()).updatePet(any())
    }

    @Test
    fun `refreshMood — reads current streak from statistics`() = runTest {
        statsFlow.value = StatisticsEntity(id = 1, currentStreak = 10)
        petFlow.value = PetEntity(id = 1, mood = DragonMood.CALM.value)

        dragonMoodEngine.refreshMood()

        verify(statisticsRepository).getStatistics()
        val captor = argumentCaptor<PetEntity>()
        verify(petRepository).updatePet(captor.capture())
        assertEquals(DragonMood.PROUD.value, captor.firstValue.mood)
    }

    @Test
    fun `refreshMood — reads recent completions within 72h window`() = runTest {
        val now = System.currentTimeMillis()
        statsFlow.value = StatisticsEntity(id = 1, currentStreak = 0)
        petFlow.value = PetEntity(id = 1, mood = DragonMood.CALM.value)
        runBlocking {
            doReturn(RecentCompletionsStats(count = 5, lastActivityTimestamp = now))
                .whenever(habitCompletionRepository)
                .getRecentCompletionsStats(any(), any())
        }

        dragonMoodEngine.refreshMood()

        verify(habitCompletionRepository).getRecentCompletionsStats(any(), any())
        val captor = argumentCaptor<PetEntity>()
        verify(petRepository).updatePet(captor.capture())
        assertEquals(DragonMood.EXCITED.value, captor.firstValue.mood)
    }

    @Test
    fun `refreshMood — creates default pet if none exists`() = runTest {
        statsFlow.value = StatisticsEntity(id = 1, currentStreak = 3)
        runBlocking {
            doReturn(flowOf(null)).whenever(petRepository).getPet()
        }

        dragonMoodEngine.refreshMood()

        val captor = argumentCaptor<PetEntity>()
        verify(petRepository).updatePet(captor.capture())
        assertEquals(1L, captor.firstValue.id)
        assertEquals(DragonMood.HAPPY.value, captor.firstValue.mood)
    }

    @Test
    fun `refreshMood — LONELY when no activity for 36+ hours`() = runTest {
        val now = System.currentTimeMillis()
        val fortyHoursAgo = now - (40L * 60L * 60L * 1000L)
        statsFlow.value = StatisticsEntity(id = 1, currentStreak = 0)
        petFlow.value = PetEntity(id = 1, mood = DragonMood.CALM.value)
        runBlocking {
            doReturn(RecentCompletionsStats(count = 0, lastActivityTimestamp = fortyHoursAgo))
                .whenever(habitCompletionRepository)
                .getRecentCompletionsStats(any(), any())
        }

        dragonMoodEngine.refreshMood()

        val captor = argumentCaptor<PetEntity>()
        verify(petRepository).updatePet(captor.capture())
        assertEquals(DragonMood.LONELY.value, captor.firstValue.mood)
    }

    @Test
    fun `refreshMood — PROUD takes priority over EXCITED`() = runTest {
        val now = System.currentTimeMillis()
        statsFlow.value = StatisticsEntity(id = 1, currentStreak = 8)
        petFlow.value = PetEntity(id = 1, mood = DragonMood.CALM.value)
        runBlocking {
            doReturn(RecentCompletionsStats(count = 5, lastActivityTimestamp = now))
                .whenever(habitCompletionRepository)
                .getRecentCompletionsStats(any(), any())
        }

        dragonMoodEngine.refreshMood()

        val captor = argumentCaptor<PetEntity>()
        verify(petRepository).updatePet(captor.capture())
        assertEquals(DragonMood.PROUD.value, captor.firstValue.mood)
    }
}
