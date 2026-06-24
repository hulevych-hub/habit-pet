package com.example.mobile.presentation.viewmodel

import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@kotlin.OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RewardsViewModelTest {

    private lateinit var inventoryItemRepository: InventoryItemRepository
    private lateinit var petRepository: PetRepository
    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var viewModel: RewardsViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val statsFlow = MutableStateFlow<StatisticsEntity>(StatisticsEntity())
    private val petFlow = MutableStateFlow<PetEntity>(PetEntity(id = 1))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        inventoryItemRepository = mock()
        petRepository = mock()
        statisticsRepository = mock()

        whenever(statisticsRepository.getStatistics()).thenReturn(statsFlow)
        whenever(petRepository.getPet()).thenReturn(petFlow)
        whenever(inventoryItemRepository.getItemsByType(any())).thenReturn(flowOf(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init — uiState has default values before data loads`() = runTest {
        statsFlow.value = StatisticsEntity()
        petFlow.value = PetEntity(id = 1)

        viewModel = RewardsViewModel(inventoryItemRepository, petRepository, statisticsRepository)
        advanceUntilIdle()

        // Default state should have zeros
        assertEquals(0, viewModel.uiState.value.totalCoins)
        assertEquals(0, viewModel.uiState.value.globalStreak)
    }

    @Test
    fun `init — uiState populates from statistics and pet`() = runTest {
        statsFlow.value = StatisticsEntity(id = 1, totalCoins = 500, currentStreak = 7)
        petFlow.value = PetEntity(id = 1, name = "Luna", level = 5, xp = 300)

        viewModel = RewardsViewModel(inventoryItemRepository, petRepository, statisticsRepository)
        // stateIn with WhileSubscribed needs a collector to activate
        // Use backgroundScope-like pattern: collect in a child coroutine
        val collected = mutableListOf<RewardsViewModel.UiState>()
        val job = launch { viewModel.uiState.collect { collected.add(it) } }
        advanceUntilIdle()

        assertTrue("Should have collected at least one state", collected.isNotEmpty())
        assertEquals(500, collected.last().totalCoins)
        assertEquals(7, collected.last().globalStreak)
        assertEquals("Luna", collected.last().pet.name)
        job.cancel()
    }

    @Test
    fun `init — loading becomes false after data loads`() = runTest {
        statsFlow.value = StatisticsEntity(id = 1)
        petFlow.value = PetEntity(id = 1)

        viewModel = RewardsViewModel(inventoryItemRepository, petRepository, statisticsRepository)
        advanceUntilIdle()

        assertFalse("Loading should be false after init", viewModel.isLoading.value)
    }

    @Test
    fun `clearError — clears error state`() = runTest {
        statsFlow.value = StatisticsEntity(id = 1)
        petFlow.value = PetEntity(id = 1)

        viewModel = RewardsViewModel(inventoryItemRepository, petRepository, statisticsRepository)
        advanceUntilIdle()

        viewModel.clearError()

        assertNull(viewModel.error.value)
    }

    @Test
    fun `purchaseItem — delegates to repository`() = runTest {
        statsFlow.value = StatisticsEntity(id = 1)
        petFlow.value = PetEntity(id = 1)
        doReturn(1).whenever(inventoryItemRepository).purchaseItem(5L)

        viewModel = RewardsViewModel(inventoryItemRepository, petRepository, statisticsRepository)
        advanceUntilIdle()

        val result = viewModel.purchaseItem(5L)

        verify(inventoryItemRepository).purchaseItem(5L)
        assertEquals(1, result)
    }

    @Test
    fun `purchaseItem — exception returns -1 and sets error`() = runTest {
        statsFlow.value = StatisticsEntity(id = 1)
        petFlow.value = PetEntity(id = 1)
        doAnswer { throw RuntimeException("Insufficient coins") }.whenever(inventoryItemRepository).purchaseItem(5L)

        viewModel = RewardsViewModel(inventoryItemRepository, petRepository, statisticsRepository)
        advanceUntilIdle()

        val result = viewModel.purchaseItem(5L)

        assertEquals(-1, result)
        assertNotNull(viewModel.error.value)
        assertEquals("Insufficient coins", viewModel.error.value)
    }

    @Test
    fun `equipItem — delegates to pet repository`() = runTest {
        statsFlow.value = StatisticsEntity(id = 1)
        petFlow.value = PetEntity(id = 1)
        doReturn(1).whenever(petRepository).equipItem("OUTFIT", "cape")

        viewModel = RewardsViewModel(inventoryItemRepository, petRepository, statisticsRepository)
        advanceUntilIdle()

        val result = viewModel.equipItem("OUTFIT", "cape")

        verify(petRepository).equipItem("OUTFIT", "cape")
        assertEquals(1, result)
    }

    @Test
    fun `equipItem — exception returns -1 and sets error`() = runTest {
        statsFlow.value = StatisticsEntity(id = 1)
        petFlow.value = PetEntity(id = 1)
        doAnswer { throw RuntimeException("Item not owned") }.whenever(petRepository).equipItem(any(), any())

        viewModel = RewardsViewModel(inventoryItemRepository, petRepository, statisticsRepository)
        advanceUntilIdle()

        val result = viewModel.equipItem("OUTFIT", "cape")

        assertEquals(-1, result)
        assertNotNull(viewModel.error.value)
    }

    @Test
    fun `unequipItem — delegates to pet repository`() = runTest {
        statsFlow.value = StatisticsEntity(id = 1)
        petFlow.value = PetEntity(id = 1)
        doReturn(1).whenever(petRepository).unequipItem("OUTFIT")

        viewModel = RewardsViewModel(inventoryItemRepository, petRepository, statisticsRepository)
        advanceUntilIdle()

        val result = viewModel.unequipItem("OUTFIT")

        verify(petRepository).unequipItem("OUTFIT")
        assertEquals(1, result)
    }

    @Test
    fun `unequipItem — exception returns -1 and sets error`() = runTest {
        statsFlow.value = StatisticsEntity(id = 1)
        petFlow.value = PetEntity(id = 1)
        doAnswer { throw RuntimeException("Nothing equipped") }.whenever(petRepository).unequipItem(any())

        viewModel = RewardsViewModel(inventoryItemRepository, petRepository, statisticsRepository)
        advanceUntilIdle()

        val result = viewModel.unequipItem("OUTFIT")

        assertEquals(-1, result)
        assertNotNull(viewModel.error.value)
    }
}
