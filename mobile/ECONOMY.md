# ECONOMY

## Overview

The economy system in Habit Pet manages the in-game currency (coins) that players earn through various activities and spend on purchasing accessories. Coins serve as the primary resource for progression and customization.

## Current Implementation

The economy consists of:
- Coin storage in StatisticsEntity (totalCoins field)
- Sources of coin income: achievements, streaks, level-ups, and pet evolution
- Spending mechanism: purchasing accessories in the Rewards screen
- Centralized coin management through StatisticsRepository

## Rules

### Coin Storage
- Single currency type: coins (integer)
- Stored in `StatisticsEntity.totalCoins`
- Default starting value: 0 coins
- Persisted via Room database

### Sources of Coin Income

1. **Achievement Rewards**
   - Each achievement grants a fixed coin reward upon unlocking
   - Rewards are defined in AchievementDatabaseInitializer.kt:
     * First Habit: 50 coins
     * 7 Day Streak: 100 coins
     * 30 Day Streak: 250 coins
     * 100 Completions: 200 coins
     * 1000 XP: 150 coins
     * Level 10: 300 coins
     * Level 25: 500 coins

2. **Streak Milestone Chests**
   - Awarded every 7 consecutive days of completing all habits
   - Reward: 50 coins (fixed amount)
   - Trigger condition: currentStreak ≥ 7 AND currentStreak % 7 = 0 AND currentStreak > lastStreakAwardedAt
   - Delivered as ChestReward with type "7_day_streak"

3. **Level Up Bonuses**
   - Awarded each time the pet gains a level
   - Base reward: level × 10 coins (e.g., level 5 → 50 coins)
   - Additional chest reward: 20 coins (fixed)
   - Delivered as:
     * LevelUpReward with coins = level × 10
     * ChestReward with type "Level up" and amount = 20 coins

4. **Timer Habit Completion**
   - Coins awarded based on session duration
   - Formula: (10 + sessionMinutes) coins
   - Where sessionMinutes is the accumulated time spent on the timer habit
   - Awarded directly via awardCoins() (not through reward queue)

### Spending Mechanics
- **Accessory Purchases**
  - Items have individual prices in coins (stored in InventoryItemEntity.price)
  - Purchase requires sufficient totalCoins ≥ item.price
  - On successful purchase:
    * totalCoins decreases by item.price
    * Item marked as isPurchased = true
  - Purchase failure codes:
    * -1: Item not found
    * -2: Already purchased
    * -3: Unable to get statistics
    * -4: Not enough coins

### Coin Processing Flow
When reward events are processed:
1. RewardManager receives reward events from RewardQueue
2. Extracts coin value based on reward type:
   - CoinReward: current.amount
   - LevelUpReward: current.coins
   - DragonEvolutionReward: 0
   - StreakReward: current.coins
   - AchievementReward: current.coins
   - ChestReward: (current.amount as? Int) ?: 0
3. If coinsToAdd > 0, calls statisticsRepository.addCoins(coinsToAdd)
4. StatisticsRepository adds coins to totalCoins and persists to database

## Configuration

All economy values are defined in source code:
- Achievement rewards: hardcoded in AchievementDatabaseInitializer.kt
- Streak chest reward: 50 coins (StreakEngine.kt line 51)
- Level up chest reward: 20 coins (HabitDetailViewModel.kt line 336)
- Level up base multiplier: 10 coins per level (HabitDetailViewModel.kt line 328)
- Timer habit base: 10 coins + 1 coin per minute (HabitDetailViewModel.kt line 244)

## Data Model

**StatisticsEntity** (app/src/main/java/com/example/mobile/data/local/entities/StatisticsEntity.kt):
- `totalCoins: Int` - total coins owned by player

**InventoryItemEntity** (app/src/main/java/com/example/mobile/data/local/entities/InventoryItemEntity.kt):
- `price: Int` - cost in coins to purchase the item

## Source Files

- app/src/main/java/com/example/mobile/data/local/entities/StatisticsEntity.kt
- app/src/main/java/com/example/mobile/data/local/entities/InventoryItemEntity.kt
- app/src/main/java/com/example/mobile/data/local/database/StatisticsDatabaseInitializer.kt
- app/src/main/java/com/example/mobile/data/repository/StatisticsRepositoryImpl.kt
- app/src/main/java/com/example/mobile/domain/repository/StatisticsRepository.kt
- app/src/main/java/com/example/mobile/domain/AchievementEngine.kt
- app/src/main/java/com/example/mobile/domain/StreakEngine.kt
- app/src/main/java/com/example/mobile/presentation/viewmodel/HabitDetailViewModel.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardManager.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardQueue.kt

## Known Gaps

1. **No Coin Caps**: No maximum limit on coin accumulation; players can earn unlimited coins through extended play.
2. **Inflation Sources**: Multiple independent coin sources with no balancing mechanism (e.g., achievements give fixed amounts regardless of player progress).
3. **Limited Spending Options**: Coins can only be spent on accessory purchases; no other sinks for currency (donations, upgrades, consumables, etc.).
4. **Price Scaling**: Item prices in the inventory are static; no dynamic pricing based on rarity, player level, or demand.
5. **No Earning Variance**: Most coin sources have fixed values; no bonus multipliers, streaks, or difficulty-based variations.
6. **Decimal Precision**: Coin system uses integers only; no support for fractional coin rewards.