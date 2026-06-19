# Customization System

## Purpose

Customization lets players personalize their dragon without changing habit completion, XP, coins, or progression balance. The system is centralized through `EquipableConfig`, `InventoryItemEntity`, and the existing `InventoryItemRepository`, so rewards, shop purchases, collection display, and equipped state all flow through the same data path.

## Item Types

The app uses three customization types:

- `OUTFIT`: a single dragon outfit layer.
- `BACKGROUND`: a scene background behind the dragon.
- `AURA`: a glow layer behind the dragon.

Type constants live in `domain/CustomizationTypes.kt`. Stable equipable IDs, display names, nullable phase metadata, nullable shop price, rarity, and unlock source live in `domain/EquipableConfig.kt`.

## Persistence

Customization items are stored in `inventory_items`:

- `id`: local database row id.
- `item_id`: stable item identifier used by rendering and migration.
- `type`: `OUTFIT`, `BACKGROUND`, or `AURA`.
- `name`: display name.
- `imageUrl`: expected asset path.
- `rarity`: `NORMAL`, `RARE`, `EPIC`, or `LEGENDARY`.
- `price`: shop price in coins. Config price is nullable for non-shop items; the database stores `0` for those rows.
- `isUnlocked`: whether the item is visible/purchasable in the shop.
- `unlock_source`: `SHOP`, `CHEST`, or `ACHIEVEMENT`.
- `isPurchased`: ownership state.
- `isEquipped`: cached equipped state.

The equipped state is stored on `PetEntity`:

- `equipped_outfit`
- `equipped_background`
- `equipped_aura`

Only one item can be equipped per type. Equipping a new item clears the previous item of that same type. The Rewards screen derives the equipped badge from `PetEntity` so cached `InventoryItemEntity.isEquipped` values do not hide currently equipped items.

## Rendering

`AnimatedPet` renders customization layers through `AssetResolver` in this order:

1. Background image from `backgrounds/`, if `equipped_background` is set and the asset exists.
2. Dragon base image from the current phase folder:
   - If `equipped_aura` is set, resolve the aura through `EquipableConfig`. If the aura has a configured phase, load `<aura>_aura` from that phase folder; if phase is `null`, search all dragon phase folders. The aura image already includes the dragon and aura.
   - Otherwise, load `default` from the phase folder.
3. Outfit overlay from `<outfit>_outfit`. If the outfit has a configured phase, use that phase folder; if phase is `null`, search all dragon phase folders.

Missing assets fall back without crashing:

1. Dragon base falls back to the phase `default`.
2. Missing backgrounds are skipped.
3. Missing outfits are skipped.
4. Missing auras fall back to the phase `default`.

Missing asset lookups are logged with `AssetResolver` / `AssetRenderer` for debugging.

## Asset Naming Convention

Packaged assets live under `res/drawable/` subfolders and are exposed to runtime asset loading through the Gradle `assets.srcDirs += "src/main/res/drawable"` configuration.

Dragon phase folders:

- `egg/`
- `hatchling/`
- `young_dragon/`
- `adult_dragon/`
- `ancient_dragon/`

Background folder:

- `backgrounds/`

Each phase folder contains the assets available for that phase:

- `default` is the transparent-base dragon appearance for that phase. The resolver also accepts a phase-named default alias if one exists, so legacy assets do not need to be renamed.
- `*_outfit` files are outfit overlays. The item name is everything before `_outfit`.
- `*_aura` files are dragon-plus-aura images. If an aura has a configured phase, it is phase-specific; if phase is `null`, the resolver searches all phase folders.

Backgrounds are loaded by file name from `backgrounds/`. Outfits and auras use the configured equipable ID, nullable phase metadata, and asset suffix from `EquipableConfig`; `AssetResolver` then resolves the matching packaged asset file.

## Collection Screen

The Rewards collection screen is opened from the shared header coin amount for the locked tab and from the Pet screen Attribute Card edit icon for the owned tab. It displays:

- Owned and locked tabs.
- Rarity filter.
- Type filter for outfits, backgrounds, and auras.
- Unlock source for each item.
- Shop purchase for unlocked items.
- Equip action for owned items.

## Reward Flow

Chest rewards use `ChestRewardConfig.customizationRarity` and `customizationDropChance`. When a customization reward is rolled, `InventoryItemRepository.getUnownedItemsByRarity` selects from unpurchased items and prefers locked items first. Achievement-only items (`unlockSource = "ACHIEVEMENT"`) are excluded from chest rolls. The selected item is granted through `InventoryItemRepository.grantItem`, and the queued reward carries the stable `equipableId` so the reward popup can show the actual item name.

## Default Items

The database initializer synchronizes `EquipableConfig` entries into `inventory_items` on startup. Existing player-owned, purchased, and equipped states are preserved; catalog metadata such as name, type, asset path, price, rarity, unlock source, and `isUnlocked` is refreshed from config. New config entries are inserted automatically.

Current equipables are defined in `EquipableConfig`. The catalog currently has 20 unique equipables:

- Outfits: `wizard_outfit`, `adventure_outfit`, `knight_outfit`, `ninja_outfit`, `royal_outfit`, `fairy_outfit`, `icy_outfit`.
- Auras: `sakura_aura`, `fire_aura`, `frost_aura`, `shadow_aura`, `celestial_aura`.
- Backgrounds: `background_forest`, `background_majestic`, `background_volcanic`, `background_sakura`, `background_icelandic`, `background_beach`, `background_mountains`, `background_night_sky`.

Unlock source examples:
- Shop: `wizard_outfit`, `background_majestic`, `background_volcanic`, `background_sakura`, `background_icelandic`.
- Chest: `knight_outfit`, `ninja_outfit`, `fairy_outfit`, `fire_aura`, `shadow_aura`, `background_mountains`, `background_night_sky`.
- Achievement: `royal_outfit`, `icy_outfit`, `sakura_aura`, `frost_aura`, `celestial_aura`, `background_forest`, `background_beach`, `adventure_outfit`.

A `phase = null` value means the item is not phase-specific and can be equipped at any dragon phase. Backgrounds currently use `phase = null`. Shop-sourced equipables are immediately purchasable only when `price` is not `null`. Chest-sourced and achievement-only equipables keep `price = null`, remain visible as locked catalog items, and are not purchasable with coins.
