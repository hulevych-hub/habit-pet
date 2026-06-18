package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.ChallengeDao
import com.example.mobile.data.local.dao.HabitCompletionDao
import com.example.mobile.data.local.dao.HabitDao
import com.example.mobile.data.local.dao.InventoryItemDao
import com.example.mobile.data.local.dao.PetDao
import com.example.mobile.data.local.dao.StatisticsDao
import com.example.mobile.data.local.entities.ChallengeEntity
import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.ChallengeAvailability
import com.example.mobile.domain.ChallengeConfig
import com.example.mobile.domain.ChallengeDefinition
import com.example.mobile.domain.ChallengeRewardDefinition
import com.example.mobile.domain.ChallengeType
import com.example.mobile.domain.CustomizationTypes
import com.example.mobile.domain.UnlockSources
import com.example.mobile.domain.repository.ChallengeClaimResult
import com.example.mobile.domain.repository.ChallengeRepository
import com.example.mobile.domain.repository.ChallengeUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.random.Random

class ChallengeRepositoryImpl @Inject constructor(
    private val challengeDao: ChallengeDao,
    private val statisticsDao: StatisticsDao,
    private val habitCompletionDao: HabitCompletionDao,
    private val habitDao: HabitDao,
    private val inventoryItemDao: InventoryItemDao,
    private val petDao: PetDao
) : ChallengeRepository {

    override fun getActiveChallengeUiState(): Flow<ChallengeUiState> =
        challengeDao.getActiveChallenge().map { challenge ->
            challenge?.toUiState() ?: ChallengeUiState.empty()
        }

    override suspend fun ensureActiveChallenge() {
        val current = challengeDao.getActiveChallengeOnce()
        if (current != null && !current.isClaimed) return

        challengeDao.upsert(chooseNextChallenge())
    }

    override suspend fun recordHabitCompleted(habitId: Long, xpEarned: Long, coinsEarned: Int) {
        val current = challengeDao.getActiveChallengeOnce()
            ?: run {
                ensureActiveChallenge()
                challengeDao.getActiveChallengeOnce()
            }
            ?: return
        if (current.isClaimed) return

        val progress = when (ChallengeType.from(current.type)) {
            ChallengeType.HABIT_COMPLETION -> current.progressValue + 1
            ChallengeType.XP_EARNED -> current.progressValue + xpEarned.coerceAtLeast(0L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            ChallengeType.COINS_EARNED -> current.progressValue + coinsEarned.coerceAtLeast(0)
            else -> current.progressValue
        }
        upsertProgress(current, progress)
    }

    override suspend fun recordXpEarned(amount: Long) {
        val current = challengeDao.getActiveChallengeOnce()
            ?: run {
                ensureActiveChallenge()
                challengeDao.getActiveChallengeOnce()
            }
            ?: return
        if (current.isClaimed) return

        val progress = if (ChallengeType.from(current.type) == ChallengeType.XP_EARNED) {
            current.progressValue + amount.coerceAtLeast(0L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        } else {
            current.progressValue
        }
        upsertProgress(current, progress)
    }

    override suspend fun recordCoinsEarned(amount: Int) {
        val current = challengeDao.getActiveChallengeOnce()
            ?: run {
                ensureActiveChallenge()
                challengeDao.getActiveChallengeOnce()
            }
            ?: return
        if (current.isClaimed) return

        val progress = if (ChallengeType.from(current.type) == ChallengeType.COINS_EARNED) {
            current.progressValue + amount.coerceAtLeast(0)
        } else {
            current.progressValue
        }
        upsertProgress(current, progress)
    }

    override suspend fun recordChestOpened() {
        val current = challengeDao.getActiveChallengeOnce() ?: return
        if (current.isClaimed) return

        val progress = if (ChallengeType.from(current.type) == ChallengeType.CHEST_OPENED) {
            current.progressValue + 1
        } else {
            current.progressValue
        }
        upsertProgress(current, progress)
    }

    override suspend fun recordCustomizationUnlocked(equipableId: String) {
        val current = challengeDao.getActiveChallengeOnce() ?: return
        if (current.isClaimed) return

        val progress = if (ChallengeType.from(current.type) == ChallengeType.CUSTOMIZATION_UNLOCKED) {
            current.progressValue + 1
        } else {
            current.progressValue
        }
        upsertProgress(current, progress)
    }

    override suspend fun recordCustomizationEquipped(type: String, itemId: String) {
        val current = challengeDao.getActiveChallengeOnce() ?: return
        if (current.isClaimed) return

        val progress = if (
            ChallengeType.from(current.type) == ChallengeType.CUSTOMIZATION_EQUIPPED &&
            current.challengeId == when (type) {
                CustomizationTypes.OUTFIT -> "equip_outfit"
                CustomizationTypes.AURA -> "equip_aura"
                CustomizationTypes.BACKGROUND -> "change_background"
                else -> null
            }
        ) {
            current.progressValue + 1
        } else {
            current.progressValue
        }
        upsertProgress(current, progress)
    }

    override suspend fun recordStreak(streak: Int) {
        val current = challengeDao.getActiveChallengeOnce()
            ?: run {
                ensureActiveChallenge()
                challengeDao.getActiveChallengeOnce()
            }
            ?: return
        if (current.isClaimed) return

        val progress = if (ChallengeType.from(current.type) == ChallengeType.STREAK) {
            current.progressValue.coerceAtLeast(streak)
        } else {
            current.progressValue
        }
        upsertProgress(current, progress)
    }

    override suspend fun claimActiveChallenge(): ChallengeClaimResult {
        val current = challengeDao.getActiveChallengeOnce()
            ?: chooseNextChallenge().also { challengeDao.upsert(it) }

        if (!current.isCompleted || current.isClaimed) {
            return ChallengeClaimResult(current, emptyList())
        }

        val rewards = ChallengeConfig.definition(current.challengeId)?.rewards.orEmpty()
        challengeDao.upsert(chooseNextChallenge(current.challengeId, ChallengeType.from(current.type)))

        return ChallengeClaimResult(current, rewards)
    }

    override suspend fun reset() {
        challengeDao.deleteAll()
        ensureActiveChallenge()
    }

    private suspend fun upsertProgress(current: ChallengeEntity, progressValue: Int) {
        val cappedProgress = progressValue.coerceAtMost(current.targetValue)
        val completed = cappedProgress >= current.targetValue
        val now = System.currentTimeMillis()

        challengeDao.upsert(
            current.copy(
                progressValue = cappedProgress,
                isCompleted = completed,
                completedAt = if (completed && !current.isCompleted) now else current.completedAt
            )
        )
    }

    private suspend fun chooseNextChallenge(
        previousChallengeId: String? = null,
        previousType: ChallengeType? = null
    ): ChallengeEntity {
        val context = AvailabilityContext(
            statistics = statisticsDao.getStatistics().firstOrNull() ?: StatisticsEntity(id = 1),
            inventoryItems = inventoryItemDao.getAllItems().firstOrNull().orEmpty(),
            pet = petDao.getPet().firstOrNull() ?: PetEntity(id = 1)
        )
        val definition = selectDefinition(previousChallengeId, previousType, context)
        val now = System.currentTimeMillis()

        return ChallengeEntity(
            id = 1,
            challengeId = definition.id,
            title = definition.title,
            description = definition.description,
            icon = definition.icon,
            type = definition.type.value,
            targetValue = definition.targetValue,
            progressValue = 0,
            rewardIdsJson = definition.rewardIdsJson(),
            previousChallengeId = previousChallengeId,
            createdAt = now
        )
    }

    private fun selectDefinition(
        previousChallengeId: String?,
        previousType: ChallengeType?,
        context: AvailabilityContext
    ): ChallengeDefinition {
        val available = ChallengeConfig.definitions.filter {
            it.id != previousChallengeId && isAvailable(it, context)
        }
        val candidates = available.ifEmpty {
            ChallengeConfig.definitions.filter { it.id != previousChallengeId }.ifEmpty {
                ChallengeConfig.definitions
            }
        }
        val varietyCandidates = if (previousType != null) {
            candidates.filter { it.type != previousType }.ifEmpty { candidates }
        } else {
            candidates
        }

        val totalWeight = varietyCandidates.sumOf { it.weight.coerceAtLeast(1) }
        var roll = Random.nextInt(totalWeight)
        for (definition in varietyCandidates) {
            roll -= definition.weight.coerceAtLeast(1)
            if (roll < 0) return definition
        }
        return varietyCandidates.first()
    }

    private fun isAvailable(definition: ChallengeDefinition, context: AvailabilityContext): Boolean =
        when (definition.availability) {
            ChallengeAvailability.ALWAYS -> true
            ChallengeAvailability.HAS_CHEST_AVAILABLE -> context.statistics.rewardChestsAvailable > 0
            ChallengeAvailability.HAS_PURCHASABLE_CUSTOMIZATION -> {
                val purchasable = context.inventoryItems
                    .filter { !it.isPurchased && it.unlockSource == UnlockSources.SHOP && it.price > 0 }
                purchasable.isNotEmpty() && context.statistics.totalCoins >= purchasable.minOf { it.price }
            }
            ChallengeAvailability.HAS_OWNED_OUTFIT_TO_EQUIP ->
                hasOwnedItemToEquip(context.inventoryItems, CustomizationTypes.OUTFIT, context.pet.equippedOutfit)
            ChallengeAvailability.HAS_OWNED_AURA_TO_EQUIP ->
                hasOwnedItemToEquip(context.inventoryItems, CustomizationTypes.AURA, context.pet.equippedAura)
            ChallengeAvailability.HAS_OWNED_BACKGROUND_TO_EQUIP ->
                hasOwnedItemToEquip(context.inventoryItems, CustomizationTypes.BACKGROUND, context.pet.equippedBackground)
        }

    private fun hasOwnedItemToEquip(
        items: List<InventoryItemEntity>,
        type: String,
        currentlyEquippedId: String?
    ): Boolean = items.any { it.isPurchased && it.type == type && it.itemId != currentlyEquippedId }

    private fun ChallengeDefinition.rewardIdsJson(): String = rewards.joinToString(
        prefix = "[",
        postfix = "]"
    ) { reward ->
        when (reward) {
            is ChallengeRewardDefinition.CoinReward -> "{\"type\":\"COIN\",\"amount\":${reward.amount}}"
            is ChallengeRewardDefinition.ExpReward -> "{\"type\":\"EXP\",\"amount\":${reward.amount}}"
            is ChallengeRewardDefinition.ChestReward -> "{\"type\":\"CHEST\",\"chestType\":\"${reward.chestType}\"}"
            is ChallengeRewardDefinition.CustomizationReward -> "{\"type\":\"CUSTOMIZATION\",\"equipableId\":\"${reward.equipableId}\"}"
        }
    }

    private fun ChallengeEntity.toUiState(): ChallengeUiState {
        val definition = ChallengeConfig.definition(challengeId)
        val rewards = definition?.rewards.orEmpty()
        val fraction = if (targetValue > 0) progressValue.toFloat() / targetValue.toFloat() else 0f

        return ChallengeUiState(
            challenge = this,
            progress = progressValue,
            target = targetValue,
            progressFraction = fraction.coerceIn(0f, 1f),
            progressLabel = "$progressValue/$targetValue",
            isCompleted = isCompleted,
            isClaimed = isClaimed,
            rewards = rewards
        )
    }

    private data class AvailabilityContext(
        val statistics: StatisticsEntity,
        val inventoryItems: List<InventoryItemEntity>,
        val pet: PetEntity
    )
}
