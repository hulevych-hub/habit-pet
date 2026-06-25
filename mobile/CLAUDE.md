# Habit Pet - AI Execution Manual

## Operating rules

- Modify only directly connected nodes. Work incrementally; do not refactor unrelated systems.
- Source-of-truth precedence: source code > Room schema > `.md` docs > comments.
- Search existing usage before creating new classes, APIs, packages, repositories, entities, or ViewModels.
- Never invent APIs or package names. Match existing conventions and verify package declarations match file locations.
- Read related docs before changing code. Update related docs after implementation so docs match actual behavior.
- `TODO.md` is the execution source of truth. If it contains unchecked work, complete items in order and do not start later tasks early.
- After modifying code, run `graphify update .` to keep the dependency graph current.
- Prefer extension over replacement. Keep changes minimal, localized, and backward-compatible.

## Test Coverage Rule

Whenever implementing a new feature, behavior change, UI flow, system, configuration, repository, engine, or reward logic:

- Add all necessary unit, integration, and UI tests for the new functionality.
- Ensure the new implementation is covered by automated tests before considering the task complete.
- Run the relevant test suites after implementation.

If existing tests fail:

- If failures are caused by intentional changes (new parameters, updated APIs, renamed fields, expected behavior updates, etc.), update the tests to reflect the new intended behavior.
- If the feature request explicitly changes existing behavior, update affected tests accordingly.
- Otherwise, do not modify failing tests. Report the failures, root cause, and impacted functionality in the final output.

A task is not complete until:

1. Code compiles successfully.
2. Relevant tests pass successfully.
3. New functionality is covered by automated tests.
4. Documentation and TODO.md are updated if required.

## Graphify workflow

- `graphify-out/graph.json` exists. For codebase questions, first run:
  - `graphify query "<question>"` for scoped architecture/context.
  - `graphify path "<A>" "<B>"` for relationships.
  - `graphify explain "<concept>"` for focused concepts.
- Use `graphify-out/wiki/index.md` for broad navigation if it exists.
- Do not use `GRAPH_REPORT.md` or raw scans as the first lookup unless graph queries are insufficient.

## Project shape

- Android app, Kotlin, Jetpack Compose, Hilt, Room, Navigation Compose.
- Gradle: `build.gradle`, `settings.gradle.kts`; app namespace/package is `com.example.mobile`.
- Main source: `app/src/main/java/com/example/mobile`.
- Package conventions:
  - `data.local.database` - Room database, migrations, initializers.
  - `data.local.entities` - Room entities.
  - `data.local.dao` - DAOs.
  - `data.repository` - Repository implementations.
  - `domain.repository` - Repository interfaces.
  - `domain` - Configs, engines, domain models.
  - `presentation.viewmodel` - Hilt ViewModels.
  - `presentation.ui.screens` - Compose screens and a few legacy screen-local ViewModels.
  - `presentation.ui.components` - Reusable Compose components.
  - `presentation.ui.reward` - Reward queue, manager, overlay, screen.
  - `presentation.ui.events` - Shared UI event sealed classes.
  - `presentation.ui.feedback` - Micro-feedback overlays.
  - `navigation` - Navigation graph/routes.
  - `di` - Hilt modules.
  - `util` - Android utilities/preferences.
  - `ui.theme` - Compose theme tokens.

## Critical files

- App shell: `MainActivity.kt`, `HabitPetApp.kt`, `navigation/NavGraph.kt`.
- Database: `data/local/database/AppDatabase.kt`.
- Hilt: `di/DatabaseModule.kt`, `di/RepositoryModule.kt`.
- XP/progression: `domain/ExpConfig.kt`, `data/local/entities/PetEntity.kt`, `data/local/entities/StatisticsEntity.kt`.
- Challenges: `domain/ChallengeConfig.kt`, `domain/ChallengeEngine.kt`, `data/repository/ChallengeRepositoryImpl.kt`, `data/local/entities/ChallengeEntity.kt`.
- Habit completion: `data/repository/HabitCompletionRepositoryImpl.kt`, `domain/repository/HabitCompletionRepository.kt`.
- Habit reward flows: `presentation/viewmodel/HabitDetailViewModel.kt`, `presentation/viewmodel/HabitsViewModel.kt`.
- Timeline: `domain/ActivityTimelineEngine.kt`, `domain/GameEventFactory.kt`, `domain/GameEventType.kt`, `data/local/entities/GameEventEntity.kt`.
- Rewards: `presentation/ui/events/RewardUiEvent.kt`, `presentation/ui/reward/RewardQueue.kt`, `presentation/ui/reward/RewardManager.kt`, `presentation/ui/reward/RewardOverlayHost.kt`.
- Achievements: `domain/AchievementsConfig.kt`, `domain/AchievementEngine.kt`, `domain/AchievementRewardProcessor.kt`, `data/local/entities/AchievementEntity.kt`.
- Streaks: `domain/StreakEngine.kt`, `domain/StreakCalculator.kt`.
- Customization: `domain/EquipableConfig.kt`, `domain/CustomizationTypes.kt`, `domain/UnlockSources.kt`, `util/AssetResolver.kt`, `presentation/ui/components/AssetPainter.kt`, `presentation/ui/components/AnimatedPet.kt`.
- Economy/chests: `domain/EconomyConfig.kt`, `domain/ChestType.kt`, `domain/ChestRewardConfig.kt`, `domain/ChestRewardConfigProvider.kt`, `domain/ChestRewardFactory.kt`.
- Notifications: `util/NotificationHelper.kt`, `util/NotificationPrefs.kt`, `util/NotificationPublisher.kt`, `util/BootCompletedReceiver.kt`, `util/ReinforcementMessageProvider.kt`.

## Architecture rules

- Use Clean Architecture: UI -> ViewModel -> repository/domain -> Room.
- UI screens should not call DAOs directly. Use repositories or injected domain engines.
- ViewModels own navigation events and screen state. Compose screens call ViewModel functions and collect state.
- Domain engines inject repositories and own cross-cutting progression logic.
- Use Room transactions for multi-table writes that must succeed/fail together.
- Prefer Flow for persisted state. Use `MutableSharedFlow` for one-shot UI events.
- Avoid unnecessary abstraction. Add a class only when existing usage clearly needs it.

## Room schema

- `AppDatabase` version is `19`. Schema changes require migrations and version bump.
- Core entities:
  - `HabitEntity` - user habits.
  - `HabitCompletionEntity` - idempotent daily habit completions with `xpEarned`.
  - `HabitProgressEntity` - timer habit accumulated minutes per day.
  - `PetEntity` - pet XP, level, evolution stage, mood, equipped customization.
  - `StatisticsEntity` - coins, streaks, completions, combo state, global streak freeze metadata.
  - `InventoryItemEntity` - customization catalog, ownership, equipped state.
  - `AchievementEntity` - achievement progress/unlock/claim state.
  - `GameEventEntity` - persistent activity timeline events.
  - `ChallengeEntity` - single active rotating challenge.
  - `JournalEntryEntity` - legacy entity only; not active in current logic.
- Key DAOs:
  - `HabitDao`, `HabitCompletionDao`, `HabitProgressDao`, `PetDao`, `StatisticsDao`, `InventoryItemDao`, `AchievementDao`, `GameEventDao`, `ChallengeDao`.

## Single sources of truth

- XP, level, evolution, habit reward XP, combo: `ExpConfig`.
- Challenge targets, randomization weights, availability, and rewards: `ChallengeConfig`.
- Coin rewards, prices, chest probabilities, surprise chest behavior: `EconomyConfig`.
- Chest reward contents/probabilities: `ChestRewardConfigProvider` + `ChestRewardFactory`.
- Achievement metadata/rewards: `AchievementsConfig`.
- Customization catalog: `EquipableConfig`.
- Customization category constants: `CustomizationTypes`.
- Unlock source constants: `UnlockSources`.
- Pet equipped state: `PetEntity.equippedOutfit`, `equippedBackground`, `equippedAura`.
- Customization ownership/equipped cache: `InventoryItemEntity`.
- Streak, coins, completions, combo: `StatisticsEntity`.
- Persistent gameplay moments: `GameEventEntity` through `ActivityTimelineEngine`.

## Progression values

- Checkbox habit: `10 XP`, `10 coins`.
- Timer habit: `5 + 1 * minutes` XP; `5 + 2 * minutes` coins.
- Combo: consecutive completions within 2 hours; `+1 XP` per completion after first, capped at `+4 XP`.
- Combo milestones: `3`, `5`, `10` hits.
- Level formula: `totalXpForLevel(level) = 15 * level * (level + 1)`. Use `ExpConfig.calculateLevelFromXp()`.
- Evolution thresholds: `0 Egg`, `75 Hatchling`, `300 Young Dragon`, `900 Adult Dragon`, `2500 Ancient Dragon`. Use `ExpConfig.calculateEvolutionStageFromXp()`.
- Level-up base coins: `level * 10` via `ExpConfig.levelUpCoins(level)`.
- Level-up chest: random chest from `ChestRewardConfigProvider.getRandomChestType()`.

## Habit completion flow

Preferred flow:

1. UI calls `HabitDetailViewModel.completeCheckboxHabit()` / `stopTimerHabit()` or `HabitsViewModel.completeCheckboxHabit()`.
2. ViewModel writes through `HabitCompletionRepository.addCompletionWithCombo()`.
3. If `completionResult.isNewCompletion == false`, stop; do not award XP, coins, streaks, rewards, or timeline events.
4. ViewModel updates pet XP/level/evolution through `PetRepository`.
5. ViewModel awards direct coins through `StatisticsRepository`.
6. `StreakEngine.evaluateTodayStreak()` updates streak state.
7. `ChallengeRepository` advances the active challenge; `ActivityTimelineEngine` logs habit/combo/challenge/progression events.
8. `RewardQueue` receives major reward UI events.
9. `MicroFeedbackManager` handles small non-blocking XP/coin feedback.
10. `DragonMoodEngine.refreshMood()` updates pet mood.

Important: list completion is optimistic in `HabitsViewModel`; remove optimistic IDs on failure or duplicate completion.

## Reward pipeline

- All reward-related experiences must go through the centralized reward system.
- Use `RewardQueue` for queued reward UI events. Do not create isolated reward overlays.
- Use `RewardManager` as the central processor for queued reward events.
- Use `RewardEventBus` only for UI-only reward overlay signaling.
- Use `ActivityTimelineEngine` for persistent reward/progression history.
- Use `MicroFeedbackManager` for non-blocking XP/coin animations.

`RewardUiEvent` types:

- `CoinReward`
- `LevelUpReward`
- `DragonEvolutionReward`
- `StreakReward`
- `AchievementReward`
- `ChestReward`
- `ExpReward`
- `CustomizationReward`

Reward queue ordering:

1. `LevelUpReward`
2. `DragonEvolutionReward`
3. `StreakReward`
4. `ChestReward`
5. `AchievementReward`
6. `ExpReward`
7. `CustomizationReward`
8. `CoinReward`

Reward processing rules:

- `RewardManager.rewardCompleted()` processes the currently displayed reward.
- `CoinReward`, `StreakReward`, and `ChestReward` coin amounts add coins through `StatisticsRepository`.
- `ExpReward` and `ChestReward.expAmount` add pet XP through `RewardManager.addPetExp()`, then check for level/evolution changes.
- `ChestReward` grants customization through `InventoryItemRepository.grantItemByItemId()` or `grantItem()`.
- `CustomizationReward` grants a configured customization item through `InventoryItemRepository.grantItemByItemId()`.
- `LevelUpReward` adds coins through `StatisticsRepository` in `RewardManager.rewardCompleted()`. ViewModels do NOT award level-up base coins directly — they only queue the reward. This is the single source of truth for level-up coins.
- `AchievementReward` is already processed by `AchievementRewardProcessor`; `RewardManager` ignores its coin/exp fields to avoid double-processing.

## Achievement system

- `AchievementDatabaseInitializer` syncs `AchievementsConfig` definitions to Room on startup.
- `AchievementEngine` observes habit count, streak, completions, XP, level, and owned customization count.
- Achievements unlock when progress reaches configured target. Once unlocked, they remain unlocked even if live progress later drops.
- `AchievementScreen` displays progress and claim buttons.
- `AchievementViewModel.claimAchievement()` calls `AchievementEngine.claimAchievement()`.
- `AchievementRewardProcessor` processes rewards in a Room transaction and marks achievement claimed only after reward processing succeeds.
- Reward types:
  - `AchievementReward.CoinReward`
  - `AchievementReward.ExpReward`
  - `AchievementReward.ChestReward`
  - `AchievementReward.CustomizationReward`
- Achievement customization rewards must reference stable `EquipableConfig` IDs with `unlockSource = "ACHIEVEMENT"`.

## Streak system

- Current source of truth: `StatisticsEntity.currentStreak`.
- `StatisticsEntity.globalStreak` mirrors current streak for UI.
- Streak means all habits are completed for the day.
- `StreakEngine.evaluateTodayStreak()` increments only once per day.
- Global streak milestones: `7`, `14`, `30`, `60`, `100`.
- Milestone chest mapping: `7 = Normal`, `14 = Rare`, `30/60 = Epic`, `100 = Legendary`.
- Streak rewards are immersive and must use `RewardQueue` + `RewardManager`.
- `StreakCalculator` derives current streak from habit completion history; use it when recalculating.
- Global streak freeze is global-only: it does not create habit completions or affect per-habit streaks.
- Freeze prompt: when the app opens and yesterday would break a global streak, `StreakEngine.checkPendingStreakFreeze()` asks the user to use one freeze.
- Freeze rules: allowed only for a one-day break, once every 7 days, and never for two frozen streak days in a row.
- Frozen streak calendar days render as cold-blue streak icons.

## Activity timeline integration

- Use `ActivityTimelineEngine` for persistent gameplay moments. Do not write directly to `GameEventDao` from UI.
- Timeline event types in `GameEventType`:
  - `PET_ARRIVED`
  - `HABIT_COMPLETED`
  - `ACHIEVEMENT_UNLOCKED`
  - `LEVEL_UP`
  - `DRAGON_EVOLUTION`
  - `CHEST_OPENED`
  - `STREAK_MILESTONE`
  - `FIRST_DAILY_LOGIN`
  - `SURPRISE_REWARD`
  - `CHALLENGE_COMPLETED`
  - `COMBO_MILESTONE`
  - `EVOLUTION_MILESTONE_NEARING`
- `GameEventFactory` formats titles/descriptions/reward summaries.
- `GameEventEntity` stores `id`, `type`, `title`, `description`, `timestamp`, `rewardSummary`, `rarity`, `metadataJson`.
- `FIRST_DAILY_LOGIN` is once-per-day and uses local preferences for last login day.
- `ActivityTimelineScreen` appends contextual reinforcement text through `ReinforcementMessageProvider`.

## Customization system

- Legacy accessory slots (`HAT`, `GLASSES`, `SCARF`) are obsolete. Use `CUSTOMIZATION.md`, `EquipableConfig`, and `CustomizationTypes`.
- Current categories: `OUTFIT`, `BACKGROUND`, `AURA`.
- `PetEntity` stores equipped customization IDs.
- `InventoryItemEntity` stores catalog rows, ownership, purchase state, equipped cache, rarity, unlock source, price, phase, and sort order.
- `EquipableConfig` is the catalog source of truth. Do not hardcode cosmetic item lists.
- `unlockSource` values: `SHOP`, `CHEST`, `ACHIEVEMENT`.
- Shop items use numeric `price`; chest/achievement items use nullable/no coin price.
- Phase metadata is nullable. Null phase means usable across phases.
- `InventoryItemDatabaseInitializer` seeds default equipped items and purchasable catalog items.
- Customization rewards must use stable `equipableId`, not display name.

## Customization rendering order

Render in this exact order:

1. Background
2. Dragon base image or aura image
3. Outfit overlay

Rules:

- If an aura is equipped, render the aura image instead of the dragon base image.
- If no aura is equipped, render the phase/mood dragon base image.
- Outfit overlay is always rendered above the dragon/aura.
- Background is always rendered behind the dragon/aura/outfit.
- `AssetResolver` resolves drawable asset paths from packaged drawable assets.
- Missing assets should fall back to default assets, but missing catalog assets should be treated as a configuration error.

## Asset structure

Gradle packages drawable assets as assets via `assets.srcDirs += "src/main/res/drawable"`.

Main drawable folders:

- `egg/`
- `hatchling/`
- `young/`
- `adult/`
- `ancient/`
- `backgrounds/`
- `outfits/`
- `auras/`
- `icons/`

Important components:

- `AssetResolver` resolves phase, mood, background, outfit, aura, and icon asset names.
- `AssetPainter` draws layered assets with content scale and alpha.
- `AssetPreview` previews customization assets.
- `AnimatedPet` composes background, dragon/aura, outfit, phase transition, and idle animation.
- `PetPhaseTransition` handles evolution stage transitions.

## UI principles

- Habit Pet should feel warm, cute, playful, premium, motivating, and reward-focused.
- Avoid spreadsheet-like, dense, enterprise UI.
- Prefer rounded cards, clear hierarchy, emotional copy, visible progress, and generous spacing.
- Reuse existing components before adding new UI:
  - `ProgressHeader`
  - `EvolutionTeaser`
  - `ChallengeCard`
  - `CurrencyIcon`
  - `AssetPainter`
  - `AssetPreview`
  - `AnimatedPet`
  - `PetPhaseTransition`
  - `RewardOverlayHost`
- Major progression moments must be immersive and sequential: level-ups, evolutions, chest openings, streak milestones, achievements.
- Major progression overlays should block navigation until continued. Do not overlap major progression moments.

## Navigation

- `NavGraph` owns the app shell and bottom navigation.
- Bottom nav destinations: Home, Habits, Pet, Achievements, Settings.
- Rewards is not a bottom nav destination. It opens from the shared header coin amount on the locked tab and from the Pet screen Attribute Card edit icon on the owned tab.
- Detail/create/edit routes hide bottom nav:
  - `habit_detail/{habitId}`
  - `habit_creation`
  - `habit_edit/{habitId}`
  - `notifications`
- Do not add a new bottom nav item without adding route, screen, ViewModel if needed, and destination handling.
- Keep route constants in `NavGraph`.

## Notifications

- Notification system uses `AlarmManager`, `NotificationManager`, and `SharedPreferences`.
- Components:
  - `NotificationHelper` schedules/cancels/shows reminders.
  - `NotificationPrefs` stores reminder preferences and context.
  - `NotificationPublisher` receives alarms and respects frequency rules.
  - `BootCompletedReceiver` reschedules after reboot.
  - `ReinforcementMessageProvider` supplies contextual supportive copy.
- Reminder types: daily/dragon waiting, streak encouragement, pet bond reminder.
- Tone must be supportive, never harsh or demanding.
- Default reminder interval suppression: 3 hours.
- Settings live in `NotificationSettingsScreen` and `NotificationSettingsViewModel`.

## Build and verification

- Build command: `./gradlew assembleDebug`.
- After code changes:
  - Run relevant Gradle build/check.
  - Run `graphify update .`.
  - Update related docs.
  - Verify only intended files changed.
  - At the end of the code changes and after all builds and runs succeed, generate a debug APK named exactly `habit-pet.apk` into the folder C:\Users\serhi\Documents\Development\desktop-android-sharing

## Known documentation gaps / audit points

- `ACCESSORIES.md` is legacy only. Use customization docs and `EquipableConfig`.
- `QUESTS.md` and `ENDGAME.md` document absent systems. Do not treat quests or a defined endgame as implemented.
- `DAILY_REWARDS.md` now documents the rotating challenge system and the migration from obsolete daily goals.
- `JournalEntryEntity` is legacy and not used by current DAOs or repositories.
- ~~Level-up coin double-award risk exists in current implementation~~ (FIXED: `LevelUpReward` coins are now awarded solely in `RewardManager.rewardCompleted()`; ViewModels only queue the reward and do not award coins directly).
- Some statistics are tracked but not prominently used/displayed (`bestStreak`, `rewardChestsAvailable`, `petAgeDays`, `lastStreakAwardedAt`).
- `StatisticsEntity.currentStreak` is the persisted streak source; `globalStreak` mirrors it.
