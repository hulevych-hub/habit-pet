# ENDGAME

## Overview

Habit Pet does not have a defined "endgame" state or completion condition. The game is designed as an open-ended habit tracking experience where players can continue to progress indefinitely through leveling, XP accumulation, and streaks. Reaching the maximum pet evolution stage (Ancient Dragon) is a milestone rather than an endpoint.

## Current Implementation

The game continues to provide progression and rewards beyond reaching maximum evolution:
- XP accumulation continues infinitely with no level cap
- Coin earning continues through achievements, streaks, and level-ups
- Habit tracking and streak maintenance remain core gameplay
- Accessory collection and customization continue to be available
- Journal entries continue to be generated for evolution and streak milestones

## Rules

### Pet Evolution Limit
The pet has a maximum evolution stage of 4 (Ancient Dragon):
- **Evolution Stages**: 0=Egg, 1=Hatchling, 2=Young Dragon, 3=Adult Dragon, 4=Ancient Dragon
- **Maximum Stage**: 4 (Ancient Dragon) - no further visual evolution possible
- **Evolution Calculation** (inconsistent between two implementations):
  * HabitCompletionRepositoryImpl: Stage 4 at ≥2500 XP
  * HabitDetailViewModel: Stage 4 at ≥6000 XP
- Once stage 4 is reached, the pet remains at this evolution stage regardless of additional XP earned

### Level Progression (Uncapped)
- No maximum level exists
- Level formula: Each level L requires (100 + L × 50) XP to reach from level L-1
- This creates an arithmetic progression where each subsequent level requires 50 more XP than the previous
- Example XP requirements:
  * Level 1: 100 XP
  * Level 2: 150 XP (cumulative: 250)
  * Level 3: 200 XP (cumulative: 450)
  * Level 10: 550 XP (cumulative: 2950)
  * Level 25: 1200 XP (cumulative: 14750)
  * Level 50: 2450 XP (cumulative: 63250)
  * Level 100: 4950 XP (cumulative: 249500)

### Achievement Completion
- All 7 achievements can be unlocked
- Once unlocked, achievements remain unlocked permanently
- No additional achievements exist beyond the initial set
- No achievement progression tiers (e.g., bronze/silver/gold)

### Streak System
- Streaks can continue indefinitely
- Chest rewards continue every 7 days (7, 14, 21, 28, ...)
- No streak-based achievements beyond 30-day streak
- No special recognition for extremely long streaks (e.g., 100+ days beyond journal entry)

### Journal System
- Journal entries continue to be generated for:
  * Pet arrival (one-time)
  * Evolution milestones (one-time per stage)
  * Streak milestones (7, 14, 30, 60, 100 days, then repeats at 60-day intervals)
- No additional journal entry types for extreme milestones

### Economy
- Coin earning continues through:
  * Level-up bonuses (level × 10 coins)
  * Level-up chests (20 coins)
  * Streak chests (50 coins every 7 days)
  * Achievement rewards (fixed amounts)
- No coin caps or diminishing returns
- Accessory purchases continue to be available

## Configuration

All progression systems use hardcoded formulas with no caps:
- Evolution stage thresholds: Hardcoded in two inconsistent implementations
- Level formula: 100 + (level × 50) XP per level
- Streak chest interval: 7 days
- Streak chest reward: 50 coins
- Level-up chest reward: 20 coins
- Level-up base reward: level × 10 coins

## Data Model

No special endgame state is tracked:
- **PetEntity**: Continues to store increasing `level` and `xp` values, with `evolutionStage` capped at 4
- **StatisticsEntity**: Continues to accumulate `totalXp`, `totalCompletions`, `totalCoins`, etc.
- **AchievementEntity**: Achievement completion status (`isUnlocked`) remains true after unlocking
- No additional entities or fields track "completion" or endgame status

## Source Files

- app/src/main/java/com/example/mobile/data/local/entities/PetEntity.kt
- app/src/main/java/com/example/mobile/data/local/entities/StatisticsEntity.kt
- app/src/main/java/com/example/mobile/data/repository/HabitCompletionRepositoryImpl.kt
- app/src/main/java/com/example/mobile/presentation/viewmodel/HabitDetailViewModel.kt
- app/src/main/java/com/example/mobile/domain/AchievementEngine.kt
- app/src/main/java/com/example/mobile/domain/StreakEngine.kt
- app/src/main/java/com/example/mobile/domain/JournalEngine.kt

## Known Gaps

1. **No True Endgame**: The game lacks completion conditions, victory states, or content that signifies "beating" the game.
2. **Evolution Cap**: Visual pet evolution stops at Ancient Dragon with no further transformations despite infinite leveling.
3. **Achievement Completion**: No additional content or recognition after unlocking all achievements.
4. **Streak Recognition**: No special rewards or recognition for exceptionally long streaks beyond the standard 7-day chest cycle.
5. **Progression Saturation**: While numbers continue to increase, the gameplay loop remains identical regardless of progress level.
6. **No Prestige/Rebirth System**: No mechanism to reset progress for bonus rewards or ongoing engagement.
7. **Milestone Gaps**: Large gaps between meaningful milestones (e.g., no recognition between level 25 and much higher levels).
8. **Visual Stagnation**: Pet appearance doesn't change after Ancient Dragon despite continuing to level up.