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
- XP observations trigger achievement unlocks (e.g., 1000 XP achievement)

## Rules

### XP Earning
- Checkbox habit completion awards a fixed 1500 XP
- Timer habit completion awards XP based on duration: (10 + total minutes) XP, where total minutes is the accumulated time spent on the habit
- XP is added to the pet's current XP total

### Level Calculation
- To reach level 1 from level 0: 100 XP required
- To reach level 2 from level 1: 150 XP required
- To reach level L+1 from level L: (100 + L * 50) XP required
- This creates an arithmetic progression where each level requires 50 more XP than the previous level

### Evolution Stages
Pet evolution stages are determined by XP thresholds, but there are two different implementations causing inconsistency:

**In HabitCompletionRepositoryImpl:**
- Egg: 0-99 XP
- Hatchling: 100-349 XP
- Young Dragon: 350-999 XP
- Adult Dragon: 1000-2499 XP
- Ancient Dragon: 2500+ XP

**In HabitDetailViewModel:**
- Egg: 0-499 XP
- Hatchling: 500-1499 XP
- Young Dragon: 1500-2999 XP
- Adult Dragon: 3000-5999 XP
- Ancient Dragon: 6000+ XP

### Achievement Integration
- Reaching 1000 XP unlocks the "1000 XP" achievement (observed via AchievementEngine.observeXp())

## Configuration

No external configuration files found for EXP values. All values are hardcoded in the source code:
- Checkbox habit XP: 1500 (HabitDetailViewModel.kt line 163)
- Timer habit XP base: 10 XP plus 1 XP per minute (HabitDetailViewModel.kt line 244)
- Level formula: 100 + (level * 50) (PetScreen.kt line 163, HabitCompletionRepositoryImpl.kt lines 143-153, HabitDetailViewModel.kt lines 367-378)
- Evolution stage thresholds vary between implementations as noted above

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

## Known Gaps

1. **Inconsistent Evolution Stage Calculation**: Two different formulas for determining pet evolution stage from XP exist in HabitCompletionRepositoryImpl.kt and HabitDetailViewModel.kt, leading to potential discrepancies in pet evolution based on which code path updates the pet.

2. **Redundant Level Calculation**: The level calculation formula is duplicated in multiple places (PetScreen.kt, HabitCompletionRepositoryImpl.kt, HabitDetailViewModel.kt) rather than being centralized.

3. **XP Awarding Inconsistency**: XP is updated through two different paths:
   - Via HabitCompletionRepositoryImpl.updatePetProgress() (used when adding completions through the repository)
   - Via HabitDetailViewModel.awardPetXpAndCoins() (used directly in the ViewModel for habit completion)
   This creates two separate update mechanisms that may evolve differently.