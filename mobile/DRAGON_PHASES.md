# DRAGON_PHASES

## Overview

The dragon phase system in Habit Pet tracks the pet's evolution through five distinct stages based on accumulated XP. Each stage represents a visual transformation of the pet and influences how it is displayed in the game.

## Current Implementation

The dragon phase system consists of:
- Evolution stage stored in PetEntity (evolution_stage field: 0-4)
- Stage determination based on XP thresholds (**NOW CONSISTENT - single source in `ExpConfig`**)
- Visual representation through different pet images for each stage
- Lightweight idle animations for each stage using subtle scale, rotation, and vertical translation
- Journal logging of evolution events
- Evolution stage rewards (DragonEvolutionReward - currently 0 coins)
- Display in UI showing stage name alongside level
- **Centralized configuration: `ExpConfig`** (single source of truth)

## Rules

### Evolution Stages
The pet has five evolution stages, represented by integer values:
- **Stage 0: Egg** - Initial form
- **Stage 1: Hatchling** - First evolution
- **Stage 2: Young Dragon** - Second evolution
- **Stage 3: Adult Dragon** - Third evolution
- **Stage 4: Ancient Dragon** - Final form

### Evolution Stage Calculation (NOW CONSISTENT)

**Single source of truth: `ExpConfig.calculateEvolutionStageFromXp()`**

| Stage | Name | XP Range | Threshold |
|-------|------|----------|-----------|
| 0 | Egg | 0 - 499 | 0 |
| 1 | Hatchling | 500 - 1,499 | 500 |
| 2 | Young Dragon | 1,500 - 2,999 | 1,500 |
| 3 | Adult Dragon | 3,000 - 5,999 | 3,000 |
| 4 | Ancient Dragon | 6,000+ | 6,000 |

*Previously inconsistent between HabitCompletionRepositoryImpl and HabitDetailViewModel - now unified in ExpConfig*

### Visual Representation
Each evolution stage has a corresponding visual representation:
- Stage 0 (Egg): R.drawable.egg
- Stage 1 (Hatchling): R.drawable.hatchling
- Stage 2 (Young Dragon): R.drawable.young_dragon
- Stage 3 (Adult Dragon): R.drawable.adult_dragon
- Stage 4 (Ancient Dragon): R.drawable.ancient_dragon

Idle animation behavior is handled in `AnimatedPet.kt` with Compose infinite transitions so the static drawables feel alive without frame swapping:
- Stage 0 (Egg): slow breathing scale, gentle left-right tilt, and very subtle bounce
- Stage 1 (Hatchling): breathing scale plus vertical motion with slow idle sway
- Stage 2 (Young Dragon): breathing scale plus vertical motion with slower idle sway
- Stage 3 (Adult Dragon): calmer breathing scale plus vertical motion and slower sway
- Stage 4 (Ancient Dragon): slowest and smallest breathing scale, vertical motion, and sway

### Evolution Triggers
Evolution stage increases when:
1. XP crosses a threshold in `ExpConfig.EVOLUTION_THRESHOLDS`
2. The pet object is updated via `HabitCompletionRepositoryImpl.updatePetProgress()` OR `HabitDetailViewModel.awardPetXpAndCoins()` (both now use `ExpConfig`)

### Evolution Events
When evolution stage increases:
1. **Journal Entry**: JournalEngine creates a log entry "[Pet Name] evolved to [Stage Name]."
2. **Reward**: A DragonEvolutionReward event is added to the reward queue (currently provides 0 coins)
3. **Visual Update**: The pet's image changes to match the new stage
4. **UI Update**: Display shows new stage name (e.g., "Level 5 Ancient Dragon")

### Display Format
In the home screen and pet screen, the pet is displayed as:
"{pet.name} Lv. {pet.level} {evolution stage name}"
Example: "Luna Lv. 5 Ancient Dragon"

Stage names sourced from `ExpConfig.EVOLUTION_STAGE_NAMES`

## Configuration

Evolution stage thresholds are now centralized in `ExpConfig`:
- `ExpConfig.EVOLUTION_THRESHOLDS` - XP thresholds for each stage
- `ExpConfig.EVOLUTION_STAGE_NAMES` - Display names for each stage
- `ExpConfig.calculateEvolutionStageFromXp()` - Single calculation function
- `ExpConfig.evolutionStageName()` - Single name lookup function

Visual asset mappings and idle animation behavior remain in:
- AnimatedPet.kt (pet images, backgrounds, equipped items, and stage-specific idle animations)

## Data Model

**PetEntity** (app/src/main/java/com/example/mobile/data/local/entities/PetEntity.kt):
- `evolutionStage: Int` - current evolution stage (0-4)
- Comments indicate mapping: 0: Egg, 1: Hatchling, 2: Young Dragon, 3: Adult Dragon, 4: Ancient Dragon

## Source Files

- app/src/main/java/com/example/mobile/data/local/entities/PetEntity.kt
- app/src/main/java/com/example/mobile/data/repository/HabitCompletionRepositoryImpl.kt
- app/src/main/java/com/example/mobile/presentation/viewmodel/HabitDetailViewModel.kt
- app/src/main/java/com/example/mobile/presentation/ui/components/AnimatedPet.kt
- app/src/main/java/com/example/mobile/domain/JournalEngine.kt
- app/src/main/java/com/example/mobile/presentation/ui/screens/HomeScreen.kt
- app/src/main/java/com/example/mobile/presentation/ui/screens/PetScreen.kt
- app/src/main/java/com/example/mobile/domain/ExpConfig.kt (NEW - centralized configuration)

## Known Gaps (UPDATED)

1. ✅ **FIXED: Inconsistent Evolution Calculation** - Now single source in `ExpConfig.calculateEvolutionStageFromXp()`
2. **Valueless Evolution**: DragonEvolutionReward provides 0 coins, making evolution stages progression-reward neutral.
3. **Stage Names Hardcoded in UI**: Evolution stage names still duplicated in HomeScreen.kt, PetScreen.kt (should use `ExpConfig.evolutionStageName()`)
4. **No Evolution Effects**: Beyond visual changes and journal entries, evolution stages don't affect gameplay mechanics (no stat boosts, special abilities, etc.).
5. **Visual Inconsistency**: While pet images change with evolution, equipped items (hats, glasses, etc.) remain the same across all stages, which may look incongruous.
6. **No Evolution Requirements**: No additional requirements beyond XP (e.g., specific habits, time periods, or items) needed to evolve.

## Progression Timeline

| Stage | XP Required | Est. Checkbox Habits | Est. Days (3/day) |
|-------|-------------|---------------------|-------------------|
| Egg (0) | 0 | 0 | 0 |
| Hatchling (1) | 500 | 5 | ~1.5 |
| Young Dragon (2) | 1,500 | 15 | ~5 |
| Adult Dragon (3) | 3,000 | 30 | ~10 |
| Ancient Dragon (4) | 6,000 | 60 | ~20 |

*Assumes 100 XP per checkbox habit completion*