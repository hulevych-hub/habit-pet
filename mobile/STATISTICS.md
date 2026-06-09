# STATISTICS

## Overview

The statistics system in Habit Pet tracks various player metrics and progress indicators. These statistics are stored in a persistent database and used for gameplay progression, achievement unlocking, streak tracking, and UI display.

## Current Implementation

Statistics are implemented as a Room database entity (StatisticsEntity) with a dedicated repository for access and modification. The system tracks both cumulative lifetime metrics and session-based progress indicators.

## Rules

### Tracked Statistics

The StatisticsEntity tracks the following metrics:

1. **id: Long** - Database identifier (constant value of 1)
2. **currentStreak: Int** - Current consecutive day streak (where all habits are completed)
3. **bestStreak: Int** - Highest streak ever achieved
4. **globalStreak: Int** - Appears to be unused/duplicate of currentStreak
5. **totalCompletions: Int** - Total number of habit completions across all habits
6. **totalXp: Long** - Total accumulated experience points
7. **daysActive: Int** - Number of days with at least one habit completion
8. **totalHabitsCompleted: Int** - Total number of unique habits ever created
9. **petAgeDays: Int** - Days since pet creation (appears to be unimplemented)
10. **totalCoins: Int** - Total accumulated in-game currency
11. **lastStreakAwardedAt: Int** - Last streak value at which a chest reward was given
12. **lastUpdated: Long** - Timestamp of last statistics update
13. **rewardChestsAvailable: Int** - Appears to be unused
14. **lastStreakDate: Long** - Date (in days since epoch) of last streak counting

### Statistics Updates

Statistics are updated through the following mechanisms:

**From Habit Completions** (HabitCompletionRepositoryImpl.updateStatistics()):
- totalCompletions += 1
- totalHabitsCompleted += 1 (when new habit is created)
- totalXp += XP earned from completion
- daysActive = habitCompletionDao.getActiveDayCount() (count of days with activity)
- lastUpdated = current timestamp

**From Streak Engine** (StatisticsRepositoryImpl):
- incrementStreak(): currentStreak += 1
- markStreakUpdatedToday(): lastStreakDate = today's date
- isStreakAlreadyCountedToday(): checks if streak already counted today

**From Reward System** (StatisticsRepositoryImpl):
- addCoins(amount): totalCoins += amount

**From System Reset** (StatisticsRepositoryImpl):
- reset(): clears all statistics to default values

### Statistics Usage

Statistics are used for:
- **Achievement Unlocking**: AchievementEngine monitors statistics for milestone triggers
- **Streak Rewards**: StreakEngine uses streaks to determine chest reward eligibility
- **UI Display**: HomeScreenViewModel exposes statistics for display in home screen
- **Pet Progress**: XP statistics influence pet level and evolution stage
- **Economy**: Coin statistics track purchasing power

### Displayed Statistics

The following statistics are visible in the UI (Home Screen):
- **Streak**: Displayed as "{currentStreak} Day Streak"
- **Coins**: Displayed as "{totalCoins} Coins"
- **Level & Evolution**: Derived from pet statistics (not directly from StatisticsEntity)
- **XP Progress**: Shows current XP toward next level

## Configuration

All statistics tracking values are hardcoded in the implementation:
- Streak threshold for rewards: 7 days (StreakEngine.kt)
- Chest reward amount: 50 coins (StreakEngine.kt)
- DaysActive calculation: handled by habitCompletionDao.getActiveDayCount()

## Data Model

**StatisticsEntity** (app/src/main/java/com/example/mobile/data/local/entities/StatisticsEntity.kt):
- Table: `statistics`
- Columns: id, currentStreak, bestStreak, globalStreak, totalCompletions, totalXp, daysActive, totalHabitsCompleted, petAgeDays, totalCoins, lastStreakAwardedAt, lastUpdated, rewardChestsAvailable, lastStreakDate

## Source Files

- app/src/main/java/com/example/mobile/data/local/entities/StatisticsEntity.kt
- app/src/main/java/com/example/mobile/data/local/dao/StatisticsDao.kt
- app/src/main/java/com/example/mobile/data/local/database/StatisticsDatabaseInitializer.kt
- app/src/main/java/com/example/mobile/data/repository/StatisticsRepositoryImpl.kt
- app/src/main/java/com/example/mobile/domain/repository/StatisticsRepository.kt
- app/src/main/java/com/example/mobile/domain/AchievementEngine.kt
- app/src/main/java/com/example/mobile/domain/StreakEngine.kt
- app/src/main/java/com/example/mobile/data/repository/HabitCompletionRepositoryImpl.kt
- app/src/main/java/com/example/mobile/presentation/ui/screens/HomeScreenViewModel.kt
- app/src/main/java/com/example/mobile/presentation/ui/screens/HomeScreen.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardManager.kt

## Known Gaps

1. **Unused Statistics**: Several tracked statistics appear to be unused in the current implementation:
   - bestStreak (only updated when currentStreak exceeds it, but never displayed)
   - globalStreak (appears redundant with currentStreak)
   - rewardChestsAvailable (tracked but never used for logic or display)
   - petAgeDays (never updated or displayed)
   - lastStreakAwardedAt (used internally but not exposed to player)

2. **Inconsistent Naming**: The presence of both currentStreak and globalStreak creates confusion about which represents the active streak.

3. **Limited Statistics Exposure**: Only two statistics (streak and coins) are directly visible to players in the main UI.

4. **Derived vs Stored**: Some valuable metrics like pet level and evolution stage are derived from XP rather than stored directly, requiring calculation each time.

5. **No Statistics History**: The system only tracks current values; no historical progression or milestones are recorded beyond achievements.

6. **Streak Definition**: Streaks require completing ALL habits each day, which becomes increasingly difficult as players add more habits.