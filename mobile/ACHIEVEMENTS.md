# ACHIEVEMENTS

## Overview

The achievements system gives players long-term goals across habit creation, habit completion, streaks, XP, pet levels, and customization collection. Achievement definitions are centralized in `AchievementsConfig`; the Room table stores only player progress and claim state.

## Current Implementation

Achievements are persisted in Room and monitored by `AchievementEngine`. The flow is:

1. `AchievementDatabaseInitializer` syncs config-defined achievement rows with persisted progress rows on startup.
2. `AchievementEngine` observes habit, streak, completion, XP, level, and customization collection state.
3. When a configured threshold is reached, the matching achievement row is marked unlocked while remaining unclaimed.
4. The player opens the Achievements screen and claims unlocked rewards.
5. `AchievementViewModel.claimAchievement(...)` calls `AchievementEngine.claimAchievement(...)`.
6. `AchievementRewardProcessor` processes the configured reward list for that achievement.
7. Reward UI is queued through `RewardQueue` and also emitted through `RewardEventBus`.

Achievement rewards can be:
- Coins
- EXP
- Chest rewards
- Customization item grants
- Multiple reward types on the same achievement

Global streak milestones are separate from claimable achievements. They trigger immediately through `StreakEngine`, display an immersive celebration screen, and then flow into the centralized chest reward pipeline.

## Achievement Configuration

`AchievementsConfig` is the source of truth for achievement metadata and rewards. Each definition includes:

- `id: String` - stable config identifier
- `name: String` - achievement title
- `description: String` - requirement description
- `icon: String` - icon identifier
- `progressSource: AchievementProgressSource` - tracked milestone source
- `targetValue: Int?` - threshold, nullable for instant-reward achievements
- `rewards: List<AchievementReward>` - configured reward list

Reward types are modeled as `AchievementReward`:

- `AchievementReward.CoinReward(amount)`
- `AchievementReward.ExpReward(amount)`
- `AchievementReward.ChestReward(chestType)`
- `AchievementReward.CustomizationReward(itemId, type)`

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
    - Reward: Rare chest + 50 coins
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

1. Finds the config definition by stable achievement ID.
2. Updates the persisted row with the latest progress.
3. Marks the achievement unlocked once progress reaches the configured target.
4. Leaves `isClaimed = false` so the reward can be claimed from the Achievements screen.

Once unlocked, an achievement remains unlocked even if the current live value later drops below the target.

## Claiming Flow

Unlocked achievements are claimable from `AchievementScreen`.

When the player taps **Claim**:

1. `AchievementViewModel.claimAchievement(...)` calls `AchievementEngine.claimAchievement(...)`.
2. The engine verifies that the achievement is unlocked and not already claimed.
3. `AchievementRewardProcessor` processes all configured rewards for the achievement inside one Room transaction.
4. The same transaction marks the achievement row `isClaimed = true`, so rewards and claim state succeed or fail together.
5. A configured `RewardUiEvent.AchievementReward` is queued for display and emitted through `RewardEventBus`.

Claims are serialized with a mutex to prevent duplicate processing from rapid taps.

## Reward Processing

`AchievementRewardProcessor` owns achievement reward execution:

- Coin rewards are added with `statisticsRepository.addCoins(...)`.
- EXP rewards are added to the current pet.
- Chest rewards are built through `ChestRewardFactory` and queued as `RewardUiEvent.ChestReward`.
- Customization rewards are granted by stable `itemId` through `InventoryItemRepository`.
- Reward UI is queued through `RewardQueue` and emitted through `RewardEventBus`.

`RewardManager` remains the centralized processor for queued non-achievement reward events and does not double-process achievement reward coins or EXP.

This keeps achievement rewards inside the same reward flow used by streaks, level-ups, evolutions, and chest openings while allowing achievement reward definitions to contain multiple reward types.

## UI

`AchievementScreen` displays:

- Total unlocked progress
- Number of achievements ready to claim
- Per-achievement progress bars
- Current value vs target value under each achievement
- Multiple reward preview chips
- Claim button for unlocked, unclaimed achievements
- Claimed, unlocked, or locked status for non-claimable achievements

## Configuration

Achievement definitions are initialized in:

- `app/src/main/java/com/example/mobile/domain/AchievementsConfig.kt`

Coin values reuse `EconomyConfig`. Chest rewards use `ChestType` and `ChestRewardConfigProvider`. Customization rewards use stable `InventoryItemEntity.itemId` values.

## Data Model

**AchievementEntity** (`app/src/main/java/com/example/mobile/data/local/entities/AchievementEntity.kt`):

- Table: `achievements`
- Columns:
  - `id: String` - stable achievement identifier from `AchievementsConfig`
  - `progress: Int` - latest persisted progress for the configured source
  - `isUnlocked: Boolean` - milestone completion status
  - `isClaimed: Boolean` - whether the player has claimed the reward
  - `unlockedDate: Long?` - timestamp when the achievement was unlocked

Achievement metadata and reward definitions are not stored in the database. They are loaded from `AchievementsConfig`, which allows new achievements to be added without changing the Room schema.

Database version was increased from 13 to 14 with `MIGRATION_13_14`, which preserves existing achievement progress by legacy achievement name and maps it to stable config IDs.

## Source Files

- `app/src/main/java/com/example/mobile/data/local/entities/AchievementEntity.kt`
- `app/src/main/java/com/example/mobile/data/local/dao/AchievementDao.kt`
- `app/src/main/java/com/example/mobile/data/local/database/AchievementDatabaseInitializer.kt`
- `app/src/main/java/com/example/mobile/data/local/database/AchievementMetadataMigration.kt`
- `app/src/main/java/com/example/mobile/data/local/database/AppDatabase.kt`
- `app/src/main/java/com/example/mobile/data/repository/AchievementRepositoryImpl.kt`
- `app/src/main/java/com/example/mobile/domain/AchievementEngine.kt`
- `app/src/main/java/com/example/mobile/domain/AchievementProgressSource.kt`
- `app/src/main/java/com/example/mobile/domain/AchievementReward.kt`
- `app/src/main/java/com/example/mobile/domain/AchievementRewardProcessor.kt`
- `app/src/main/java/com/example/mobile/domain/AchievementsConfig.kt`
- `app/src/main/java/com/example/mobile/domain/repository/AchievementRepository.kt`
- `app/src/main/java/com/example/mobile/domain/StreakEngine.kt`
- `app/src/main/java/com/example/mobile/presentation/viewmodel/AchievementViewModel.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/reward/RewardScreen.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/reward/RewardOverlay.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/screens/AchievementScreen.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/events/RewardUiEvent.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/reward/RewardQueue.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/reward/RewardManager.kt`
- `app/src/main/java/com/example/mobile/di/DatabaseModule.kt`
- `app/src/main/java/com/example/mobile/HabitPetApp.kt`

## Known Gaps

1. Achievement conditions use simple thresholds rather than compound rules.
2. Achievement icons are stored but the UI currently uses generic check/lock/trophy icons.
3. Achievements are shown as one list rather than grouped by type or difficulty.
4. Unlock feedback uses the standard reward popup rather than a dedicated achievement animation or notification.
