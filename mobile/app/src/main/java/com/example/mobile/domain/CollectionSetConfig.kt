package com.example.mobile.domain

object CollectionSetConfig {

    const val SAKURA_SET = "sakura_set"
    const val CRYSTAL_SET = "crystal_set"
    const val ROYAL_SET = "royal_set"
    const val VOLCANIC_SET = "volcanic_set"
    const val SHADOW_SET = "shadow_set"

    data class SetBonus(
        val coins: Int = 0,
        val expAmount: Int = 0,
        val titleId: String? = null,
        val frameId: String? = null
    )

    data class SetDefinition(
        val id: String,
        val name: String,
        val description: String,
        val itemIds: List<String>,
        val bonus: SetBonus
    )

    val sets: List<SetDefinition> = listOf(
        SetDefinition(
            id = SAKURA_SET,
            name = "Sakura Set",
            description = "The cherry blossom collection",
            itemIds = listOf(
                EquipableConfig.ROYAL_OUTFIT,
                EquipableConfig.SAKURA_AURA,
                EquipableConfig.BACKGROUND_SAKURA
            ),
            bonus = SetBonus(coins = 200, titleId = PetTitleConfig.SET_COLLECTOR)
        ),
        SetDefinition(
            id = CRYSTAL_SET,
            name = "Crystal Set",
            description = "The frozen crystal collection",
            itemIds = listOf(
                EquipableConfig.KNIGHT_OUTFIT,
                EquipableConfig.FROST_AURA,
                EquipableConfig.BACKGROUND_ICELANDIC
            ),
            bonus = SetBonus(coins = 300, frameId = AvatarFrameConfig.FROST)
        ),
        SetDefinition(
            id = ROYAL_SET,
            name = "Royal Set",
            description = "The majestic royal collection",
            itemIds = listOf(
                EquipableConfig.ROYAL_OUTFIT,
                EquipableConfig.BACKGROUND_MAJESTIC
            ),
            bonus = SetBonus(coins = 150, frameId = AvatarFrameConfig.GOLDEN)
        ),
        SetDefinition(
            id = VOLCANIC_SET,
            name = "Volcanic Set",
            description = "The fiery volcanic collection",
            itemIds = listOf(
                EquipableConfig.NINJA_OUTFIT,
                EquipableConfig.FIRE_AURA,
                EquipableConfig.BACKGROUND_VOLCANIC
            ),
            bonus = SetBonus(coins = 400, frameId = AvatarFrameConfig.FLAME)
        ),
        SetDefinition(
            id = SHADOW_SET,
            name = "Shadow Set",
            description = "The dark shadow collection",
            itemIds = listOf(
                EquipableConfig.SHADOW_AURA,
                EquipableConfig.BACKGROUND_NIGHT_SKY
            ),
            bonus = SetBonus(coins = 250, frameId = AvatarFrameConfig.SHADOW)
        )
    )

    fun setById(id: String): SetDefinition? = sets.firstOrNull { it.id == id }

    fun findSetsContainingItem(itemId: String): List<SetDefinition> =
        sets.filter { it.itemIds.contains(itemId) }
}
