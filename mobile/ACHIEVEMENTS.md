# Achievements

## Overview

The achievements system gives players long-term goals across habit creation, habit completion, streaks, XP, pet levels, and customization collection. Achievement definitions are centralized in `AchievementsConfig`; the Room table stores only player progress and claim state.

## Source of truth

`AchievementsConfig.kt` is the source of truth for:

- Achievement IDs
- Names
- Descriptions
- Icons
- Progress sources
- Target values
- Rewards
- Display difficulty order through `difficultyRank`

Runtime code should not hardcode achievement conditions or rewards.

## Achievement flow

1. `AchievementDatabaseInitializer` syncs config-defined achievement rows with persisted progress rows on startup.
2. `AchievementEngine` observes habit, streak, completion, XP, level, and customization collection state.
3. When a configured threshold is reached, the matching achievement row is marked unlocked while remaining unclaimed.
4. The player opens the Achievements screen and claims unlocked rewards.
5. `AchievementViewModel.claimAchievement(...)` calls `AchievementEngine.claimAchievement(...)`.
6. `AchievementRewardProcessor` validates and prepares the configured reward list for that achievement, then marks the achievement claimed.
7. Prepared rewards are queued through `RewardQueue` and applied once by `RewardManager` in the centralized reward pipeline.

Achievement rewards can be:

- Coins
- Chest rewards
- Customization item grants for achievement-only equipables
- Multiple reward types on the same achievement

Global streak milestones are separate from claimable achievements. They trigger immediately through `StreakEngine`, display an immersive celebration screen, and then flow into the centralized chest reward pipeline.

## Reward rules

Achievement rewards should be meaningful without bypassing progression.

Important rule:

- Achievement conditions must not be paid back with the same progression resource.
- XP milestone achievements must not reward direct XP.
- Coin milestone achievements should reward coins, chests, or cosmetics, but not enough to collapse saving goals.

Examples:

| Bad pattern | Better pattern |
|---|---|
| Reach `1000 XP` → reward `1000 XP` | Reach `1000 XP` → reward coins or chest |
| Collect customization → reward the same customization | Collect customization count → reward coins/chest, or reward a later locked item |

## Current reward types

`AchievementReward` supports:

- `AchievementReward.CoinReward(amount)`
- `AchievementReward.ChestReward(chestType)`
- `AchievementReward.CustomizationReward(equipableId, type)`

`AchievementReward.ExpReward` remains supported by the processor for backward compatibility, but the current achievement configuration does not use direct XP rewards for progression milestones. When present, it is applied by `RewardManager` so level and evolution rewards are recalculated.

Customization rewards must reference stable `EquipableConfig` IDs and use `unlockSource = "ACHIEVEMENT"`.

## Validated order and rewards

The Achievements screen is sorted by `difficultyRank` from `AchievementsConfig.kt`. The current target values were kept; only the display order and selected reward bundles were adjusted. Tied requirements are grouped together, with lighter rewards before richer customization or chest bundles.

| Rank | Achievement | Condition | Rewards |
|---:|---|---:|---|
| 1 | `First Habit` | `1 habit` | `50 coins` |
| 2 | `First Customization` | `1 owned customization` | `60 coins` |
| 3 | `First Aura Glow` | `10 completions` | `Sakura Aura` |
| 4 | `3 Habit Builder` | `3 habits` | `Normal chest + 90 coins` |
| 5 | `5 Habit Builder` | `5 habits` | `Normal chest + 120 coins` |
| 6 | `3 Customizations` | `3 owned customizations` | `Normal chest + 150 coins` |
| 7 | `25 Completions` | `25 completions` | `Normal chest + 100 coins` |
| 8 | `Royal Outfit` | `25 completions` | `Royal Outfit` |
| 9 | `Level 5` | `level 5` | `Normal chest + 80 coins` |
| 10 | `7 Day Streak` | `7-day streak` | `Normal chest + 100 coins` |
| 11 | `14 Day Streak` | `14-day streak` | `Normal chest + 150 coins` |
| 12 | `1000 XP` | `1000 XP` | `Normal chest + 150 coins` |
| 13 | `2500 XP` | `2500 XP` | `Normal chest + 200 coins` |
| 14 | `Forest Background` | `2500 XP` | `Normal chest + Forest Background` |
| 15 | `10 Habit Builder` | `10 habits` | `Normal chest + 250 coins` |
| 16 | `15 Habit Builder` | `15 habits` | `Normal chest + 350 coins` |
| 17 | `100 Completions` | `100 completions` | `Normal chest + 200 coins` |
| 18 | `Frost Aura` | `100 completions` | `Frost Aura` |
| 19 | `250 Completions` | `250 completions` | `Normal chest + 300 coins` |
| 20 | `Level 10` | `level 10` | `Normal chest + 300 coins` |
| 21 | `Level 15` | `level 15` | `Normal chest + 350 coins` |
| 22 | `3000 XP` | `3000 XP` | `Normal chest + 300 coins` |
| 23 | `30 Day Streak` | `30-day streak` | `Rare chest + 400 coins` |
| 24 | `Customization Collector` | `5 owned customizations` | `Rare chest + 50 coins` |
| 25 | `Crystal Crown` | `7 owned customizations` | `Epic chest` |
| 26 | `8 Customizations` | `8 owned customizations` | `Rare chest + 200 coins` |
| 27 | `5000 XP` | `5000 XP` | `Rare chest + 350 coins` |
| 28 | `20 Habit Builder` | `20 habits` | `Rare chest + 500 coins` |
| 29 | `500 Completions` | `500 completions` | `Rare chest + 500 coins` |
| 30 | `Beach Background` | `level 25` | `Normal chest + Beach Background` |
| 31 | `Level 25` | `level 25` | `Rare chest + 500 coins` |
| 32 | `Level 40` | `level 40` | `Rare chest + 800 coins` |
| 33 | `Adventure Outfit` | `level 40` | `Rare chest + Adventure Outfit` |
| 34 | `Level 50` | `level 50` | `Rare chest + 700 coins` |
| 35 | `7500 XP` | `7500 XP` | `Rare chest + 350 coins` |
| 36 | `10000 XP` | `10000 XP` | `Epic chest + 450 coins` |
| 37 | `15000 XP` | `15000 XP` | `Epic chest + 600 coins` |
| 38 | `60 Day Streak` | `60-day streak` | `Epic chest + 600 coins` |
| 39 | `10 Customization Spark` | `10 owned customizations` | `Rare chest` |
| 40 | `10 Customizations` | `10 owned customizations` | `Epic chest + 300 coins` |
| 41 | `11 Customization Hoard` | `11 owned customizations` | `Legendary chest` |
| 42 | `11 Customizations` | `11 owned customizations` | `Epic chest + 250 coins` |
| 43 | `19 Customization Milestone` | `19 owned customizations` | `Epic chest` |
| 44 | `19 Customizations` | `19 owned customizations` | `Legendary chest + 200 coins` |
| 45 | `Celestial Realm` | `19 owned customizations` | `Legendary chest` |
| 46 | `Celestial Finale` | `19 owned customizations` | `Legendary chest + 200 coins` |
| 47 | `1000 Completions` | `1000 completions` | `Epic chest + 800 coins` |
| 48 | `25000 XP` | `25000 XP` | `Legendary chest + 700 coins` |
| 49 | `100 Day Streak` | `100-day streak` | `Legendary chest + 800 coins` |
| 50 | `Level 60` | `level 60` | `Legendary chest + 900 coins` |

## Reward balance intent

- Early achievements still give small coin payouts, and many now add a Normal chest for a small non-progression reward.
- Mid-tier milestones use Rare chests, which can add EXP through the existing chest reward pipeline without directly paying back XP milestones.
- XP milestone achievements continue to reward coins/chests, not direct XP, so they do not bypass progression.
- High-rarity chests are reserved for rare milestones like `25000 XP`, `100 Day Streak`, `19 Customizations`, `Celestial Realm`, `Celestial Finale`, and `Level 60`.
- Customization rewards are tied to stable `EquipableConfig` IDs, not display names.
- If the equipable catalog changes, update collection achievement targets and docs so "all customizations" matches the actual unique count.

## Achievement-only customization rewards

These rewards grant specific locked equipables that are not purchasable with coins. Their requirements are non-customization milestones so the displayed condition is not the same item as the reward:

- `FIRST_AURA_GLOW` → Sakura Aura (`10 completions`)
- `COZY_OUTFIT` → Royal Outfit (`25 completions`)
- `FOREST_BACKGROUND` → Forest Background (`2500 XP`) plus a Normal chest
- `CRYSTAL_AURA` → Frost Aura (`100 completions`)
- `CRYSTAL_CAVE` → Beach Background (`level 25`) plus a Normal chest
- `STARLIGHT_ARMOR` → Adventure Outfit (`level 40`) plus a Rare chest

## Data model

**AchievementEntity** (`app/src/main/java/com/example/mobile/data/local/entities/AchievementEntity.kt`):

- Table: `achievements`
- Columns:
  - `id: String` - stable achievement identifier from `AchievementsConfig`
  - `progress: Int` - latest persisted progress for the configured source
  - `isUnlocked: Boolean` - milestone completion status
  - `isClaimed: Boolean` - whether the player has claimed the reward
  - `unlockedDate: Long?` - timestamp when the achievement was unlocked

Achievement metadata and reward definitions are not stored in the database. They are loaded from `AchievementsConfig`, which allows new achievements to be added without changing the Room schema.

## Known gaps

1. Achievement conditions use simple thresholds rather than compound rules.
2. Achievement icons are stored but the UI currently uses generic check/lock/trophy icons.
3. Achievements are shown as one list rather than grouped by type or difficulty.
4. Unlock feedback uses the standard reward popup rather than a dedicated achievement animation or notification.
5. Achievement-only customization rewards should continue to use non-customization conditions so claiming an achievement does not require already owning the rewarded item.
