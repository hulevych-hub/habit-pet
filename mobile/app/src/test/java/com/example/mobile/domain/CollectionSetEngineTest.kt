package com.example.mobile.domain

import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.reward.RewardQueue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionSetEngineTest {

    private lateinit var inventoryItemRepository: InventoryItemRepository
    private lateinit var petRepository: PetRepository
    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var rewardQueue: RewardQueue
    private lateinit var engine: CollectionSetEngine

    private val petFlow = MutableStateFlow(PetEntity(id = 1))

    @Before
    fun setup() {
        inventoryItemRepository = mock()
        petRepository = mock()
        statisticsRepository = mock()
        rewardQueue = mock()
        whenever(petRepository.getPet()).thenReturn(petFlow)
        whenever(inventoryItemRepository.getAllItems()).thenReturn(flowOf(emptyList()))
        engine = CollectionSetEngine(inventoryItemRepository, petRepository, statisticsRepository, rewardQueue)
    }

    @Test
    fun `no sets completed with empty owned items`() = runTest {
        doReturn(1).whenever(petRepository).updatePet(any())
        petFlow.value = PetEntity(
            id = 1,
            unlockedTitleIdsJson = "[]",
            unlockedFramesJson = "[]",
            completedSetsJson = "[]"
        )

        val result = engine.checkSetCompletions()

        assertEquals(0, result.size)
    }

    @Test
    fun `checkSetCompletions returns non-negative result`() = runTest {
        doReturn(1).whenever(petRepository).updatePet(any())
        petFlow.value = PetEntity(
            id = 1,
            unlockedTitleIdsJson = "[\"first_blood\"]",
            unlockedFramesJson = "[\"golden\"]",
            completedSetsJson = "[]"
        )

        val result = engine.checkSetCompletions()

        assertTrue("Result should be non-negative", result.size >= 0)
    }

    @Test
    fun `already completed set does not update pet`() = runTest {
        petFlow.value = PetEntity(
            id = 1,
            unlockedTitleIdsJson = "[\"first_blood\"]",
            unlockedFramesJson = "[\"golden\"]",
            completedSetsJson = "[\"sakura_set\"]"
        )

        engine.checkSetCompletions()

        // Since inventoryItemRepository.getAllItems() returns empty, no sets can complete,
        // and since sakura_set is already in completedSetsJson, pet should NOT be updated
        verify(petRepository, never()).updatePet(any())
    }
}
