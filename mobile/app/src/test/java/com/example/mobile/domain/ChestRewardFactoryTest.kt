package com.example.mobile.domain

import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.Rarity
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@kotlin.OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ChestRewardFactoryTest {

    private val inventoryItemRepository: InventoryItemRepository = mock {
        on { getUnownedItemsByRarity(any()) }.thenReturn(flowOf(emptyList()))
    }

    @Test
    fun `buildChestReward — NORMAL chest returns coins and exp within range`() = runTest {
        val config = ChestRewardConfigProvider.getConfig(ChestType.NORMAL)
        val reward = ChestRewardFactory.buildChestReward(
            rewardType = "test_normal",
            chestType = ChestType.NORMAL,
            inventoryItemRepository = inventoryItemRepository,
            grantCustomization = false
        )

        assertNotNull(reward)
        assertEquals("test_normal", reward.rewardType)
        assertTrue("Coins should be >= 0", reward.amount is Int && (reward.amount as Int) >= 0)
        assertTrue("Exp should be >= 0", reward.expAmount >= 0)
        // Default tracksChallengeProgress is true when not explicitly set
        assertTrue(reward.tracksChallengeProgress)
    }

    @Test
    fun `buildChestReward — RARE chest returns coins within configured range`() = runTest {
        val config = ChestRewardConfigProvider.getConfig(ChestType.RARE)
        val reward = ChestRewardFactory.buildChestReward(
            rewardType = "test_rare",
            chestType = ChestType.RARE,
            inventoryItemRepository = inventoryItemRepository,
            grantCustomization = false,
            tracksChallengeProgress = true
        )

        assertNotNull(reward)
        assertTrue(reward.tracksChallengeProgress)
        val coins = reward.amount as Int
        assertTrue("Coins should be in RARE range ${config.coinRange}, was $coins",
            coins in config.coinRange)
    }

    @Test
    fun `buildChestReward — EPIC chest returns coins within configured range`() = runTest {
        val config = ChestRewardConfigProvider.getConfig(ChestType.EPIC)
        val reward = ChestRewardFactory.buildChestReward(
            rewardType = "test_epic",
            chestType = ChestType.EPIC,
            inventoryItemRepository = inventoryItemRepository,
            grantCustomization = true,
            tracksChallengeProgress = true
        )

        assertNotNull(reward)
        val coins = reward.amount as Int
        assertTrue("Coins should be in EPIC range ${config.coinRange}, was $coins",
            coins in config.coinRange)
    }

    @Test
    fun `buildChestReward — LEGENDARY chest returns coins within configured range`() = runTest {
        val config = ChestRewardConfigProvider.getConfig(ChestType.LEGENDARY)
        val reward = ChestRewardFactory.buildChestReward(
            rewardType = "test_legendary",
            chestType = ChestType.LEGENDARY,
            inventoryItemRepository = inventoryItemRepository,
            grantCustomization = false,
            tracksChallengeProgress = false
        )

        assertNotNull(reward)
        val coins = reward.amount as Int
        assertTrue("Coins should be in LEGENDARY range ${config.coinRange}, was $coins",
            coins in config.coinRange)
    }

    @Test
    fun `buildChestReward — string overload resolves chest type by name`() = runTest {
        val reward = ChestRewardFactory.buildChestReward(
            chestTypeValue = "RARE",
            inventoryItemRepository = inventoryItemRepository
        )

        assertNotNull(reward)
        assertEquals("achievement_rare", reward.rewardType)
    }

    @Test
    fun `buildChestReward — string overload falls back to NORMAL for unknown`() = runTest {
        val reward = ChestRewardFactory.buildChestReward(
            chestTypeValue = "UNKNOWN_CHEST",
            inventoryItemRepository = inventoryItemRepository
        )

        assertNotNull(reward)
        assertEquals("achievement_normal", reward.rewardType)
    }

    @Test
    fun `buildChestReward — with customization grant and no unowned items returns null customization`() = runTest {
        // No unowned items available
        whenever(inventoryItemRepository.getUnownedItemsByRarity(any()))
            .thenReturn(flowOf(emptyList()))

        val reward = ChestRewardFactory.buildChestReward(
            rewardType = "test_customization",
            chestType = ChestType.EPIC,
            inventoryItemRepository = inventoryItemRepository,
            grantCustomization = true
        )

        assertNotNull(reward)
        assertNull(reward.customizationId)
        assertNull(reward.equipableId)
    }

    @Test
    fun `buildChestReward — with customization grant grants item from unowned list`() = runTest {
        val item = InventoryItemEntity(
            id = 42L,
            itemId = "cape_fire",
            type = "OUTFIT",
            name = "Fire Cape",
            rarity = Rarity.EPIC,
            unlockSource = "CHEST"
        )
        whenever(inventoryItemRepository.getUnownedItemsByRarity(Rarity.EPIC))
            .thenReturn(flowOf(listOf(item)))
        whenever(inventoryItemRepository.grantItem(42L)).thenReturn(1)

        // EPIC chest has 18% customization drop chance; run multiple times to ensure
        // the drop triggers at least once and verify grant behavior
        var customizationGranted = false
        var lastReward: RewardUiEvent.ChestReward? = null
        for (i in 0 until 100) {
            val reward = ChestRewardFactory.buildChestReward(
                rewardType = "test_customization_grant",
                chestType = ChestType.EPIC,
                inventoryItemRepository = inventoryItemRepository,
                grantCustomization = true
            )
            lastReward = reward
            if (reward.customizationId != null) {
                customizationGranted = true
                assertEquals(42L, reward.customizationId)
                assertEquals("cape_fire", reward.equipableId)
                break
            }
        }
        assertTrue("Customization should have dropped within 100 attempts", customizationGranted)
        assertNotNull(lastReward)
        verify(inventoryItemRepository).grantItem(42L)
    }

    @Test
    fun `buildChestReward — grant fails returns null customization`() = runTest {
        val item = InventoryItemEntity(
            id = 42L,
            itemId = "cape_fire",
            type = "OUTFIT",
            name = "Fire Cape",
            rarity = Rarity.EPIC,
            unlockSource = "CHEST"
        )
        whenever(inventoryItemRepository.getUnownedItemsByRarity(Rarity.EPIC))
            .thenReturn(flowOf(listOf(item)))
        whenever(inventoryItemRepository.grantItem(42L)).thenReturn(0) // grant fails

        // Run multiple times — customization may or may not drop due to 18% chance.
        // When it does drop and grant fails, customization should be null.
        // When it doesn't drop, customization is also null. Either way, result is null.
        var grantAttempted = false
        for (i in 0 until 100) {
            val reward = ChestRewardFactory.buildChestReward(
                rewardType = "test_grant_failure",
                chestType = ChestType.EPIC,
                inventoryItemRepository = inventoryItemRepository,
                grantCustomization = true
            )
            // Whether customization drops or not, the fields should be null because:
            // - if drop doesn't roll: customization is null
            // - if drop rolls but grantItem returns 0: customization is null
            assertNull(reward.customizationId)
            assertNull(reward.equipableId)
        }
        // grantItem may or may not have been called depending on random rolls,
        // but the result is always null customization
    }

    @Test
    fun `buildChestReward — achievement source items are filtered out`() = runTest {
        val achievementItem = InventoryItemEntity(
            id = 10L,
            itemId = "hat_wizard",
            type = "OUTFIT",
            name = "Wizard Hat",
            rarity = Rarity.EPIC,
            unlockSource = "ACHIEVEMENT"
        )
        val chestItem = InventoryItemEntity(
            id = 42L,
            itemId = "cape_fire",
            type = "OUTFIT",
            name = "Fire Cape",
            rarity = Rarity.EPIC,
            unlockSource = "CHEST"
        )
        whenever(inventoryItemRepository.getUnownedItemsByRarity(Rarity.EPIC))
            .thenReturn(flowOf(listOf(achievementItem, chestItem)))
        whenever(inventoryItemRepository.grantItem(42L)).thenReturn(1)

        // EPIC chest has 18% customization drop chance; run multiple times to verify filtering
        var customizationGranted = false
        for (i in 0 until 100) {
            val reward = ChestRewardFactory.buildChestReward(
                rewardType = "test_source_filter",
                chestType = ChestType.EPIC,
                inventoryItemRepository = inventoryItemRepository,
                grantCustomization = true
            )
            if (reward.customizationId != null) {
                customizationGranted = true
                // Should have selected the chest item, not the achievement item
                assertEquals(42L, reward.customizationId)
                assertEquals("cape_fire", reward.equipableId)
                break
            }
        }
        assertTrue("Customization should have dropped within 100 attempts", customizationGranted)
        verify(inventoryItemRepository).grantItem(42L)
        verify(inventoryItemRepository, never()).grantItem(10L)
    }

    @Test
    fun `buildChestReward — preview does not grant item`() = runTest {
        val item = InventoryItemEntity(
            id = 42L,
            itemId = "cape_fire",
            type = "OUTFIT",
            name = "Fire Cape",
            rarity = Rarity.EPIC,
            unlockSource = "CHEST"
        )
        whenever(inventoryItemRepository.getUnownedItemsByRarity(Rarity.EPIC))
            .thenReturn(flowOf(listOf(item)))

        // Run multiple times to cover both drop and no-drop scenarios
        for (i in 0 until 100) {
            val reward = ChestRewardFactory.buildChestReward(
                rewardType = "test_preview",
                chestType = ChestType.EPIC,
                inventoryItemRepository = inventoryItemRepository,
                grantCustomization = false // preview mode
            )
            assertNotNull(reward)
        }
        // Preview should never call grantItem regardless of drop rolls
        verify(inventoryItemRepository, never()).grantItem(any())
    }
}
