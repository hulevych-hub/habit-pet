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

Runtime code should not hardcode achievement conditions or rewards.

## Achievement flow

1. `AchievementDatabaseInitializer` syncs config-defined achievement rows with persisted progress rows on startup.
2. `AchievementEngine` observes habit, streak, completion, XP, level, and customization collection state.
3. When a configured threshold is reached, the matching achievement row is marked unlocked while remaining unclaimed.
4. The player opens the Achievements screen and claims unlocked rewards.
5. `AchievementViewModel.claimAchievement(...)` calls `AchievementEngine.claimAchievement(...)`.
6. `AchievementRewardProcessor` processes the configured reward list for that achievement.
7. Reward UI is queued through `RewardQueue` and emitted through `RewardEventBus`.

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
- XP milestone achievements must not reward XP.
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

`AchievementReward.ExpReward` remains supported by the processor for backward compatibility, but the current achievement configuration no longer uses XP rewards for progression milestones.

Customization rewards must reference stable `EquipableConfig` IDs and use `unlockSource = "ACHIEVEMENT"`.

## Achievement rebalance summary

### XP achievements

XP achievements now reward coins and/or chests instead of XP.

| Achievement | Condition | Old reward | New reward |
|---|---:|---|---|
| `1000 XP` | `1000 XP` | `150 coins` | `150 coins` |
| `3000 XP` | `3000 XP` | `150 XP` | `300 coins` |
| `5000 XP` | `5000 XP` | `300 XP` | `350 coins` |
| `7500 XP` | `7500 XP` | `Epic chest + 500 coins` | `Rare chest + 350 coins` |
| `10000 XP` | `10000 XP` | `Epic chest + 600 coins` | `Epic chest + 450 coins` |
| `15000 XP` | `15000 XP` | `Epic chest + 800 coins` | `Epic chest + 600 coins` |
| `25000 XP` | `25000 XP` | `Legendary chest + 1000 coins` | `Legendary chest + 700 coins` |

### Habit and completion achievements

| Achievement | Condition | Old reward | New reward |
|---|---:|---|---|
| First Habit | `1 habit` | `50 coins` | `50 coins` |
| 3 Habit Builder | `3 habits` | `100 coins` | `90 coins` |
| 5 Habit Builder | `5 habits` | `150 coins` | `120 coins` |
| 10 Habit Builder | `10 habits` | `300 coins` | `250 coins` |
| 15 Habit Builder | `15 habits` | `400 coins` | `350 coins` |
| 20 Habit Builder | `20 habits` | `600 coins` | `500 coins` |
| 25 Completions | `25 completions` | `100 coins` | `100 coins` |
| 100 Completions | `100 completions` | `200 coins` | `200 coins` |
| 250 Completions | `250 completions` | `300 coins` | `300 coins` |
| 500 Completions | `500 completions` | `600 coins` | `500 coins` |
| 1000 Completions | `1000 completions` | `Epic chest + 1000 coins` | `Epic chest + 800 coins` |

### Streak achievements

| Achievement | Condition | Old reward | New reward |
|---|---:|---|---|
| 7 Day Streak | `7 days` | `100 coins` | `100 coins` |
| 14 Day Streak | `14 days` | `150 coins` | `150 coins` |
| 30 Day Streak | `30 days` | `250 coins` | `400 coins` |
| 60 Day Streak | `60 days` | `Epic chest + 600 coins` | `Epic chest + 600 coins` |
| 100 Day Streak | `100 days` | `Legendary chest + 1000 coins` | `Legendary chest + 800 coins` |

### Level achievements

| Achievement | Condition | Old reward | New reward |
|---|---:|---|---|
| Level 5 | `level 5` | `100 coins` | `80 coins` |
| Level 10 | `level 10` | `300 coins` | `300 coins` |
| Level 15 | `level 15` | `300 coins` | `350 coins` |
| Level 25 | `level 25` | `500 coins` | `500 coins` |
| Level 40 | `level 40` | `800 coins` | `800 coins` |
| Level 50 | `level 50` | `Epic chest + 1000 coins` | `Rare chest + 700 coins` |
| Level 60 | `level 60` | `Legendary chest + 1200 coins` | `Legendary chest + 900 coins` |

### Customization achievements

Collection milestones count every unique equipable in `EquipableConfig`. The current catalog has 16 unique equipables, so final collection achievements use `16 owned`.

| Achievement | Condition | Old reward | New reward |
|---|---:|---|---|
| First Customization | `1 owned` | `75 coins` | `60 coins` |
| 3 Customizations | `3 owned` | `150 coins` | `150 coins` |
| Customization Collector | `5 owned` | `Rare chest + 50 coins` | `Rare chest + 50 coins` |
| Crystal Crown | `7 owned` | `Epic chest` | `Epic chest` |
| 8 Customizations | `8 owned` | `Rare chest + 200 coins` | `Rare chest + 200 coins` |
| 10 Customization Spark | `10 owned` | `Epic chest` | `Rare chest` |
| 10 Customizations | `10 owned` | `Epic chest + 300 coins` | `Epic chest + 300 coins` |
| 11 Customization Hoard | `11 owned` | `Epic chest` | `Legendary chest` |
| 11 Customizations | `11 owned` | `Epic chest + 250 coins` | `Epic chest + 250 coins` |
| 16 Customization Milestone | `16 owned` | `12 owned → Epic chest` | `Epic chest` |
| 16 Customizations | `16 owned` | `12 owned → Legendary chest + 300 coins` | `Legendary chest + 200 coins` |
| Celestial Realm | `16 owned` | `12 owned → Legendary chest` | `Legendary chest` |
| Celestial Finale | `16 owned` | `12 owned → Sakura Aura` | `Legendary chest + 200 coins` |

### Achievement-only customization rewards

These rewards grant specific locked equipables that are not purchasable with coins. Their requirements are non-customization milestones so the displayed condition is not the same item as the reward:

- `FIRST_AURA_GLOW` → Sakura Aura (`10 completions`)
- `COZY_OUTFIT` → Royal Outfit (`25 completions`)
- `FOREST_BACKGROUND` → Forest Background (`2500 XP`)
- `CRYSTAL_AURA` → Icy Aura (`100 completions`)
- `CRYSTAL_CAVE` → Beach Background (`level 25`)
- `STARLIGHT_ARMOR` → Adventure Outfit (`level 40`)

## Reward balance intent

- Early achievements give small coin payouts to keep onboarding rewarding.
- XP achievements reward coins/chests, not XP, so they do not bypass progression.
- Chest rewards are milestone-based and become stronger only for long-term achievements.
- High-rarity chests are reserved for rare milestones like `25000 XP`, `100 Day Streak`, `16 Customizations`, `Celestial Realm`, `Celestial Finale`, and `Level 60`.
- Customization rewards are tied to stable `EquipableConfig` IDs, not display names.
- If the equipable catalog changes, update collection achievement targets and docs so "all customizations" matches the actual unique count.

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
