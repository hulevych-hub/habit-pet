# Dragon Phases

## Overview

The dragon phase system tracks the pet's evolution through five stages based on accumulated XP. Each stage represents a visual transformation of the pet and should feel like a meaningful milestone in the player's long-term attachment to the dragon.

## Source of truth

Evolution stages are centralized in `ExpConfig.kt`:

- `ExpConfig.EVOLUTION_THRESHOLDS`
- `ExpConfig.EVOLUTION_STAGE_NAMES`
- `ExpConfig.calculateEvolutionStageFromXp()`
- `ExpConfig.evolutionStageName()`
- `ExpConfig.xpThresholdForStage()`

Runtime code should not hardcode evolution thresholds.

## Evolution stages

The pet has five evolution stages:

| Stage | Name | XP Range |
|---:|---|---:|
| 0 | Egg | `0-74` |
| 1 | Hatchling | `75-299` |
| 2 | Young Dragon | `300-899` |
| 3 | Adult Dragon | `900-2499` |
| 4 | Ancient Dragon | `2500+` |

## Rebalanced thresholds

| Evolution | Old threshold | New threshold | Old pacing at 100 XP/habit | New pacing, mostly checkbox habits |
|---|---:|---:|---:|---:|
| Egg → Hatchling | `500 XP` | `75 XP` | 5 habits / ~1.7 days at 3/day | 2-3 days at 3-5/day |
| Hatchling → Young Dragon | `1500 XP` | `300 XP` | 15 habits / ~5 days at 3/day | 5-8 more days |
| Young Dragon → Adult Dragon | `3000 XP` | `900 XP` | 30 habits / ~10 days at 3/day | 12-20 more days |
| Adult Dragon → Ancient Dragon | `6000 XP` | `2500 XP` | 60 habits / ~20 days at 3/day | 32-54 more days |

Ancient Dragon is now a long-term attachment goal for mostly checkbox play.

## Expected progression timeline

Assumptions:

- 3-5 habits completed per day
- Mostly checkbox habits
- 5-15 minutes of daily play
- Challenge completion may add a small amount of bonus XP but does not reset daily
- No heavy timer-habit grinding

| Stage | Total XP required | Total checkbox habits | Days at 3/day | Days at 5/day | Balance intent |
|---|---:|---:|---:|---:|---|
| Egg | `0` | 0 | 0 | 0 | Immediate onboarding form. |
| Hatchling | `75` | 8 | 2-3 | 1-2 | Achievable within the first few days. |
| Young Dragon | `300` | 30 | 6-10 | 4-6 | Sustained early engagement. |
| Adult Dragon | `900` | 90 | 18-30 | 12-18 | Long-term play milestone. |
| Ancient Dragon | `2500` | 250 | 50-83 | 33-50 | Weeks-to-months endgame achievement. |

## Evolution triggers

Evolution stage increases when:

1. XP crosses a threshold in `ExpConfig.EVOLUTION_THRESHOLDS`.
2. The pet object is updated through one of the progression paths:
   - `HabitCompletionRepositoryImpl.updatePetProgress()`
   - `HabitDetailViewModel.awardPetXpAndCoins()`
   - `RewardManager.addPetExp()` for reward-flow XP

## Evolution events

When evolution stage increases:

1. A `DragonEvolutionReward` event is added to the reward queue.
2. `ActivityTimelineEngine` logs a dragon evolution event with previous stage, reached stage, and current XP.
3. `ActivityTimelineEngine` logs the next evolution milestone nearing event when progress reaches 80%.
4. The reward overlay shows a phase transition screen after level-up rewards, then the pet settles into the new idle animation.
5. UI displays the new stage name, e.g. `"Luna Lv. 5 Adult Dragon"`.

Evolution itself remains reward-neutral. It does not generate coins, XP, or economy-changing rewards.

## Visual representation

Each evolution stage loads its dragon base from the matching phase asset folder:

- Stage 0 (Egg): `res/drawable/egg/default`
- Stage 1 (Hatchling): `res/drawable/hatchling/default`
- Stage 2 (Young Dragon): `res/drawable/young/default`
- Stage 3 (Adult Dragon): `res/drawable/adult/default`
- Stage 4 (Ancient Dragon): `res/drawable/ancient/default`

`AssetResolver` always looks for the phase `default` first, then accepts a phase-named default alias if one exists. If an aura or outfit has `phase = null` in `EquipableConfig`, it is treated as usable at any dragon phase and the resolver searches all phase folders. Otherwise, the resolver tries the configured phase folder first and falls back to the phase default when the aura asset is missing. If no dragon base asset exists, the dragon base layer is skipped and the missing asset is logged; the app does not crash.

## Rendering order

The rendering order is fixed:

1. Background
2. Dragon base image or aura image
3. Outfit overlay

If an aura is equipped, the aura image replaces the dragon base image. Outfit overlay is always rendered above the dragon or aura.

## Idle animations

Idle animation behavior is handled in `AnimatedPet.kt` with Compose infinite transitions.

- Egg: slow breathing scale, gentle left-right tilt, and very subtle bounce
- Hatchling: breathing scale plus vertical motion with slow idle sway
- Young Dragon: breathing scale plus vertical motion with slower idle sway
- Adult Dragon: calmer breathing scale plus vertical motion and slower sway
- Ancient Dragon: slowest and smallest breathing scale, vertical motion, and sway

## Phase transitions

Phase transition behavior is shared through `PetPhaseTransition` in `AnimatedPet.kt`. When `PetEntity.evolutionStage` changes to a new adjacent stage, the previous stage image crossfades into the next stage image with easing-based scale and vertical shift.

Transitions:

- Stage 0 → 1: Egg → Hatchling
- Stage 1 → 2: Hatchling → Young Dragon
- Stage 2 → 3: Young Dragon → Adult Dragon
- Stage 3 → 4: Adult Dragon → Ancient Dragon

## Mood

Mood is calculated by `DragonMoodEngine` from:

1. `currentStreak` from `StatisticsRepository`
2. Last activity timestamp from recent habit completions
3. Recent habit completion count within the last 72 hours

Mood states:

- `Lonely`: last activity is older than 36 hours
- `Proud`: current streak is at least 7 days
- `Excited`: 3 or more habit completions occurred in the last 72 hours
- `Happy`: current streak is greater than 0
- `Calm`: default state when no stronger mood applies

Mood is persisted in `PetEntity.mood`. `DragonMoodEngine.refreshMood()` is called on app open, habit completion, and streak changes.

## Known gaps

1. Evolution rewards are progression-neutral and do not grant coins or XP.
2. Evolution stages do not change gameplay mechanics beyond visuals, mood, and reward-overlay presentation.
3. No additional evolution requirements exist beyond XP.
4. Some late achievement-only customization rewards may become no-ops if the item was already unlocked earlier.
