# CHEST_REWARDS

## Overview

Chest rewards are a type of reward event in Habit Pet that players can collect to receive various items, including coins, EXP, and accessories. They are awarded for reaching streak milestones and upon leveling up the pet, with different chest types offering varying reward potentials.

## Current Implementation

The chest reward system consists of:
- Chest reward events (`RewardUiEvent.ChestReward`) with a type string, amount, EXP amount, and optional accessory ID
- Two UI presentations: interactive chest opening in `RewardScreen` and simple chest with collect button in `RewardOverlay`
- Priority-based queuing in `RewardQueue` (priority level 4)
- Automatic processing of collected coins, EXP, and accessories via `RewardManager`
- Two sources: streak milestone rewards and level-up bonuses
- Four chest types: Normal, Rare, Epic, and Legendary, each with different reward potentials
- **Centralized configuration: `EconomyConfig` + `ChestRewardConfigProvider`**

## Rules

### Chest Reward Structure
- `rewardType: String` - identifies the source/reason for the chest (e.g., "7_day_streak_normal", "level_up_rare")
- `amount: Any` - the coin reward content (for backward compatibility)
- `expAmount: Int` - the EXP reward amount
- `accessoryId: Long?` - the ID of the accessory granted (if any)

### Chest Reward Sources

1. **Streak Milestone Chests** (StreakEngine.kt):
   - Awarded when completing 7-day streaks (7, 14, 21, ... days)
   - Condition: `currentStreak >= 7 && currentStreak % 7 == 0 && currentStreak > lastStreakAwardedAt`
   - Chest type is randomly determined (from `EconomyConfig` probabilities)
   - Reward type: `"7_day_streak_{chest_type}"` (e.g., "7_day_streak_normal")
   - Rewards based on chest type configuration (from `EconomyConfig`):
     - **Normal (55%)**: 10-30 coins, no EXP, no accessory
     - **Rare (30%)**: 30-80 coins, 50-150 EXP, 15% chance for Rare accessory
     - **Epic (12%)**: 80-180 coins, 150-350 EXP, 30% chance for Epic accessory
     - **Legendary (3%)**: 180-400 coins, 350-800 EXP, 50% chance for Legendary accessory

2. **Level Up Chests** (HabitDetailViewModel.kt):
   - Awarded every time the pet levels up (unconditional)
   - Chest type is randomly determined (from `EconomyConfig` probabilities)
   - Reward type: `"level_up_{chest_type}"` (e.g., "level_up_epic")
   - Rewards based on chest type configuration (same as streak chests above)

### Reward Queue Priority
Chest rewards have priority level 4 in the RewardQueue:
- LevelUpReward: 1 (highest priority)
- DragonEvolutionReward: 2
- StreakReward: 3
- ChestReward: 4
- AchievementReward: 5 (lowest priority)

### Reward Processing
When a chest reward is collected:
- RewardManager extracts coin amount: `(current.amount as? Int) ?: 0` and adds to player statistics
- RewardManager adds EXP amount to pet's total EXP: `current.expAmount`
- RewardManager grants accessory by marking it as purchased: `inventoryItemRepository.grantItem(current.accessoryId)`
- Non-Int coin amounts result in 0 coins awarded

### Accessory Grant Logic
- Only grants accessories **not already owned** (uses `inventoryItemRepository.getUnownedItemsByType()`)
- Prioritizes unowned items of the chest's target rarity
- If all accessories of that rarity are owned, falls back to coin/EXP rewards
- Avoids duplicate rewards whenever possible

## Configuration

Chest reward values are configured through `ChestRewardConfigProvider` which sources from `EconomyConfig`:

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

### Accessory Drop Chances (from `EconomyConfig`)
- **Rare Chest**: 15% chance for Rare accessory
- **Epic Chest**: 30% chance for Epic accessory
- **Legendary Chest**: 50% chance for Legendary accessory
- **Normal Chest**: 0% (no accessory drops)

### Chest Type Probabilities (from `EconomyConfig`)
- **Normal**: 55%
- **Rare**: 30%
- **Epic**: 12%
- **Legendary**: 3%

## Data Model

No persistent data model for chest rewards; they are transient events.
The `RewardUiEvent.ChestReward` data class defines the structure:
- `rewardType: String` - identifies the source and chest type
- `amount: Any` - coin reward (backward compatibility)
- `expAmount: Int` - EXP reward amount
- `accessoryId: Long?` - granted accessory ID (null if none)

## Source Files

- app/src/main/java/com/example/mobile/domain/ChestType.kt
- app/src/main/java/com/example/mobile/domain/ChestRewardConfig.kt
- app/src/main/java/com/example/mobile/domain/ChestRewardConfigProvider.kt
- app/src/main/java/com/example/mobile/domain/EconomyConfig.kt (NEW - centralized economy values)
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
3. **Accessory Name Display**: UI shows "Accessory!" instead of the actual accessory name due to complexity of accessing inventory names in reward UI.
4. **Configuration Flexibility**: While a configuration system exists, values are still hardcoded in `EconomyConfig`; future work could load configurations from remote sources or allow tuning.

## Balance Validation

### Expected Value Per Chest (Average)

| Chest Type | Probability | Avg Coins | Avg EXP | Accessory EV* |
|------------|-------------|-----------|---------|---------------|
| Normal | 55% | 20 | 0 | 0 |
| Rare | 30% | 55 | 100 | 0.15 × Rare accessory |
| Epic | 12% | 130 | 250 | 0.30 × Epic accessory |
| Legendary | 3% | 290 | 575 | 0.50 × Legendary accessory |

*Accessory EV = Drop chance × Accessory value (approx. coin equivalent: Rare=300, Epic=800, Legendary=2000)

### Combined Expected Value
- **Weighted avg coins per chest**: ~52 coins
- **Weighted avg EXP per chest**: ~68 EXP
- **Weighted accessory value per chest**: ~45 coin-equivalent

### Progression Impact
- Level-up chest every level → ~52 coins + ~68 EXP average
- 7-day streak chest → ~52 coins + ~68 EXP average
- With 3 habits/day + streaks → ~1 chest every 2-3 days
- Sustainable economy with accessory spending