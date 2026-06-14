# TODO - Habit Pet MVP

---

# 🧠 GENERAL EXECUTION RULES

For every task below:

1. Read the related documentation files before implementing.
2. Update related documentation files immediately after implementation.
3. Mark tasks as completed when finished.
4. Do not leave documentation outdated.
5. If implementation decisions are required, document them in the relevant `.md` file.
6. Complete tasks strictly in order.
7. Do not start a later section until the current section is fully completed.

---

The equipable asset system is currently inconsistent. Some assets exist in the project but do not appear in Rewards, Inventory, Chests, or Achievements.

I need a full audit and refactor to create a single, centralized equipable system.

Follow existing architecture and documentation. Do NOT invent package names, classes, or systems if equivalent implementations already exist.

Build and verify after changes.

---

# GOAL

Create a centralized `EquipableConfig` that becomes the SINGLE SOURCE OF TRUTH for all equipables in the game.

All systems must use it:

* Rewards screen
* Inventory
* Equip/unequip
* Chest rewards
* Achievements
* Dragon rendering
* Future reward systems

The system must allow adding new rewards later without changing database schema or business logic.

---

# CURRENT ASSETS

## Young Dragon Outfits

Location:

`app/src/main/res/drawable/young_dragon/`

Files:

* `wizard_outfit.jpg`
* `adventure_outfit.jpg`
* `knight_outfit.jpg`
* `ninja_outfit.jpg`
* `royal_outfit.jpg`

## Young Dragon Auras

Location:

`app/src/main/res/drawable/young_dragon/`

Files:

* `sakura_aura.jpg`
* `fire_aura.jpg`
* `icy_aura.jpg`

## Backgrounds

Location:

`app/src/main/res/drawable/backgrounds/`

Backgrounds should also be treated as equipables.

---

# TASK 1 - ROOT CAUSE ANALYSIS

**Status:** Completed. Root cause was fragmented equipable discovery/seeding plus achievement rewards referencing stale item IDs. The fix moved equipable definitions into `EquipableConfig`, synchronized them into inventory without overwriting player state, and routed chest/achievement rewards through stable equipable IDs.

Investigate why these assets do not appear in:

* Rewards screen
* Inventory
* Chest rewards
* Achievement rewards

Trace the complete flow:

Asset  Config  Database  Repository  ViewModel  UI

Validate:

* Asset discovery
* Drawable loading
* Database initialization
* Inventory creation
* Reward generation
* Filters
* Purchase state
* Unlock state
* Equip state
* Resource IDs
* Rendering

Fix the root cause.

Do NOT guess.

---

# TASK 2 - CREATE EQUIPABLE CONFIG

**Status:** Completed. `domain/EquipableConfig.kt` now defines all current outfits, auras, and backgrounds with stable IDs, names, types, nullable phases, drawable names, rarity, nullable shop price, unlock source, and metadata support.

Create a centralized configuration file:

`EquipableConfig.kt`

This becomes the SINGLE SOURCE OF TRUTH.

Each equipable definition should support:

* id (stable unique ID)
* name
* type

  * OUTFIT
  * AURA
  * BACKGROUND
* phase (nullable; `null` means usable at any dragon phase)

  * EGG
  * HATCHLING
  * YOUNG_DRAGON
  * ADULT_DRAGON
  * ANCIENT_DRAGON
* drawableName
* rarity
* price (nullable; `null` means not purchasable with coins)
* unlockSource (`SHOP`, `CHEST`, or `ACHIEVEMENT`)
* future metadata support

Example:

```kotlin
EquipableDefinition(
    id = "royal_outfit",
    name = "Royal Outfit",
    type = OUTFIT,
    phase = YOUNG_DRAGON,
    drawableName = "royal_outfit",
    rarity = EPIC,
    price = 800 // nullable; use null for non-coin rewards
)
```

Do NOT hardcode equipables anywhere else.

All systems must read from `EquipableConfig`.

---

# TASK 3 - DATABASE SYNCHRONIZATION

**Status:** Completed. `InventoryItemDatabaseInitializer` now syncs `EquipableConfig` entries into `inventory_items`, inserts missing equipables, stores `0` for nullable prices, and updates metadata while preserving purchased, equipped, and unlocked player state.

On app startup:

* Load all equipables from `EquipableConfig`
* Compare with database
* Insert missing equipables automatically
* Preserve player data:

  * purchased state
  * equipped state
  * unlock state

Never delete player progress.

Support future additions automatically.

Adding a new equipable to `EquipableConfig` should be sufficient for it to appear in-game.

---

# TASK 4 - CHEST REWARD INTEGRATION

**Status:** Completed. Chest rewards now carry `equipableId`, select unowned non-achievement items by rarity through the inventory repository, and grant through `grantItemByItemId`.

Refactor chest rewards to use `EquipableConfig`.

Current chest reward flow must:

* select unowned equipables
* avoid duplicates whenever possible
* filter by rarity
* support:

  * outfits
  * auras
  * backgrounds

Chest rewards must grant equipables by ID:

Example:

```kotlin
ChestReward(
    equipableId = "royal_outfit"
)
```

or dynamically select an equipable matching rarity rules.

No hardcoded item names.

No hardcoded IDs.

Update chest logic to use the centralized config.

Update documentation if behavior changes.

---

# TASK 5 - ACHIEVEMENT REWARD INTEGRATION

**Status:** Completed. `AchievementsConfig` uses `EquipableConfig` constants for customization rewards, `AchievementRewardProcessor` resolves rewards through `EquipableConfig` before granting them, and achievement-only sources are supported.

Achievements must also use equipable IDs.

Example:

```kotlin
CustomizationReward(
    equipableId = "fire_aura"
)
```

Achievement reward processing must resolve rewards through `EquipableConfig`.

No hardcoded item names.

No duplicated reward logic.

---

# TASK 6 - INVENTORY & REWARDS SCREEN

**Status:** Completed. Rewards screen filters now show all configured equipables, derives equipped badges from `PetEntity`, and reward popups resolve equipable names from `EquipableConfig`.

Ensure all equipables appear correctly in:

* Rewards screen
* Inventory
* Locked items
* Purchased items
* Equipped items

Verify the following assets appear:

Outfits:

* wizard_outfit
* adventure_outfit
* knight_outfit
* ninja_outfit
* royal_outfit

Auras:

* sakura_aura
* fire_aura
* icy_aura

Backgrounds:

* all assets inside `backgrounds/`

Support:

* unlock
* purchase
* equip
* unequip

---

# TASK 7 - DRAGON RENDERING

**Status:** Completed. `AssetResolver` uses nullable `EquipableConfig` phase metadata for aura and outfit lookups while preserving fallback behavior.

Rendering order:

1. Background
2. Dragon base
3. Aura
4. Outfit overlay

Default phase dragon:

* `default`

If aura equipped:

* use `<aura>_aura`

If outfit equipped:

* overlay `<outfit>_outfit`

Outfits have transparent backgrounds and should render above the aura image.

Example:

`fire_aura + royal_outfit`

renders:

Fire aura dragon base + Royal outfit overlay.

---

# TASK 8 - FALLBACKS

**Status:** Completed. Missing asset lookups remain non-fatal and `AssetResolver` logs missing files while falling back to configured/default behavior.

If an asset is missing:

Fallback order:

1. phase default
2. no aura
3. no outfit
4. no background

The app must never crash due to missing assets.

Log missing assets for debugging.

---

# TASK 9 - VALIDATION

**Status:** Completed. `./gradlew :app:assembleDebug` succeeds.

Verify:

* Equipables are loaded correctly
* Inventory is initialized correctly
* Chest rewards grant correct equipables
* Achievement customization rewards grant correct achievement-only equipables
* Chest-only equipables flow through the chest reward pipeline
* Rendering works correctly
* Existing player data is preserved
* Database migration remains valid
* Build succeeds

---

# TASK 10 - DOCUMENTATION

**Status:** Completed. Updated `ACCESSORIES.md`, `DRAGON_PHASES.md`, `CHEST_REWARDS.md`, `ACHIEVEMENTS.md`, `DATA_MODEL.md`, and `CUSTOMIZATION.md` to match nullable phase, nullable price, and unlock-source behavior.

Update all affected documentation:

* ACCESSORIES.md (or replacement file)
* CHEST_REWARDS.md
* ACHIEVEMENTS.md
* DATA_MODEL.md
* DRAGON_PHASES.md

Documentation must reflect actual implementation exactly.

Provide a final report:

* files changed
* files created
* migrations added
* documentation updated
* remaining issues (if any)
* missing assets (if any)

