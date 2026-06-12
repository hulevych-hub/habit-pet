# DRAGON_PHASES

## Overview

The dragon phase system in Habit Pet tracks the pet's evolution through five distinct stages based on accumulated XP. Each stage represents a visual transformation of the pet and influences how it is displayed in the game.

## Current Implementation

The dragon phase system consists of:
- Evolution stage stored in PetEntity (evolution_stage field: 0-4)
- Stage determination based on XP thresholds (**NOW CONSISTENT - single source in `ExpConfig`**)
- Visual representation through different pet images for each stage
- Lightweight idle animations for each stage using subtle scale, rotation, and vertical translation
- Dragon mood state persisted in `PetEntity.mood` with values: `Happy`, `Calm`, `Excited`, `Proud`, and `Lonely`
- Mood-driven idle intensity, alpha, and speed adjustments in `AnimatedPet.kt`
- One-time phase transition animations for adjacent evolution stages using crossfade, scale, and vertical shift
- ActivityTimeline logging of evolution and evolution milestone nearing events
- Reward-overlay phase transition screen that appears after level-up rewards for evolution events
- Evolution stage rewards (DragonEvolutionReward - currently 0 coins)
- Display in UI showing stage name alongside level
- Evolution teasing UI through `EvolutionTeaser`, showing the next stage name and XP needed to reach it
- Evolution milestone timeline events through `ActivityTimelineEngine.logEvolutionMilestoneNearing()` when progress reaches 80% toward a locked stage
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

Phase transition behavior is shared through `PetPhaseTransition` in `AnimatedPet.kt`. When `PetEntity.evolutionStage` changes to a new adjacent stage, the previous stage image crossfades into the next stage image with easing-based scale and vertical shift. The reward overlay uses the same component for evolution reward screens. The transitions are:
- Stage 0 → 1: Egg → Hatchling
- Stage 1 → 2: Hatchling → Young Dragon
- Stage 2 → 3: Young Dragon → Adult Dragon
- Stage 3 → 4: Adult Dragon → Ancient Dragon

### Evolution Triggers
Evolution stage increases when:
1. XP crosses a threshold in `ExpConfig.EVOLUTION_THRESHOLDS`
2. The pet object is updated via `HabitCompletionRepositoryImpl.updatePetProgress()` OR `HabitDetailViewModel.awardPetXpAndCoins()` (both now use `ExpConfig`)

### Evolution Events
When evolution stage increases:
1. **Reward**: A DragonEvolutionReward event is added to the reward queue (currently provides 0 coins)
2. **Timeline Event**: ActivityTimelineEngine logs a dragon evolution event with the previous stage, reached stage, and current XP
3. **Milestone Nearing Event**: XP reward paths call `ActivityTimelineEngine.logEvolutionMilestoneNearing()` for the next locked stage; it records one event when that stage reaches 80% progress
4. **Visual Update**: The reward overlay shows a phase transition screen after level-up rewards, then the pet settles into the new idle animation
5. **UI Update**: Display shows new stage name (e.g., "Level 5 Ancient Dragon")

### Display Format
In the home screen and pet screen, the pet is displayed as:
"{pet.name} Lv. {pet.level} {evolution stage name}"
Example: "Luna Lv. 5 Ancient Dragon"

Stage names are sourced from `ExpConfig.EVOLUTION_STAGE_NAMES`. `ProgressHeader` shows a compact next evolution label and evolution percentage, while `EvolutionTeaser` shows the next stage name, a progress bar, and the XP needed to reach it.

### Dragon Mood
`DragonMood` defines the current mood state used by the dragon rendering system:

- `HAPPY` / `Happy`
- `CALM` / `Calm`
- `EXCITED` / `Excited`
- `PROUD` / `Proud`
- `LONELY` / `Lonely`

Mood is calculated by `DragonMoodEngine` from three inputs:

1. `currentStreak` from `StatisticsRepository`
2. Last activity timestamp from recent habit completions
3. Recent habit completion count within the last 72 hours

Calculation priority:

- `Lonely`: last activity is older than 36 hours
- `Proud`: current streak is at least 7 days
- `Excited`: 3 or more habit completions occurred in the last 72 hours
- `Happy`: current streak is greater than 0
- `Calm`: default state when no stronger mood applies

Mood is persisted in `PetEntity.mood`. `DragonMoodEngine.refreshMood()` is called on app open, habit completion, and streak changes. `PetDao.resetPet()` resets the stored mood to `Calm`.

### Mood Rendering
`AnimatedPet.kt` applies mood to the dragon rendering system without changing evolution logic:

- `PetAnimations.applyMoodModifier()` adjusts idle scale, vertical motion, rotation, and bounce amplitude.
- `PetAnimations.moodDurationMultiplier()` adjusts idle animation speed.
- `PetAnimations.moodIntensity()` applies a subtle alpha/brightness-style intensity modifier to the pet image.
- Home and Pet screens display the current mood label from `DragonMood.from(pet.mood).displayName`.

## Configuration

Evolution stage thresholds are now centralized in `ExpConfig`:
- `ExpConfig.EVOLUTION_THRESHOLDS` - XP thresholds for each stage
- `ExpConfig.EVOLUTION_STAGE_NAMES` - Display names for each stage
- `ExpConfig.calculateEvolutionStageFromXp()` - Single calculation function
- `ExpConfig.evolutionStageName()` - Single name lookup function
- `ExpConfig.xpThresholdForStage()` - Single XP threshold lookup function used by evolution teasing UI and timeline milestone events

Visual asset mappings and animation behavior remain in:
- AnimatedPet.kt (pet images, backgrounds, equipped items, stage-specific idle animations, and shared phase transitions)
- PetTransitionPrefs.kt (SharedPreferences keys that prevent completed phase transitions from replaying)
- RewardScreen.kt (reward-overlay phase transition screen)
- RewardManager.kt and RewardOverlayHost.kt (current pet state passed into reward screens)

## Data Model

**PetEntity** (app/src/main/java/com/example/mobile/data/local/entities/PetEntity.kt):
- `evolutionStage: Int` - current evolution stage (0-4)
- `mood: String` - current dragon mood (`Happy`, `Calm`, `Excited`, `Proud`, or `Lonely`)
- Comments indicate mapping: 0: Egg, 1: Hatchling, 2: Young Dragon, 3: Adult Dragon, 4: Ancient Dragon

## Source Files

- app/src/main/java/com/example/mobile/data/local/entities/PetEntity.kt
- app/src/main/java/com/example/mobile/data/repository/HabitCompletionRepositoryImpl.kt
- app/src/main/java/com/example/mobile/presentation/viewmodel/HabitDetailViewModel.kt
- app/src/main/java/com/example/mobile/presentation/viewmodel/HabitsViewModel.kt
- app/src/main/java/com/example/mobile/presentation/ui/components/AnimatedPet.kt
- app/src/main/java/com/example/mobile/presentation/ui/animations/PetAnimations.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardScreen.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardManager.kt
- app/src/main/java/com/example/mobile/presentation/ui/reward/RewardOverlayHost.kt
- app/src/main/java/com/example/mobile/presentation/ui/components/ProgressHeader.kt
- app/src/main/java/com/example/mobile/util/PetTransitionPrefs.kt
- app/src/main/java/com/example/mobile/domain/ExpConfig.kt (centralized configuration)
- app/src/main/java/com/example/mobile/domain/ActivityTimelineEngine.kt
- app/src/main/java/com/example/mobile/domain/GameEventFactory.kt
- app/src/main/java/com/example/mobile/domain/DragonMood.kt
- app/src/main/java/com/example/mobile/domain/DragonMoodEngine.kt
- app/src/main/java/com/example/mobile/domain/StreakEngine.kt
- app/src/main/java/com/example/mobile/MainActivity.kt
- app/src/main/java/com/example/mobile/data/local/dao/PetDao.kt
- app/src/main/java/com/example/mobile/presentation/ui/screens/HomeScreen.kt
- app/src/main/java/com/example/mobile/presentation/ui/screens/PetScreen.kt

## Known Gaps (UPDATED)

1. ✅ **FIXED: Inconsistent Evolution Calculation** - Now single source in `ExpConfig.calculateEvolutionStageFromXp()`
2. **Valueless Evolution**: DragonEvolutionReward provides 0 coins, making evolution stages progression-reward neutral.
3. ✅ **FIXED: Stage Names Hardcoded in UI** - Stage names now use `ExpConfig.evolutionStageName()`, and `EvolutionTeaser` uses the same centralized stage names.
4. **No Evolution Effects**: Beyond visual changes and journal entries, evolution stages don't affect gameplay mechanics (no stat boosts, special abilities, etc.).
5. ✅ **FIXED: Evolution Teasing Visibility** - Home, pet, and progress header surfaces now show the next evolution stage and XP needed to reach it.
6. ✅ **FIXED: Evolution Milestone Timeline Visibility** - XP reward paths now create activity timeline events when a locked evolution stage reaches 80% progress.
7. **Visual Inconsistency**: While pet images change with evolution, equipped outfit and aura placeholders remain the same across all stages, which may look incongruous.
8. **No Evolution Requirements**: No additional requirements beyond XP (e.g., specific habits, time periods, or items) needed to evolve.

## Progression Timeline

| Stage | XP Required | Est. Checkbox Habits | Est. Days (3/day) |
|-------|-------------|---------------------|-------------------|
| Egg (0) | 0 | 0 | 0 |
| Hatchling (1) | 500 | 5 | ~1.5 |
| Young Dragon (2) | 1,500 | 15 | ~5 |
| Adult Dragon (3) | 3,000 | 30 | ~10 |
| Ancient Dragon (4) | 6,000 | 60 | ~20 |

*Assumes 100 XP per checkbox habit completion*

## Progression Timing Validation

The evolution timeline is intentionally tied to the same EXP values used for level progression:

| Stage | XP Threshold | Est. Checkbox Habits | Est. Days (3/day) | Balance Intent |
|-------|-------------|---------------------|-------------------|----------------|
| Egg (0) | 0 | 0 | 0 | Immediate onboarding form |
| Hatchling (1) | 500 | 5 | ~1.7 | First visible evolution early enough to feel rewarding |
| Young Dragon (2) | 1,500 | 15 | ~5 | Second evolution within the first week |
| Adult Dragon (3) | 3,000 | 30 | ~10 | Mid-game milestone for consistent players |
| Ancient Dragon (4) | 6,000 | 60 | ~20 | Long-term goal that remains reachable |

Timer habits accelerate this timeline, while chest EXP can add small additional progression bumps. Evolution itself remains reward-neutral; it does not generate coins or change economy balance.