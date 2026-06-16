package com.example.mobile.domain

import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.Rarity

object UnlockSources {
    const val SHOP = "SHOP"
    const val CHEST = "CHEST"
    const val ACHIEVEMENT = "ACHIEVEMENT"
}

enum class EquipableType(val value: String) {
    OUTFIT("OUTFIT"),
    AURA("AURA"),
    BACKGROUND("BACKGROUND");

    companion object {
        fun fromValue(value: String): EquipableType? =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
    }
}

enum class DragonPhase(val folderName: String) {
    EGG("egg"),
    HATCHLING("hatchling"),
    YOUNG_DRAGON("young_dragon"),
    ADULT_DRAGON("adult_dragon"),
    ANCIENT_DRAGON("ancient_dragon")
}

data class EquipableDefinition(
    val id: String,
    val name: String,
    val type: EquipableType,
    val phase: DragonPhase? = null,
    val drawableName: String,
    val rarity: Rarity,
    val price: Int? = null,
    val unlockSource: String = UnlockSources.SHOP,
    val metadata: Map<String, String> = emptyMap()
) {
    val isPurchasable: Boolean = unlockSource == UnlockSources.SHOP && price != null
    val isUnlocked: Boolean = isPurchasable

    val imageUrl: String = when (type) {
        EquipableType.BACKGROUND -> "backgrounds/$drawableName"
        EquipableType.OUTFIT,
        EquipableType.AURA -> phase?.let { "${it.folderName}/$drawableName" } ?: drawableName
    }

    fun toInventoryEntity(
        isPurchased: Boolean = false,
        isEquipped: Boolean = false,
        isUnlockedOverride: Boolean? = null
    ): InventoryItemEntity = InventoryItemEntity(
        itemId = id,
        name = name,
        type = type.value,
        imageUrl = imageUrl,
        isUnlocked = isUnlockedOverride ?: isUnlocked,
        isPurchased = isPurchased,
        isEquipped = isEquipped,
        price = price ?: 0,
        rarity = rarity,
        unlockSource = unlockSource
    )
}

object EquipableConfig {
    const val WIZARD_OUTFIT = "wizard_outfit"
    const val ADVENTURE_OUTFIT = "adventure_outfit"
    const val KNIGHT_OUTFIT = "knight_outfit"
    const val NINJA_OUTFIT = "ninja_outfit"
    const val ROYAL_OUTFIT = "royal_outfit"

    const val SAKURA_AURA = "sakura_aura"
    const val FIRE_AURA = "fire_aura"
    const val ICY_AURA = "icy_aura"

    const val BACKGROUND_BEACH = "beach"
    const val BACKGROUND_MOUNTAINS = "mountains"
    const val BACKGROUND_NIGHT_SKY = "night"

    const val BACKGROUND_MAJESTIC = "majestic"
    const val BACKGROUND_SAKURA = "sakura"
    const val BACKGROUND_VOLCANIC = "volcanic"
    const val BACKGROUND_FOREST = "forest"
    const val BACKGROUND_ICELANDIC = "icelandic"

    val equipables: List<EquipableDefinition> = listOf(
        EquipableDefinition(
            id = WIZARD_OUTFIT,
            name = "Wizard Outfit",
            type = EquipableType.OUTFIT,
            phase = DragonPhase.YOUNG_DRAGON,
            drawableName = WIZARD_OUTFIT,
            rarity = Rarity.NORMAL,
            price = EconomyConfig.customizationPrice(Rarity.NORMAL),
            unlockSource = UnlockSources.SHOP
        ),
        EquipableDefinition(
            id = ADVENTURE_OUTFIT,
            name = "Adventure Outfit",
            type = EquipableType.OUTFIT,
            phase = DragonPhase.YOUNG_DRAGON,
            drawableName = ADVENTURE_OUTFIT,
            rarity = Rarity.RARE,
            price = null,
            unlockSource = UnlockSources.ACHIEVEMENT
        ),
        EquipableDefinition(
            id = KNIGHT_OUTFIT,
            name = "Knight Outfit",
            type = EquipableType.OUTFIT,
            phase = DragonPhase.YOUNG_DRAGON,
            drawableName = KNIGHT_OUTFIT,
            rarity = Rarity.EPIC,
            price = null,
            unlockSource = UnlockSources.CHEST
        ),
        EquipableDefinition(
            id = NINJA_OUTFIT,
            name = "Ninja Outfit",
            type = EquipableType.OUTFIT,
            phase = DragonPhase.YOUNG_DRAGON,
            drawableName = NINJA_OUTFIT,
            rarity = Rarity.LEGENDARY,
            price = null,
            unlockSource = UnlockSources.CHEST
        ),
        EquipableDefinition(
            id = ROYAL_OUTFIT,
            name = "Royal Outfit",
            type = EquipableType.OUTFIT,
            phase = DragonPhase.YOUNG_DRAGON,
            drawableName = ROYAL_OUTFIT,
            rarity = Rarity.EPIC,
            price = null,
            unlockSource = UnlockSources.ACHIEVEMENT
        ),
        EquipableDefinition(
            id = SAKURA_AURA,
            name = "Sakura Aura",
            type = EquipableType.AURA,
            phase = DragonPhase.YOUNG_DRAGON,
            drawableName = SAKURA_AURA,
            rarity = Rarity.NORMAL,
            price = null,
            unlockSource = UnlockSources.ACHIEVEMENT
        ),
        EquipableDefinition(
            id = FIRE_AURA,
            name = "Fire Aura",
            type = EquipableType.AURA,
            phase = DragonPhase.YOUNG_DRAGON,
            drawableName = FIRE_AURA,
            rarity = Rarity.RARE,
            price = null,
            unlockSource = UnlockSources.CHEST
        ),
        EquipableDefinition(
            id = ICY_AURA,
            name = "Icy Aura",
            type = EquipableType.AURA,
            phase = DragonPhase.YOUNG_DRAGON,
            drawableName = ICY_AURA,
            rarity = Rarity.EPIC,
            price = null,
            unlockSource = UnlockSources.ACHIEVEMENT
        ),
        EquipableDefinition(
            id = BACKGROUND_FOREST,
            name = "Forest Background",
            type = EquipableType.BACKGROUND,
            phase = null,
            drawableName = BACKGROUND_FOREST,
            rarity = Rarity.NORMAL,
            price = null,
            unlockSource = UnlockSources.ACHIEVEMENT
        ),
        EquipableDefinition(
            id = BACKGROUND_MAJESTIC,
            name = "Majestic Background",
            type = EquipableType.BACKGROUND,
            phase = null,
            drawableName = BACKGROUND_MAJESTIC,
            rarity = Rarity.NORMAL,
            price = EconomyConfig.customizationPrice(Rarity.NORMAL),
            unlockSource = UnlockSources.SHOP
        ),
        EquipableDefinition(
            id = BACKGROUND_VOLCANIC,
            name = "Forest Background",
            type = EquipableType.BACKGROUND,
            phase = null,
            drawableName = BACKGROUND_VOLCANIC,
            rarity = Rarity.NORMAL,
            price = EconomyConfig.customizationPrice(Rarity.NORMAL),
            unlockSource = UnlockSources.SHOP
        ),
        EquipableDefinition(
            id = BACKGROUND_SAKURA,
            name = "Sakura Background",
            type = EquipableType.BACKGROUND,
            phase = null,
            drawableName = BACKGROUND_SAKURA,
            rarity = Rarity.NORMAL,
            price = EconomyConfig.customizationPrice(Rarity.NORMAL),
            unlockSource = UnlockSources.SHOP
        ),
        EquipableDefinition(
            id = BACKGROUND_ICELANDIC,
            name = "Icelandic Background",
            type = EquipableType.BACKGROUND,
            phase = null,
            drawableName = BACKGROUND_ICELANDIC,
            rarity = Rarity.NORMAL,
            price = EconomyConfig.customizationPrice(Rarity.NORMAL),
            unlockSource = UnlockSources.SHOP
        ),
        EquipableDefinition(
            id = BACKGROUND_BEACH,
            name = "Beach Background",
            type = EquipableType.BACKGROUND,
            phase = null,
            drawableName = BACKGROUND_BEACH,
            rarity = Rarity.RARE,
            price = null,
            unlockSource = UnlockSources.ACHIEVEMENT
        ),
        EquipableDefinition(
            id = BACKGROUND_MOUNTAINS,
            name = "Mountains Background",
            type = EquipableType.BACKGROUND,
            phase = null,
            drawableName = BACKGROUND_MOUNTAINS,
            rarity = Rarity.EPIC,
            price = null,
            unlockSource = UnlockSources.CHEST
        ),
        EquipableDefinition(
            id = BACKGROUND_NIGHT_SKY,
            name = "Night Sky Background",
            type = EquipableType.BACKGROUND,
            phase = null,
            drawableName = BACKGROUND_NIGHT_SKY,
            rarity = Rarity.LEGENDARY,
            price = null,
            unlockSource = UnlockSources.CHEST
        )
    )

    private val byId: Map<String, EquipableDefinition> = equipables.associateBy { it.id }

    fun definition(id: String): EquipableDefinition? = byId[id]

    fun definitionsByType(type: EquipableType): List<EquipableDefinition> =
        equipables.filter { it.type == type }

    fun displayName(type: EquipableType): String = when (type) {
        EquipableType.OUTFIT -> "Outfits"
        EquipableType.BACKGROUND -> "Backgrounds"
        EquipableType.AURA -> "Auras"
    }

    fun inventoryEntities(): List<InventoryItemEntity> =
        equipables.map { it.toInventoryEntity() }
}
