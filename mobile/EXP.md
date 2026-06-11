# EXP

## Overview

EXP (Experience Points) is a core progression system in Habit Pet. Players earn EXP by completing habits, which is used to level up their pet and unlock evolution stages. EXP also contributes to certain achievements.

## Current Implementation

The EXP system is implemented across multiple components:
- XP is stored in the PetEntity database table
- XP is awarded when habits are completed (both checkbox and timer habits)
- XP determines pet level and evolution stage
- Level progression uses a formula where each level requires increasingly more XP
- Evolution stages are determined by XP thresholds
- XP observations trigger achievement unlocks (e.g. 1000 XP achievement)
- **Centralized configuration: `ExpConfig`** (single source of truth)

## Rules

### XP Earning

- **Checkbox habit completion**: 100 XP (from `ExpConfig.CHECKBOX_HABIT_XP`)
- **Timer habit completion**: 10 base XP + 5 XP per minute (from `ExpConfig.TIMER_HABIT_BASE_XP` + `ExpConfig.TIMER_HABIT_XP_PER_MINUTE`)
  - Example: 30 min session → 10 + 150 = 160 XP
- XP is added to the pet's current XP total

### Level Calculation

- To reach level 1 from level 0: 100 XP required (`ExpConfig.BASE_XP_FOR_LEVEL_1`)
- To reach level 2 from level 1: 150 XP required
- To reach level L+1 from level L: `BASE_XP_FOR_LEVEL_1 + L * XP_PER_LEVEL_INCREMENT` (50 more XP per level)
- This creates an arithmetic progression where each level requires 50 more XP than the previous level
- **Centralized in `ExpConfig.calculateLevelFromXp()`** - single source of truth

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

### Achievement Integration

- Reaching 1000 XP unlocks the "1000 XP" achievement (observed via AchievementEngine.observeXp())

### Level-Up Rewards

- **Coins**: level × 10 (from `ExpConfig.LEVEL_UP_COIN_MULTIPLIER`)
- **Chest**: Random chest reward (Normal 55%, Rare 30%, Epic 12%, Legendary 3%)
- Chest rewards may include coins, EXP, and a chance for customization items based on `EconomyConfig` and `ChestRewardConfigProvider`

## Configuration

All EXP values are now centralized in `ExpConfig` (app/src/main/java/com/example/mobile/domain/ExpConfig.kt):

- Checkbox habit XP: 100 (`ExpConfig.CHECKBOX_HABIT_XP`)
- Timer habit XP base: 10 (`ExpConfig.TIMER_HABIT_BASE_XP`)
- Timer habit XP per minute: 5 (`ExpConfig.TIMER_HABIT_XP_PER_MINUTE`)
- Level formula: 100 + (level * 50) (`ExpConfig.BASE_XP_FOR_LEVEL_1` + `ExpConfig.XP_PER_LEVEL_INCREMENT`)
- Evolution stage thresholds: Defined in `ExpConfig.EVOLUTION_THRESHOLDS`
- Level-up coin multiplier: 10 (`ExpConfig.LEVEL_UP_COIN_MULTIPLIER`)

## Data Model

**PetEntity** (app/src/main/java/com/example/mobile/data/local/entities/PetEntity.kt):
- `xp: Long` - stores total accumulated XP
- `level: Int` - current pet level (derived from XP)
- `evolution_stage: Int` - current evolution stage (0-4, derived from XP)

## Source Files

- app/src/main/java/com/example/mobile/data/local/entities/PetEntity.kt
- app/src/main/java/com/example/mobile/data/repository/HabitCompletionRepositoryImpl.kt
- app/src/main/java/com/example/mobile/presentation/viewmodel/HabitDetailViewModel.kt
- app/src/main/java/com/example/mobile/domain/AchievementEngine.kt
- app/src/main/java/com/example/mobile/presentation/ui/screens/PetScreen.kt
- app/src/main/java/com/example/mobile/domain/ExpConfig.kt (centralized configuration)

## Known Gaps (RESOLVED)

1. ✅ **FIXED: Inconsistent Evolution Stage Calculation** - Now single source in `ExpConfig.calculateEvolutionStageFromXp()`
2. ✅ **FIXED: Redundant Level Calculation** - Now centralized in `ExpConfig.calculateLevelFromXp()`
3. ✅ **FIXED: XP Awarding Inconsistency** - Both paths now use `ExpConfig` for calculations

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
- Timer habits accelerate XP and coin gain when completed for their configured minimum duration.
- Level-up base coins are awarded directly with XP progression.
- Level-up chests add additional coins, EXP, and occasional customization items, keeping early-game economy near the target of roughly 100 coins/day when recurring rewards are included.
- Evolution remains progression-gated by XP only, with no additional item or streak requirements.
