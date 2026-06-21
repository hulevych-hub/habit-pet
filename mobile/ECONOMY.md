# Economy

## Overview

The Habit Pet economy manages coins earned through active play and spent on customization. The rebalanced economy is designed to be grindy enough that cosmetics feel valuable, but not so scarce that players feel stuck.

Coins should feel:

- Earned
- Meaningful
- Balanced over long-term play
- Supportive of long-term pet attachment

## Source of truth

All economy values are centralized in `EconomyConfig.kt`. Runtime code should read coin rewards, chest rewards, surprise rewards, and customization prices from `EconomyConfig` rather than hardcoding progression numbers.

## Coin storage

- Currency type: coins (`Int`)
- Stored in: `StatisticsEntity.totalCoins`
- Initial value: `0`
- Persisted via Room database

No fractional currency is supported.

## Coin sources

### 1. Habit completion rewards

| Habit type | Formula | Example |
|---|---:|---:|
| Checkbox habit | `10 coins` | One completed checkbox habit = `10 coins` |
| Timer habit | `5 base coins + 2 coins per completed minute` | 15-minute timer = `35 coins` |

Checkbox habit coins are unchanged. Timer habit coins remain unchanged because timer rewards already represent time commitment.

### 2. Surprise habit rewards

Surprise rewards are rare, non-blocking bonuses after successful habit completions.

| Setting | Old value | New value |
|---|---:|---:|
| Minimum completions before another roll | None | `3` |
| Surprise chest chance | `10%` per completion | `4%` after cooldown |
| Direct surprise XP | `25` | `5` |
| Direct surprise coins | `15` | `3` |
| Surprise Rare probability | `80%` | `85%` |
| Surprise Epic probability | `18%` | `13%` |
| Surprise Legendary probability | implied `2%` | implied `2%` |

The cooldown is session-scoped in `HabitDetailViewModel` and `HabitsViewModel`, so surprise rewards cannot spam across rapid completions.

### 3. Challenge rewards

Challenge rewards are granted when the player completes and claims the active challenge.

- Trigger: completing the active challenge target and pressing the claim button
- Reward types: `CoinReward`, `ExpReward`, `ChestReward`, and `CustomizationReward`
- Delivery: queued through `RewardQueue` and processed by `RewardManager`
- Timeline event: `GameEventType.CHALLENGE_COMPLETED`
- Source of truth: `ChallengeConfig.kt`

Current examples include small coin, XP, chest, and customization rewards. Challenge rewards do not scale with player level.

### 4. Achievement rewards

Achievement rewards are configured in `AchievementsConfig`; coin values reuse `EconomyConfig`.

XP achievements no longer award XP directly. They reward coins and/or chests so achievements do not bypass progression pacing.

| Achievement | Old reward | New reward |
|---|---|---|
| First Habit | `50 coins` | `50 coins` |
| 3 Habit Builder | `100 coins` | `90 coins` |
| 7 Day Streak | `100 coins` | `100 coins` |
| 30 Day Streak | `250 coins` | `400 coins` |
| 100 Completions | `200 coins` | `200 coins` |
| 1000 XP | `150 coins` | `150 coins` |
| 3000 XP | `150 XP` | `300 coins` |
| 5000 XP | `300 XP` | `350 coins` |
| Level 10 | `300 coins` | `300 coins` |
| Level 25 | `500 coins` | `500 coins` |
| Level 50 | `Epic chest + 1000 coins` | `Rare chest + 700 coins` |
| Level 60 | `Legendary chest + 1200 coins` | `Legendary chest + 900 coins` |
| First Customization | `75 coins` | `60 coins` |
| Customization Collector | `Rare chest + 50 coins` | `Rare chest + 50 coins` |

Achievement rewards are prepared by `AchievementRewardProcessor.process(...)` and applied once by `RewardManager` through the centralized reward pipeline.

### 5. Streak milestone chests

Streak milestone chests are milestone-based rather than fixed coin payouts:

- 7 days → Normal chest
- 14 days → Rare chest
- 30 or 60 days → Epic chest
- 100 days → Legendary chest

They are queued through `StreakEngine` and processed by `RewardManager`.

### 6. Level-up rewards

Each level-up grants:

- Base reward: `level × 10 coins`, defined in `ExpConfig.LEVEL_UP_COIN_MULTIPLIER`
- Chest reward: one randomized chest from `ChestRewardConfigProvider.getRandomChestType()`

The `EconomyConfig.LEVEL_UP_CHEST_BONUS_COINS` constant is retained as a tuning reference but is not used by the current reward pipeline.

### 7. Chest rewards

Chest rewards are awarded for:

- Streak milestone rewards
- Challenge rewards
- Every level-up
- Achievement chest rewards
- Surprise habit reward chests

Chest contents are configured in `EconomyConfig` and built by `ChestRewardConfigProvider` + `ChestRewardFactory`.

| Chest | Coin range | EXP range | Customization drop chance |
|---|---:|---:|---:|
| Normal | `5-15` | `0-0` | `0%` |
| Rare | `15-35` | `8-20` | `8%` |
| Epic | `35-75` | `20-50` | `18%` |
| Legendary | `75-150` | `50-100` | `35%` |

Randomized level-up chest type probabilities:

| Chest | Old probability | New probability |
|---|---:|---:|
| Normal | `55%` | `65%` |
| Rare | `30%` | `25%` |
| Epic | `12%` | `8%` |
| Legendary | `3%` | `2%` |

## Coin spending

Coins are spent only on customization items.

Purchase rules:

- Each item has a fixed price from `InventoryItemEntity.price`.
- Price is calculated from `EconomyConfig.customizationPrice(rarity)`.
- Purchase requires: `totalCoins >= price`.
- On success:
  - Deduct coins.
  - Mark item as purchased.

### Customization pricing

| Rarity | Old price | New price | Target save time at `75 coins/day` |
|---|---:|---:|---:|
| Normal | `100` | `120` | `2` days |
| Rare | `300` | `400` | `6` days |
| Epic | `800` | `1000` | `14` days |
| Legendary | `2000` | `3000` | `40` days |

Cosmetics should require saving, but Normal items remain reachable quickly.

## Coin flow pipeline

Habit completion coins, level-up base coins, and surprise bonus coins flow directly through:

- `HabitDetailViewModel.awardCoins()` for detail-screen completion
- `HabitsViewModel.awardCoins()` for one-tap checkbox completion from the habit list

Queued rewards flow through:

`RewardManager → RewardQueue → StatisticsRepository`

Achievement rewards flow through:

`AchievementRewardProcessor → Room transaction → RewardQueue → RewardManager → StatisticsRepository / PetRepository / InventoryItemRepository`

Processing rules:

1. `RewardManager` receives all reward events.
2. Coin value is extracted by type:
   - `LevelUpReward` → coins
   - `StreakReward` → coins
   - `ChestReward` → amount
   - `CoinReward` → amount
   - `DragonEvolutionReward` → `0`
3. If coins are greater than `0`, `statisticsRepository.addCoins(coins)` is called.

## Design intent

The rebalanced economy targets:

- Steady active play income from habit completions.
- Cosmetics that feel valuable without feeling unreachable.
- Level-ups that remain satisfying throughout progression.
- Chests that feel exciting but do not break progression.
- No passive farming loops.

Coins should feel earned, not free.

## Anti-inflation rules

Do not introduce new permanent coin sources without:

- Defining the purpose clearly.
- Updating this file.
- Evaluating long-term economy impact.
- Ensuring balance with XP, chests, achievements, and customization prices.
- Using `EconomyConfig` as the single source of truth.

## Economy balance validation

Assuming mostly checkbox habits and light challenge completion:

| Daily completions | Habit coins | Typical challenge coins | Expected surprise/direct coins | Estimated coins/day |
|---:|---:|---:|---:|---:|
| 3 | `30` | `0-20` | `~1-2` | `31-52` before level-up chests |
| 5 | `50` | `0-20` | `~1-3` | `51-73` before level-up chests |

Including early level-up chests, active players should usually land near the `75 coins/day` target. Level-up chest frequency naturally falls as the level curve slows.

### Expected randomized chest value

| Chest type | Avg coins | Avg EXP | Customization EV* |
|---|---:|---:|---:|
| Normal | `10` | `0` | `0` |
| Rare | `25` | `14` | `32` coin-equivalent |
| Epic | `55` | `35` | `180` coin-equivalent |
| Legendary | `112.5` | `75` | `1050` coin-equivalent |

\*Customization EV = drop chance × customization price (`Rare=400`, `Epic=1000`, `Legendary=3000`).

Weighted randomized chest value:

- Avg coins: `19.4`
- Avg EXP: `7.8`
- Customization EV: about `43.4` coin-equivalent

## Known gaps

1. No coin cap.
2. No dynamic pricing.
3. Limited spending mechanics beyond customization items.
4. No persistent reward multipliers.
5. Chest rewards still use the same chest image regardless of chest type.
6. Chest type-specific outfit/background/aura drop tables are not explicit.
