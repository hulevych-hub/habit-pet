package com.example.mobile.domain

import android.util.Log
import androidx.room.withTransaction
import com.example.mobile.data.local.database.AppDatabase
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.reward.RewardEventBus
import com.example.mobile.presentation.ui.reward.RewardQueue
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRewardProcessor @Inject constructor(
    private val database: AppDatabase,
    private val statisticsRepository: StatisticsRepository,
    private val petRepository: PetRepository,
    private val inventoryItemRepository: InventoryItemRepository,
    private val rewardQueue: RewardQueue,
    private val rewardEventBus: RewardEventBus
) {

    suspend fun process(
        definition: AchievementsConfig.AchievementDefinition,
        achievementId: String
    ): Boolean {
        val preparedRewards: List<PreparedAchievementReward>

        try {
            preparedRewards = database.withTransaction {
                val rewards = mutableListOf<PreparedAchievementReward>()

                definition.rewards.forEach { reward ->
                    when (reward) {
                        is AchievementReward.CoinReward -> {
                            if (reward.amount > 0) {
                                statisticsRepository.addCoins(reward.amount)
                                rewards.add(PreparedAchievementReward(reward))
                            }
                        }

                        is AchievementReward.ExpReward -> {
                            if (reward.amount > 0) {
                                addPetExp(reward.amount)
                                rewards.add(PreparedAchievementReward(reward))
                            }
                        }

                        is AchievementReward.ChestReward -> {
                            rewards.add(
                                PreparedAchievementReward(
                                    reward = reward,
                                    chestReward = buildChestReward(reward.chestType)
                                )
                            )
                        }

                        is AchievementReward.CustomizationReward -> {
                            grantCustomization(reward)
                            rewards.add(PreparedAchievementReward(reward))
                        }
                    }
                }

                database.achievementDao().markClaimed(achievementId)

                rewards
            }

            preparedRewards.forEach { preparedReward ->
                preparedReward.chestReward?.let { rewardQueue.addReward(it) }
            }

            val event = RewardUiEvent.AchievementReward(
                achievementName = definition.name,
                rewards = preparedRewards.map { it.reward }
            )

            rewardQueue.addReward(event)
            rewardEventBus.emit(event)
            return true
        } catch (e: Exception) {
            Log.e("AchievementRewardProcessor", "Achievement reward processing failed", e)
            return false
        }
    }

    private suspend fun addPetExp(expAmount: Int) {
        val currentPet = petRepository.getPet().firstOrNull() ?: PetEntity(id = 1)
        petRepository.updatePet(currentPet.copy(xp = currentPet.xp + expAmount))
    }

    private suspend fun grantCustomization(reward: AchievementReward.CustomizationReward) {
        val item = inventoryItemRepository.getItemByItemId(reward.itemId).firstOrNull()
            ?: throw IllegalStateException("Missing customization reward: ${reward.itemId}")

        if (item.isPurchased) {
            return
        }

        val result = inventoryItemRepository.grantItem(item.id)
        if (result < 0) {
            throw IllegalStateException("Failed to grant customization reward: ${reward.itemId}")
        }
    }

    private data class PreparedAchievementReward(
        val reward: AchievementReward,
        val chestReward: RewardUiEvent.ChestReward? = null
    )

    private suspend fun buildChestReward(chestTypeValue: ChestType): RewardUiEvent.ChestReward {
        val config = ChestRewardConfigProvider.getConfig(chestTypeValue)
        var coinAmount = config.getRandomCoins()
        var expAmount = config.getRandomExp()
        var customizationId: Long? = null

        if (config.customizationRarity != null && Math.random() < config.customizationDropChance) {
            val unownedItems = inventoryItemRepository.getUnownedItemsByRarity(config.customizationRarity)
                .firstOrNull()
                ?.toList()
                .orEmpty()

            if (unownedItems.isNotEmpty()) {
                val selectedItem = unownedItems.random()
                if (inventoryItemRepository.grantItem(selectedItem.id) == 1) {
                    customizationId = selectedItem.id
                }
            }
        }

        return RewardUiEvent.ChestReward(
            rewardType = "achievement_${chestTypeValue.name.lowercase()}",
            amount = coinAmount,
            expAmount = expAmount,
            customizationId = customizationId
        )
    }
}
