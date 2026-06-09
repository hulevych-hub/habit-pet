# ACHIEVEMENTS

## Overview

The achievements system in Habit Pet provides long-term goals for players to strive toward. Achievements are unlocked by reaching specific milestones in habit tracking, streaks, XP accumulation, and pet leveling. Each achievement grants a coin reward upon completion.

## Current Implementation

Achievements are implemented as a Room database entity with automatic unlocking via the AchievementEngine. The system includes:
- Persistent storage of achievement state (name, description, target value, reward, unlock status)
- Automatic monitoring of game conditions to unlock achievements
- Coin rewards distributed through the reward queue system
- UI display separating completed and locked achievements

## Rules

### Achievement Entity Structure
Each achievement has the following properties:
- `id: Long` - unique identifier
- `name: String` - achievement title
- `description: String` - detailed description of the requirement
- `icon: String` - icon identifier (currently unused in UI)
- `targetValue: Int` - the value that must be reached or exceeded to unlock
- `rewardCoins: Int` - coins awarded upon unlocking
- `isUnlocked: Boolean` - current unlock status
- `unlockedDate: Long?` - timestamp when achievement was unlocked (null if locked)

### Default Achievements
The game initializes with 7 predefined achievements:

1. **First Habit**
   - Target: 1 habit created
   - Reward: 50 coins
   - Unlock condition: habit count ≥ 1

2. **7 Day Streak**
   - Target: 7-day streak
   - Reward: 100 coins
   - Unlock condition: current streak ≥ 7

3. **30 Day Streak**
   - Target: 30-day streak
   - Reward: 250 coins
   - Unlock condition: current streak ≥ 30

4. **100 Completions**
   - Target: 100 habit completions
   - Reward: 200 coins
   - Unlock condition: total completions ≥ 100

5. **1000 XP**
   - Target: 1000 XP earned
   - Reward: 150 coins
   - Unlock condition: pet XP ≥ 1000

6. **Level 10**
   - Target: reach level 10
   - Reward: 300 coins
   - Unlock condition: pet level ≥ 10

7. **Level 25**
   - Target: reach level 25
   - Reward: 500 coins
   - Unlock condition: pet level ≥ 25

### Achievement Unlocking
The AchievementEngine monitors five different game aspects:
- **Habit count**: triggers "First Habit" achievement
- **Streaks**: triggers "7 Day Streak" and "30 Day Streak" achievements
- **Completions**: triggers "100 Completions" achievement
- **XP**: triggers "1000 XP" achievement
- **Level**: triggers "Level 10" and "Level 25" achievements

When a condition is met, the engine:
1. Checks if the corresponding achievement is already unlocked
2. If not, updates the achievement record (isUnlocked = true, unlockedDate = timestamp)
3. Creates a `RewardUiEvent.AchievementReward` with the achievement name and rewardCoins
4. Adds the reward event to the reward queue for processing

### Reward Processing
Upon achievement unlock:
- RewardManager extracts the rewardCoins from the AchievementReward event
- Coins are added to the player's total via `statisticsRepository.addCoins()`
- The reward is displayed in the standard reward popup UI

## Configuration

All achievement definitions are hardcoded in:
- `app/src/main/java/com/example/mobile/data/local/database/AchievementDatabaseInitializer.kt`
  - Achievement names, descriptions, target values, and reward coins

## Data Model

**AchievementEntity** (app/src/main/java/com/example/mobile/data/local/entities/AchievementEntity.kt):
- Table: `achievements`
- Columns: id, name, description, icon, targetValue, rewardCoins, isUnlocked, unlockedDate

## Source Files

- app/src/main/java/com/example/mobile/data/local/entities/AchievementEntity.kt
- app/src/main/java/com/example/mobile/data/local/database/AchievementDatabaseInitializer.kt
- app/src/main/java/com/example/mobile/data/local/dao/AchievementDao.kt
- app/src/main/java/com/example/mobile/data/local/database/AppDatabase.kt
- app/src/main/java/com/example/mobile/data/repository/AchievementRepositoryImpl.kt
- app/src/main/java/com/example/mobile/domain/repository/AchievementRepository.kt
- app/src/main/java/com/example/mobile/domain/AchievementEngine.kt
- app/src/main/java/com/example/mobile/presentation/viewmodel/AchievementViewModel.kt
- app/src/main/java/com/example/mobile/presentation/ui/screens/AchievementScreen.kt
- app/src/main/java/com/example/mobile/presentation/ui/events/RewardUiEvent.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardQueue.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardManager.kt

## Known Gaps

1. **Static Content**: All achievements are predefined; no system for downloading, creating, or modifying achievements post-launch.
2. **Simple Conditions**: All achievements use simple threshold comparisons; no complex conditions (e.g., "complete 5 different habits in one day").
3. **Missing Progression**: No multi-tier achievements (e.g., bronze/silver/gold for the same milestone).
4. **Icon Underutilization**: The `icon` field is stored but not referenced in the AchievementItem UI (uses generic check/lock icons).
5. **No Categorization**: All achievements are presented in a single list without grouping by type or difficulty.
6. **Limited Feedback**: Achievement unlocks use the standard reward popup; no special animations, sounds, or notifications.
7. **No Retroactive Awards**: Achievements only check conditions moving forward; no system to award achievements for past accomplishments if added later.