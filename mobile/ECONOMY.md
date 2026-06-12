# ECONOMY

---

# 🧠 OVERVIEW

The economy system in Habit Pet manages the in-game currency (coins) that players earn through gameplay and spend on customization.

Coins are the primary progression resource and must feel:

- Earned
- Meaningful
- Balanced over long-term play

---

# 🧩 CURRENT IMPLEMENTATION

The economy consists of:

- Coin storage in `StatisticsEntity.totalCoins`
- Central repository: `StatisticsRepository`
- Reward-based income system
- Shop-based spending system (customization items)
- Low-probability surprise bonuses on habit completion
- Daily goal completion bonus coins
- **Centralized configuration: `EconomyConfig`**

---

# ⚠️ SYSTEM AUTHORITY RULE

The **RewardManager is the single source of truth for queued non-achievement reward processing**.

Documented direct coin updates are allowed only in:

- `HabitDetailViewModel.awardCoins()` for habit completion coins, level-up base coins, and surprise bonus coins from the detail-screen flow
- `HabitsViewModel.awardCoins()` for one-tap checkbox habit completion coins, level-up base coins, and surprise bonus coins from the habit-list flow
- `AchievementRewardProcessor.process(...)` for achievement coin rewards
- `RewardManager.rewardCompleted()` for queued reward events, including daily goal rewards

All coin sources MUST be explicitly documented in this file.

---

# 💰 COIN STORAGE

- Currency type: coins (Int only)
- Stored in: `StatisticsEntity.totalCoins`
- Initial value: 0
- Persisted via Room database

No fractional currency is supported.

---

# 📈 COIN SOURCES

Coins can be earned from the following systems:

---

## 1. Habit Completion Rewards

### Checkbox Habits
- **Reward**: 10 coins per completion
- **Sources**:
  - Direct reward via `HabitDetailViewModel.awardCoins()` from the habit detail screen
  - Direct reward via `HabitsViewModel.awardCoins()` from one-tap list completion
- **UX**: one-tap list completion uses optimistic completion state, then executes the same XP, coin, streak, activity timeline, micro-feedback, and reward-queue pipeline as detail-screen completion

### Timer Habits
- **Formula**: 5 base coins + 2 coins per completed minute
- **Example**: 30 min session → 5 + 60 = 65 coins
- **Source**: Direct reward via `HabitDetailViewModel.awardCoins()`

---

## 2. Surprise Habit Rewards

Surprise rewards are rare, non-blocking bonuses that can trigger after a successful habit completion.

Rules:
- Trigger chance: 8% after at least 3 successful habit completions since the previous surprise.
- Reward bundle:
  - `EconomyConfig.SURPRISE_BONUS_XP` bonus XP
  - `EconomyConfig.SURPRISE_BONUS_COINS` bonus coins
  - one extra surprise chest queued through `RewardQueue`
- Surprise chest rarity is always Rare, Epic, or Legendary.
- Triggering is in-memory and session-scoped, so it cannot spam across app restarts.
- Surprise chest rewards flow through the existing chest reward queue and `RewardManager`.
- Surprise reward events are logged into `ActivityTimeline` as `SURPRISE_REWARD`.

Balance note: surprise rewards are intentionally low-frequency and rate-limited. They add emotional variety without becoming a predictable farming loop.

---

## 3. Daily Goal Rewards

Daily goal rewards are granted when the XP-based daily goal is completed.

Rules:
- Trigger: reaching `ExpConfig.DAILY_XP_GOAL` XP on the current date key
- Coin bonus: `EconomyConfig.DAILY_GOAL_COIN_BONUS`
- XP bonus: `ExpConfig.DAILY_GOAL_BONUS_XP`
- Delivery: queued through `RewardUiEvent.DailyGoalReward` and processed by `RewardManager`
- Timeline event: `GameEventType.DAILY_GOAL_COMPLETED`

Balance note: the daily goal bonus is intentionally small so it feels rewarding without disrupting the long-term coin economy.

---

## 4. Achievement Rewards

Achievement rewards are configured in `AchievementsConfig` and coin values reuse `EconomyConfig`:

- First Habit → 50 coins
- 3 Habit Builder → 100 coins
- 7 Day Streak → 100 coins
- 30 Day Streak → 250 coins
- 100 Completions → 200 coins
- 1000 XP → 150 coins
- Level 10 → 300 coins
- Level 25 → 500 coins
- First Customization → 75 coins
- Customization Collector → Rare chest + 50 coins

Achievement coin rewards are applied by `AchievementRewardProcessor.process(...)`. `RewardManager` does not double-process `RewardUiEvent.AchievementReward` coin values.

---

## 5. Streak Milestone Chests

Triggered on milestone streaks:

Conditions:
- currentStreak ≥ 7
- currentStreak is one of the configured milestones: 7, 14, 30, 60, 100
- currentStreak > lastStreakAwardedAt

Reward:
- No fixed 50-coin milestone payout
- A `ChestReward` is queued through `StreakEngine`
- Chest type is milestone-based:
  - 7 days → Normal
  - 14 days → Rare
  - 30 or 60 days → Epic
  - 100 days → Legendary

Delivered via the centralized reward queue and processed by `RewardManager`.

---

## 6. Level Up Rewards

Each level-up grants:

- **Base reward**: level × 10 coins, defined in `ExpConfig.LEVEL_UP_COIN_MULTIPLIER`
- **Chest reward**: one randomized chest from `ChestRewardConfigProvider.getRandomChestType()`

Example:
- Level 5 → 50 base coins + one random chest

The `EconomyConfig.LEVEL_UP_CHEST_BONUS_COINS` constant is retained as a tuning reference but is not used by the current reward pipeline.

---

## 7. Chest Rewards

Chest rewards are awarded for:

- Streak milestone rewards
- Every level-up
- Achievement chest rewards
- Surprise habit reward chests

### Level-up and achievement chest type probabilities

Chest type is randomly determined with these probabilities from `EconomyConfig`:

- **Normal (55%)**: 10-30 coins, no EXP, no customization item
- **Rare (30%)**: 30-80 coins, 50-150 EXP, 15% chance for Rare customization item
- **Epic (12%)**: 80-180 coins, 150-350 EXP, 30% chance for Epic customization item
- **Legendary (3%)**: 180-400 coins, 350-800 EXP, 50% chance for Legendary customization item

### Streak milestone chest type mapping

Streak milestone chests do not randomize chest rarity. They use the milestone mapping above.

---

## 8. Micro-feedback UI Events

`MicroFeedbackManager` is a UI-only feedback system. It emits lightweight global feedback for habit completion, tab switches, XP gain events, and coin gain events. It does not award coins, modify `StatisticsEntity`, or change economy balance.

Coin feedback is triggered from existing coin award paths:

- `HabitDetailViewModel.awardCoins()` triggers coin feedback for direct habit completion coins, direct level-up base coins, and surprise bonus coins from the detail-screen flow.
- `HabitsViewModel.awardCoins()` triggers coin feedback for one-tap checkbox habit completion coins, direct level-up base coins, and surprise bonus coins from the habit-list flow.
- `RewardManager.rewardCompleted()` triggers coin feedback for queued reward coins from `CoinReward`, `StreakReward`, `DailyGoalReward`, and `ChestReward`.
- `RewardManager.rewardCompleted()` intentionally skips `LevelUpReward` coin feedback because those level-up base coins are already awarded directly by `HabitDetailViewModel.awardCoins()` or `HabitsViewModel.awardCoins()`.

XP feedback is triggered when XP is added directly through habit completion or through queued chest and daily goal rewards. The feedback overlay is non-blocking and does not affect EXP, level, or evolution calculations.

## Reward Moment Amplification

Reward UI emphasis is handled in `RewardScreen` as a presentation-layer effect only. It does not award coins, EXP, customization items, or timeline events.

Emphasis tiers:
- Small: low-value coin rewards and simple pickups
- Rare: level-up, daily goal, streak, rare chest, and most achievement moments
- Epic: dragon evolution, epic or legendary chests, high-value chest contents, and major streak milestones

Implemented emphasis states:
- Scale animation on the reward content
- Colored glow around the reward surface
- Tier-based chest tint and chest size
- Tier-based reward text color
- Longer emphasis duration for epic moments

These states are intentionally short-lived so reward moments interrupt the flow positively without changing the reward pipeline or blocking reward processing.

---

# 🧾 COIN SPENDING

## Customization Purchases

Coins are spent only on customization items.

Rules:
- Each item has a fixed price (`InventoryItemEntity.price`) calculated from `EconomyConfig.customizationPrice(rarity)`
- Purchase requires: totalCoins ≥ price
- On success:
  - Deduct coins
  - Mark item as purchased

### Customization Pricing (from `EconomyConfig`)

Pricing is rarity-based and type-neutral: outfits, backgrounds, and auras use the same rarity price.

| Rarity | Base Multiplier | Price | Target Save Time |
|--------|----------------|-------|------------------|
| Normal | 1.0x | 100 coins | ~1 day with recurring rewards; ~3.3 days from checkbox habit coins alone |
| Rare | 3.0x | 300 coins | ~3 days with recurring rewards; ~10 days from checkbox habit coins alone |
| Epic | 8.0x | 800 coins | ~8 days with recurring rewards; ~26.7 days from checkbox habit coins alone |
| Legendary | 20.0x | 2000 coins | ~20 days with recurring rewards; ~66.7 days from checkbox habit coins alone |

---

## Purchase Failure Codes

- -1 → Item not found
- -2 → Already purchased
- -3 → Statistics unavailable
- -4 → Not enough coins

---

# 🔄 COIN FLOW PIPELINE

Habit completion coins, level-up base coins, and surprise bonus coins flow directly through `HabitDetailViewModel.awardCoins()` for detail-screen completion or `HabitsViewModel.awardCoins()` for one-tap checkbox completion from the habit list.

Queued rewards flow through:

RewardManager → RewardQueue → StatisticsRepository

Achievement rewards flow through:

AchievementRewardProcessor → Room transaction over StatisticsRepository / PetRepository / InventoryItemRepository → RewardQueue → RewardEventBus

Processing rules:
1. RewardManager receives non-achievement reward events
2. Extract coin value based on type:
   - CoinReward → amount
   - LevelUpReward → coins
   - AchievementReward → 0, because achievement coins are already applied by `AchievementRewardProcessor`
   - StreakReward → coins
   - ChestReward → amount
   - DragonEvolutionReward → 0
3. If coins > 0:
   - statisticsRepository.addCoins(coins)

---

# 🎯 DESIGN INTENT

The economy is designed to:

- Provide steady progression (~30 coins/day from 3 checkbox habits)
- Reach roughly ~100 coins/day early-game when recurring level-up and streak chests are included
- Encourage daily engagement through streak milestone chests
- Make customization items feel valuable without making Normal items feel unreachable
- Avoid inflation spikes
- Prevent passive farming loops

Coins should always feel **earned, not free**.

---

# 🚫 ANTI-INFLATION RULE

Do NOT introduce new permanent coin sources without:

- Defining purpose clearly
- Updating this file
- Evaluating long-term economy impact
- Ensuring balance with existing systems

---

# ⚙️ ECONOMY CHANGE RULE

Any change to economy must:

1. Update this file first
2. Update dependent systems afterward
3. Preserve balance across progression systems
4. Avoid untracked coin generation
5. **Use `EconomyConfig` as single source of truth**

---

# 📦 DATA MODEL

## StatisticsEntity

- totalCoins: Int

---

## InventoryItemEntity

- price: Int (coin cost for purchase)

---

# 📂 SOURCE FILES

- StatisticsEntity.kt
- InventoryItemEntity.kt
- StatisticsRepository.kt
- StatisticsRepositoryImpl.kt
- AchievementEngine.kt
- StreakEngine.kt
- HabitDetailViewModel.kt
- HabitsViewModel.kt
- HabitsScreen.kt
- ActivityTimelineEngine.kt
- ActivityTimelineScreen.kt
- GameEventFactory.kt
- GameEventType.kt
- RewardManager.kt
- RewardQueue.kt
- RewardScreen.kt
- MicroFeedbackManager.kt
- MicroFeedbackOverlay.kt
- MicroFeedbackEvent.kt
- EconomyConfig.kt (centralized configuration)
- ExpConfig.kt (centralized XP/level configuration)

---

# ⚠️ KNOWN GAPS

1. No coin cap (infinite accumulation possible)
2. No inflation control system
3. No dynamic pricing (static rarity-based)
4. Limited spending mechanics (only customization items)
5. No persistent reward multipliers
6. No economy sink systems beyond customization items

---

# 📊 ECONOMY BALANCE VALIDATION

## Target Metrics (from `EconomyConfig`)

- **Base daily coins** (3 checkbox habits/day): 30 coins
- **Surprise bonus expectation**: low-frequency session-scoped bonus after a 3-completion cooldown; average direct surprise value is about 2 XP and 1.2 coins per completion
- **Target early-game daily coins** (base coins + recurring level-up/streak chests): ~100 coins/day
- **Normal customization item**: 100 coins (~1 day with recurring rewards)
- **Rare customization item**: 300 coins (~3 days with recurring rewards)
- **Epic customization item**: 800 coins (~8 days with recurring rewards)
- **Legendary customization item**: 2000 coins (~20 days with recurring rewards)

## Progression Validation

| Level | Total XP | Est. Habits (Checkbox) | Est. Days (3/day) | Coins from Level-Up Base Rewards |
|-------|----------|------------------------|-------------------|----------------------------------|
| 1 | 100 | 1 | 0.3 | 10 |
| 5 | 1,000 | 10 | 3.3 | 150 |
| 10 | 3,250 | 32.5 | 10.8 | 550 |
| 15 | 6,750 | 67.5 | 22.5 | 1,200 |
| 20 | 11,500 | 115 | 38.3 | 2,100 |
| 25 | 17,500 | 175 | 58.3 | 3,250 |
| 30 | 24,750 | 247.5 | 82.5 | 4,650 |
| 40 | 43,000 | 430 | 143.3 | 8,200 |
| 50 | 66,250 | 662.5 | 220.8 | 12,750 |

*Assumes 100 XP per checkbox habit and no timer-habit bonus XP.*

## Reward Balance Validation

- Average randomized chest coin value: ~52 coins
- Average randomized chest EXP value: ~77 EXP
- Average randomized chest customization coin-equivalent value: ~72 coins
- Combined randomized chest value: ~52 coins + ~77 EXP + ~72 coin-equivalent
- Streak milestone chests are milestone-based rather than random, so their expected value depends on which milestone is reached.
- Surprise chests are rare, rate-limited, and always Rare/Epic/Legendary; their expected value is small because they only trigger after a completion cooldown and an 8% chance roll.
- Economy stability is maintained by:
  - Fixed habit rewards
  - Fixed rarity pricing
  - Duplicate-preventing customization chest selection
  - No coin generation from evolution rewards
