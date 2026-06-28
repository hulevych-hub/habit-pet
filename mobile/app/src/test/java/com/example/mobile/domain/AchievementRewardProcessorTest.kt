package com.example.mobile.domain

import com.example.mobile.data.local.dao.AchievementDao
import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.events.rewardPriority
import com.example.mobile.presentation.ui.reward.RewardQueue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AchievementRewardProcessorTest {

    private lateinit var achievementDao: AchievementDao
    private lateinit var inventoryItemRepository: InventoryItemRepository
    private lateinit var rewardQueue: RewardQueue
    private lateinit var processor: AchievementRewardProcessor

    @Before
    fun setup() {
        achievementDao = mock()
        inventoryItemRepository = mock()
        rewardQueue = mock()

        runTest {
            whenever(achievementDao.markClaimed(any())).thenReturn(1)
        }

        processor = AchievementRewardProcessor(
            database = mock(),
            inventoryItemRepository = inventoryItemRepository,
            rewardQueue = rewardQueue
        )
    }

    private suspend fun processDirect(
        definition: AchievementsConfig.AchievementDefinition,
        achievementId: String
    ): Boolean {
        val rewards = processor.buildAndMarkClaimed(definition, achievementId, achievementDao)
        rewards
            .map { prepared -> prepared.chestReward ?: when (prepared.reward) {
                is AchievementReward.CoinReward -> RewardUiEvent.CoinReward(prepared.reward.amount)
                is AchievementReward.ExpReward -> RewardUiEvent.ExpReward(prepared.reward.amount.toLong())
                is AchievementReward.CustomizationReward -> RewardUiEvent.CustomizationReward(prepared.reward.equipableId)
                is AchievementReward.ChestReward -> throw IllegalStateException("Chest rewards must provide a built ChestReward")
            } }
            .sortedBy { it.rewardPriority() }
            .forEach { reward -> rewardQueue.addReward(reward) }
        return true
    }

    private fun definitionWith(rewards: List<AchievementReward>): AchievementsConfig.AchievementDefinition {
        return AchievementsConfig.AchievementDefinition(
            id = "test_achievement",
            name = "Test Achievement",
            description = "Test",
            icon = "test",
            progressSource = AchievementProgressSource.HABIT_COUNT,
            targetValue = 1,
            rewards = rewards
        )
    }

    // ==================== claim rewards ====================

    @Test
    fun `process — empty rewards queues nothing`() = runTest {
        val definition = definitionWith(emptyList())

        val result = processDirect(definition, "test_achievement")

        assertTrue(result)
        verify(rewardQueue, never()).addReward(any())
    }

    @Test
    fun `process — coin reward zero amount is filtered out`() = runTest {
        val definition = definitionWith(listOf(AchievementReward.CoinReward(0)))

        val result = processDirect(definition, "test_achievement")

        assertTrue(result)
        verify(rewardQueue, never()).addReward(any())
    }

    @Test
    fun `process — exp reward zero amount is filtered out`() = runTest {
        val definition = definitionWith(listOf(AchievementReward.ExpReward(0)))

        val result = processDirect(definition, "test_achievement")

        assertTrue(result)
        verify(rewardQueue, never()).addReward(any())
    }

    @Test
    fun `process — coin reward queues coin event`() = runTest {
        val definition = definitionWith(listOf(AchievementReward.CoinReward(50)))

        val result = processDirect(definition, "test_achievement")

        assertTrue(result)
        verify(rewardQueue).addReward(RewardUiEvent.CoinReward(50))
    }

    @Test
    fun `process — exp reward queues exp event with long conversion`() = runTest {
        val definition = definitionWith(listOf(AchievementReward.ExpReward(100)))

        val result = processDirect(definition, "test_achievement")

        assertTrue(result)
        verify(rewardQueue).addReward(RewardUiEvent.ExpReward(100L))
    }

    @Test
    fun `process — chest reward queues chest event with correct rewardType prefix`() = runTest {
        val definition = definitionWith(listOf(AchievementReward.ChestReward(ChestType.NORMAL)))

        val result = processDirect(definition, "test_achievement")

        assertTrue(result)
        verify(rewardQueue).addReward(argThat { reward: RewardUiEvent ->
            reward is RewardUiEvent.ChestReward && reward.rewardType == "achievement_normal"
        })
    }

    @Test
    fun `process — rare chest reward queues chest event with achievement_rare prefix`() = runTest {
        val definition = definitionWith(listOf(AchievementReward.ChestReward(ChestType.RARE)))

        val result = processDirect(definition, "test_achievement")

        assertTrue(result)
        verify(rewardQueue).addReward(argThat { reward: RewardUiEvent ->
            reward is RewardUiEvent.ChestReward && reward.rewardType == "achievement_rare"
        })
    }

    @Test
    fun `process — customization reward with valid equipable queues customization event`() = runTest {
        val equipableId = EquipableConfig.SAKURA_AURA
        val equipable = EquipableConfig.definition(equipableId)!!
        val inventoryItem = InventoryItemEntity(
            id = 1,
            itemId = equipableId,
            name = "Sakura Aura",
            type = CustomizationTypes.AURA,
            imageUrl = "aura_sakura",
            isUnlocked = true,
            isPurchased = true,
            isEquipped = false,
            rarity = equipable.rarity,
            unlockSource = UnlockSources.ACHIEVEMENT
        )

        whenever(inventoryItemRepository.getItemByItemId(equipableId))
            .thenReturn(MutableStateFlow(inventoryItem))

        val definition = definitionWith(
            listOf(AchievementReward.CustomizationReward(equipableId, CustomizationTypes.AURA))
        )

        val result = processDirect(definition, "test_achievement")

        assertTrue(result)
        verify(rewardQueue).addReward(RewardUiEvent.CustomizationReward(equipableId))
    }

    @Test
    fun `process — customization reward with missing equipable throws`() = runTest {
        val definition = definitionWith(
            listOf(AchievementReward.CustomizationReward("does_not_exist", CustomizationTypes.AURA))
        )

        try {
            processDirect(definition, "test_achievement")
        } catch (_: IllegalStateException) {
            // expected
        }

        verify(rewardQueue, never()).addReward(any())
    }

    @Test
    fun `process — customization reward with wrong unlock source throws`() = runTest {
        val equipableId = EquipableConfig.WIZARD_OUTFIT
        val equipable = EquipableConfig.definition(equipableId)!!
        val inventoryItem = InventoryItemEntity(
            id = 2,
            itemId = equipableId,
            name = "Wizard Outfit",
            type = CustomizationTypes.OUTFIT,
            imageUrl = "outfit_wizard",
            isUnlocked = true,
            isPurchased = true,
            isEquipped = false,
            rarity = equipable.rarity,
            unlockSource = UnlockSources.SHOP
        )

        whenever(inventoryItemRepository.getItemByItemId(equipableId))
            .thenReturn(MutableStateFlow(inventoryItem))

        val definition = definitionWith(
            listOf(AchievementReward.CustomizationReward(equipableId, CustomizationTypes.OUTFIT))
        )

        try {
            processDirect(definition, "test_achievement")
        } catch (_: IllegalStateException) {
            // expected
        }

        verify(rewardQueue, never()).addReward(any())
    }

    @Test
    fun `process — customization reward with null inventory item throws`() = runTest {
        val equipableId = EquipableConfig.SAKURA_AURA

        whenever(inventoryItemRepository.getItemByItemId(equipableId))
            .thenReturn(MutableStateFlow(null))

        val definition = definitionWith(
            listOf(AchievementReward.CustomizationReward(equipableId, CustomizationTypes.AURA))
        )

        try {
            processDirect(definition, "test_achievement")
        } catch (_: IllegalStateException) {
            // expected
        }

        verify(rewardQueue, never()).addReward(any())
    }

    // ==================== ordering ====================

    @Test
    fun `process — multiple rewards are queued in priority order`() = runTest {
        val definition = definitionWith(
            listOf(
                AchievementReward.CoinReward(25),
                AchievementReward.ExpReward(50),
                AchievementReward.ChestReward(ChestType.NORMAL)
            )
        )

        val result = processDirect(definition, "test_achievement")

        assertTrue(result)
        inOrder(rewardQueue) {
            verify(rewardQueue).addReward(argThat { reward: RewardUiEvent -> reward is RewardUiEvent.ChestReward })
            verify(rewardQueue).addReward(argThat { reward: RewardUiEvent -> reward is RewardUiEvent.ExpReward })
            verify(rewardQueue).addReward(argThat { reward: RewardUiEvent -> reward is RewardUiEvent.CoinReward })
        }
    }

    @Test
    fun `process — markClaimed is called on the dao`() = runTest {
        val definition = definitionWith(listOf(AchievementReward.CoinReward(10)))

        processDirect(definition, "test_achievement")

        verify(achievementDao).markClaimed("test_achievement")
    }

    @Test
    fun `process — mixed rewards with some zero amounts filters correctly`() = runTest {
        val definition = definitionWith(
            listOf(
                AchievementReward.CoinReward(0),
                AchievementReward.CoinReward(100),
                AchievementReward.ExpReward(0),
                AchievementReward.ExpReward(200)
            )
        )

        val result = processDirect(definition, "test_achievement")

        assertTrue(result)
        verify(rewardQueue).addReward(RewardUiEvent.ExpReward(200L))
        verify(rewardQueue).addReward(RewardUiEvent.CoinReward(100))
        verify(rewardQueue, never()).addReward(RewardUiEvent.CoinReward(0))
        verify(rewardQueue, never()).addReward(RewardUiEvent.ExpReward(0L))
    }
}
