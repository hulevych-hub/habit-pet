# TODO - Habit Pet MVP

---

# 🧠 GENERAL EXECUTION RULES

For every task below:

1. Read the related documentation files before implementing.
2. Update related documentation files immediately after implementation.
3. Mark tasks as completed when finished.
4. Do not leave documentation outdated.
5. If implementation decisions are required, document them in the relevant `.md` file.
6. Complete tasks strictly in order.
7. Do not start a later section until the current section is fully completed.

---

# 1. ACHIEVEMENTS SYSTEM (CONFIG-DRIVEN + MULTI-REWARD)

## Documentation

Read:
- ACHIEVEMENTS.md
- ECONOMY.md
- EXP.md
- CHEST_REWARDS.md
- DATA_MODEL.md

Update:
- ACHIEVEMENTS.md
- DATA_MODEL.md
- ECONOMY.md (if reward logic affects balance)

---

## 🎯 GOAL

Replace current hardcoded achievement system with a fully data-driven system where:

- Achievements are defined in `AchievementsConfig`
- Player progress is stored in DB only
- Rewards are fully configurable per achievement
- Achievements support multiple reward types
- UI supports progress tracking (e.g. 2/3, 5/10)
- New achievements can be added without changing DB schema or logic

---

## 🧠 CORE RULES

- Achievement definitions MUST live in `AchievementsConfig`
- Database stores ONLY:
  - progress
  - isUnlocked
  - isClaimed
- Reward logic MUST NOT be hardcoded per achievement
- Reward system must support multiple rewards per achievement
- UI must dynamically adapt to reward types

---

## 📦 ACHIEVEMENT CONFIG SYSTEM

- [x] Create `AchievementsConfig`
- [x] Define achievement model:
  - id
  - name
  - description
  - icon
  - targetValue (nullable for non-progress achievements)
  - reward list (generic structure)
- [x] Support multiple reward types per achievement:
  - CoinReward
  - ExpReward
  - ChestReward (with rarity)
  - CustomizationReward (outfit/background/aura)
- [x] Ensure config is the SINGLE source of truth for achievement definitions

---

## 🗃️ DATABASE REFACTOR

- [x] Remove achievement metadata from DB (name, description, rewards, icon)
- [x] Keep only:
  - id
  - progress
  - isUnlocked
  - isClaimed
- [x] Ensure backward compatibility for existing player data
- [x] Implement migration or safe mapping layer

---

## 📊 PROGRESS SYSTEM

- [x] Implement generic progress tracking per achievement
- [x] Support incremental updates (e.g. +1 habit, +XP gained, +streak updated)
- [x] Ensure progress is persisted in DB
- [x] Ensure config targetValue is used for comparison

---

## 📈 UI REQUIREMENTS (IMPORTANT)

- [x] Show achievement progress when not completed:
  - Example: "2 / 3"
  - Show under achievement name and description
- [x] Show progress bar or visual indicator
- [x] Clearly indicate locked vs unlocked vs claimable state
- [x] Ensure reward preview is visible before claiming
- [x] Support multiple reward icons in UI

---

## 🎁 REWARD SYSTEM (GENERIC ENGINE)

- [x] Create generic `AchievementRewardProcessor`
- [x] Support reward types:
  - CoinReward → adds coins
  - ExpReward → adds XP
  - ChestReward → grants chest with rarity
  - CustomizationReward → unlocks cosmetic item (outfit/background/aura)
- [x] Support multiple rewards per achievement
- [x] Ensure reward execution is atomic (all or nothing)
- [x] Ensure rewards are triggered ONLY on claim

---

## 🔁 ACHIEVEMENT SYNC SYSTEM

- [x] On app startup:
  - Load `AchievementsConfig`
  - Merge with DB entries
  - Insert missing achievements automatically
- [x] Do NOT delete existing progress when config changes
- [x] Allow new achievements to be added safely over time

---

## 🧾 CLAIM LOGIC

- [x] Allow claiming only when progress ≥ targetValue
- [x] Prevent double claiming
- [x] On claim:
  - Mark achievement as claimed
  - Process all configured rewards
  - Trigger reward UI via RewardEventBus

---

## 🧪 EDGE CASES

- [x] Handle achievements without progress (instant unlock type)
- [x] Handle missing reward types gracefully
- [x] Handle config changes without breaking DB state
- [x] Handle partial reward failures safely

---

## 📄 DOCUMENTATION

- [x] Fully rewrite ACHIEVEMENTS.md to reflect config-based system
- [x] Document reward types and structure
- [x] Document progress tracking rules
- [x] Document sync behavior between config and DB

---

# 2. ACTIVITY TIMELINE SYSTEM (JOURNAL REWORK)

## Documentation

Read:
- ACTIVITY_LOG.md
- ACHIEVEMENTS.md
- EXP.md
- ECONOMY.md
- DRAGON_PHASES.md

Update:
- ACTIVITY_LOG.md
- DATA_MODEL.md

## Tasks

### Core System

- [x] Replace JournalScreen with ActivityTimelineScreen
- [x] Implement persistent GameEventEntity table
- [x] Define event types:
  - HABIT_COMPLETED
  - ACHIEVEMENT_UNLOCKED
  - LEVEL_UP
  - DRAGON_EVOLUTION
  - CHEST_OPENED
  - STREAK_MILESTONE
  - FIRST_DAILY_LOGIN
- [x] Ensure all event types are extensible for future systems

---

### Event Generation Integration

- [x] Hook Habit completion flow → emit HABIT_COMPLETED event
- [x] Hook AchievementEngine → emit ACHIEVEMENT_UNLOCKED event
- [x] Hook Level system → emit LEVEL_UP event
- [x] Hook Dragon evolution system → emit DRAGON_EVOLUTION event
- [x] Hook RewardEventBus → emit CHEST_OPENED event
- [x] Hook StreakEngine → emit STREAK_MILESTONE event
- [x] Implement FIRST_DAILY_LOGIN event tracking system

---

### Data Model

- [x] Create GameEventEntity with:
  - id
  - type
  - timestamp
  - title
  - description
  - icon
  - rarity (COMMON / RARE / EPIC / LEGENDARY)
  - payload (optional JSON for extensibility)
- [x] Implement Room DAO for event insertion and querying
- [x] Implement repository for event logging and retrieval

---

### UI / UX Implementation

- [x] Build ActivityTimelineScreen (replace JournalScreen)
- [x] Display events in reverse chronological order
- [x] Group events by:
  - Today
  - Yesterday
  - Earlier
- [x] Implement event cards with:
  - Icon
  - Title
  - Time ago label
  - Optional reward preview (XP / coins / chest)
- [x] Auto-scroll to latest event on open

---

### Addictive Game Feel Enhancements

- [x] Implement special FIRST_DAILY_LOGIN welcome event
  - Example: "Welcome back. Your dragon missed you 🐉"
- [x] Highlight LEVEL_UP events with enhanced visual styling
- [x] Highlight DRAGON_EVOLUTION events as full-width “milestone cards”
- [x] Add streak milestone visual reinforcement in timeline
- [x] Ensure emotional / game-like text for all system messages

---

### Performance & Storage

- [x] Store events append-only (no deletion)
- [x] Load latest 50–100 events by default
- [x] Implement lazy loading for older events
- [x] Ensure timeline retrieval is optimized for mobile performance

---

### System Rules

- [x] Every major gameplay action MUST generate a timeline event
- [x] No silent progression events allowed
- [x] Events must never block gameplay flow
- [x] Event logging must be asynchronous
- [x] Ensure consistency across all game systems

---

---

# 3. DAILY FIRST TOUCH MOMENT SYSTEM

## Documentation

Read:
- ACTIVITY_LOG.md
- STATISTICS.md
- EXP.md

Update:
- ACTIVITY_LOG.md
- STATISTICS.md

## Tasks

- [x] Detect first app open of the day (FIRST_DAILY_LOGIN event)
- [x] Store last active session timestamp
- [x] Implement Daily Welcome Event trigger
- [x] Show “Welcome Back” entry in ActivityTimelineScreen
- [x] Include:
  - streak status
  - last session time difference
  - short motivational message
- [x] Ensure event triggers only once per day
- [x] Log FIRST_DAILY_LOGIN into Activity Log system

---

# 4. MICRO-REWARD FEEDBACK SYSTEM

## Documentation

Read:
- ECONOMY.md
- EXP.md

Update:
- ECONOMY.md

## Tasks

- [x] Add global micro-feedback trigger system for UI actions
- [x] Trigger subtle feedback on:
  - habit completion
  - tab switches (optional lightweight)
  - XP gain events
  - coin gain events
- [x] Implement lightweight UI feedback hooks:
  - small pulse animation trigger (no asset dependency)
  - subtle glow state toggle
  - XP bar smooth update animation trigger
- [x] Ensure feedback system is non-blocking and async
- [x] Centralize feedback calls to avoid duplication

---

# 5. DRAGON MOOD SYSTEM (NO NEW ASSETS REQUIRED)

## Documentation

Read:
- DRAGON_PHASES.md
- STATISTICS.md

Update:
- DRAGON_PHASES.md

## Tasks

- [x] Define DragonMood state model:
  - HAPPY
  - CALM
  - EXCITED
  - PROUD
  - LONELY
- [x] Calculate mood based on:
  - streak status
  - last activity time
  - recent habit completions
- [x] Persist current mood in local state
- [x] Expose mood state to UI rendering system
- [x] Modify existing dragon rendering logic to slightly adjust:
  - intensity (visual scaling / brightness factor)
  - idle behavior speed multiplier
- [x] Ensure mood updates happen on:
  - habit completion
  - app open
  - streak changes

---

# 6. PROGRESS ALWAYS VISIBLE SYSTEM

## Documentation

Read:
- EXP.md
- ECONOMY.md

Update:
- EXP.md

## Tasks

- [x] Ensure ALL major screens display at least one persistent progress indicator:
  - XP progress bar OR
  - streak indicator OR
  - next reward progress
- [x] Add unified ProgressHeader component (reusable)
- [x] Integrate ProgressHeader into:
  - Home screen
  - Habit screen
  - Inventory screen
  - Journal/Timeline screen
- [x] Ensure progress updates are real-time
- [x] Avoid screens with “no feedback state”

---

# 7. SURPRISE REWARD SYSTEM (NON-ASSET BASED)

## Documentation

Read:
- ECONOMY.md
- CHEST_REWARDS.md
- ACTIVITY_LOG.md

Update:
- ECONOMY.md
- CHEST_REWARDS.md
- ACTIVITY_LOG.md

## Tasks

- [x] Implement rare bonus trigger system:
  - low probability reward override on habit completion
- [x] Possible surprise events:
  - bonus coins
  - bonus XP
  - extra chest trigger
- [x] Ensure system is:
  - non-predictable
  - non-intrusive
  - rate-limited (no spam)
- [x] Log surprise events into ActivityTimeline
- [x] Ensure surprises never block reward flow

---

# 8. SOFT NOTIFICATION RE-ENTRY SYSTEM

## Documentation

Read:
- NOTIFICATIONS.md

Update:
- NOTIFICATIONS.md

## Tasks

- [x] Redesign notification messages to be emotional instead of functional
- [x] Implement notification templates:
  - “Your dragon is waiting 🐉”
  - “Something grew while you were away”
  - “You’re close to a reward”
- [x] Remove harsh/commanding tone notifications
- [x] Add logic for context-based notification selection:
  - streak low → encouragement message
  - streak high → reinforcement message
- [x] Ensure notification system respects user activity frequency

---

# 9. ONE-TAP COMPLETION FLOW OPTIMIZATION

## Documentation

Read:
- EXP.md
- ECONOMY.md

Update:
- EXP.md
- ECONOMY.md

## Tasks

- [x] Reduce habit completion friction to single action
- [x] Ensure immediate reward pipeline execution:
  - XP gain
  - coin gain
  - event log creation
  - micro-feedback trigger
- [x] Remove unnecessary confirmation steps
- [x] Ensure reward screen does NOT block flow unless required by system rules
- [x] Optimize flow so completion feels instant and satisfying

---

# 10. EVOLUTION TEASING SYSTEM (NO NEW ASSETS REQUIRED)

## Documentation

Read:
- DRAGON_PHASES.md
- EXP.md

Update:
- DRAGON_PHASES.md

## Tasks

- [x] Display next evolution stage name in UI:
  - “Next: Hatchling”
  - “Next: Young Dragon”
- [x] Show progress toward next evolution threshold
- [x] Add “locked preview text” (no image required)
- [x] Ensure evolution progress is always visible somewhere in UI
- [x] Trigger timeline event when nearing evolution milestone (e.g. 80%)

---

# 11. EMPTY STATE EMOTIONAL SYSTEM

## Documentation

Read:
- NONE

Update:
- NONE (unless UI components added)

## Tasks

- [x] Replace all empty screens with emotional messages:
  - Journal empty → “Your story begins here”
  - No achievements → “Your first milestone is close”
  - No items → “New discoveries await you”
- [x] Ensure empty states feel encouraging, not neutral
- [x] Keep tone consistent with game world narrative
- [x] Add subtle progression hint in every empty state

---

12. DAILY STREAK VISUAL REINFORCEMENT SYSTEM
Documentation

Read:

STATISTICS.md
EXP.md

Update:

STATISTICS.md
Tasks
 Add persistent streak indicator in main UI (always visible)
 Show streak “state” visually (low / stable / strong)
 Add streak milestone markers (3, 7, 14, 30, etc.)
 Trigger subtle animation on streak increase (no new assets required)
 Add “streak protection” feedback when user is about to lose streak
 Ensure streak is emotionally framed (not just numeric)
13. DAILY GOAL SYSTEM (LIGHTWEIGHT TASK GOALS)
Documentation

Read:

EXP.md
ECONOMY.md

Update:

EXP.md
Tasks
 Define daily XP goal system (configurable target per day)
 Track progress toward daily goal (XP-based or habit-based)
 Display daily goal progress in Home screen
 Reward completion of daily goal (coins or XP bonus)
 Reset daily goal at midnight automatically
 Ensure goal feels achievable within normal usage
14. COMBO / MOMENTUM SYSTEM
Documentation

Read:

EXP.md
ECONOMY.md

Update:

EXP.md
Tasks
 Implement combo system for consecutive habit completions
 Increase XP slightly based on streaked activity (short-term momentum)
 Reset combo after inactivity window (configurable time threshold)
 Show subtle “combo multiplier” feedback in UI
 Ensure system is additive but not overpowered
 Log combo milestones in Activity Timeline system
15. POSITIVE REINFORCEMENT MESSAGE SYSTEM
Documentation

Read:

NOTIFICATIONS.md

Update:

NOTIFICATIONS.md
Tasks
 Replace generic system messages with emotional reinforcement messages
 Add dynamic message pool based on user behavior:
consistent user → “You’re building something strong”
inactive user → “Your dragon is still waiting for you”
streak user → “Your consistency is rare”
 Ensure messages feel human and supportive, not robotic
 Integrate reinforcement messages into:
notifications
activity timeline
reward screens
 Ensure message selection is contextual and non-repetitive
16. REWARD MOMENT AMPLIFICATION SYSTEM
Documentation

Read:

ECONOMY.md
CHEST_REWARDS.md

Update:

ECONOMY.md
Tasks
 Enhance all reward moments (XP, coins, chest, achievement)
 Add visual emphasis states (scale, glow, pause effect logic only)
 Ensure reward moments briefly “interrupt flow” positively
 Differentiate reward tiers:
small reward → subtle feedback
rare reward → stronger feedback
epic reward → full emphasis moment
 Ensure reward feedback never feels repetitive or annoying
 Integrate with existing reward pipelines without blocking logic

