# Accessories Legacy Note

The accessory slot system has been replaced by the customization system.

Use `CUSTOMIZATION.md` and `EquipableConfig` as the source of truth for current cosmetic item behavior, including nullable phase metadata, nullable shop price, and unlock source (`SHOP`, `CHEST`, or `ACHIEVEMENT`).

## What Changed

The old slot model used:

- `HAT`
- `GLASSES`
- `SCARF`
- `BACKGROUND`

The current model uses:

- `OUTFIT`
- `BACKGROUND`
- `AURA`

`PetEntity` now stores:

- `equippedOutfit`
- `equippedBackground`
- `equippedAura`

The old `equippedHat`, `equippedGlasses`, and `equippedScarf` columns are removed from the current Room schema. The `12 -> 13` migration maps an old equipped hat into `equipped_outfit` as a placeholder compatibility step.

## Current Source Files

- `CUSTOMIZATION.md`
- `domain/CustomizationTypes.kt`
- `domain/EquipableConfig.kt`
- `data/local/database/InventoryItemDatabaseInitializer.kt`
- `data/local/entities/InventoryItemEntity.kt`
- `data/local/entities/PetEntity.kt`
- `presentation/ui/screens/RewardsScreen.kt`
- `presentation/ui/components/AnimatedPet.kt`
- `util/AssetResolver.kt`
