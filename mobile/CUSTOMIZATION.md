# Customization System

## Purpose

Customization lets players personalize their dragon without changing habit completion, XP, coins, or progression balance. The system is centralized through `InventoryItemEntity` and the existing `InventoryItemRepository`, so rewards, shop purchases, collection display, and equipped state all flow through the same data path.

## Item Types

The app uses three customization types:

- `OUTFIT`: a single dragon outfit layer.
- `BACKGROUND`: a scene background behind the dragon.
- `AURA`: a glow layer behind the dragon.

Type constants live in `domain/CustomizationTypes.kt`.

## Persistence

Customization items are stored in `inventory_items`:

- `id`: local database row id.
- `item_id`: stable item identifier used by rendering and migration.
- `type`: `OUTFIT`, `BACKGROUND`, or `AURA`.
- `name`: display name.
- `imageUrl`: expected asset path.
- `rarity`: `NORMAL`, `RARE`, `EPIC`, or `LEGENDARY`.
- `price`: shop price in coins.
- `isUnlocked`: whether the item is visible/purchasable in the shop.
- `isPurchased`: ownership state.
- `isEquipped`: cached equipped state.
- `unlock_source`: `SHOP` or `CHEST`.

The equipped state is stored on `PetEntity`:

- `equipped_outfit`
- `equipped_background`
- `equipped_aura`

Only one item can be equipped per type. Equipping a new item clears the previous item of that same type.

## Rendering

`AnimatedPet` renders customization layers in this order:

1. Aura placeholder, if `equipped_aura` is set.
2. Background image, if `equipped_background` is set.
3. Dragon phase image.
4. Outfit placeholder, if `equipped_outfit` is set.

Current placeholder drawables:

- `res/drawable/outfit_placeholder.xml`
- `res/drawable/aura_placeholder.xml`

Backgrounds use existing drawable assets and are mapped by `item_id`.

## Asset Naming Convention

Expected final PNG assets should use lowercase underscore names:

- Outfits: `res/drawable/outfit_<item_id>.png`
- Auras: `res/drawable/aura_<item_id>.png`
- Backgrounds: `res/drawable/background_<item_id>.png`

Until final art is available, the app uses:

- `outfit_placeholder.xml` for all outfit rewards.
- `aura_placeholder.xml` for all aura rewards.
- Existing background drawables for background rewards.

## Collection Screen

The bottom navigation tab is named `Collection`. It displays:

- Owned and locked tabs.
- Rarity filter.
- Type filter for outfits, backgrounds, and auras.
- Unlock source for each item.
- Shop purchase for unlocked items.
- Equip action for owned items.

## Reward Flow

Chest rewards use `ChestRewardConfig.customizationRarity` and `customizationDropChance`. When a customization reward is rolled, `InventoryItemRepository.getUnownedItemsByRarity` selects from unpurchased items and prefers locked items first. The selected item is granted through `InventoryItemRepository.grantItem`.

## Default Items

The database initializer seeds 12 customization items:

- 4 outfits.
- 4 backgrounds.
- 4 auras.

Some legendary items are chest-locked and are not purchasable until granted by a reward.
