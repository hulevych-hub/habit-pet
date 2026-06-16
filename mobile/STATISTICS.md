# Statistics

## Overview

The statistics system in Habit Pet tracks player metrics and progress indicators used for progression, achievements, streaks, challenge availability, and UI display. Statistics are stored in a persistent Room database through `StatisticsEntity`.

The Home screen exposes the active streak to `ProgressHeader` and exposes coins, streak state, combo state, and other progression signals through `HomeScreenViewModel`. Challenge progress is tracked separately in the `challenges` table, not in `StatisticsEntity`.

## Rules

### Tracked Statistics

The `StatisticsEntity` tracks the following metrics:

1. **id: Long** - Database identifier (constant value of 1)
2. **currentStreak: Int** - Current consecutive day streak where all habits are completed
3. **bestStreak: Int** - Highest streak ever achieved
4. **globalStreak: Int** - Mirrors the active all-habits streak and is exposed to the Home screen
5. **totalCompletions: Int** - Total number of habit completions across all habits
6. **totalXp: Long** - Total accumulated experience points
7. **daysActive: Int** - Number of days with at least one habit completion
8. **totalHabitsCompleted: Int** - Total number of unique habits ever created
9. **petAgeDays: Int** - Days since pet creation (currently limited in use)
10. **totalCoins: Int** - Total accumulated in-game currency
11. **lastStreakAwardedAt: Int** - Last global streak milestone value that awarded a reward
12. **lastUpdated: Long** - Timestamp of last statistics update
13. **rewardChestsAvailable: Int** - Tracks available reward chests
14. **lastStreakDate: Long** - Date key used to avoid counting the same streak twice in one day
15. **currentCombo: Int** - Current combo hit count
16. **bestCombo: Int** - Highest combo hit count achieved
17. **lastHabitCompletionTimestamp: Long** - Used for combo activity windows

### Statistics Updates

Statistics are updated through the following mechanisms:

**From Habit Completions** (`HabitCompletionRepositoryImpl.updateStatistics()`):
- `totalCompletions += 1`
- `totalHabitsCompleted += 1` when a new habit is created
- `totalXp += XP` earned from completion
- `daysActive = habitCompletionDao.getActiveDayCount()`
- `lastUpdated = current timestamp`

**From Challenge Progress**:
- Habit completions, XP rewards, coin rewards, chest openings, customization unlocks, customization equips, and streak milestones update `ChallengeEntity.progressValue`.
- Challenge completion and claim state are stored in the `challenges` table, not in `StatisticsEntity`.

**From Streak Engine** (`StatisticsRepositoryImpl`):
- `incrementStreak()`: `currentStreak += 1`, `globalStreak` mirrors the new streak, `bestStreak` is updated when needed
- `markStreakUpdatedToday()`: `lastStreakDate = today's date key`
- `isStreakAlreadyCountedToday()`: checks if streak already counted today
- Milestone rewards update `lastStreakAwardedAt` only when a defined global streak milestone is awarded

**From Reward System** (`StatisticsRepositoryImpl`):
- `addCoins(amount)`: `totalCoins += amount`

**From System Reset** (`StatisticsRepositoryImpl`):
- `reset()`: clears statistics to default values

### Statistics Usage

Statistics are used for:
- **Achievement Unlocking**: `AchievementEngine` monitors statistics for milestone triggers
- **Streak Rewards**: `StreakEngine` uses milestone streaks to determine global streak celebration and chest reward eligibility
- **Challenge Availability**: chest, customization, and streak challenges can check statistics and inventory state before being selected
- **Daily Welcome Event**: `ActivityTimelineEngine` reads `currentStreak` for the once-per-day `FIRST_DAILY_LOGIN` timeline entry
- **Dragon Mood**: `DragonMoodEngine` reads `currentStreak` when recalculating pet mood
- **UI Display**: `HomeScreenViewModel` exposes statistics for streak rhythm, coins, combo state, and completion status
- **Pet Progress**: XP statistics influence pet level and evolution stage
- **Economy**: coin statistics track purchasing power

### Displayed Statistics

The following statistics are visible in the UI:
- **Streak**: Displayed as an emotional rhythm indicator with low / stable / strong state colors, milestone markers at 3, 7, 14, 30, 60, and 100 days, a subtle pulse on streak increases, and protection messaging when the active streak has not been counted today
- **Active Challenge**: Displayed in `ChallengeCard` with icon, title, description, progress, progress bar, reward preview, and claim button when completed
- **Coins**: Displayed as `{totalCoins} Coins`
- **Level & Evolution**: Derived from pet statistics, not directly from `StatisticsEntity`
- **XP Progress**: Shows current XP toward next level

## Configuration

All statistics tracking values are hardcoded in the implementation:
- Streak UI milestone markers: 3, 7, 14, 30, 60, and 100 days (`ProgressHeader.kt`)
- Global streak reward milestones: 7, 14, 30, 60, and 100 days (`StreakEngine.kt`)
- Milestone chest mapping: 7 = Normal, 14 = Rare, 30/60 = Epic, 100 = Legendary
- Chest reward amounts come from `ChestRewardConfigProvider` and `EconomyConfig`
- DaysActive calculation: handled by `habitCompletionDao.getActiveDayCount()`

## Data Model

**StatisticsEntity** (`app/src/main/java/com/example/mobile/data/local/entities/StatisticsEntity.kt`):
- Table: `statistics`
- Columns: `id`, `currentStreak`, `bestStreak`, `globalStreak`, `totalCompletions`, `totalXp`, `daysActive`, `totalHabitsCompleted`, `petAgeDays`, `totalCoins`, `lastStreakAwardedAt`, `lastUpdated`, `rewardChestsAvailable`, `lastStreakDate`, `currentCombo`, `bestCombo`, `lastHabitCompletionTimestamp`

**ChallengeEntity** (`app/src/main/java/com/example/mobile/data/local/entities/ChallengeEntity.kt`):
- Table: `challenges`
- Stores the single active challenge row, including definition ID, progress, target, reward list, completion state, and claim state.

## Source Files

- `app/src/main/java/com/example/mobile/data/local/entities/StatisticsEntity.kt`
- `app/src/main/java/com/example/mobile/data/local/entities/ChallengeEntity.kt`
- `app/src/main/java/com/example/mobile/data/local/dao/StatisticsDao.kt`
- `app/src/main/java/com/example/mobile/data/local/dao/ChallengeDao.kt`
- `app/src/main/java/com/example/mobile/data/local/database/StatisticsDatabaseInitializer.kt`
- `app/src/main/java/com/example/mobile/data/local/database/ChallengeDatabaseInitializer.kt`
- `app/src/main/java/com/example/mobile/data/local/database/ChallengeMigration.kt`
- `app/src/main/java/com/example/mobile/data/repository/StatisticsRepositoryImpl.kt`
- `app/src/main/java/com/example/mobile/data/repository/ChallengeRepositoryImpl.kt`
- `app/src/main/java/com/example/mobile/domain/repository/StatisticsRepository.kt`
- `app/src/main/java/com/example/mobile/domain/repository/ChallengeRepository.kt`
- `app/src/main/java/com/example/mobile/domain/AchievementEngine.kt`
- `app/src/main/java/com/example/mobile/domain/StreakEngine.kt`
- `app/src/main/java/com/example/mobile/domain/DragonMoodEngine.kt`
- `app/src/main/java/com/example/mobile/data/repository/HabitCompletionRepositoryImpl.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/screens/HomeScreenViewModel.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/components/ChallengeCard.kt`

## Known Gaps

1. **Unused Statistics**: Several tracked statistics remain limited in the current implementation:
   - `bestStreak` (updated internally but not prominently displayed)
   - `rewardChestsAvailable` (tracked but used only by challenge availability checks)
   - `petAgeDays` (never updated or displayed)
   - `lastStreakAwardedAt` (used internally and not exposed directly to player)

2. **Stored vs Derived Metrics**: `currentStreak` is the persisted source of truth while `globalStreak` mirrors it for UI display.

3. **Daily Welcome Event Storage**: The daily first-touch system uses local preferences for `last_login_day` and `last_active_session_timestamp`; it does not add new columns to `StatisticsEntity`.

4. **Dragon Mood Storage**: Mood is calculated using `currentStreak` but is persisted in `PetEntity.mood`, not in `StatisticsEntity`.

5. **Limited Statistics Exposure**: Streak, coins, and active challenge progress are directly visible in the main UI; most other statistics live behind progression and achievement systems.

6. **Derived vs Stored**: Pet level and evolution stage are derived from XP rather than stored directly, requiring calculation each time.

7. **No Statistics History**: The system only tracks current values; no historical progression or milestones are recorded beyond achievements and the activity timeline.

8. **Streak Definition**: Streaks require completing all habits each day, which becomes increasingly difficult as players add more habits.
