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
- Shop-based spending system (accessories)
- **Centralized configuration: `EconomyConfig`**

---

# ⚠️ SYSTEM AUTHORITY RULE

The **RewardManager is the single source of truth for all coin rewards**.

Exceptions:
- Direct `statisticsRepository.addCoins()` is only allowed for explicitly documented cases (e.g. timer habit rewards)

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
- **Source**: Direct reward via `HabitDetailViewModel.awardPetXpAndCoins()`

### Timer Habits
- **Formula**: 5 base coins + 2 coins per minute
- **Example**: 30 min session → 5 + 60 = 65 coins
- **Source**: Direct reward via `HabitDetailViewModel.awardPetXpAndCoins()`

---

## 2. Achievement Rewards

Each achievement grants a fixed reward:

- First Habit → 50 coins
- 7 Day Streak → 100 coins
- 30 Day Streak → 250 coins
- 100 Completions → 200 coins
- 1000 XP → 150 coins
- Level 10 → 300 coins
- Level 25 → 500 coins

---

## 3. Streak Rewards

Triggered on milestone streaks:

Conditions:
- currentStreak ≥ 7
- currentStreak % 7 == 0
- currentStreak > lastStreakAwardedAt

Reward:
- 50 coins per milestone (delivered via ChestReward system as part of streak chest)

Delivered via ChestReward system.

---

## 4. Level Up Rewards

Each level-up grants:

- **Base reward**: level × 10 coins (defined in `ExpConfig.LEVEL_UP_COIN_MULTIPLIER`)
- **Additional chest reward**: 20 coins (defined in `EconomyConfig.LEVEL_UP_CHEST_BONUS_COINS`)

Example:
- Level 5 → 50 coins + 20 coin chest bonus

---

## 5. Chest Rewards (Streak Milestones & Level-Ups)

Chest rewards are awarded for:
- 7-day streak milestones
- Every level-up

Chest type is randomly determined with the following probabilities (from `EconomyConfig`):
- **Normal (55%)**: 10-30 coins, no EXP, no accessory
- **Rare (30%)**: 30-80 coins, 50-150 EXP, 15% chance for Rare accessory
- **Epic (12%)**: 80-180 coins, 150-350 EXP, 30% chance for Epic accessory
- **Legendary (3%)**: 180-400 coins, 350-800 EXP, 50% chance for Legendary accessory

---

# 🧾 COIN SPENDING

## Accessory Purchases

Coins are spent only on accessories.

Rules:
- Each item has a fixed price (`InventoryItemEntity.price`) calculated from `EconomyConfig.accessoryPrice(rarity)`
- Purchase requires: totalCoins ≥ price
- On success:
  - Deduct coins
  - Mark item as purchased

### Accessory Pricing (from `EconomyConfig`)

| Rarity | Base Multiplier | Price | Target Save Time |
|--------|----------------|-------|------------------|
| Normal | 1.0x | 100 coins | ~1 day |
| Rare | 3.0x | 300 coins | ~3 days |
| Epic | 8.0x | 800 coins | ~8 days |
| Legendary | 20.0x | 2000 coins | ~20 days |

---

## Purchase Failure Codes

- -1 → Item not found
- -2 → Already purchased
- -3 → Statistics unavailable
- -4 → Not enough coins

---

# 🔄 COIN FLOW PIPELINE

All rewards flow through:

RewardManager → RewardQueue → StatisticsRepository

Processing rules:

1. RewardManager receives reward events
2. Extract coin value based on type:
   - CoinReward → amount
   - LevelUpReward → coins
   - AchievementReward → coins
   - StreakReward → coins
   - ChestReward → amount
   - DragonEvolutionReward → 0
3. If coins > 0:
   - statisticsRepository.addCoins(coins)

---

# 🎯 DESIGN INTENT

The economy is designed to:

- Provide steady progression (~100 coins/day for active player)
- Encourage daily engagement (streaks)
- Make accessories feel valuable (1-20 days to save)
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
- RewardManager.kt
- RewardQueue.kt
- EconomyConfig.kt (NEW - centralized configuration)
- ExpConfig.kt (NEW - centralized XP/level configuration)

---

# ⚠️ KNOWN GAPS

1. No coin cap (infinite accumulation possible)
2. No inflation control system
3. No dynamic pricing (static rarity-based)
4. Limited spending mechanics (only accessories)
5. No reward multipliers or dynamic bonuses
6. No economy sink systems beyond accessories

---

# 📊 ECONOMY BALANCE VALIDATION

## Target Metrics (from `EconomyConfig`)

- **Target daily coins** (3 habits/day): ~100 coins
- **Normal accessory**: 100 coins (~1 day)
- **Rare accessory**: 300 coins (~3 days)
- **Epic accessory**: 800 coins (~8 days)
- **Legendary accessory**: 2000 coins (~20 days)

## Progression Validation

| Level | Total XP | Coins from Level-Ups | Est. Days |
|-------|----------|---------------------|-----------|
| 1 | 100 | 10 | 0.1 |
| 5 | 750 | 150 | 1.5 |
| 10 | 3,250 | 550 | 5.5 |
| 25 | 20,000 | 3,250 | 32.5 |
| 50 | 65,250 | 12,750 | 127.5 |

*Assumes no spending. With spending, progression extends naturally.*