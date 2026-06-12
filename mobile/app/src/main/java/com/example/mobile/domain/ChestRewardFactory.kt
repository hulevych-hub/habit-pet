package com.example.mobile.domain

import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import kotlinx.coroutines.flow.firstOrNull

object ChestRewardFactory {

    suspend fun buildChestReward(
        rewardType: String,
        chestType: ChestType,
        inventoryItemRepository: InventoryItemRepository
    ): RewardUiEvent.ChestReward {
        val config = ChestRewardConfigProvider.getConfig(chestType)
        val coinAmount = config.getRandomCoins()
        val expAmount = config.getRandomExp()
        val customizationId = grantRandomCustomizationIfNeeded(config, inventoryItemRepository)

        return RewardUiEvent.ChestReward(
            rewardType = rewardType,
            amount = coinAmount,
            expAmount = expAmount,
            customizationId = customizationId
        )
    }

    suspend fun buildChestReward(
        chestTypeValue: String,
        inventoryItemRepository: InventoryItemRepository
    ): RewardUiEvent.ChestReward {
        val chestType = ChestType.values()
            .firstOrNull { it.name.equals(chestTypeValue, ignoreCase = true) }
            ?: ChestType.NORMAL

        return buildChestReward(
            rewardType = "achievement_${chestType.name.lowercase()}",
            chestType = chestType,
            inventoryItemRepository = inventoryItemRepository
        )
    }

    private suspend fun grantRandomCustomizationIfNeeded(
        config: ChestRewardConfig,
        inventoryItemRepository: InventoryItemRepository
    ): Long? {
        if (config.customizationRarity == null || Math.random() >= config.customizationDropChance) {
            return null
        }

        val unownedItems = inventoryItemRepository.getUnownedItemsByRarity(config.customizationRarity)
            .firstOrNull()
            ?.toList()
            .orEmpty()

        if (unownedItems.isEmpty()) return null

        val selectedItem = unownedItems.random()
        return if (inventoryItemRepository.grantItem(selectedItem.id) == 1) {
            selectedItem.id
        } else {
            null
        }
    }
}
