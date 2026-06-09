# CHEST_REWARDS

## Overview

Chest rewards are a type of reward event in Habit Pet that players can collect to receive various items, primarily coins. They are awarded for reaching streak milestones and upon leveling up the pet.

## Current Implementation

The chest reward system consists of:
- Chest reward events (`RewardUiEvent.ChestReward`) with a type string and amount
- Two UI presentations: interactive chest opening in `RewardScreen` and simple chest with collect button in `RewardOverlay`
- Priority-based queuing in `RewardQueue` (priority level 4)
- Automatic processing of collected coins via `RewardManager`
- Two sources: streak milestone rewards and level-up bonuses

## Rules

### Chest Reward Structure
- `rewardType: String` - identifies the source/reason for the chest (e.g., "7_day_streak", "Level up")
- `amount: Any` - the reward content; currently used for:
  - `Int`: coin amount
  - `String`: message or item description (displayed as-is)

### Chest Reward Sources

1. **Streak Milestone Chests** (StreakEngine.kt):
   - Awarded when completing 7-day streaks (7, 14, 21, ... days)
   - Condition: `currentStreak >= 7 && currentStreak % 7 == 0 && currentStreak > lastStreakAwardedAt`
   - Reward type: `"7_day_streak"`
   - Amount: `50` (as Int, representing 50 coins)

2. **Level Up Chests** (HabitDetailViewModel.kt):
   - Awarded every time the pet levels up (unconditional)
   - Reward type: `"Level up"`
   - Amount: `20` (as Int, representing 20 coins)

### Reward Queue Priority
Chest rewards have priority level 4 in the RewardQueue:
- LevelUpReward: 1 (highest priority)
- DragonEvolutionReward: 2
- StreakReward: 3
- ChestReward: 4
- AchievementReward: 5 (lowest priority)

### Reward Processing
When a chest reward is collected:
- RewardManager extracts coin amount: `(current.amount as? Int) ?: 0`
- Coins are added to player statistics via `statisticsRepository.addCoins()`
- Non-Int amounts result in 0 coins awarded

## Configuration

All chest reward values are hardcoded:
- Streak milestone chest coins: 50 (StreakEngine.kt line 51)
- Level up chest coins: 20 (HabitDetailViewModel.kt line 332)
- Reward type strings: "7_day_streak" and "Level up"
- Chest reward priority: 4 (RewardQueue.kt line 21)

## Data Model

No persistent data model for chest rewards; they are transient events.
The `RewardUiEvent.ChestReward` data class defines the structure:
- `rewardType: String`
- `amount: Any`

## Source Files

- app/src/main/java/com/example/mobile/domain/StreakEngine.kt
- app/src/main/java/com/example/mobile/presentation/viewmodel/HabitDetailViewModel.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardQueue.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardManager.kt
- app/src/main/java/com/example/mobile/presentation/ui/events/RewardUiEvent.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardScreen.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardOverlay.kt

## Known Gaps

1. **Limited Reward Types**: Despite `amount` being typed as `Any` to support flexible rewards, only coin amounts (Int) and strings are currently used.
2. **Unconditional Level Up Chests**: Chest rewards are granted on every level up without any probability or condition, which may not align with intended game balance.
3. **Generic UI Presentation**: All chest rewards use the same chest image regardless of reward type; no visual differentiation based on `rewardType`.
4. **Underutilized Type System**: The `rewardType` string is displayed in UI but not used for any gameplay mechanics or special effects.
5. **Missing Visual Feedback**: No particle effects or animations specific to chest rewards beyond the basic opening animation.