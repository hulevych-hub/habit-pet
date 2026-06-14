package com.example.mobile.domain

sealed class AchievementReward {
    data class CoinReward(val amount: Int) : AchievementReward()
    data class ExpReward(val amount: Int) : AchievementReward()
    data class ChestReward(val chestType: ChestType) : AchievementReward()
    data class CustomizationReward(
        val equipableId: String,
        val type: String = CustomizationTypes.OUTFIT
    ) : AchievementReward()
}
