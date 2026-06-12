# ENDGAME

## Overview

Habit Pet does not have a defined "endgame" state or completion condition. The game is designed as an open-ended habit tracking experience where players can continue to progress indefinitely through leveling, XP accumulation, streaks, customization collection, and journal milestones. Reaching the maximum pet evolution stage (Ancient Dragon) is a milestone rather than an endpoint.

## Current Implementation

The game continues to provide progression and rewards beyond reaching maximum evolution:
- XP accumulation continues infinitely with no level cap
- Coin earning continues through habit completions, achievements, streak milestone chests, and level-ups
- Habit tracking and streak maintenance remain core gameplay
- Customization collection continues through outfits, backgrounds, and auras
- Activity timeline events continue to be logged for evolution and streak milestones

## Rules

### Pet Evolution Limit
The pet has a maximum evolution stage of 4 (Ancient Dragon):
- **Evolution Stages**: 0=Egg, 1=Hatchling, 2=Young Dragon, 3=Adult Dragon, 4=Ancient Dragon
- **Maximum Stage**: 4 (Ancient Dragon) - no further visual evolution possible
- **Evolution Calculation**: centralized in `ExpConfig.calculateEvolutionStageFromXp()`
- **Evolution Thresholds**:
  - Stage 0: 0 XP
  - Stage 1: 500 XP
  - Stage 2: 1,500 XP
  - Stage 3: 3,000 XP
  - Stage 4: 6,000 XP
- Once stage 4 is reached, the pet remains at this evolution stage regardless of additional XP earned

### Level Progression (Uncapped)
- No maximum level exists
- Level formula: each level L requires `100 + (L - 1) × 50` XP to reach from level L-1
- This creates an arithmetic progression where each subsequent level requires 50 more XP than the previous
- Example XP requirements:
  * Level 1: 100 XP (cumulative: 100)
  * Level 2: 150 XP (cumulative: 250)
  * Level 3: 200 XP (cumulative: 450)
  * Level 10: 550 XP (cumulative: 3,250)
  * Level 25: 1,300 XP (cumulative: 17,500)
  * Level 50: 2,550 XP (cumulative: 66,250)
  * Level 100: 5,050 XP (cumulative: 250,000)

### Achievement Completion
- The default achievement set includes habit creation, completion, streak, XP, level, and customization milestones
- Once unlocked, achievements remain unlocked permanently
- Claimed achievements remain visible but are no longer claimable
- No achievement progression tiers (e.g., bronze/silver/gold) are currently implemented

### Streak System
- Streaks can continue indefinitely
- Chest rewards are awarded at configured milestones: 7, 14, 30, 60, 100
- No streak-based achievements beyond 30-day streak
- Special recognition for extremely long streaks is limited to the configured milestone rewards and activity timeline events

### Activity Timeline
- Activity timeline events continue to be logged for:
  * Pet arrival (one-time)
  * Evolution milestones (one-time per stage)
  * Streak milestones (7, 14, 30, 60, 100 days)
- No additional timeline event types for extreme milestones

### Economy
- Coin earning continues through:
  * Checkbox habit completions: 10 coins
  * Timer habit completions: 5 base coins + 2 coins per minute
  * Level-up base rewards: level × 10 coins
  * Level-up chests: randomized coins, EXP, and possible customization item
  * Streak milestone chests: milestone-based chest rewards
  * Achievement rewards: fixed amounts and occasional chests
- No coin caps or diminishing returns
- Customization purchases continue to be available for outfits, backgrounds, and auras

## Configuration

All progression systems use centralized configuration where available:
- Evolution stage thresholds: `ExpConfig.EVOLUTION_THRESHOLDS`
- Level formula: `ExpConfig.BASE_XP_FOR_LEVEL_1` + `ExpConfig.XP_PER_LEVEL_INCREMENT`
- Habit XP rewards: `ExpConfig`
- Habit coin rewards: `EconomyConfig`
- Chest reward ranges and customization drop chances: `EconomyConfig` + `ChestRewardConfigProvider`
- Streak chest milestones: `StreakEngine.STREAK_MILESTONES`
- Level-up base reward: `ExpConfig.LEVEL_UP_COIN_MULTIPLIER`

## Data Model

No special endgame state is tracked:
- **PetEntity**: Continues to store increasing `level` and `xp` values, with `evolutionStage` capped at 4
- **StatisticsEntity**: Continues to accumulate `totalXp`, `totalCompletions`, `totalCoins`, etc.
- **AchievementEntity**: Achievement completion status (`isUnlocked`, `isClaimed`) remains after unlocking/claiming
- **InventoryItemEntity**: Stores customization ownership, purchase, and equipped state
- No additional entities or fields track "completion" or endgame status

## Source Files

- app/src/main/java/com/example/mobile/data/local/entities/PetEntity.kt
- app/src/main/java/com/example/mobile/data/local/entities/StatisticsEntity.kt
- app/src/main/java/com/example/mobile/data/local/entities/InventoryItemEntity.kt
- app/src/main/java/com/example/mobile/data/repository/HabitCompletionRepositoryImpl.kt
- app/src/main/java/com/example/mobile/presentation/viewmodel/HabitDetailViewModel.kt
- app/src/main/java/com/example/mobile/domain/AchievementEngine.kt
- app/src/main/java/com/example/mobile/domain/StreakEngine.kt
- app/src/main/java/com/example/mobile/domain/ExpConfig.kt
- app/src/main/java/com/example/mobile/domain/EconomyConfig.kt

## Known Gaps

1. **No True Endgame**: The game lacks completion conditions, victory states, or content that signifies "beating" the game.
2. **Evolution Cap**: Visual pet evolution stops at Ancient Dragon with no further transformations despite infinite leveling.
3. **Achievement Completion**: No additional content or recognition after unlocking all achievements.
4. **Streak Recognition**: No special rewards or recognition for exceptionally long streaks beyond the configured milestone chests.
5. **Progression Saturation**: While numbers continue to increase, the gameplay loop remains identical regardless of progress level.
6. **No Prestige/Rebirth System**: No mechanism to reset progress for bonus rewards or ongoing engagement.
7. **Milestone Gaps**: Large gaps between meaningful milestones (e.g., no recognition between level 25 and much higher levels).
8. **Visual Stagnation**: Pet appearance doesn't change after Ancient Dragon despite continuing to level up.
