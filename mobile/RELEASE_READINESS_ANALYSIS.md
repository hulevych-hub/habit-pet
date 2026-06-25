# Habit Pet — Release Readiness Analysis

**Date:** 2026-06-24
**Branch:** `main` (1 commit ahead of origin, working tree clean)
**Target:** Offline-first release for friends to use long-term
**Notifications:** Declared out of scope per request

---

## Executive Summary

The app is **mostly feature-complete** for an offline first release. Core loops (habits, completions, XP, leveling, evolution, streaks, achievements, customization, shop, rewards, activity timeline) are all wired end-to-end. The codebase is clean, well-structured, and follows the architecture documented in `CLAUDE.md`.

However, there are **several real gaps** that will cause confusion or rough edges for long-term users, plus a few **blockers** that should be addressed before handing it to friends.

---

## CRITICAL (should fix before release)

### 1. TODO.md is the execution source of truth and it is mostly unchecked
`CLAUDE.md` states: *"TODO.md is the execution source of truth. If it contains unchecked work, complete items in order."* The file is a ~640-line test plan. Phase 1 (pure logic tests) is largely done. Phases 2–9 are almost entirely unchecked. That means:
- Repository tests (HabitCompletion, Statistics, Pet) — **not written**
- ViewModel tests — **not written**
- Integration tests — **not written**
- UI tests — **not written**
- Doc audit (Phase 9) — **not done**

This is the single biggest risk: there is no automated regression protection on the habit completion flow, reward pipeline, streak system, or evolution logic. A small refactor can silently break the core loop.

**Recommendation:** At minimum, write the Phase 2 + Phase 3 tests that protect `HabitCompletionRepositoryImpl`, `StreakEngine`, `RewardManager`, and `AchievementEngine`. These are the highest-risk areas.

### 2. No integration / end-to-end tests
There are zero files under `app/src/androidTest/`. The existing 27 test files are all pure unit tests. The most critical user flows have zero coverage:
- Habit completion → XP → level-up → reward queue → overlay
- Streak evaluation across days
- Achievement unlock → claim → reward
- Evolution threshold crossing
- Combo state across completions

**Recommendation:** Add a small in-memory Room integration test suite (5–10 tests) that exercise the real database + repository + engine stack. This is high value for long-term stability.

### 3. Build was initiated but result not confirmed in this session
`./gradlew assembleDebug` was started but the output was truncated before final result. The pre-existing `app-debug.apk` and `habit-pet.apk` in the repo root suggest it was building recently, but you should re-run and confirm a clean build before shipping.

**Recommendation:** Run `./gradlew assembleDebug` fresh, confirm `BUILD SUCCESSFUL`, and copy the APK to `C:\Users\serhi\Documents\Development\desktop-android-sharing\habit-pet.apk`.

---

## HIGH (will cause user-facing rough edges)

### 4. No onboarding / first-launch experience
There is no first-run tutorial, empty-state guidance, or example habit. A friend opening the app for the first time sees:
- Empty Home screen
- Empty Habits screen
- Empty Pet screen (Egg, but no context)
- Empty Achievements / Rewards

The `EmptyStateCard` component exists, but there is no onboarding flow that teaches the loop: *create habit → complete → earn XP → level up → earn coins → buy customization*.

**Recommendation:** Add a minimal first-launch screen or dialog that explains the loop and offers to create a first habit.

### 5. Streak system has no long-term tested edge cases
The streak freeze logic is intricate (once per 7 days, never two frozen days in a row, only for one-day gaps). The TODO.md shows basic freeze tests are written, but:
- No test for streak across month/year boundaries
- No test for timezone changes
- No test for streak recalculation after habit deletion
- No integration test for the full freeze flow

For long-term use, streak bugs are the most likely source of user frustration.

**Recommendation:** Add 3–4 edge-case tests for streak freeze + recalculation, and manually test the freeze flow once before release.

### 6. Reward pipeline has a known double-award audit point
`CLAUDE.md` documents: *"Level-up coin double-award risk exists: habit ViewModels award level-up base coins directly and also queue LevelUpReward, which RewardManager processes."* This is a latent bug. It may be intentional, but it should be audited and documented.

**Recommendation:** Trace the exact flow in `HabitDetailViewModel.completeCheckboxHabit()` and `RewardManager.rewardCompleted()` to confirm whether double-award happens. If it does, fix it before release.

### 7. Activity timeline has no "first daily login" long-term test
`ActivityTimelineEngine.ensureFirstDailyLoginEvent()` is once-per-day. The TODO shows the "logs again next day" test is unchecked. For long-term use, this is a minor but real gap.

**Recommendation:** Add the "next day" test.

### 8. No data export / backup
Friends using the app long-term will accumulate habit history, achievements, and customization. There is no export, backup, or migration path. If they clear app data or switch devices, everything is lost.

**Recommendation:** Consider adding a simple JSON export/import of user data (habits, completions, pet state, statistics). This is a "nice to have" but important for long-term retention.

---

## MEDIUM (polish / completeness)

### 9. Boilerplate test files still present
`ExampleUnitTest.kt` and `ExampleInstrumentedTest.kt` (the latter may not exist) are boilerplate. They should be removed.

### 10. Legacy documentation is stale and contradictory
`CLAUDE.md` already flags these, but they are still in the repo root:
- `ACCESSORIES.md` — references obsolete HAT/GLASSES/SCARF
- `QUESTS.md` — documents an absent system
- `ENDGAME.md` — documents an absent system
- `DAILY_REWARDS.md` — documents the rotating challenge system (current code uses `ChallengeConfig`)

These will confuse anyone reading the docs. The Phase 9 doc audit is entirely unchecked.

**Recommendation:** Either delete these files or add a clear "LEGACY / NOT IMPLEMENTED" header.

### 11. Notification system is partially implemented but not wired as a user-facing feature
You said notifications are not important, but the code is there (`NotificationHelper`, `NotificationPrefs`, `NotificationPublisher`, `BootCompletedReceiver`, `NotificationSettingsScreen`). The settings screen is in the bottom nav. A user will see it and expect it to work. If it's not fully tested, it may crash or drain battery.

**Recommendation:** Either test it fully or remove the settings screen from the bottom nav until it's ready.

### 12. Statistics screen exists but some stats are unused
`CLAUDE.md` notes: *"Some statistics are tracked but not prominently used/displayed (bestStreak, rewardChestsAvailable, petAgeDays, lastStreakAwardedAt)."* The `StatisticsScreen` exists but may be sparse.

**Recommendation:** Verify the Statistics screen displays meaningful data. If it's mostly empty, consider hiding it or populating it.

### 13. No UI tests for any screen
Zero Compose UI tests. The most likely regressions are visual (wrong text, missing icon, broken layout on small screens). Without UI tests, these will only be caught by manual testing.

**Recommendation:** Manually test every screen on a small device before release.

### 14. Achievement system has no "claim all" or bulk flow
Users with multiple unlocked achievements must claim them one by one. For long-term users, this is tedious.

**Recommendation:** Consider adding a "Claim All" button or batch claim.

---

## LOW (nice to have)

### 15. No analytics or crash reporting
For a release to friends, there is no way to know if the app is crashing. Consider adding a simple crash reporter or at least a "share crash log" option.

### 16. No in-app feedback mechanism
Friends have no way to report bugs or send feedback from within the app.

### 17. No version / about screen
No way for users to see the app version or build number.

---

## System-by-System Status

| System | Status | Notes |
|---|---|---|
| Navigation (NavGraph) | ✅ Done | All routes wired, bottom nav works |
| Database (Room v19) | ✅ Done | All entities, DAOs, migrations present |
| Habit completion flow | ✅ Done | XP, coins, combo, streak, timeline, rewards wired |
| Reward pipeline | ✅ Done | Queue, Manager, OverlayHost, EventBus all present |
| Evolution / Level-up | ✅ Done | Thresholds match ExpConfig |
| Challenge system | ✅ Done | ChallengeConfig, Engine, Repository present |
| Achievement system | ✅ Done | Engine, RewardProcessor, Screen present |
| Customization / Shop | ✅ Done | EquipableConfig, Inventory, RewardsScreen present |
| Pet screen | ✅ Done | AnimatedPet, AssetPainter, customization rendering |
| Activity Timeline | ✅ Done | Engine, GameEventFactory, Screen present |
| Streak system | ✅ Done | Engine, Calculator, freeze logic present |
| Economy (coins, chests) | ✅ Done | EconomyConfig, ChestRewardFactory present |
| Notifications | ⚠️ Partial | Code exists but untested; you said not important |
| Onboarding | ❌ Missing | No first-run experience |
| Data export | ❌ Missing | No backup/migration path |
| Tests (integration) | ❌ Missing | No androidTest files at all |
| Tests (unit, critical paths) | ⚠️ Partial | Phase 1 done; Phases 2–9 unchecked |
| Doc audit | ❌ Missing | Legacy docs contradict code |

---

## Recommended Action Plan (before release)

1. **Run `./gradlew assembleDebug`** and confirm clean build
2. **Write integration tests** for: habit completion → XP → level → reward; streak freeze; achievement claim; evolution
3. **Audit the level-up coin double-award** and fix if it's a real bug
4. **Add a minimal onboarding** first-launch dialog/screen
5. **Remove or mark legacy docs** (ACCESSORIES.md, QUESTS.md, ENDGAME.md)
6. **Manually test every screen** on a small device
7. **Decide on notifications**: test fully or hide the settings screen
8. **Copy final APK** to `C:\Users\serhi\Documents\Development\desktop-android-sharing\habit-pet.apk`

---

## Bottom Line

The app is **playable and feature-complete** for an offline single-player experience. The core loop works. The architecture is clean. The main risks are:
- **No regression tests** on the most critical flows (completion, rewards, streaks)
- **No onboarding** for first-time users
- **No data export** for long-term retention
- **One known latent bug** (level-up coin double-award) that should be audited

If you fix the double-award, add a first-run dialog, and manually smoke-test the core loop once, it should be ready for friends to use. For long-term confidence, add integration tests before the first friend report comes in.
