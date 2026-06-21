package com.example.mobile.domain

import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import kotlinx.coroutines.flow.firstOrNull

object ChestRewardFactory {

    suspend fun buildChestReward(
        rewardType: String,
        chestType: ChestType,
        inventoryItemRepository: InventoryItemRepository,
        grantCustomization: Boolean = false,
        tracksChallengeProgress: Boolean = true
    ): RewardUiEvent.ChestReward {
        val config = ChestRewardConfigProvider.getConfig(chestType)
        val coinAmount = config.getRandomCoins()
        val expAmount = config.getRandomExp()
        val customizationGrant = if (grantCustomization) {
            grantRandomCustomizationIfNeeded(config, inventoryItemRepository)
        } else {
            previewRandomCustomizationIfNeeded(config, inventoryItemRepository)
        }

        return RewardUiEvent.ChestReward(
            rewardType = rewardType,
            amount = coinAmount,
            expAmount = expAmount,
            customizationId = customizationGrant?.databaseItemId,
            equipableId = customizationGrant?.equipableId,
            tracksChallengeProgress = tracksChallengeProgress
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

    private suspend fun previewRandomCustomizationIfNeeded(
        config: ChestRewardConfig,
        inventoryItemRepository: InventoryItemRepository
    ): ChestCustomizationGrant? {
        if (config.customizationRarity == null || Math.random() >= config.customizationDropChance) {
            return null
        }

        return inventoryItemRepository.getUnownedItemsByRarity(config.customizationRarity)
            .firstOrNull()
            ?.toList()
            ?.filter { it.unlockSource != UnlockSources.ACHIEVEMENT }
            ?.randomOrNull()
            ?.let { selectedItem ->
                ChestCustomizationGrant(
                    databaseItemId = selectedItem.id,
                    equipableId = selectedItem.itemId
                )
            }
    }

    private suspend fun grantRandomCustomizationIfNeeded(
        config: ChestRewardConfig,
        inventoryItemRepository: InventoryItemRepository
    ): ChestCustomizationGrant? {
        val preview = previewRandomCustomizationIfNeeded(config, inventoryItemRepository) ?: return null

        return if (inventoryItemRepository.grantItem(preview.databaseItemId) == 1) {
            preview
        } else {
            null
        }
    }

    private data class ChestCustomizationGrant(
        val databaseItemId: Long,
        val equipableId: String
    )
}
