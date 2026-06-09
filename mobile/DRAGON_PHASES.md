# DRAGON_PHASES

## Overview

The dragon phase system in Habit Pet tracks the pet's evolution through five distinct stages based on accumulated XP. Each stage represents a visual transformation of the pet and influences how it is displayed in the game.

## Current Implementation

The dragon phase system consists of:
- Evolution stage stored in PetEntity (evolution_stage field: 0-4)
- Stage determination based on XP thresholds (with inconsistent implementations)
- Visual representation through different pet images for each stage
- Journal logging of evolution events
- Evolution stage rewards (though currently valueless)
- Display in UI showing stage name alongside level

## Rules

### Evolution Stages
The pet has five evolution stages, represented by integer values:
- **Stage 0: Egg** - Initial form
- **Stage 1: Hatchling** - First evolution
- **Stage 2: Young Dragon** - Second evolution
- **Stage 3: Adult Dragon** - Third evolution
- **Stage 4: Ancient Dragon** - Final form

### Evolution Stage Calculation (INCONSISTENT)
There are two different implementations for calculating evolution stage from XP:

**In HabitCompletionRepositoryImpl.kt:**
- Egg: 0-99 XP
- Hatchling: 100-349 XP
- Young Dragon: 350-999 XP
- Adult Dragon: 1000-2499 XP
- Ancient Dragon: 2500+ XP

**In HabitDetailViewModel.kt:**
- Egg: 0-499 XP
- Hatchling: 500-1499 XP
- Young Dragon: 1500-2999 XP
- Adult Dragon: 3000-5999 XP
- Ancient Dragon: 6000+ XP

*Note: This inconsistency means the pet's evolution stage may differ depending on which code path last updated the pet.*

### Visual Representation
Each evolution stage has a corresponding visual representation:
- Stage 0 (Egg): R.drawable.egg
- Stage 1 (Hatchling): R.drawable.hatchling
- Stage 2 (Young Dragon): R.drawable.young_dragon
- Stage 3 (Adult Dragon): R.drawable.adult_dragon
- Stage 4 (Ancient Dragon): R.drawable.ancient_dragon

### Evolution Triggers
Evolution stage increases when:
1. XP crosses a threshold threshold in either calculation method
2. The pet object is updated via HabitCompletionRepositoryImpl.updatePetProgress() OR HabitDetailViewModel.awardPetXpAndCoins()

### Evolution Events
When evolution stage increases:
1. **Journal Entry**: JournalEngine creates a log entry "[Pet Name] evolved to stage [X]."
2. **Reward**: A DragonEvolutionReward event is added to the reward queue (currently provides 0 coins)
3. **Visual Update**: The pet's image changes to match the new stage
4. **UI Update**: Display shows new stage name (e.g., "Level 5 Ancient Dragon")

### Display Format
In the home screen and pet screen, the pet is displayed as:
"{pet.name} Lv. {pet.level} {evolution stage name}"
Example: "Luna Lv. 5 Ancient Dragon"

## Configuration

Evolution stage thresholds are hardcoded in two locations:
- HabitCompletionRepositoryImpl.kt lines 155-161
- HabitDetailViewModel.kt lines 380-388

Visual asset mappings are hardcoded in:
- AnimatedPet.kt lines 42-49 (pet images)
- AnimatedPet.kt lines 61-67 (backgrounds)
- AnimatedPet.kt lines 89-92 (scarves)
- AnimatedPet.kt lines 105-108 (glasses)
- AnimatedPet.kt lines 121-125 (hats)

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

## Known Gaps

1. **Inconsistent Evolution Calculation**: Two different XP-to-stage formulas exist, causing potential desynchronization between game systems.
2. **Valueless Evolution**: DragonEvolutionReward provides 0 coins, making evolution stages progression-reward neutral.
3. **Stage Names Hardcoded**: Evolution stage names are duplicated in multiple places (HomeScreen.kt, PetEntity.kt comments, AnimatedPet.kt logic).
4. **No Evolution Effects**: Beyond visual changes and journal entries, evolution stages don't affect gameplay mechanics (no stat boosts, special abilities, etc.).
5. **Visual Inconsistency**: While pet images change with evolution, equipped items (hats, glasses, etc.) remain the same across all stages, which may look incongruous.
6. **No Evolution Requirements**: No additional requirements beyond XP (e.g., specific habits, time periods, or items) needed to evolve.