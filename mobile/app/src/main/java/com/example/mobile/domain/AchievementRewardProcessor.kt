package com.example.mobile.domain

import android.util.Log
import androidx.room.withTransaction
import com.example.mobile.data.local.database.AppDatabase
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.EquipableConfig
import com.example.mobile.domain.UnlockSources
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
                                    chestReward = ChestRewardFactory.buildChestReward(
                                        rewardType = "achievement_${reward.chestType.name.lowercase()}",
                                        chestType = reward.chestType,
                                        inventoryItemRepository = inventoryItemRepository
                                    )
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

            preparedRewards
                .map { it.chestReward ?: it.reward.toRewardUiEvent() }
                .sortedBy { rewardPriority(it) }
                .forEach { reward ->
                    rewardQueue.addReward(reward)
                    rewardEventBus.emit(reward)
                }
            return true
        } catch (e: Exception) {
            Log.e("AchievementReward", "Achievement reward processing failed", e)
            return false
        }
    }

    private suspend fun addPetExp(expAmount: Int) {
        val currentPet = petRepository.getPet().firstOrNull() ?: PetEntity(id = 1)
        petRepository.updatePet(currentPet.copy(xp = currentPet.xp + expAmount))
    }

    private suspend fun grantCustomization(reward: AchievementReward.CustomizationReward) {
        val equipable = EquipableConfig.definition(reward.equipableId)
            ?: throw IllegalStateException("Missing customization reward: ${reward.equipableId}")

        if (equipable.unlockSource != UnlockSources.ACHIEVEMENT) {
            throw IllegalStateException("Achievement customization reward must use ACHIEVEMENT source: ${equipable.id}")
        }

        val item = inventoryItemRepository.getItemByItemId(equipable.id).firstOrNull()
            ?: throw IllegalStateException("Missing customization reward: ${equipable.id}")

        if (item.isPurchased) {
            return
        }

        val result = inventoryItemRepository.grantItem(item.id)
        if (result < 0) {
            throw IllegalStateException("Failed to grant customization reward: ${equipable.id}")
        }
    }

    private fun AchievementReward.toRewardUiEvent(): RewardUiEvent = when (this) {
        is AchievementReward.CoinReward -> RewardUiEvent.CoinReward(amount)
        is AchievementReward.ExpReward -> RewardUiEvent.ExpReward(amount.toLong())
        is AchievementReward.ChestReward -> throw IllegalStateException("Chest rewards must provide a built ChestReward")
        is AchievementReward.CustomizationReward -> RewardUiEvent.CustomizationReward(equipableId)
    }

    private fun rewardPriority(reward: RewardUiEvent): Int = when (reward) {
        is RewardUiEvent.LevelUpReward -> 1
        is RewardUiEvent.DragonEvolutionReward -> 2
        is RewardUiEvent.StreakReward -> 3
        is RewardUiEvent.ChestReward -> 4
        is RewardUiEvent.AchievementReward -> 5
        is RewardUiEvent.ExpReward -> 6
        is RewardUiEvent.CustomizationReward -> 7
        is RewardUiEvent.CoinReward -> 8
    }

    private data class PreparedAchievementReward(
        val reward: AchievementReward,
        val chestReward: RewardUiEvent.ChestReward? = null
    )
}
