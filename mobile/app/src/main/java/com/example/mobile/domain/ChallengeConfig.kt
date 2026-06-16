package com.example.mobile.domain

enum class ChallengeType(val value: String) {
    HABIT_COMPLETION("HABIT_COMPLETION"),
    XP_EARNED("XP_EARNED"),
    COINS_EARNED("COINS_EARNED"),
    CHEST_OPENED("CHEST_OPENED"),
    CUSTOMIZATION_UNLOCKED("CUSTOMIZATION_UNLOCKED"),
    CUSTOMIZATION_EQUIPPED("CUSTOMIZATION_EQUIPPED"),
    STREAK("STREAK");

    companion object {
        fun from(value: String): ChallengeType =
            entries.firstOrNull { it.value == value } ?: HABIT_COMPLETION
    }
}

sealed interface ChallengeRewardDefinition {
    data class CoinReward(val amount: Int) : ChallengeRewardDefinition
    data class ExpReward(val amount: Long) : ChallengeRewardDefinition
    data class ChestReward(val chestType: String) : ChallengeRewardDefinition
    data class CustomizationReward(val equipableId: String) : ChallengeRewardDefinition
}

enum class ChallengeAvailability {
    ALWAYS,
    HAS_CHEST_AVAILABLE,
    HAS_PURCHASABLE_CUSTOMIZATION,
    HAS_OWNED_OUTFIT_TO_EQUIP,
    HAS_OWNED_AURA_TO_EQUIP,
    HAS_OWNED_BACKGROUND_TO_EQUIP
}

data class ChallengeDefinition(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val type: ChallengeType,
    val targetValue: Int,
    val rewards: List<ChallengeRewardDefinition>,
    val weight: Int = 100,
    val availability: ChallengeAvailability = ChallengeAvailability.ALWAYS
)

object ChallengeConfig {
    val definitions: List<ChallengeDefinition> = listOf(
        ChallengeDefinition(
            id = "habit_1",
            title = "One tiny win",
            description = "Complete 1 habit and give your dragon a small cheer.",
            icon = "habit",
            type = ChallengeType.HABIT_COMPLETION,
            targetValue = 1,
            rewards = listOf(
                ChallengeRewardDefinition.CoinReward(15),
                ChallengeRewardDefinition.ExpReward(10)
            ),
            weight = 140
        ),
        ChallengeDefinition(
            id = "habit_3",
            title = "Three habit rhythm",
            description = "Complete 3 habits at an easy pace.",
            icon = "habit",
            type = ChallengeType.HABIT_COMPLETION,
            targetValue = 3,
            rewards = listOf(
                ChallengeRewardDefinition.CoinReward(25),
                ChallengeRewardDefinition.ExpReward(25)
            ),
            weight = 90
        ),
        ChallengeDefinition(
            id = "habit_5",
            title = "Five little sparks",
            description = "Complete 5 habits over today or the next few days.",
            icon = "habit",
            type = ChallengeType.HABIT_COMPLETION,
            targetValue = 5,
            rewards = listOf(
                ChallengeRewardDefinition.CoinReward(40),
                ChallengeRewardDefinition.ExpReward(35),
                ChallengeRewardDefinition.ChestReward(ChestType.NORMAL.name)
            ),
            weight = 45
        ),
        ChallengeDefinition(
            id = "xp_25",
            title = "XP spark",
            description = "Earn 25 XP through small wins.",
            icon = "xp",
            type = ChallengeType.XP_EARNED,
            targetValue = 25,
            rewards = listOf(
                ChallengeRewardDefinition.CoinReward(15),
                ChallengeRewardDefinition.ExpReward(10)
            ),
            weight = 120
        ),
        ChallengeDefinition(
            id = "xp_30",
            title = "XP warmup",
            description = "Earn 30 XP to keep the rhythm going.",
            icon = "xp",
            type = ChallengeType.XP_EARNED,
            targetValue = 30,
            rewards = listOf(
                ChallengeRewardDefinition.CoinReward(20),
                ChallengeRewardDefinition.ExpReward(20)
            ),
            weight = 55
        ),
        ChallengeDefinition(
            id = "xp_50",
            title = "XP glow",
            description = "Earn 50 XP while building consistency.",
            icon = "xp",
            type = ChallengeType.XP_EARNED,
            targetValue = 50,
            rewards = listOf(
                ChallengeRewardDefinition.CoinReward(30),
                ChallengeRewardDefinition.ExpReward(25)
            ),
            weight = 80
        ),
        ChallengeDefinition(
            id = "xp_100",
            title = "XP burst",
            description = "Earn 100 XP over a relaxed stretch.",
            icon = "xp",
            type = ChallengeType.XP_EARNED,
            targetValue = 100,
            rewards = listOf(
                ChallengeRewardDefinition.CoinReward(50),
                ChallengeRewardDefinition.ExpReward(40),
                ChallengeRewardDefinition.ChestReward(ChestType.NORMAL.name)
            ),
            weight = 35
        ),
        ChallengeDefinition(
            id = "coins_20",
            title = "Coin crumbs",
            description = "Collect 20 coins from gentle progress.",
            icon = "coins",
            type = ChallengeType.COINS_EARNED,
            targetValue = 20,
            rewards = listOf(
                ChallengeRewardDefinition.ExpReward(20),
                ChallengeRewardDefinition.CoinReward(20)
            ),
            weight = 100
        ),
        ChallengeDefinition(
            id = "coins_50",
            title = "Coin stash",
            description = "Collect 50 coins without rushing.",
            icon = "coins",
            type = ChallengeType.COINS_EARNED,
            targetValue = 50,
            rewards = listOf(
                ChallengeRewardDefinition.ExpReward(35),
                ChallengeRewardDefinition.CoinReward(40)
            ),
            weight = 65
        ),
        ChallengeDefinition(
            id = "chest_1",
            title = "Open a chest",
            description = "Open 1 available reward chest.",
            icon = "chest",
            type = ChallengeType.CHEST_OPENED,
            targetValue = 1,
            rewards = listOf(
                ChallengeRewardDefinition.ExpReward(35),
                ChallengeRewardDefinition.CoinReward(35)
            ),
            availability = ChallengeAvailability.HAS_CHEST_AVAILABLE,
            weight = 55
        ),
        ChallengeDefinition(
            id = "customization_unlock_1",
            title = "Unlock a look",
            description = "Unlock 1 customization item from your available options.",
            icon = "customization",
            type = ChallengeType.CUSTOMIZATION_UNLOCKED,
            targetValue = 1,
            rewards = listOf(
                ChallengeRewardDefinition.ExpReward(30),
                ChallengeRewardDefinition.CoinReward(30)
            ),
            availability = ChallengeAvailability.HAS_PURCHASABLE_CUSTOMIZATION,
            weight = 55
        ),
        ChallengeDefinition(
            id = "equip_outfit",
            title = "Try an outfit",
            description = "Equip any owned outfit once.",
            icon = "outfit",
            type = ChallengeType.CUSTOMIZATION_EQUIPPED,
            targetValue = 1,
            rewards = listOf(
                ChallengeRewardDefinition.ExpReward(25),
                ChallengeRewardDefinition.CoinReward(25)
            ),
            availability = ChallengeAvailability.HAS_OWNED_OUTFIT_TO_EQUIP,
            weight = 65
        ),
        ChallengeDefinition(
            id = "equip_aura",
            title = "Light an aura",
            description = "Equip any owned aura once.",
            icon = "aura",
            type = ChallengeType.CUSTOMIZATION_EQUIPPED,
            targetValue = 1,
            rewards = listOf(
                ChallengeRewardDefinition.ExpReward(30),
                ChallengeRewardDefinition.CoinReward(30)
            ),
            availability = ChallengeAvailability.HAS_OWNED_AURA_TO_EQUIP,
            weight = 45
        ),
        ChallengeDefinition(
            id = "change_background",
            title = "Change the scene",
            description = "Equip a different owned background.",
            icon = "background",
            type = ChallengeType.CUSTOMIZATION_EQUIPPED,
            targetValue = 1,
            rewards = listOf(
                ChallengeRewardDefinition.ExpReward(25),
                ChallengeRewardDefinition.CoinReward(25)
            ),
            availability = ChallengeAvailability.HAS_OWNED_BACKGROUND_TO_EQUIP,
            weight = 65
        ),
        ChallengeDefinition(
            id = "streak_2",
            title = "Two-day rhythm",
            description = "Reach a 2-day streak with your habits.",
            icon = "streak",
            type = ChallengeType.STREAK,
            targetValue = 2,
            rewards = listOf(
                ChallengeRewardDefinition.CoinReward(30),
                ChallengeRewardDefinition.ExpReward(25)
            ),
            weight = 70
        ),
        ChallengeDefinition(
            id = "streak_3",
            title = "Three-day rhythm",
            description = "Reach a 3-day streak and keep the rhythm soft.",
            icon = "streak",
            type = ChallengeType.STREAK,
            targetValue = 3,
            rewards = listOf(
                ChallengeRewardDefinition.CoinReward(45),
                ChallengeRewardDefinition.ExpReward(40)
            ),
            weight = 40
        )
    )

    private val byId: Map<String, ChallengeDefinition> = definitions.associateBy { it.id }

    fun definition(id: String): ChallengeDefinition? = byId[id]

    fun firstAvailableFallback(): ChallengeDefinition =
        definitions.first { it.availability == ChallengeAvailability.ALWAYS }
}

fun ChallengeRewardDefinition.rewardLabel(): String = when (this) {
    is ChallengeRewardDefinition.CoinReward -> "+$amount coins"
    is ChallengeRewardDefinition.ExpReward -> "+$amount EXP"
    is ChallengeRewardDefinition.ChestReward -> {
        val label = chestType.lowercase().replaceFirstChar { it.uppercase() }
        "$label chest"
    }
    is ChallengeRewardDefinition.CustomizationReward ->
        EquipableConfig.definition(equipableId)?.name ?: "Customization"
}
