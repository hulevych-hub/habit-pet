# Challenges

## Overview

Habit Pet uses lightweight rotating challenges instead of daily goals. Challenges are not calendar-based rewards and do not scale with player level or progression. They exist to encourage consistency and create small dopamine moments after ordinary play.

The current design is inspired by lightweight quest systems such as Duolingo quests, Pokémon GO field research, and Tamagotchi-style objectives.

## Core rules

- The player has exactly one active challenge at a time.
- Challenges are random and short-term.
- Challenges are intended to take roughly 5–30 minutes of active play or 1–3 days of casual play.
- Challenges must remain achievable for players with only 1–2 habits.
- Challenge progress is never stressful and never impossible.
- A completed challenge shows a claim button.
- Claiming grants rewards exactly once, logs an activity event, and automatically reveals the next random challenge.

## Current challenge sources

All reward mechanisms require player activity:

- Habit completions.
- XP earned from habits or rewards.
- Coins earned from habits or rewards.
- Chest openings.
- Customization unlocks.
- Customization equips.
- Streak milestones.

No rewards are granted solely for opening the app or for a calendar date change.

## Challenge configuration

`ChallengeConfig.kt` is the single source of truth for challenge definitions.

Each definition supports:

- `id`
- `title`
- `description`
- `icon`
- `ChallengeType`
- `targetValue`
- `rewards`
- optional `weight`
- optional `ChallengeAvailability`

Challenge definitions are intentionally simple. They do not require many habits and do not reference the player's level.

## Example challenge pool

Current examples include:

- Complete 1 habit.
- Complete 3 habits.
- Complete 5 habits.
- Earn 25 XP.
- Earn 50 XP.
- Earn 100 XP.
- Earn 20 coins.
- Earn 50 coins.
- Open 1 chest.
- Unlock 1 customization item.
- Equip an outfit.
- Equip an aura.
- Change background once.
- Reach a 2-day streak.
- Reach a 3-day streak.

## Randomization

`ChallengeRepositoryImpl` selects the next challenge after a claim.

Rules:

1. The same `challengeId` is not repeated consecutively.
2. When possible, the next challenge uses a different `ChallengeType`.
3. Weighted random selection is used so common challenges appear more often.
4. Availability checks prevent impossible challenges, such as equipping an owned aura when the player has no owned aura.

## Rewards

Challenge rewards are processed through the existing reward pipeline:

`ChallengeEngine → RewardQueue → RewardManager`

Supported reward types:

- `CoinReward`
- `ExpReward`
- `ChestReward`
- `CustomizationReward`

Reward claiming is idempotent. The active challenge row is replaced only after the claim path succeeds, and the UI only shows the claim button for completed, unclaimed challenges.

## Activity timeline

Completed challenges are logged as `GameEventType.CHALLENGE_COMPLETED` by `ActivityTimelineEngine.logChallengeCompleted()`.

The timeline event includes:

- Challenge name.
- Reward summary.
- Reinforcement messaging from `ReinforcementMessageProvider`.

## Statistics and persistence

Challenge state is stored in `ChallengeEntity` (`table: challenges`).

The row stores:

- Active challenge ID.
- Challenge definition ID.
- Title, description, and icon.
- Challenge type and target value.
- Current progress.
- Serialized reward list.
- Completion and claim state.
- Timestamps for creation, completion, and claim.

`StatisticsEntity` no longer stores daily goal fields. It continues to track coins, streaks, completions, XP totals, combo state, and other progression metrics.

## Migration note

Existing users are migrated through `MIGRATION_17_18`.

The migration:

- Creates the `challenges` table.
- Removes obsolete daily goal columns from `statistics`.
- Converts existing daily XP progress into a safe `xp_30` challenge.
- Marks the migrated challenge as completed only when old daily progress reached the old goal.
- Marks the migrated challenge as claimed only when the old daily goal was already completed and claimed.

## Source files

- `domain/ChallengeConfig.kt`
- `domain/ChallengeEngine.kt`
- `domain/repository/ChallengeRepository.kt`
- `data/repository/ChallengeRepositoryImpl.kt`
- `data/local/entities/ChallengeEntity.kt`
- `data/local/dao/ChallengeDao.kt`
- `data/local/database/ChallengeMigration.kt`
- `data/local/database/ChallengeDatabaseInitializer.kt`
- `presentation/ui/components/ChallengeCard.kt`
- `presentation/ui/screens/HomeScreenViewModel.kt`
- `presentation/ui/screens/HabitsScreen.kt`
