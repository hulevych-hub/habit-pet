# Chest Rewards

## Overview

Chest rewards are reward events that players collect to receive coins, EXP, and occasional customization items. They are awarded from streak milestones, level-ups, achievements, and surprise habit rewards.

Chests should feel exciting, but they must not break long-term progression pacing.

## Source of truth

Chest reward values are configured through:

- `EconomyConfig.kt`
- `ChestRewardConfigProvider.kt`
- `ChestRewardFactory.kt`

Runtime reward construction should use `ChestRewardFactory.buildChestReward(...)` instead of hardcoding chest contents.

## Chest reward structure

`RewardUiEvent.ChestReward` contains:

- `rewardType: String` - identifies the source/reason for the chest
- `amount: Any` - coin reward content, retained for backward compatibility
- `expAmount: Int` - EXP reward amount
- `customizationId: Long?` - legacy local database item ID
- `equipableId: String?` - stable `EquipableConfig` item ID granted, if any

## Chest reward sources

### 1. Streak milestone chests

Awarded when the player crosses configured streak milestones:

- 7 days → Normal chest
- 14 days → Rare chest
- 30 or 60 days → Epic chest
- 100 days → Legendary chest

Streak milestone chests use fixed chest types rather than random chest probabilities.

### 2. Level-up chests

Awarded every time the pet levels up. Chest type is randomly determined using `EconomyConfig` probabilities:

| Chest | Old probability | New probability |
|---|---:|---:|
| Normal | `55%` | `65%` |
| Rare | `30%` | `25%` |
| Epic | `12%` | `8%` |
| Legendary | `3%` | `2%` |

Level-up chests are queued as `RewardUiEvent.LevelUpReward` after the pet XP/level state is updated.

### 3. Achievement chests

Awarded when an achievement grants `AchievementReward.ChestReward`. The chest type is read from `AchievementsConfig`.

Achievement chest rewards are intentionally milestone-based. XP achievements no longer award XP directly, so achievements do not bypass progression pacing.

### 4. Surprise habit reward chests

Awarded only when the surprise reward trigger succeeds after a successful habit completion.

| Setting | Old value | New value |
|---|---:|---:|
| Minimum completions before another roll | None | `3` |
| Surprise chest chance | `10%` per completion | `4%` after cooldown |
| Surprise Rare probability | `80%` | `85%` |
| Surprise Epic probability | `18%` | `13%` |
| Surprise Legendary probability | implied `2%` | implied `2%` |

The cooldown is session-scoped in `HabitDetailViewModel` and `HabitsViewModel`.

## Reward queue priority

Chest rewards have priority level `4` in the `RewardQueue`:

1. `LevelUpReward`: `1`
2. `DragonEvolutionReward`: `2`
3. `StreakReward`: `3`
4. `ChestReward`: `4`
5. `AchievementReward`: `5`

## Reward processing

When a chest reward is collected:

1. `RewardManager` extracts the coin amount and adds it to player statistics.
2. `RewardManager` adds EXP to the pet.
3. `RewardManager` updates pet level and evolution stage from the new XP.
4. If the new level or evolution stage increases, `RewardManager` queues level-up or evolution reward events.
5. If the chest contains a customization item, `RewardManager` grants it through `InventoryItemRepository.grantItemByItemId(...)`.

## Customization grant logic

Chest customization rewards target a rarity, not a specific type.

- Selection uses `inventoryItemRepository.getUnownedItemsByRarity()`.
- The DAO query prefers locked items first and then unowned purchasable items.
- Chest rewards exclude `unlockSource = "ACHIEVEMENT"` items because those are granted only by achievement reward flow.
- If all target-rarity items are already purchased or achievement-only, the chest falls back to coin/EXP rewards.
- Duplicate purchased rewards are avoided whenever possible.

Default chest-sourced inventory:

- Rare: `fire_aura`
- Epic: `knight_outfit`, `background_mountains`
- Legendary: `ninja_outfit`, `background_night_sky`

## Chest contents

### Coin ranges

| Chest | Old range | New range |
|---|---:|---:|
| Normal | `10-30` | `5-15` |
| Rare | `30-80` | `15-35` |
| Epic | `80-180` | `35-75` |
| Legendary | `180-400` | `75-150` |

### EXP ranges

| Chest | Old range | New range |
|---|---:|---:|
| Normal | `0-0` | `0-0` |
| Rare | `50-150` | `8-20` |
| Epic | `150-350` | `20-50` |
| Legendary | `350-800` | `50-100` |

### Customization drop chances

| Chest | Old chance | New chance |
|---|---:|---:|
| Normal | `0%` | `0%` |
| Rare | `15%` | `8%` |
| Epic | `30%` | `18%` |
| Legendary | `50%` | `35%` |

## Expected value

| Chest type | Probability | Avg coins | Avg EXP | Customization EV* |
|---|---:|---:|---:|---:|
| Normal | `65%` | `10` | `0` | `0` |
| Rare | `25%` | `25` | `14` | `32` coin-equivalent |
| Epic | `8%` | `55` | `35` | `180` coin-equivalent |
| Legendary | `2%` | `112.5` | `75` | `1050` coin-equivalent |

\*Customization EV = drop chance × customization price (`Rare=400`, `Epic=1000`, `Legendary=3000`).

Weighted randomized chest value:

- Avg coins: `19.4`
- Avg EXP: `7.8`
- Customization EV: about `43.4` coin-equivalent

## Surprise chest expected value

Surprise chests use a special rarity distribution:

| Chest type | Probability | Avg coins | Avg EXP | Customization EV |
|---|---:|---:|---:|---:|
| Rare | `85%` | `25` | `14` | `32` coin-equivalent |
| Epic | `13%` | `55` | `35` | `180` coin-equivalent |
| Legendary | `2%` | `112.5` | `75` | `1050` coin-equivalent |

Approximate surprise chest EV:

- Coins: `30.65`
- EXP: `17.95`
- Customization EV: about `71.5` coin-equivalent

Because surprise chests require a 3-completion cooldown and a 4% chance roll, their long-term economy impact stays small.

## Balance validation

- Level-up chest every level → about `19.4 coins` + `7.8 EXP` + `43.4` coin-equivalent customization value on average.
- Streak milestone chests are milestone-based and become stronger at larger milestones.
- Surprise chests are rare, rate-limited, and always Rare/Epic/Legendary.
- Legendary rewards remain rare and memorable.
- Normal rewards remain useful without inflating progression.
- Chest EXP is capped at `100 XP` even for Legendary chests.
- Chests do not award evolution coins or XP milestones that bypass the configured progression curve.

## Known gaps

1. Visual differentiation: all chest rewards use the same chest image regardless of chest type.
2. Particle effects: chest rewards do not yet have chest-type-specific particle effects.
3. Type-specific drop tables: chests target rarity only; outfit/background/aura odds depend on inventory composition.
4. Configuration flexibility: values are still centralized in `EconomyConfig`, not remotely tunable.
