package com.example.mobile.domain

import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.reward.RewardQueue
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionSetEngine @Inject constructor(
    private val inventoryItemRepository: InventoryItemRepository,
    private val petRepository: PetRepository,
    private val statisticsRepository: StatisticsRepository,
    private val rewardQueue: RewardQueue
) {
    data class SetCompletionResult(
        val setId: String,
        val setName: String,
        val bonusCoins: Int,
        val unlockedTitleId: String?,
        val unlockedFrameId: String?
    )

    /**
     * Checks all collection sets for newly-completed ones based on currently owned items.
     * Grants bonuses and unlocks titles/frames for any set the player now fully owns.
     * Returns the list of newly completed sets (empty if none).
     */
    suspend fun checkSetCompletions(): List<SetCompletionResult> {
        val ownedItemIds = inventoryItemRepository.getAllItems()
            .firstOrNull()
            .orEmpty()
            .filter { it.isUnlocked }
            .map { it.itemId }
            .toSet()

        val pet = petRepository.getPet().firstOrNull() ?: PetEntity(id = 1)
        val completedSetIds = PetEntity.parseUnlockedIds(pet.unlockedTitleIdsJson).let { _ ->
            // We track completed sets via titles/frames already granted.
            // A set is "already processed" if its bonus title/frame is already unlocked.
            emptySet<String>()
        }

        val results = mutableListOf<SetCompletionResult>()

        for (set in CollectionSetConfig.sets) {
            val ownsAll = set.itemIds.all { it in ownedItemIds }
            if (!ownsAll) continue

            // Check if this set's bonus was already granted
            val alreadyProcessed = isSetBonusAlreadyGranted(set, pet)
            if (alreadyProcessed) continue

            // Grant the bonus
            val result = grantSetBonus(set, pet)
            if (result != null) {
                results.add(result)
            }
        }

        return results
    }

    private fun isSetBonusAlreadyGranted(
        set: CollectionSetConfig.SetDefinition,
        pet: PetEntity
    ): Boolean {
        val unlockedTitles = PetEntity.parseUnlockedIds(pet.unlockedTitleIdsJson)
        val unlockedFrames = PetEntity.parseUnlockedIds(pet.unlockedFramesJson)

        val titleGranted = set.bonus.titleId == null || set.bonus.titleId in unlockedTitles
        val frameGranted = set.bonus.frameId == null || set.bonus.frameId in unlockedFrames

        return titleGranted && frameGranted
    }

    private suspend fun grantSetBonus(
        set: CollectionSetConfig.SetDefinition,
        pet: PetEntity
    ): SetCompletionResult? {
        val unlockedTitles = PetEntity.parseUnlockedIds(pet.unlockedTitleIdsJson).toMutableSet()
        val unlockedFrames = PetEntity.parseUnlockedIds(pet.unlockedFramesJson).toMutableSet()

        var unlockedTitleId: String? = null
        var unlockedFrameId: String? = null

        set.bonus.titleId?.let { titleId ->
            if (titleId !in unlockedTitles) {
                unlockedTitles.add(titleId)
                unlockedTitleId = titleId
            }
        }

        set.bonus.frameId?.let { frameId ->
            if (frameId !in unlockedFrames) {
                unlockedFrames.add(frameId)
                unlockedFrameId = frameId
            }
        }

        // Persist unlocked titles/frames
        petRepository.updatePet(
            pet.copy(
                unlockedTitleIdsJson = PetEntity.unlockedIdsToJson(unlockedTitles),
                unlockedFramesJson = PetEntity.unlockedIdsToJson(unlockedFrames),
                completedSetsJson = PetEntity.unlockedIdsToJson(
                    PetEntity.parseUnlockedIds(pet.completedSetsJson).toMutableSet().apply { add(set.id) }
                )
            )
        )

        // Grant coins
        if (set.bonus.coins > 0) {
            statisticsRepository.addCoins(set.bonus.coins)
        }

        // Queue rewards
        if (set.bonus.coins > 0) {
            rewardQueue.addReward(RewardUiEvent.CoinReward(amount = set.bonus.coins))
        }
        if (set.bonus.expAmount > 0) {
            rewardQueue.addReward(RewardUiEvent.ExpReward(amount = set.bonus.expAmount.toLong()))
        }

        return SetCompletionResult(
            setId = set.id,
            setName = set.name,
            bonusCoins = set.bonus.coins,
            unlockedTitleId = unlockedTitleId,
            unlockedFrameId = unlockedFrameId
        )
    }

    /**
     * Call when a new customization item is granted/purchased to check for set completions.
     */
    suspend fun onItemGranted(itemId: String): List<SetCompletionResult> {
        val relevantSets = CollectionSetConfig.findSetsContainingItem(itemId)
        if (relevantSets.isEmpty()) return emptyList()
        return checkSetCompletions()
    }
}
