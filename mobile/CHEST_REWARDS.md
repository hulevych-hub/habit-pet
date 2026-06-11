# CHEST_REWARDS

## Overview

Chest rewards are reward events in Habit Pet that players collect to receive coins, EXP, and occasional customization items. They are awarded from streak milestones, level-ups, and achievement chest rewards, with different chest types offering varying reward potential.

## Current Implementation

The chest reward system consists of:
- Chest reward events (`RewardUiEvent.ChestReward`) with a source string, coin amount, EXP amount, and optional customization item ID
- Two UI presentations: interactive chest opening in `RewardScreen` and simple chest with collect button in `RewardOverlay`
- Priority-based queuing in `RewardQueue` (priority level 4)
- Automatic processing of collected coins, EXP, and customization items via `RewardManager`
- Sources:
  - Streak milestone rewards
  - Level-up bonuses
  - Achievement chest rewards
- Four chest types: Normal, Rare, Epic, and Legendary
- **Centralized configuration: `EconomyConfig` + `ChestRewardConfigProvider`**

## Rules

### Chest Reward Structure
- `rewardType: String` - identifies the source/reason for the chest
- `amount: Any` - the coin reward content (stored as `Any` for backward compatibility)
- `expAmount: Int` - the EXP reward amount
- `customizationId: Long?` - the ID of the customization item granted, if any

### Chest Reward Sources

1. **Streak Milestone Chests** (`StreakEngine.kt`):
   - Awarded when the player crosses configured streak milestones
   - Current milestones: 7, 14, 30, 60, 100
   - Condition: `currentStreak >= milestone && milestone > lastStreakAwardedAt`
   - Chest type is milestone-based, not random:
     - 7 days → Normal
     - 14 days → Rare
     - 30 or 60 days → Epic
     - 100 days → Legendary
   - Reward type: `"global_streak_{streak}_{chest_type}"`
   - Rewards are based on the milestone chest type configuration from `EconomyConfig`

2. **Level Up Chests** (`HabitDetailViewModel.kt`):
   - Awarded every time the pet levels up
   - Chest type is randomly determined using `EconomyConfig` probabilities
   - Reward type: `"level_up_{chest_type}"`
   - Rewards are based on the selected chest type configuration

3. **Achievement Chests** (`RewardManager.kt`):
   - Awarded when an achievement grants a chest reward
   - Chest type is read from `AchievementEntity.rewardChestType`
   - Reward type: `"achievement_{chest_type}"`
   - Rewards are based on the selected chest type configuration

### Reward Queue Priority
Chest rewards have priority level 4 in the RewardQueue:
- LevelUpReward: 1 (highest priority)
- DragonEvolutionReward: 2
- StreakReward: 3
- ChestReward: 4
- AchievementReward: 5 (lowest priority)

### Reward Processing
When a chest reward is collected:
- RewardManager extracts coin amount: `(current.amount as? Int) ?: 0` and adds it to player statistics
- RewardManager adds EXP amount to the pet's total EXP: `current.expAmount`
- RewardManager grants customization item by marking it as purchased: `inventoryItemRepository.grantItem(current.customizationId)`
- Non-Int coin amounts result in 0 coins awarded

### Customization Grant Logic
- Chest customization rewards target a rarity, not a specific type
- Selection uses `inventoryItemRepository.getUnownedItemsByRarity()`
- The DAO query prefers locked items first and then unowned purchased-eligible items:
  - `isPurchased = 0`
  - ordered by `isUnlocked ASC, type, name`
- If all target-rarity items are already purchased, the chest falls back to coin/EXP rewards
- Duplicate purchased rewards are avoided whenever possible

### Outfit, Background, and Aura Reward Probabilities

The current implementation is type-neutral. Chests target a rarity, then randomly choose among available unpurchased items of that rarity.

Default seeded inventory contains one outfit, one background, and one aura per rarity. Therefore, before any target-rarity item has been purchased:

- Outfit reward chance within a customization drop: 1/3
- Background reward chance within a customization drop: 1/3
- Aura reward chance within a customization drop: 1/3

Across randomized level-up chests, each type has the same expected chance because the rarity distribution is symmetric and each rarity has one item of each type.

## Configuration

Chest reward values are configured through `ChestRewardConfigProvider`, which sources values from `EconomyConfig`:

### Coin Ranges (from `EconomyConfig`)
- **Normal Chest**: 10-30 coins
- **Rare Chest**: 30-80 coins
- **Epic Chest**: 80-180 coins
- **Legendary Chest**: 180-400 coins

### EXP Ranges (from `EconomyConfig`)
- **Normal Chest**: 0-0 EXP
- **Rare Chest**: 50-150 EXP
- **Epic Chest**: 150-350 EXP
- **Legendary Chest**: 350-800 EXP

### Customization Drop Chances (from `EconomyConfig`)
- **Rare Chest**: 15% chance for a Rare customization item
- **Epic Chest**: 30% chance for an Epic customization item
- **Legendary Chest**: 50% chance for a Legendary customization item
- **Normal Chest**: 0% (no customization item drops)

### Chest Type Probabilities (from `EconomyConfig`)

Used by level-up and achievement chests:

- **Normal**: 55%
- **Rare**: 30%
- **Epic**: 12%
- **Legendary**: 3%

Streak milestone chests use the fixed milestone mapping instead of these probabilities.

## Data Model

No persistent data model for chest rewards; they are transient events.
The `RewardUiEvent.ChestReward` data class defines the structure:
- `rewardType: String` - identifies the source and chest type
- `amount: Any` - coin reward (backward compatibility)
- `expAmount: Int` - EXP reward amount
- `customizationId: Long?` - granted customization item ID (null if none)

## Source Files

- app/src/main/java/com/example/mobile/domain/ChestType.kt
- app/src/main/java/com/example/mobile/domain/ChestRewardConfig.kt
- app/src/main/java/com/example/mobile/domain/ChestRewardConfigProvider.kt
- app/src/main/java/com/example/mobile/domain/EconomyConfig.kt (centralized economy values)
- app/src/main/java/com/example/mobile/domain/StreakEngine.kt
- app/src/main/java/com/example/mobile/presentation/viewmodel/HabitDetailViewModel.kt
- app/src/main/java/com/example/mobile/domain/repository/InventoryItemRepository.kt
- app/src/main/java/com/example/mobile/data/repository/InventoryItemRepositoryImpl.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardQueue.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardManager.kt
- app/src/main/java/com/example/mobile/presentation/ui/events/RewardUiEvent.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardScreen.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardOverlay.kt

## Known Gaps

1. **Visual Differentiation**: All chest rewards use the same chest image regardless of chest type; future work could add visual differentiation based on `chestType`.
2. **Particle Effects**: No particle effects or animations specific to chest rewards beyond the basic opening animation.
3. **Customization Name Display**: Reward UI shows a generic customization message instead of the actual item name because reward UI does not load inventory names.
4. **Configuration Flexibility**: While a configuration system exists, values are still hardcoded in `EconomyConfig`; future work could load configurations from remote sources or allow tuning.
5. **Type-Specific Drop Tables**: The current system targets rarity only. Outfit, background, and aura probabilities are balanced by inventory composition rather than explicit per-type drop tables.

## Balance Validation

### Expected Value Per Randomized Chest (Average)

| Chest Type | Probability | Avg Coins | Avg EXP | Customization EV* |
|------------|-------------|-----------|---------|-------------------|
| Normal | 55% | 20 | 0 | 0 |
| Rare | 30% | 55 | 100 | 0.15 × Rare customization item |
| Epic | 12% | 130 | 250 | 0.30 × Epic customization item |
| Legendary | 3% | 290 | 575 | 0.50 × Legendary customization item |

\*Customization EV = Drop chance × customization value (approx. coin equivalent: Rare=300, Epic=800, Legendary=2000)

### Combined Expected Value

- **Weighted avg coins per randomized chest**: ~52 coins
- **Weighted avg EXP per randomized chest**: ~77 EXP
- **Weighted customization item value per randomized chest**: ~72 coin-equivalent
- **Combined randomized chest value**: ~52 coins + ~77 EXP + ~72 coin-equivalent

### Progression Impact

- Level-up chest every level → ~52 coins + ~77 EXP average
- Streak milestone chests are milestone-based and become stronger at larger milestones
- With 3 checkbox habits/day plus recurring level-up and streak chests, early-game economy stays near the ~100 coins/day target
- Duplicate-preventing selection keeps customization rewards from inflating coin-equivalent value after items are already purchased
