package com.example.mobile.data.local.database

import com.example.mobile.data.local.dao.ChallengeDao
import com.example.mobile.data.local.entities.ChallengeEntity
import com.example.mobile.domain.ChallengeConfig
import com.example.mobile.domain.ChallengeDefinition
import com.example.mobile.domain.ChallengeRewardDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChallengeDatabaseInitializer @Inject constructor(
    private val challengeDao: ChallengeDao
) {

    suspend fun initializeChallenges() {
        if (challengeDao.count() > 0) return

        val definition = ChallengeConfig.firstAvailableFallback()
        val now = System.currentTimeMillis()
        challengeDao.upsert(
            ChallengeEntity(
                id = 1,
                challengeId = definition.id,
                title = definition.title,
                description = definition.description,
                icon = definition.icon,
                type = definition.type.value,
                targetValue = definition.targetValue,
                progressValue = 0,
                rewardIdsJson = rewardIdsJson(definition),
                createdAt = now
            )
        )
    }

    fun initializeChallengesAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            initializeChallenges()
        }
    }

    private fun rewardIdsJson(definition: ChallengeDefinition): String = definition.rewards.joinToString(
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
}
