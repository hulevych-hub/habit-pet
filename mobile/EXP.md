# EXP System

## Overview

EXP is the core progression currency in Habit Pet. It levels the pet and unlocks dragon evolution stages. The system is designed to feel rewarding early, then slow into long-term pet attachment over weeks or months.

## Source of truth

All progression values are centralized in `ExpConfig.kt`. Runtime code should read XP, level, combo, and evolution values from `ExpConfig` rather than hardcoding progression numbers. Challenge targets and rewards are configured separately in `ChallengeConfig.kt`.

## Current XP rewards

| Source | Old value | New value | Balance reason |
|---|---:|---:|---|
| Checkbox habit base XP | `1000` | `10` | The old value made high levels and dragon phases arrive far too quickly. |
| Timer habit base XP | `10` | `5` | Timer habits still reward time spent, but no longer outpace checkbox habits by a large margin. |
| Timer habit XP per minute | `5` | `1` | A 15-minute timer habit now grants about `20 XP` instead of `85 XP`. |
| Combo bonus per consecutive completion | `5` | `1` | Combo now encourages momentum without doubling progression speed. |
| Combo max bonus XP | `20` | `4` | Max bonus is small and additive. |
| Surprise direct bonus XP | `25` | `5` | Surprise chests are rare and low-impact for raw XP. |

## Combo / momentum

Consecutive habit completions within a short inactivity window create a short-term combo. The combo bonus is additive, small, and capped.

- Active window: 2 hours (`ExpConfig.COMBO_INACTIVITY_WINDOW_MS`)
- Bonus: `+1 XP` per consecutive completion after the first
- Maximum bonus per habit: `+4 XP`
- Combo milestones: `3`, `5`, and `10` hits

| Combo hit | XP bonus |
|---:|---:|
| 1 | `0` |
| 2 | `1` |
| 3 | `2` |
| 4 | `3` |
| 5+ | `4` |

Combo XP is recorded in `HabitCompletionRepositoryImpl.addCompletionWithCombo()` and contributes to XP challenge progress through `HabitCompletionEntity.xpEarned`.

## Challenges

Challenge progress is tracked separately from XP progression. XP earned from habits, rewards, and other reward-flow events can advance active XP challenges, but challenges do not reset daily and do not scale with player level.

See `DAILY_REWARDS.md` for the full challenge system design.

## Level calculation

The level curve is a smooth quadratic curve:

```kotlin
xpRequiredForLevel(level) = 30 + (level - 1) * 30
totalXpForLevel(level) = 15 * level * (level + 1)
```

| Level | Total XP required | Expected feel |
|---:|---:|---|
| 1 | `30` | First-day onboarding reward. |
| 2 | `90` | Early setup momentum. |
| 5 | `450` | Around the Young Dragon threshold. |
| 10 | `1650` | Mid-game commitment point. |
| 20 | `6300` | Long-term progression. |
| 30 | `13950` | Late-game commitment. |
| 40 | `24600` | Very long-term play. |
| 50 | `38250` | Veteran progression. |
| 60 | `55800` | Endgame milestone. |

This keeps early levels fast while preventing multiple level gains from a single normal habit completion after the first day.

## Evolution stages

Pet evolution stages are determined by XP thresholds from `ExpConfig.EVOLUTION_THRESHOLDS`.

| Stage | Name | XP Range |
|---:|---|---:|
| 0 | Egg | `0-74` |
| 1 | Hatchling | `75-299` |
| 2 | Young Dragon | `300-899` |
| 3 | Adult Dragon | `900-2499` |
| 4 | Ancient Dragon | `2500+` |

Evolution should feel like an achievement. Ancient Dragon is now a multi-week goal for mostly checkbox play.

## Level-up rewards

- Coins: `level × 10` (`ExpConfig.LEVEL_UP_COIN_MULTIPLIER`)
- Chest: one randomized chest from `ChestRewardConfigProvider.getRandomChestType()`

Chest rewards are intentionally modest after the economy rebalance. See `CHEST_REWARDS.md` for expected values.

## Chest and achievement XP rewards

Chest XP is configured through `EconomyConfig` and `ChestRewardConfigProvider`.

| Chest | XP range | Expected XP |
|---|---:|---:|
| Normal | `0-0` | `0` |
| Rare | `8-20` | `14` |
| Epic | `20-50` | `35` |
| Legendary | `50-100` | `75` |

Achievement XP milestone rewards no longer award XP directly. The previous `AchievementReward.ExpReward` rewards for XP milestones have been replaced with coins and/or chests so achievements do not bypass progression pacing.

## Surprise rewards

Surprise rewards are intentionally small and infrequent.

| Setting | Old value | New value |
|---|---:|---:|
| Minimum completions before another roll | None | `3` |
| Surprise chest chance | `10%` per completion | `4%` after cooldown |
| Direct surprise XP | `25` | `5` |
| Direct surprise coins | `15` | `3` |
| Surprise Rare probability | `80%` | `85%` |
| Surprise Epic probability | `18%` | `13%` |
| Surprise Legendary probability | implied `2%` | implied `2%` |

Expected surprise chest value is approximately:

- Coins: `30.65`
- XP: `17.95`
- Customization coin-equivalent: about `71.5`
- Frequency: roughly one surprise chest every 75 completions, assuming the 3-completion cooldown plus 4% roll chance.

## Validation targets

Assuming mostly checkbox habits:

| Daily completions | Base XP | Combo bonus | Estimated total XP/day |
|---:|---:|---:|---:|
| 3 | `30` | `3` | `33-34` including expected surprise XP |
| 5 | `50` | `10` | `60-61` including expected surprise XP |

## Progression validation

| Level | Total XP required | Est. checkbox habits | Est. days at 3/day | Est. days at 5/day |
|---:|---:|---:|---:|---:|
| 1 | `30` | 3 | 1.0 | 0.6 |
| 5 | `450` | 45 | 15.0 | 9.0 |
| 10 | `1650` | 165 | 55.0 | 33.0 |
| 20 | `6300` | 630 | 210.0 | 126.0 |
| 30 | `13950` | 1395 | 465.0 | 279.0 |
| 40 | `24600` | 2460 | 820.0 | 492.0 |
| 50 | `38250` | 3825 | 1275.0 | 765.0 |
| 60 | `55800` | 5490 | 1830.0 | 1098.0 |

## Balance notes

- Three checkbox habits per day produce `30 XP/day` before challenge rewards, combo, and surprise rewards.
- Short-term combo momentum adds at most `+4 XP` to a single habit.
- Timer habits accelerate XP and coin gain when completed for their configured minimum duration.
- Level-up base coins are applied by `RewardManager` when a `LevelUpReward` is processed.
- Level-up chests add modest coins, XP, and occasional customization items.
- Evolution remains XP-gated with no additional item or streak requirements.
- No reward source should trivialize XP progression under normal play.
