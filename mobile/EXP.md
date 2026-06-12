# EXP

## Overview

EXP (Experience Points) is a core progression system in Habit Pet. Players earn EXP by completing habits, which is used to level up their pet and unlock evolution stages. EXP also contributes to certain achievements.

## Current Implementation

The EXP system is implemented across multiple components:
- XP is stored in the PetEntity database table
- XP is awarded when habits are completed (checkbox habits can be completed from the list or detail screen; timer habits complete from the detail screen)
- XP determines pet level and evolution stage
- Level progression uses a formula where each level requires increasingly more XP
- Evolution stages are determined by XP thresholds
- XP observations trigger achievement unlocks (e.g. 1000 XP achievement)
- Daily XP goal progress is tracked from completed habits and displayed on `HomeScreen`
- **Centralized configuration: `ExpConfig`** (single source of truth)
- **Progress is always visible through `ProgressHeader`, `DailyGoalCard`, and `EvolutionTeaser`** on major gameplay screens
- **Short-term combo momentum** adds a small additive XP bonus for consecutive habit completions within the configured inactivity window

## Progress Always Visible

The reusable `ProgressHeader` component displays the player's current XP level progress, evolution progress, next evolution stage, streak, and coin balance in one compact card. `EvolutionTeaser` shows the next stage name, a progress bar, and the XP needed to reach it.

Current integration:
- `HomeScreen` shows the header near the pet summary.
- `HabitsScreen` pins the header as a sticky header above the habit list.
- `HabitDetailScreen` shows the header above the selected habit's completion controls.
- `RewardsScreen` shows the header above collection filters so currency progress remains visible while browsing unlocks.
- `ActivityTimelineScreen` shows the header above the activity feed and empty/loading states.

`ProgressHeader` and `EvolutionTeaser` use `ExpConfig` for XP and evolution calculations, so they stay aligned with the centralized progression rules. They receive progress values from existing screen state flows, so updates remain real-time when XP, coins, streaks, or combo momentum change.

No new coin, XP, chest, or customization source was added by this system. The one-tap list completion path reuses the same `ExpConfig` values and reward queue as the detail-screen completion path.

## Rules

### XP Earning

- **Checkbox habit completion**: 100 XP (from `ExpConfig.CHECKBOX_HABIT_XP`)
- **Timer habit completion**: 10 base XP + 5 XP per minute (from `ExpConfig.TIMER_HABIT_BASE_XP` + `ExpConfig.TIMER_HABIT_XP_PER_MINUTE`)
  - Example: 30 min session → 10 + 150 = 160 XP
- XP is added to the pet's current XP total
- Checkbox habit completion from the habit list is optimistic: the UI marks the habit complete immediately after the repository write succeeds, then the same reward pipeline updates pet XP, coins, streaks, activity timeline, micro-feedback, and any queued level-up/chest rewards
- Reward overlays are reserved for major progression moments such as level-ups, evolutions, streak chests, achievements, and surprise chests; direct habit XP/coin feedback does not open a blocking reward screen

### Combo / Momentum

Consecutive habit completions within a short inactivity window create a short-term combo. The combo bonus is additive, small, and capped so it feels rewarding without overpowering normal XP progression.

- Active window: 2 hours (`ExpConfig.COMBO_INACTIVITY_WINDOW_MS`)
- Bonus: +5 XP per consecutive completion after the first (`ExpConfig.COMBO_BONUS_XP_PER_CONSECUTIVE_COMPLETION`)
- Maximum bonus per habit: +20 XP (`ExpConfig.COMBO_MAX_BONUS_XP`)
- Combo multiplier feedback is derived from the checkbox habit baseline and displayed as `Combo x1.05`, `Combo x1.10`, etc.
- Combo state is stored on `StatisticsEntity`:
  - `currentCombo`
  - `bestCombo`
  - `lastHabitCompletionTimestamp`
- The combo resets visually when the inactivity window has passed and resets in storage on the next completion after the window.
- Combo milestones at 3, 5, and 10 hits are logged in `ActivityTimelineEngine` through `GameEventType.COMBO_MILESTONE`.
- Both list completion (`HabitsViewModel`) and detail completion (`HabitDetailViewModel`) use `HabitCompletionRepository.addCompletionWithCombo()`, so the same momentum rules apply across screens.

### Daily XP Goal

The daily goal is XP-based and resets by date key:
- Target: 300 XP per day (`ExpConfig.DAILY_XP_GOAL`)
- Progress source: completed habit XP from `HabitCompletionEntity.xpEarned`
- Completion state: `StatisticsEntity.dailyGoalCompletedDate`
- Bonus reward: +25 XP (`ExpConfig.DAILY_GOAL_BONUS_XP`) plus a queued `RewardUiEvent.DailyGoalReward`
- UI: `HomeScreen.DailyGoalCard` shows progress, completion state, and the next-day reset behavior by comparing `StatisticsEntity.dailyGoalDate` with today's date key

### Level Calculation

- To reach level 1 from level 0: 100 XP required (`ExpConfig.BASE_XP_FOR_LEVEL_1`)
- To reach level 2 from level 1: 150 XP required
- To reach level L+1 from level L: `BASE_XP_FOR_LEVEL_1 + L * XP_PER_LEVEL_INCREMENT` (50 more XP per level)
- This creates an arithmetic progression where each level requires 50 more XP than the previous level
- **Centralized in `ExpConfig.calculateLevelFromXp()`** - single source of truth
- Level progress bars use the XP required for the next level, not remaining XP to the next level, so progress never appears overfilled

### Evolution Stages

Pet evolution stages are determined by XP thresholds (**NOW CONSISTENT - single source in `ExpConfig`**):

| Stage | Name | XP Range |
|-------|------|----------|
| 0 | Egg | 0 - 499 |
| 1 | Hatchling | 500 - 1,499 |
| 2 | Young Dragon | 1,500 - 2,999 |
| 3 | Adult Dragon | 3,000 - 5,999 |
| 4 | Ancient Dragon | 6,000+ |

**Thresholds defined in `ExpConfig.EVOLUTION_THRESHOLDS`**

### Evolution Teasing and Milestone Nearing
The UI surfaces the next evolution stage through `ProgressHeader` and `EvolutionTeaser`, both powered by `ExpConfig.evolutionStageName()` and `ExpConfig.xpThresholdForStage()`. Habit completion and chest reward paths call `ActivityTimelineEngine.logEvolutionMilestoneNearing()` for the next locked stage, recording one timeline event when the stage reaches 80% progress.

### Achievement Integration

- Reaching 1000 XP unlocks the "1000 XP" achievement (observed via `AchievementEngine`)
- Reaching 5000 XP unlocks the "5000 XP" achievement and grants 300 EXP as a configured achievement reward

### Level-Up Rewards

- **Coins**: level × 10 (from `ExpConfig.LEVEL_UP_COIN_MULTIPLIER`)
- **Chest**: Random chest reward (Normal 55%, Rare 30%, Epic 12%, Legendary 3%)
- Chest rewards may include coins, EXP, and a chance for customization items based on `EconomyConfig` and `ChestRewardConfigProvider`

## Configuration

All EXP values are now centralized in `ExpConfig` (app/src/main/java/com/example/mobile/domain/ExpConfig.kt):

- Checkbox habit XP: 100 (`ExpConfig.CHECKBOX_HABIT_XP`)
- Timer habit XP base: 10 (`ExpConfig.TIMER_HABIT_BASE_XP`)
- Timer habit XP per minute: 5 (`ExpConfig.TIMER_HABIT_XP_PER_MINUTE`)
- Combo inactivity window: 2 hours (`ExpConfig.COMBO_INACTIVITY_WINDOW_MS`)
- Combo bonus XP per consecutive completion after the first: 5 XP (`ExpConfig.COMBO_BONUS_XP_PER_CONSECUTIVE_COMPLETION`)
- Combo max bonus XP per habit: 20 XP (`ExpConfig.COMBO_MAX_BONUS_XP`)
- Combo milestones: 3, 5, 10 hits (`ExpConfig.COMBO_MILESTONES`)
- Daily XP goal: 300 XP (`ExpConfig.DAILY_XP_GOAL`)
- Daily goal bonus XP: 25 XP (`ExpConfig.DAILY_GOAL_BONUS_XP`)
- Level formula: 100 + (level * 50) (`ExpConfig.BASE_XP_FOR_LEVEL_1` + `ExpConfig.XP_PER_LEVEL_INCREMENT`)
- Evolution stage thresholds: Defined in `ExpConfig.EVOLUTION_THRESHOLDS`
- Level-up coin multiplier: 10 (`ExpConfig.LEVEL_UP_COIN_MULTIPLIER`)

## Data Model

**PetEntity** (app/src/main/java/com/example/mobile/data/local/entities/PetEntity.kt):
- `xp: Long` - stores total accumulated XP

- `level: Int` - current pet level (derived from XP)
- `evolution_stage: Int` - current evolution stage (0-4, derived from XP)

**StatisticsEntity** daily goal fields:
- `currentCombo: Int` - active consecutive completion count within the combo window
- `bestCombo: Int` - highest consecutive completion count reached
- `lastHabitCompletionTimestamp: Long` - timestamp used to evaluate combo inactivity
- `dailyGoalXp: Int` - configured daily XP target
- `dailyGoalProgressXp: Int` - XP progress for the stored `dailyGoalDate`
- `dailyGoalDate: Long` - date key for the stored daily progress
- `dailyGoalCompletedDate: Long` - date key when the daily goal bonus was awarded

## Source Files

- app/src/main/java/com/example/mobile/data/local/entities/PetEntity.kt
- app/src/main/java/com/example/mobile/data/local/entities/StatisticsEntity.kt
- app/src/main/java/com/example/mobile/data/local/dao/StatisticsDao.kt
- app/src/main/java/com/example/mobile/data/local/database/AppDatabase.kt
- app/src/main/java/com/example/mobile/data/repository/HabitCompletionRepositoryImpl.kt
- app/src/main/java/com/example/mobile/domain/repository/HabitCompletionRepository.kt
- app/src/main/java/com/example/mobile/presentation/viewmodel/HabitDetailViewModel.kt
- app/src/main/java/com/example/mobile/presentation/viewmodel/HabitsViewModel.kt
- app/src/main/java/com/example/mobile/domain/AchievementEngine.kt
- app/src/main/java/com/example/mobile/presentation/ui/screens/PetScreen.kt
- app/src/main/java/com/example/mobile/domain/ExpConfig.kt (centralized configuration)
- app/src/main/java/com/example/mobile/domain/EconomyConfig.kt (daily goal coin bonus)
- app/src/main/java/com/example/mobile/presentation/ui/components/ProgressHeader.kt (reusable progress indicator)
- app/src/main/java/com/example/mobile/presentation/ui/screens/HomeScreen.kt
- app/src/main/java/com/example/mobile/presentation/ui/screens/HomeScreenViewModel.kt
- app/src/main/java/com/example/mobile/presentation/ui/screens/HabitsScreen.kt
- app/src/main/java/com/example/mobile/presentation/ui/screens/HabitDetailScreen.kt
- app/src/main/java/com/example/mobile/presentation/ui/screens/RewardsScreen.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardManager.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardOverlay.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardScreen.kt
- app/src/main/java/com/example/mobile/presentation/ui/screens/ActivityTimelineScreen.kt
- app/src/main/java/com/example/mobile/domain/ActivityTimelineEngine.kt
- app/src/main/java/com/example/mobile/domain/GameEventFactory.kt
- app/src/main/java/com/example/mobile/domain/GameEventType.kt
- app/src/main/java/com/example/mobile/presentation/ui/events/MicroFeedbackEvent.kt
- app/src/main/java/com/example/mobile/presentation/ui/feedback/MicroFeedbackManager.kt
- app/src/main/java/com/example/mobile/presentation/ui/feedback/MicroFeedbackOverlay.kt

## Known Gaps (RESOLVED)

1. ✅ **FIXED: Inconsistent Evolution Stage Calculation** - Now single source in `ExpConfig.calculateEvolutionStageFromXp()`
2. ✅ **FIXED: Redundant Level Calculation** - Now centralized in `ExpConfig.calculateLevelFromXp()`
3. ✅ **FIXED: XP Awarding Inconsistency** - Both paths now use `ExpConfig` for calculations
4. ✅ **FIXED: Progress Visibility Gaps** - Major habit, collection, and activity screens now show `ProgressHeader`
5. ✅ **FIXED: One-Tap Habit Completion Friction** - Checkbox habits can now be completed directly from the habit list with immediate reward pipeline execution
6. ✅ **FIXED: No Short-Term Momentum Feedback** - Consecutive completions now produce a capped additive XP bonus, visible combo multiplier feedback, and timeline milestones

## Progression Validation

| Level | Total XP Required | Est. Habits (Checkbox) | Est. Days (3/day) |
|-------|-------------------|------------------------|-------------------|
| 1 | 100 | 1 | 0.3 |
| 5 | 1,000 | 10 | 3.3 |
| 10 | 3,250 | 32.5 | 10.8 |
| 15 | 6,750 | 67.5 | 22.5 |
| 20 | 11,500 | 115 | 38.3 |
| 25 | 17,500 | 175 | 58.3 |
| 30 | 24,750 | 247.5 | 82.5 |
| 40 | 43,000 | 430 | 143.3 |
| 50 | 66,250 | 662.5 | 220.8 |

**Evolution Milestones:**
- Hatchling (Stage 1): 500 XP → ~5 habits → ~1.7 days
- Young Dragon (Stage 2): 1,500 XP → ~15 habits → ~5 days
- Adult Dragon (Stage 3): 3,000 XP → ~30 habits → ~10 days
- Ancient Dragon (Stage 4): 6,000 XP → ~60 habits → ~20 days

*Assumes 100 XP per checkbox habit and no timer-habit bonus XP.*

## Balance Notes

- Three checkbox habits per day produce 300 XP/day and 30 coins/day.
- Short-term combo momentum can add at most +20 XP to a single habit, keeping the bonus additive and non-overpowering.
- Timer habits accelerate XP and coin gain when completed for their configured minimum duration.
- Level-up base coins are awarded directly with XP progression.
- Level-up chests add additional coins, EXP, and occasional customization items, keeping early-game economy near the target of roughly 100 coins/day when recurring rewards are included.
- Evolution remains progression-gated by XP only, with no additional item or streak requirements.
