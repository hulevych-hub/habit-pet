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

# 1.

---

# PROJECT AUDIT FINDINGS

Audit scope covered the Android/Kotlin/Jetpack Compose Habit Pet project under `app/src/main/java/com/example/mobile`, project documentation, Gradle config, Room entities/DAOs, repositories, ViewModels, reward/progression/customization systems, dragon rendering, activity timeline, and documentation consistency.

Validation performed:
- Read `CLAUDE.md`, `TODO.md`, all markdown documentation, Gradle/config files, Android manifest, repositories, entities, ViewModels, reward systems, progression systems, customization systems, timeline systems, and UI screens.
- Ran `./gradlew assembleDebug`; build completed successfully with warnings.
- Checked customization assets against `EquipableConfig` using supported drawable extensions.
- Counted configured achievements and equipables.
- Did not modify source code.
- Did not implement fixes.

Summary counts:
- Total findings: 26
- Critical findings: 4
- High priority findings: 10
- Medium priority findings: 8
- Low priority findings: 4
- Documentation inconsistencies: 8
- Potential balance issues: 4
- Potential architectural issues: 7
- Potential cleanup opportunities: 5

---

## Priority
HIGH

## Issue
`RewardQueue` uses a mutable list without synchronization.

## Impact
Reward additions, dismissals, and merge operations can race when called from different coroutine contexts, causing lost rewards, duplicate rewards, or invalid queue state.

## Recommended Fix
Replace the mutable list with a synchronized queue, Mutex-protected operations, or an actor/channel-based queue.

## Priority
HIGH

## Issue
`RewardManager` is documented as the only place that should apply rewards, but `AchievementRewardProcessor` applies coins/XP/customization directly.

## Impact
Reward application is split across two systems, creating duplicate rewards and bypassing centralized reward ordering/validation.

## Recommended Fix
Make `AchievementRewardProcessor` prepare only reward UI events or persist achievement claim state, and let `RewardManager` apply rewards through the existing centralized pipeline.

## Priority
CRITICAL

## Issue
`RewardsViewModel` is a Hilt ViewModel under `presentation.ui.screens` and depends on `HomeScreenViewModel`.

## Impact
Screen-to-screen ViewModel coupling weakens MVVM boundaries and makes reward navigation state harder to test/reuse.

## Recommended Fix
Move reward screen state into a dedicated ViewModel in `presentation.viewmodel`, or pass a simple UI state/interactor instead of injecting another screen ViewModel.

## Priority
MEDIUM

## Issue
`PetRepositoryImpl.equipItem` mutates inventory and pet state without a Room transaction.

## Impact
Partial failure between inventory updates and pet equipped-state updates could leave ownership/equipment state inconsistent.

## Recommended Fix
Wrap the pet/inventory updates in a Room `@Transaction` or DAO transaction.

## Priority
MEDIUM

## Issue
`InventoryItemDatabaseInitializer` syncs configured catalog rows by overwriting `isUnlocked` with `configuredItem.isUnlocked`.

## Impact
If configuration changes, player-owned/unlocked items can be reset even though `isPurchased`/`isEquipped` are preserved.

## Recommended Fix
Do not overwrite player-owned unlock state during catalog sync. Only update catalog metadata, price, rarity, and source unless the item has never been purchased/unlocked.

## Priority
MEDIUM

---

# Gameplay

## Issue
Timer habit coin calculation derives minutes from `completion.xpEarned - ExpConfig.TIMER_HABIT_BASE_XP`.

## Impact
XP includes combo bonus, so combo completions inflate timer minutes and coin income.

## Recommended Fix
Persist timer minutes separately in `HabitCompletionEntity`, or calculate timer coins from the original minutes field rather than XP.

## Priority
HIGH

## Issue
Challenge progress is recorded twice for normal habit completions.

## Impact
Challenge completion can occur earlier than intended, reducing retention value and distorting challenge rewards.

## Recommended Fix
Remove duplicate challenge progress calls and keep challenge advancement in the repository or completion service.

## Priority
HIGH

## Issue
`RewardManager.queueLevelAndEvolutionRewards` logs “evolution milestone nearing” on every level-up because `nextEvolutionStage = updatedPet.evolutionStage + 1`.

## Impact
Timeline can contain misleading “nearing evolution” events after unrelated level-ups.

## Recommended Fix
Compare XP to the next evolution threshold and only log when the pet is meaningfully close, not merely at the next level.

## Priority
MEDIUM

## Issue
`PetEntity.coins` exists but `StatisticsEntity` is the coin source of truth.

## Impact
Dead/misleading field can confuse future development and create accidental coin duplication if used.

## Recommended Fix
Remove `PetEntity.coins` if unused, or clearly mark it as legacy/deprecated and prevent future use.

## Priority
LOW

---

# EXP

## Issue
Achievement EXP is applied directly by `AchievementRewardProcessor.addPetExp` without recalculating level/evolution.

## Impact
Achievement XP can advance pet level/evolution but skip level-up, chest, and dragon evolution rewards.

## Recommended Fix
Use the same XP update path as habit rewards, or recalculate level/evolution and queue the appropriate reward events after achievement EXP.

## Priority
CRITICAL

## Issue
`EXP.md` states Level 60 total XP is 54900, but `ExpConfig.totalXpRequiredForLevel(60)` is 55800.

## Impact
Progression documentation is incorrect and can mislead balance decisions.

## Recommended Fix
Update `EXP.md` to match the code formula or intentionally change the formula and migration/balance data.

## Priority
MEDIUM

## Issue
Combo bonus affects timer habit minutes indirectly through `xpEarned`.

## Impact
Timer habits can earn more coins than intended when combo bonuses are active.

## Recommended Fix
Separate base XP, combo bonus XP, and timer minutes in completion data or coin calculation.

## Priority
MEDIUM

## Issue
Level-up base coins are awarded directly by habit ViewModels and also processed by `LevelUpReward`.

## Impact
Potential level-up coin double-award risk exists in the reward/economy pipeline.

## Recommended Fix
Choose one authoritative level-up coin path. Prefer `RewardManager` for reward coin application and remove direct level-up coin awards from habit ViewModels if duplicate.

## Priority
HIGH

---

# Economy

## Issue
Timer habit coin calculation uses combo-inflated XP as a proxy for minutes.

## Impact
Coin income can inflate when combo bonuses are active.

## Recommended Fix
Calculate timer habit coins from persisted minutes, not from `xpEarned`.

## Priority
HIGH

## Issue
`EconomyConfig.DAYS_FOR_RARE_CUSTOMIZATION` uses integer division and can resolve to 1 day.

## Impact
Rare customization affordability pacing may be much faster than intended.

## Recommended Fix
Review coin income vs customization prices and update affordability constants intentionally.

## Priority
MEDIUM

## Issue
Level-up coin double-award risk exists.

## Impact
Players may receive level-up coins twice, accelerating economy and reducing progression pacing.

## Recommended Fix
Centralize level-up coin application in `RewardManager` or remove one path.

## Priority
HIGH

## Issue
Chest reward customization may be granted when building the chest reward and then granted again through the `CustomizationReward` sub-event.

## Impact
Redundant grant attempts can create inconsistent ownership/challenge progress and confusing reward processing.

## Recommended Fix
Grant customization exactly once, either at chest build time with metadata only for UI or inside `RewardManager` when the sub-event is displayed.

## Priority
HIGH

---

# Rewards

## Issue
`AchievementRewardProcessor` applies coin, XP, and customization rewards immediately and then queues the same reward UI events.

## Impact
Achievement rewards can duplicate coins, XP, and customization grants.

## Recommended Fix
Do not apply rewards in `AchievementRewardProcessor`. Queue rewards for `RewardManager`, or prepare display-only reward metadata and let `RewardManager` apply them once.

## Priority
CRITICAL

## Issue
`RewardOverlayHost` makes the entire background clickable for all non-dragon rewards.

## Impact
A chest reward can be dismissed by tapping outside the chest before the player opens it, weakening the reward experience and causing premature completion.

## Recommended Fix
Only allow the reward card/content area to complete the reward. Keep dim background non-clickable for chest/achievement/level-up/streak rewards unless explicitly intended.

## Priority
CRITICAL

## Issue
`PetPhaseTransition` never invokes `onTransitionCompleted`.

## Impact
Dragon evolution reward cannot be dismissed/continued, blocking the player after evolution.

## Recommended Fix
Call `onTransitionCompleted` when the transition animation finishes, and call `PetTransitionPrefs.markTransitionPlayed` once the transition has played.

## Priority
CRITICAL

## Issue
`PetTransitionPrefs.markTransitionPlayed` is never called.

## Impact
Evolution transition animations are not persisted as played, so replay behavior may not match intended UX.

## Recommended Fix
Call `markTransitionPlayed` from `PetPhaseTransition` when the transition completes.

## Priority
MEDIUM

## Issue
`RewardQueue` is not thread-safe.

## Impact
Concurrent reward additions/dismissals can corrupt queue state.

## Recommended Fix
Use a Mutex/channel/actor or synchronized queue implementation.

## Priority
HIGH

## Issue
`RewardOverlay.kt` is unused.

## Impact
Dead UI code increases maintenance burden and can confuse future developers.

## Recommended Fix
Delete the obsolete overlay file after confirming no runtime/navigation references exist.

## Priority
LOW

---

# Achievements

## Issue
Achievement screen passes `emptyList()` to `AchievementRewardContent`.

## Impact
Achievement reward labels are not shown even though `AchievementViewModel.rewardLabels()` exists.

## Recommended Fix
Pass `achievementViewModel.rewardLabels(achievement)` to `AchievementRewardContent`.

## Priority
MEDIUM

## Issue
`AchievementRewardProcessor` applies achievement rewards outside the centralized reward manager.

## Impact
Achievement rewards bypass centralized ordering, duplicate processing, and may skip level/evolution rewards for XP.

## Recommended Fix
Refactor achievement processing so reward application goes through `RewardManager`.

## Priority
CRITICAL

## Issue
Several achievement constants/names imply customization rewards that are not stable `EquipableConfig` IDs.

## Impact
Achievement metadata is harder to reason about and may accidentally reference non-existent equipables in future changes.

## Recommended Fix
Use stable `EquipableConfig` constants for actual customization rewards and keep milestone ids/names separate from equipable ids.

## Priority
LOW

## Issue
Achievement metadata initializer inserts missing achievements but does not update changed definitions.

## Impact
If achievement names, descriptions, icons, targets, or rewards change, existing achievement rows may remain stale.

## Recommended Fix
Decide whether achievement metadata should be mutable or config-driven. If config-driven, update metadata safely without resetting player progress/claim state.

## Priority
MEDIUM

---

# Customization

## Issue
`BACKGROUND_VOLCANIC` is displayed as “Forest Background”.

## Impact
Players see the wrong background name in the rewards UI.

## Recommended Fix
Rename the `BACKGROUND_VOLCANIC` catalog item to “Volcanic Background” or another accurate label.

## Priority
LOW

## Issue
Documentation says “all 16 customization items,” but `EquipableConfig` currently has 19 items.

## Impact
Achievement and documentation targets are outdated relative to the actual catalog.

## Recommended Fix
Update docs and achievement targets to match the current catalog, or intentionally reduce catalog to 16.

## Priority
MEDIUM

## Issue
Some customization reward achievements use ids/names that do not match actual equipable ids.

## Impact
Future refactors may break customization reward mapping or create confusion between milestone achievements and actual items.

## Recommended Fix
Make customization reward achievements reference stable `EquipableConfig` IDs only when the reward is a customization item.

## Priority
MEDIUM

## Issue
`InventoryItemRepositoryImpl.grantItem`/`grantItemByItemId` do not set `isUnlocked = true`.

## Impact
Granted chest/achievement items may be purchased but not marked unlocked, depending on UI logic expectations.

## Recommended Fix
Clarify ownership semantics and set `isUnlocked = true` when granting non-shop rewards, or rename fields to avoid ambiguity.

## Priority
MEDIUM

---

# UI

## Issue
Dragon evolution reward is blocked because `PetPhaseTransition` never calls `onTransitionCompleted`.

## Impact
After dragon evolution, the player can be stuck on the evolution reward overlay.

## Recommended Fix
Invoke the transition completion callback when the animation ends and persist the transition as played.

## Priority
CRITICAL

## Issue
Chest reward can be dismissed by tapping outside the chest.

## Impact
The chest opening moment can be skipped accidentally or too easily.

## Recommended Fix
Restrict reward completion clicks to the reward card/content.

## Priority
HIGH

## Issue
Achievement rewards are hidden in the achievement screen.

## Impact
Players cannot see what rewards they will receive from achievements.

## Recommended Fix
Use `AchievementViewModel.rewardLabels()` when rendering achievement cards.

## Priority
MEDIUM

## Issue
Rewards screen is not mapped as a bottom route in `NavGraph`.

## Impact
Bottom navigation may not hide correctly when opening Rewards, despite Rewards not being a bottom nav destination.

## Recommended Fix
Add a route mapping so Rewards hides the bottom bar.

## Priority
MEDIUM

---

# Documentation

## Issue
`CUSTOMIZATION.md` says missing backgrounds/outfits/auras are skipped/fallback, but `CLAUDE.md` says missing catalog assets should be treated as configuration errors.

## Impact
Documentation conflicts with the intended asset policy.

## Recommended Fix
Update `CUSTOMIZATION.md` to state that missing catalog assets are configuration errors, while optional preview fallbacks may still be handled defensively.

## Priority
MEDIUM

## Issue
`ACHIEVEMENTS.md` and achievement descriptions reference “16 customization items,” but `EquipableConfig` has 19 equipables.

## Impact
Achievement documentation is outdated.

## Recommended Fix
Update achievement documentation and targets to reflect the current catalog size.

## Priority
MEDIUM

## Issue
`EXP.md` has an incorrect Level 60 total XP value.

## Impact
Progression documentation does not match code.

## Recommended Fix
Correct Level 60 total XP to 55800 or change the formula intentionally.

## Priority
MEDIUM

## Issue
`QUESTS.md` and `ENDGAME.md` document systems not implemented in current code.

## Impact
Docs describe unavailable features as if they exist.

## Recommended Fix
Mark quests/endgame as future concepts, remove them, or implement the missing systems.

## Priority
LOW

## Issue
`DAILY_REWARDS.md`, `NOTIFICATIONS.md`, `STATISTICS.md`, `ACTIVITY_LOG.md`, and `ACCESSORIES.md` describe systems with no clear active implementation.

## Impact
Documentation overstates implemented functionality.

## Recommended Fix
Update these docs to distinguish implemented, legacy, and future systems.

## Priority
LOW

## Issue
`ACCESSORIES.md` is legacy-only and conflicts with current customization terminology.

## Impact
Developers may use obsolete accessory concepts instead of the current outfit/background/aura model.

## Recommended Fix
Mark `ACCESSORIES.md` as legacy and point to `CUSTOMIZATION.md`.

## Priority
LOW

---

# Cleanup

## Issue
`RewardOverlay.kt` is unused.

## Impact
Dead code increases maintenance burden.

## Recommended Fix
Remove the file after confirming no references.

## Priority
LOW

## Issue
`RewardUiEvent.AchievementReward` is mostly unused.

## Impact
Dead/ambiguous reward type can confuse future reward pipeline changes.

## Recommended Fix
Remove it or document it as intentionally unused; achievements should use individual reward events through the centralized pipeline.

## Priority
LOW

## Issue
`ChallengeRewardDefinition.CustomizationReward` is defined but no challenge configs currently use it.

## Impact
Dead challenge reward type adds unused complexity.

## Recommended Fix
Either add challenge configs that use customization rewards or remove the type and handler.

## Priority
LOW

## Issue
`JournalEntryEntity` is legacy and not used by current DAOs/repositories.

## Impact
Dead data model increases schema confusion.

## Recommended Fix
Remove legacy entity/table if no migration compatibility is required, or document it as legacy.

## Priority
LOW

## Issue
`PetEntity.coins` appears unused and conflicts with `StatisticsEntity` as the coin source of truth.

## Impact
Misleading field can cause future coin bugs.

## Recommended Fix
Remove the field or clearly mark it legacy/deprecated.

## Priority
LOW

---

# Validation Notes

- `./gradlew assembleDebug` completed successfully.
- All configured equipables had matching drawable assets when checking supported image extensions.
- Configured counts found during audit:
  - Achievements: 50
  - Equipables: 19
    - Outfits: 6
    - Auras: 5
    - Backgrounds: 8
- No source code was modified during the audit.
- Only this `TODO.md` audit section was added.
