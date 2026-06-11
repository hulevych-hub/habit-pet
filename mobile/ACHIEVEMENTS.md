# ACHIEVEMENTS

## Overview

The achievements system gives players long-term goals across habit creation, habit completion, streaks, XP, pet levels, and customization collection. Achievements unlock automatically when tracked game milestones are reached, then appear as claimable rewards in the Achievements screen.

## Current Implementation

Achievements are persisted in Room and monitored by `AchievementEngine`. The flow is:

1. `AchievementEngine` observes habit, streak, completion, XP, level, and customization collection state.
2. `StreakEngine` observes daily all-habits completion and emits global streak milestone events.
3. When a threshold is reached, the matching achievement is marked unlocked.
4. The player opens the Achievements screen and claims unlocked rewards.
5. `AchievementViewModel` calls `AchievementEngine.claimAchievement(...)`.
6. The engine queues a `RewardUiEvent.AchievementReward`.
7. `RewardManager` processes the reward through the centralized reward system.

Achievement rewards can be:
- Coins
- EXP
- Chest rewards
- Coin + chest follow-up rewards

Global streak milestones are separate from claimable achievements. They trigger immediately through `StreakEngine`, display an immersive celebration screen, and then flow into the centralized chest reward pipeline.

## Achievement Entity Structure

Each achievement has the following properties:

- `id: Long` - unique identifier
- `name: String` - achievement title
- `description: String` - requirement description
- `icon: String` - icon identifier
- `targetValue: Int` - milestone value that must be reached or exceeded
- `rewardCoins: Int` - coin reward
- `rewardExp: Int` - EXP reward
- `rewardChestType: String?` - optional chest type, such as `rare`
- `isUnlocked: Boolean` - milestone completion status
- `isClaimed: Boolean` - whether the player has claimed the reward
- `unlockedDate: Long?` - timestamp when the achievement was unlocked

## Default Achievements

The game initializes with predefined achievements:

1. **First Habit**
   - Target: 1 habit created
   - Reward: 50 coins
   - Unlock condition: habit count >= 1

2. **3 Habit Builder**
   - Target: 3 habits created
   - Reward: 100 coins
   - Unlock condition: habit count >= 3

3. **7 Day Streak**
   - Target: 7-day streak
   - Reward: 100 coins
   - Unlock condition: current streak >= 7

4. **30 Day Streak**
   - Target: 30-day streak
   - Reward: 250 coins
   - Unlock condition: current streak >= 30

5. **100 Completions**
   - Target: 100 habit completions
   - Reward: 200 coins
   - Unlock condition: total completions >= 100

6. **1000 XP**
   - Target: 1000 XP earned
   - Reward: 150 coins
   - Unlock condition: pet XP >= 1000

7. **5000 XP**
   - Target: 5000 XP earned
   - Reward: 300 EXP
   - Unlock condition: pet XP >= 5000

8. **Level 10**
   - Target: reach level 10
   - Reward: 300 coins
   - Unlock condition: pet level >= 10

9. **Level 25**
   - Target: reach level 25
   - Reward: 500 coins
   - Unlock condition: pet level >= 25

10. **First Customization**
    - Target: unlock 1 customization item
    - Reward: 75 coins
    - Unlock condition: purchased customization count >= 1

11. **Customization Collector**
    - Target: unlock 5 customization items
    - Reward: Rare chest
    - Unlock condition: purchased customization count >= 5

## Achievement Unlocking

`AchievementEngine` monitors:

- Habit count
- Current streak
- Total habit completions
- Pet XP
- Pet level
- Purchased customization count across outfits, backgrounds, and auras

When a condition is met, the engine:

1. Checks if the corresponding achievement is already unlocked.
2. If not, updates the achievement record with `isUnlocked = true` and `unlockedDate`.
3. Leaves `isClaimed = false` so the reward can be claimed from the Achievements screen.

## Claiming Flow

Unlocked achievements are claimable from `AchievementScreen`.

When the player taps **Claim**:

1. `AchievementViewModel.claimAchievement(...)` calls `AchievementEngine.claimAchievement(...)`.
2. The engine verifies that the achievement is unlocked and not already claimed.
3. The achievement is marked `isClaimed = true`.
4. A reward event is queued:
   - Coins and/or EXP are shown directly in the achievement reward popup.
   - If the achievement has `rewardChestType`, claiming also queues a chest reward after the achievement popup completes.

## Reward Processing

`RewardManager` processes achievement rewards through the centralized reward pipeline:

- `rewardCoins` are added with `statisticsRepository.addCoins(...)`.
- `expAmount` is added to the current pet.
- `chestType` creates a follow-up `RewardUiEvent.ChestReward`.
- Chest rewards use `ChestRewardConfigProvider` and may grant coins, EXP, and customization items through `InventoryItemRepository`.

This keeps achievement rewards inside the same reward flow used by streaks, level-ups, evolutions, and chest openings.

## UI

`AchievementScreen` displays:

- Total unlocked progress
- Number of achievements ready to claim
- Per-achievement progress bars
- Current value vs target value
- Reward preview
- Claim button for unlocked, unclaimed achievements
- Claimed or locked status for non-claimable achievements

## Configuration

Achievement definitions are initialized in:

- `app/src/main/java/com/example/mobile/data/local/database/AchievementDatabaseInitializer.kt`

Coin values reuse `EconomyConfig` where defined. Chest rewards use `ChestType` and `ChestRewardConfigProvider`.

## Data Model

**AchievementEntity** (`app/src/main/java/com/example/mobile/data/local/entities/AchievementEntity.kt`):

- Table: `achievements`
- Columns: `id`, `name`, `description`, `icon`, `targetValue`, `rewardCoins`, `rewardExp`, `rewardChestType`, `isUnlocked`, `isClaimed`, `unlockedDate`

## Source Files

- `app/src/main/java/com/example/mobile/data/local/entities/AchievementEntity.kt`
- `app/src/main/java/com/example/mobile/data/local/database/AchievementDatabaseInitializer.kt`
- `app/src/main/java/com/example/mobile/data/local/dao/AchievementDao.kt`
- `app/src/main/java/com/example/mobile/data/local/database/AppDatabase.kt`
- `app/src/main/java/com/example/mobile/data/repository/AchievementRepositoryImpl.kt`
- `app/src/main/java/com/example/mobile/domain/repository/AchievementRepository.kt`
- `app/src/main/java/com/example/mobile/domain/AchievementEngine.kt`
- `app/src/main/java/com/example/mobile/domain/StreakEngine.kt`
- `app/src/main/java/com/example/mobile/presentation/viewmodel/AchievementViewModel.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/reward/RewardScreen.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/screens/AchievementScreen.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/events/RewardUiEvent.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/reward/RewardQueue.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/reward/RewardManager.kt`
- `app/src/main/java/com/example/mobile/di/DatabaseModule.kt`
- `app/src/main/java/com/example/mobile/HabitPetApp.kt`

## Known Gaps

1. Achievement definitions are still static and initialized locally.
2. Achievement conditions use simple thresholds rather than compound rules.
3. Achievement icons are stored but the UI currently uses generic check/lock/trophy icons.
4. Achievements are shown as one list rather than grouped by type or difficulty.
5. Unlock feedback uses the standard reward popup rather than a dedicated achievement animation or notification.
