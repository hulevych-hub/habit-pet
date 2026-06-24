# TODO — Test Implementation Plan

## Overview

This plan covers the complete test strategy for Habit Pet. The project currently has **4 test files** (2 are boilerplate) covering only the streak system. This plan adds **~120+ organized tests** across unit, integration, and UI layers.

### Current State
- `ExampleUnitTest.kt` — boilerplate (remove)
- `ExampleInstrumentedTest.kt` — boilerplate (remove)
- `StreakEngineTest.kt` — 6 tests (basic smoke tests, mostly "no crash" assertions)
- `StreakPartialCompletionTest.kt` — 9 tests (partial completion + freeze date parsing)
- `StreakCalculatorTest.kt` — 4 tests (consecutive streak calculation)
- Test dependencies: JUnit 4, Mockito-Kotlin 5.1.0, Mockito-Inline 5.2.0, Coroutines-Test 1.7.3

### Test Infrastructure Notes
- Mockito returns `null` for unstubbed Kotlin `Boolean` suspend methods → must use `doReturn(false).whenever(repo).method()` inside `runTest`
- `DragonMoodEngine` is `open` with `open suspend fun refreshMood()` for test subclassing
- `RewardQueue` is a regular class (not `object`), mockable with mockito-inline
- `mock-maker-inline` configured in `app/src/test/resources/mockito-extensions/`

---

## Phase 1: Foundation — Config & Pure Logic Unit Tests

These are pure functions and objects with no dependencies. Highest value, lowest effort.

### 1.1 ExpConfig Tests ✅
- [x] `xpRequiredForLevel — level 1 returns 30`
- [x] `xpRequiredForLevel — level 2 returns 60`
- [x] `xpRequiredForLevel — level N follows linear formula`
- [x] `totalXpRequiredForLevel — level 0 returns 0`
- [x] `totalXpRequiredForLevel — level 1 returns 30`
- [x] `totalXpRequiredForLevel — level 2 returns 90 (30+60)`
- [x] `totalXpRequiredForLevel — matches 15*L*(L+1) formula`
- [x] `calculateLevelFromXp — 0 XP = level 0`
- [x] `calculateLevelFromXp — exactly at level boundary`
- [x] `calculateLevelFromXp — between levels returns lower level`
- [x] `calculateLevelFromXp — very high XP`
- [x] `xpProgressInCurrentLevel — at level start returns 0`
- [x] `xpProgressInCurrentLevel — at level end returns full`
- [x] `xpRequiredForNextLevel — returns correct delta`
- [x] `calculateEvolutionStageFromXp — 0 XP = Egg (stage 0)`
- [x] `calculateEvolutionStageFromXp — 74 XP = Egg`
- [x] `calculateEvolutionStageFromXp — 75 XP = Hatchling (stage 1)`
- [x] `calculateEvolutionStageFromXp — 300 XP = Young Dragon (stage 2)`
- [x] `calculateEvolutionStageFromXp — 900 XP = Adult Dragon (stage 3)`
- [x] `calculateEvolutionStageFromXp — 2500 XP = Ancient Dragon (stage 4)`
- [x] `calculateEvolutionStageFromXp — very high XP stays at Ancient`
- [x] `evolutionStageName — all 5 stages return correct names`
- [x] `xpThresholdForStage — returns correct thresholds`
- [x] `levelUpCoins — level 1 returns 10`
- [x] `levelUpCoins — level 5 returns 50`
- [x] `isComboActive — within 2 hour window returns true`
- [x] `isComboActive — outside 2 hour window returns false`
- [x] `isComboActive — lastTimestamp = 0 returns false`
- [x] `comboBonusXp — combo 1 returns 0`
- [x] `comboBonusXp — combo 2 returns 1`
- [x] `comboBonusXp — combo 5 returns 4 (capped)`
- [x] `comboBonusXp — combo 10 returns 4 (capped)`
- [x] `comboMultiplier — combo 1 returns 1.0f`
- [x] `comboMultiplier — combo 5 returns 1.4f`
- [x] `comboMilestoneReached — 3, 5, 10 return true`
- [x] `comboMilestoneReached — 1, 2, 4, 6 return false`

### 1.2 EconomyConfig Tests ✅
- [x] `customizationPrice — NORMAL rarity returns 120`
- [x] `customizationPrice — RARE rarity returns 400`
- [x] `customizationPrice — EPIC rarity returns 1000`
- [x] `customizationPrice — LEGENDARY rarity returns 3000`
- [x] `customizationPrice — unknown rarity returns base price 100` (skipped - no unknown rarity)
- [x] `ceilDiv — exact division`
- [x] `ceilDiv — remainder rounds up`
- [x] `ceilDiv — zero value returns 0`
- [x] `DAYS_FOR_*_CUSTOMIZATION — computed values match targets`

### 1.3 DragonMood Tests ✅
- [x] `DragonMood.from — valid value returns correct enum`
- [x] `DragonMood.from — invalid value returns CALM default`
- [x] `DragonMood.from — case insensitive`
- [x] `calculate — no activity, no streak = CALM`
- [x] `calculate — streak > 0, no recent activity within 36h = HAPPY`
- [x] `calculate — streak >= 7 = PROUD`
- [x] `calculate — recent completions >= 3 = EXCITED`
- [x] `calculate — last activity >= 36h ago = LONELY`
- [x] `calculate — LONELY takes priority over PROUD when both conditions met`
- [x] `calculate — PROUD takes priority over EXCITED`
- [x] `calculate — zero lastActivityTimestamp = 0 hours (not LONELY)`

### 1.4 StreakCalculator Tests (expand existing) ✅
- [x] `currentDayIncomplete — returns 0` (existing)
- [x] `consecutiveCompleteDays — returns full streak` (existing)
- [x] `missedPreviousDay — stops streak at current day` (existing)
- [x] `empty completed days — returns 0`
- [x] `single day complete — returns 1`
- [x] `gap in middle — stops at gap`
- [x] `very long streak — 365 days`
- [x] `streak with only yesterday complete — today incomplete returns 0`
- [x] `streak with only day before yesterday — returns 0 (current incomplete)`

### 1.5 StatisticsEntity Tests ✅
- [x] `freezeDatesToJson — empty set returns "[]"`
- [x] `freezeDatesToJson — single date`
- [x] `freezeDatesToJson — multiple dates`
- [x] `parseFreezeDates — empty json returns empty set`
- [x] `parseFreezeDates — single date`
- [x] `parseFreezeDates — multiple dates`
- [x] `freezeDatesToJson + parseFreezeDates roundtrip`
- [x] `parseFreezeDates — malformed json returns empty set`

---

## Phase 2: Domain Engine Unit Tests

These test domain engines with mocked repositories. Critical for protecting business logic.

### 2.1 StreakEngine Tests (expand existing) ✅
- [x] `evaluateTodayStreak — increments streak when completions exist`
- [x] `evaluateTodayStreak — does not increment when no completions`
- [x] `evaluateTodayStreak — does not double-count same day`
- [x] `evaluateTodayStreak — no habits means no-op` (covered by empty habits early return)
- [x] `evaluateTodayStreak — resets broken streak before incrementing`
- [x] `evaluateTodayStreak — awards milestone chest at 7 days`
- [x] `evaluateTodayStreak — awards milestone chest at 14 days`
- [x] `evaluateTodayStreak — awards milestone chest at 30 days`
- [x] `evaluateTodayStreak — does not award milestone twice`
- [x] `evaluateTodayStreak — updates lastStreakAwardedAt after milestone`
- [x] `checkPendingStreakFreeze — returns null when streak is 0`
- [x] `checkPendingStreakFreeze — returns null when today has completions`
- [x] `checkPendingStreakFreeze — returns null when yesterday was completed`
- [x] `checkPendingStreakFreeze — returns prompt when freeze eligible`
- [x] `checkPendingStreakFreeze — returns null when freeze on cooldown`
- [x] `checkPendingStreakFreeze — returns null when yesterday was frozen`
- [x] `checkPendingStreakFreeze — returns null when day before yesterday not completed`
- [x] `useStreakFreeze — returns false when no pending freeze`
- [x] `useStreakFreeze — returns true and updates stats on success`
- [x] `useStreakFreeze — adds frozen date to set`
- [x] `useStreakFreeze — updates lastStreakFreezeDate`
- [x] `resetBrokenStreak — sets currentStreak and globalStreak to 0`
- [x] `resetBrokenStreak — no-op when streak already 0`
- [x] `recalculateTodayStreak — delegates to evaluateTodayStreak when needed`
- [x] `recalculateTodayStreak — no-op when already counted`
- [x] `isDayCompletedOrFrozen — frozen day returns true`
- [x] `isDayCompletedOrFrozen — completed day returns true`
- [x] `isDayCompletedOrFrozen — empty day returns false`

### 2.2 DragonMoodEngine Tests ✅
- [x] `refreshMood — updates pet mood when mood changes`
- [x] `refreshMood — does not update when mood unchanged`
- [x] `refreshMood — reads current streak from statistics`
- [x] `refreshMood — reads recent completions within 72h window`
- [x] `refreshMood — creates default pet if none exists`

### 2.3 ChallengeEngine Tests ✅
- [x] `recordHabitCompleted — delegates to repository`
- [x] `recordXpEarned — delegates to repository`
- [x] `recordCoinsEarned — delegates to repository`
- [x] `recordChestOpened — delegates to repository`
- [x] `recordStreak — delegates to repository`
- [x] `claimActiveChallenge — empty rewards returns result`
- [x] `claimActiveChallenge — coin reward adds to queue`
- [x] `claimActiveChallenge — exp reward adds to queue`
- [x] `claimActiveChallenge — chest reward builds and adds to queue`
- [x] `claimActiveChallenge — customization reward adds to queue`
- [x] `claimActiveChallenge — logs challenge completed to timeline`
- [x] `claimActiveChallenge — maps chest type string to ChestType enum`

### 2.4 AchievementEngine Tests ✅
- [x] `claimAchievement — returns false for non-existent achievement`
- [x] `claimAchievement — returns false when not unlocked`
- [x] `claimAchievement — returns false when target not reached`
- [x] `claimAchievement — returns false when already claimed`
- [x] `claimAchievement — returns true and processes rewards on success`
- [x] `claimAchievement — uses mutex for concurrent safety`
- [x] `updateAchievementProgress — updates matching achievements`
- [x] `updateAchievementProgress — unlocks when target reached`
- [x] `updateAchievementProgress — does not re-lock already unlocked`
- [x] `updateAchievementProgress — logs timeline event on unlock`
- [x] `updateAchievementProgress — negative progress coerced to 0`

### 2.5 ActivityTimelineEngine Tests ✅
- [x] `logHabitCompleted — creates event with correct type`
- [x] `logComboMilestone — creates event with correct type`
- [x] `logAchievementUnlocked — creates event with correct type`
- [x] `logLevelUp — creates event with correct type`
- [x] `logDragonEvolution — creates event with correct type`
- [x] `logStreakMilestone — creates event with correct type`
- [x] `logSurpriseReward — creates event with correct type`
- [x] `logChallengeCompleted — creates event with correct type`
- [x] `ensureFirstDailyLoginEvent — logs on first visit`
- [x] `ensureFirstDailyLoginEvent — does not log twice same day`
- [ ] `ensureFirstDailyLoginEvent — logs again next day`
- [ ] `evolutionProgressFraction — at stage start returns 0`
- [ ] `evolutionProgressFraction — at stage end returns 1`
- [ ] `evolutionProgressFraction — midpoint returns ~0.5`
- [ ] `formatLastSessionDifference — first visit message`
- [ ] `formatLastSessionDifference — minutes ago`
- [ ] `formatLastSessionDifference — hours ago`
- [ ] `formatLastSessionDifference — days ago`
- [ ] `motivationalMessageFor — first visit`
- [ ] `motivationalMessageFor — within a day`
- [ ] `motivationalMessageFor — 1-3 days`
- [ ] `motivationalMessageFor — 3-7 days`
- [ ] `motivationalMessageFor — over 7 days`

---

## Phase 3: Repository Implementation Unit Tests

These test repository implementations with mocked DAOs.

### 3.1 HabitCompletionRepositoryImpl Tests (expand existing)
- [ ] `addCompletionWithCombo — duplicate returns isNewCompletion=false`
- [ ] `addCompletionWithCombo — new completion returns isNewCompletion=true`
- [ ] `addCompletionWithCombo — combo active increments combo count`
- [ ] `addCompletionWithCombo — combo inactive resets combo to 1`
- [ ] `addCompletionWithCombo — combo bonus XP added to earned`
- [ ] `addCompletionWithCombo — combo milestone detected`
- [ ] `addCompletionWithCombo — updates statistics after completion`
- [ ] `addCompletionWithCombo — updates habit streak`
- [ ] `addCompletionWithCombo — insert failure returns -1 with isNewCompletion=false`
- [ ] `hasAnyCompletionOnDate — count > 0 returns true`
- [ ] `hasAnyCompletionOnDate — count = 0 returns false`
- [ ] `areAllHabitsCompletedOnDate — all done returns true`
- [ ] `areAllHabitsCompletedOnDate — some done returns false`
- [ ] `areAllHabitsCompletedOnDate — no habits returns true`
- [ ] `isPartialCompletionOnDate — 1 of 3 returns true`
- [ ] `isPartialCompletionOnDate — 2 of 3 returns true`
- [ ] `isPartialCompletionOnDate — 0 of 3 returns false`
- [ ] `isPartialCompletionOnDate — 3 of 3 returns false`
- [ ] `isPartialCompletionOnDate — no habits returns false`
- [ ] `updateHabitStreak — consecutive day increments streak`
- [ ] `updateHabitStreak — non-consecutive day resets to 1`
- [ ] `updateHabitStreak — updates bestStreak when exceeded`
- [ ] `updateStatistics — increments totalCompletions`
- [ ] `updateStatistics — adds XP to totalXp`
- [ ] `updateStatistics — updates combo state`
- [ ] `updateStatistics — updates lastHabitCompletionTimestamp`
- [ ] `calculateTimerHabitCoins — base + per-minute calculation`
- [ ] `upsertStatistics — inserts when not exists`
- [ ] `upsertStatistics — updates when exists`

### 3.2 StatisticsRepositoryImpl Tests
- [ ] `addCoins — increases coin balance`
- [ ] `incrementStreak — increases currentStreak by 1`
- [ ] `markStreakUpdatedToday — sets flag`
- [ ] `isStreakAlreadyCountedToday — returns correct state`
- [ ] `getStatistics — returns flow of statistics`
- [ ] `updateStatistics — updates all fields`

### 3.3 PetRepositoryImpl Tests
- [ ] `getPet — returns flow of pet entity`
- [ ] `updatePet — updates pet in database`
- [ ] `addXp — increases pet XP`
- [ ] `addXp — recalculates level`
- [ ] `addXp — recalculates evolution stage`

---

## Phase 4: ViewModel Unit Tests

These test ViewModels with mocked repositories and verification of UI state/navigation events.

### 4.1 HomeScreenViewModel Tests
- [ ] `loadData — populates habits, pet, statistics, challenge state`
- [ ] `onHabitCompleted — refreshes all data`
- [ ] `globalStreakCompletedToday — true when streak counted`
- [ ] `globalStreakCompletedToday — false when not counted`
- [ ] `freeze prompt — shown when checkPendingStreakFreeze returns prompt`
- [ ] `useStreakFreeze — calls engine and refreshes data`
- [ ] `dismissFreezePrompt — clears prompt state`

### 4.2 HabitsViewModel Tests
- [ ] `completeCheckboxHabit — calls repository and awards XP/coins`
- [ ] `completeCheckboxHabit — duplicate completion is no-op`
- [ ] `completeCheckboxHabit — updates streak via StreakEngine`
- [ ] `completeCheckboxHabit — advances challenge progress`
- [ ] `completeCheckboxHabit — logs timeline event`
- [ ] `completeCheckboxHabit — queues reward events`
- [ ] `completeCheckboxHabit — optimistic completion removed on failure`
- [ ] `completeCheckboxHabit — optimistic completion removed on duplicate`
- [ ] `deleteHabit — calls repository delete`
- [ ] `deleteHabit — refreshes habit list`

### 4.3 HabitDetailViewModel Tests
- [ ] `completeCheckboxHabit — full flow with XP, coins, streak, rewards`
- [ ] `completeCheckboxHabit — duplicate stops before awarding`
- [ ] `stopTimerHabit — calculates XP from minutes`
- [ ] `stopTimerHabit — calculates coins from minutes`
- [ ] `stopTimerHabit — full flow with rewards`
- [ ] `loadHabit — populates habit details`
- [ ] `loadCompletions — loads completion history`
- [ ] `deleteHabit — navigates back on success`

### 4.4 HabitCreationViewModel Tests
- [ ] `createHabit — checkbox type creates habit`
- [ ] `createHabit — timer type creates habit with duration`
- [ ] `createHabit — validates required fields`
- [ ] `createHabit — navigates back on success`
- [ ] `createHabit — shows error on failure`

### 4.5 HabitEditViewModel Tests
- [ ] `loadHabit — populates form fields`
- [ ] `updateHabit — saves changes`
- [ ] `updateHabit — navigates back on success`
- [ ] `updateHabit — shows error on failure`

### 4.6 AchievementViewModel Tests
- [ ] `loadAchievements — populates achievement list`
- [ ] `claimAchievement — calls engine and refreshes`
- [ ] `claimAchievement — shows reward on success`
- [ ] `claimAchievement — shows error on failure`
- [ ] `claimAchievement — already claimed shows appropriate state`

### 4.7 RewardsViewModel Tests
- [ ] `loadRewards — populates inventory items`
- [ ] `purchaseItem — deducts coins and grants item`
- [ ] `purchaseItem — insufficient coins shows error`
- [ ] `purchaseItem — already owned shows error`
- [ ] `equipItem — updates equipped state`
- [ ] `equipItem — unequips previous item of same type`

### 4.8 NotificationSettingsViewModel Tests
- [ ] `loadSettings — populates notification preferences`
- [ ] `toggleReminder — updates preference`
- [ ] `setReminderTime — updates time`
- [ ] `setFrequency — updates frequency`

---

## Phase 5: Reward System Tests

### 5.1 RewardQueue Tests
- [ ] `addReward — enqueues reward`
- [ ] `addReward — maintains priority ordering`
- [ ] `rewardEvents — emits rewards in priority order`
- [ ] `rewardDismissed — advances to next reward`
- [ ] `mergeNextRewardIfPossible — merges compatible rewards`
- [ ] `mergeNextRewardIfPossible — does not merge incompatible`
- [ ] `empty queue — emits nothing`

### 5.2 RewardManager Tests
- [ ] `addReward — delegates to queue`
- [ ] `rewardCompleted — processes CoinReward adds coins`
- [ ] `rewardCompleted — processes LevelUpReward adds coins`
- [ ] `rewardCompleted — processes ExpReward adds XP`
- [ ] `rewardCompleted — processes ExpReward checks level up`
- [ ] `rewardCompleted — processes ExpReward checks evolution`
- [ ] `rewardCompleted — processes ChestReward decomposes into sub-rewards`
- [ ] `rewardCompleted — processes CustomizationReward grants item`
- [ ] `rewardCompleted — processes AchievementReward skips coin/exp (already processed)`
- [ ] `rewardCompleted — tracks challenge progress when flagged`
- [ ] `rewardCompleted — clears current reward`
- [ ] `addPetExp — updates pet XP, level, evolution stage`
- [ ] `queueLevelAndEvolutionRewards — queues LevelUpReward on level up`
- [ ] `queueLevelAndEvolutionRewards — queues DragonEvolutionReward on evolution`
- [ ] `queueLevelAndEvolutionRewards — logs evolution nearing at 80% progress`

### 5.3 ChestRewardFactory Tests
- [ ] `buildChestReward — Normal chest generates coins in range`
- [ ] `buildChestReward — Rare chest generates coins in range`
- [ ] `buildChestReward — Epic chest generates coins in range`
- [ ] `buildChestReward — Legendary chest generates coins in range`
- [ ] `buildChestReward — Rare chest has customization chance`
- [ ] `buildChestReward — Epic chest has customization chance`
- [ ] `buildChestReward — Legendary chest has customization chance`
- [ ] `buildChestReward — Normal chest has no customization chance`

### 5.4 ChestRewardConfigProvider Tests
- [ ] `getConfig — returns correct config for each ChestType`
- [ ] `getRandomChestType — returns valid ChestType`
- [ ] `getRandomChestType — distribution roughly matches probabilities`

---

## Phase 6: Integration Tests

These test multi-component flows with in-memory Room database.

### 6.1 Habit Completion Flow Integration Tests
- [ ] `complete checkbox habit — end-to-end with real database`
- [ ] `complete timer habit — end-to-end with real database`
- [ ] `complete same habit twice — idempotent, no double XP/coins`
- [ ] `complete habit — XP added to pet`
- [ ] `complete habit — coins added to statistics`
- [ ] `complete habit — combo state updated`
- [ ] `complete habit — timeline event logged`
- [ ] `complete habit — challenge progress advanced`
- [ ] `complete habit — streak incremented`
- [ ] `complete habit — milestone chest awarded at 7 days`

### 6.2 Streak System Integration Tests
- [ ] `streak increments — consecutive days with completions`
- [ ] `streak resets — gap day breaks streak`
- [ ] `streak freeze — freeze preserves streak`
- [ ] `streak freeze — consecutive freeze not allowed`
- [ ] `streak freeze — 7-day cooldown enforced`
- [ ] `streak recalculation — after habit deletion`
- [ ] `partial completion — 1 of N habits continues streak`
- [ ] `full completion — all habits continues streak`
- [ ] `milestone rewards — correct chest type per milestone`

### 6.3 Achievement System Integration Tests
- [ ] `achievement unlocks — habit count triggers achievement`
- [ ] `achievement unlocks — streak triggers achievement`
- [ ] `achievement unlocks — XP triggers achievement`
- [ ] `achievement unlocks — level triggers achievement`
- [ ] `achievement unlocks — completion count triggers achievement`
- [ ] `achievement unlocks — customization count triggers achievement`
- [ ] `achievement claim — coins awarded`
- [ ] `achievement claim — XP awarded`
- [ ] `achievement claim — chest awarded`
- [ ] `achievement claim — customization awarded`
- [ ] `achievement claim — cannot claim twice`
- [ ] `achievement claim — cannot claim before unlocked`

### 6.4 Evolution System Integration Tests
- [ ] `evolution — Egg to Hatchling at 75 XP`
- [ ] `evolution — Hatchling to Young Dragon at 300 XP`
- [ ] `evolution — Young Dragon to Adult Dragon at 900 XP`
- [ ] `evolution — Adult Dragon to Ancient Dragon at 2500 XP`
- [ ] `evolution — DragonEvolutionReward queued`
- [ ] `evolution — timeline event logged`
- [ ] `level up — correct level at XP boundaries`
- [ ] `level up — LevelUpReward queued`
- [ ] `level up — coins awarded`

### 6.5 Economy Integration Tests
- [ ] `purchase customization — coins deducted`
- [ ] `purchase customization — item granted`
- [ ] `purchase customization — insufficient coins fails`
- [ ] `purchase customization — already owned fails`
- [ ] `chest opening — coins added`
- [ ] `chest opening — XP added`
- [ ] `chest opening — customization granted`
- [ ] `surprise chest — triggered by probability`

---

## Phase 7: UI Tests (Compose)

These test Compose UI interactions with Hilt-injected test doubles.

### 7.1 Home Screen UI Tests
- [ ] `displays pet with correct evolution stage`
- [ ] `displays current streak`
- [ ] `displays habit cards`
- [ ] `habit card tap — completes habit`
- [ ] `habit card tap — shows completion feedback`
- [ ] `progress header — shows XP progress`
- [ ] `evolution teaser — shows when near evolution`
- [ ] `freeze prompt — displays when applicable`
- [ ] `freeze prompt — accept uses freeze`
- [ ] `freeze prompt — dismiss closes prompt`

### 7.2 Habits Screen UI Tests
- [ ] `displays habit list`
- [ ] `empty state — shows when no habits`
- [ ] `FAB — navigates to creation screen`
- [ ] `habit card — checkbox tap completes`
- [ ] `habit card — long press shows options`
- [ ] `habit card — swipe to delete`
- [ ] `navigation — tap habit goes to detail`

### 7.3 Habit Detail Screen UI Tests
- [ ] `displays habit info`
- [ ] `completion button — completes habit`
- [ ] `timer start/stop — tracks time`
- [ ] `timer completion — awards XP and coins`
- [ ] `completion history — shows past completions`
- [ ] `edit button — navigates to edit screen`
- [ ] `delete button — deletes and navigates back`
- [ ] `back button — navigates back`

### 7.4 Habit Creation/Edit Screen UI Tests
- [ ] `name field — accepts input`
- [ ] `type selection — checkbox selected by default`
- [ ] `type selection — timer shows duration picker`
- [ ] `duration picker — increments/decrements`
- [ ] `emoji picker — shows emoji options`
- [ ] `save button — creates habit`
- [ ] `save button — validates required fields`
- [ ] `cancel button — navigates back without saving`

### 7.5 Pet Screen UI Tests
- [ ] `displays animated pet`
- [ ] `displays equipped customization`
- [ ] `customization grid — shows items`
- [ ] `customization tap — equips item`
- [ ] `customization long press — shows preview`
- [ ] `attribute cards — show stats`
- [ ] `rewards button — navigates to rewards`

### 7.6 Achievements Screen UI Tests
- [ ] `displays achievement list`
- [ ] `achievement card — shows progress`
- [ ] `achievement card — shows locked state`
- [ ] `claim button — visible when unlocked`
- [ ] `claim button — not visible when locked`
- [ ] `claim button — not visible when already claimed`
- [ ] `claim button — shows reward overlay`

### 7.7 Rewards Screen UI Tests
- [ ] `displays owned items tab`
- [ ] `displays shop tab`
- [ ] `shop item — shows price`
- [ ] `shop item — purchase button works`
- [ ] `shop item — insufficient coins shows error`
- [ ] `owned item — equip button works`
- [ ] `tab switching — owned/shop`

### 7.8 Reward Overlay UI Tests
- [ ] `level up overlay — displays level and coins`
- [ ] `evolution overlay — displays stage change`
- [ ] `chest overlay — displays rewards`
- [ ] `streak overlay — displays streak count`
- [ ] `overlay — blocks navigation until dismissed`
- [ ] `overlay — dismiss advances to next reward`
- [ ] `overlay queue — processes in correct order`

### 7.9 Navigation Tests
- [ ] `bottom nav — all tabs reachable`
- [ ] `bottom nav — correct tab highlighted`
- [ ] `detail screens — bottom nav hidden`
- [ ] `back navigation — returns to previous screen`
- [ ] `deep link — habit detail by ID`

---

## Phase 8: Edge Cases & Regression Tests

### 8.1 Edge Cases
- [ ] `habit completion — at midnight boundary`
- [ ] `streak — across month/year boundaries`
- [ ] `streak — timezone changes`
- [ ] `XP — overflow protection`
- [ ] `coins — negative balance prevention`
- [ ] `combo — exactly at 2 hour window boundary`
- [ ] `combo — exactly at max bonus cap (combo 5+)`
- [ ] `evolution — exactly at threshold XP`
- [ ] `level up — exactly at threshold XP`
- [ ] `empty database — all screens handle gracefully`
- [ ] `concurrent habit completions — no race conditions`
- [ ] `rapid habit completion/deletion — consistent state`

### 8.2 Known Bug Regression Tests
- [ ] `level-up coin double-award — document current behavior`
- [ ] `AchievementReward coin/exp — RewardManager skips to avoid double-processing`
- [ ] `mock-maker-inline — Boolean unboxing NPE prevention`

---

## Phase 9: Documentation Consistency Checks

### 9.1 Doc Audit
- [ ] `ACCESSORIES.md — mark as legacy (references obsolete HAT/GLASSES/SCARF)`
- [ ] `QUESTS.md — mark as not implemented`
- [ ] `ENDGAME.md — mark as not implemented`
- [ ] `DAILY_REWARDS.md — verify challenge system docs match code`
- [ ] `EXP.md — verify level formula matches ExpConfig`
- [ ] `ECONOMY.md — verify coin values match EconomyConfig`
- [ ] `CUSTOMIZATION.md — verify categories match CustomizationTypes`
- [ ] `ACHIEVEMENTS.md — verify achievement list matches AchievementsConfig`
- [ ] `DRAGON_PHASES.md — verify evolution thresholds match ExpConfig`
- [ ] `STATISTICS.md — verify entity fields match StatisticsEntity`
- [ ] `EVENTS.md — verify event types match GameEventType`
- [ ] `NOTIFICATIONS.md — verify notification types match code`
- [ ] `DATA_MODEL.md — verify Room schema matches AppDatabase`

---

## Implementation Order

1. **Phase 1** — Pure logic tests (no setup needed, immediate value)
2. **Phase 2** — Domain engine tests (protect core business logic)
3. **Phase 3** — Repository tests (protect data layer)
4. **Phase 4** — ViewModel tests (protect UI state management)
5. **Phase 5** — Reward system tests (protect reward pipeline)
6. **Phase 6** — Integration tests (protect end-to-end flows)
7. **Phase 7** — UI tests (protect user interactions)
8. **Phase 8** — Edge cases (protect against regressions)
9. **Phase 9** — Doc audit (ensure docs match code)

---

## Test File Organization

```
app/src/test/java/com/example/mobile/
├── domain/
│   ├── ExpConfigTest.kt
│   ├── EconomyConfigTest.kt
│   ├── DragonMoodTest.kt
│   ├── StreakCalculatorTest.kt (expand existing)
│   ├── StreakEngineTest.kt (expand existing)
│   ├── DragonMoodEngineTest.kt
│   ├── ChallengeEngineTest.kt
│   ├── AchievementEngineTest.kt
│   ├── ActivityTimelineEngineTest.kt
│   └── RewardCalculatorTest.kt
├── data/
│   └── repository/
│       ├── HabitCompletionRepositoryImplTest.kt (expand existing)
│       ├── StatisticsRepositoryImplTest.kt
│       └── PetRepositoryImplTest.kt
├── presentation/
│   ├── viewmodel/
│   │   ├── HomeScreenViewModelTest.kt
│   │   ├── HabitsViewModelTest.kt
│   │   ├── HabitDetailViewModelTest.kt
│   │   ├── HabitCreationViewModelTest.kt
│   │   ├── HabitEditViewModelTest.kt
│   │   ├── AchievementViewModelTest.kt
│   │   ├── RewardsViewModelTest.kt
│   │   └── NotificationSettingsViewModelTest.kt
│   └── reward/
│       ├── RewardQueueTest.kt
│       ├── RewardManagerTest.kt
│       ├── ChestRewardFactoryTest.kt
│       └── ChestRewardConfigProviderTest.kt
├── integration/
│   ├── HabitCompletionFlowTest.kt
│   ├── StreakSystemTest.kt
│   ├── AchievementSystemTest.kt
│   ├── EvolutionSystemTest.kt
│   └── EconomyTest.kt
└── edge/
    └── EdgeCaseTest.kt

app/src/androidTest/java/com/example/mobile/
├── ui/
│   ├── HomeScreenTest.kt
│   ├── HabitsScreenTest.kt
│   ├── HabitDetailScreenTest.kt
│   ├── HabitCreationScreenTest.kt
│   ├── PetScreenTest.kt
│   ├── AchievementsScreenTest.kt
│   ├── RewardsScreenTest.kt
│   ├── RewardOverlayTest.kt
│   └── NavigationTest.kt
└── db/
    └── AppDatabaseTest.kt
```
