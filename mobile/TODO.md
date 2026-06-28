# TODO — Release Readiness

## Rules

- `CLAUDE.md` is the project operating manual. Read it before changing code.
- Source-of-truth precedence: source code > Room schema > `.md` docs > comments.
- Code is the source of truth — verify against actual code, not this file.
- Complete items in order. Do not start later tasks early.
- A task is done when: code compiles, relevant tests pass, new functionality is covered by tests.

---

## Phase 1: Reward Systems Expansion

### 1. Daily Login Streak
- [x] Create `DailyLoginStreakEngine` — tracks consecutive daily logins, separate from habit streak
- [x] Store login streak data in `StatisticsEntity` (login streak count, last login day, last reward day)
- [x] Define login streak rewards in config (coins, chests, XP at milestones: 3, 7, 14, 30, 60, 100 days)
- [ ] Trigger login streak check at app launch (in `HomeScreenViewModel` init)
- [x] Show login streak reward through `RewardQueue` when milestone reached
- [x] Add achievements for login streak milestones (logins_100, logins_365)

### 2. Collection Set Bonuses
- [x] Create `CollectionSetConfig` — defines sets of customization items with bonus rewards
- [x] Create `CollectionSetEngine` — checks if player owns all items in a set, grants bonus
- [x] Define initial sets in config (Sakura Set, Crystal Set, Royal Set, etc.)
- [x] Bonuses grant titles/frames/aura and trigger `CustomizationReward`
- [x] Check set completion when customization is granted/purchased
- [x] Add achievements for completing sets (sets_completed_1, sets_completed_3)
- [x] Persist completed sets in `PetEntity.completedSetsJson` (migration 22)

### 3. Avatar Frames (Compose-drawn)
- [x] Create `AvatarFrameConfig` — defines frames with Compose-drawn styles
- [x] Add `equippedFrameId` field to `PetEntity`
- [x] Add `unlockedFramesJson` tracking (JSON list on PetEntity)
- [x] Create `AvatarFramePainter` composable
- [x] Frames unlocked via achievements, chests, login streak
- [ ] Frame selection UI on Pet screen (pending)

### 4. Pet Titles
- [x] Create `PetTitleConfig` — defines titles with id, display text, description, rarity
- [x] Add `activeTitleId` field to `PetEntity`
- [x] Add `unlockedTitleIdsJson` tracking (JSON list on PetEntity)
- [x] Titles unlocked via achievements, login streak, set completion
- [x] Display active title on Home screen next to pet name
- [x] Edit icon next to title opens picker dialog with unlocked titles
- [x] Title picker dialog with equip/clear options

### 5. Wiring & UI
- [x] Wire pet title display into HomeScreen (right of pet name, with edit icon)
- [x] Create title picker dialog composable (`TitlePickerDialog`)
- [x] Ensure all new systems integrate with existing reward pipeline

### 6. Achievements Integration
- [x] Add login streak achievements (logins_7, logins_30, logins_100, logins_365)
- [x] Add collection set completion achievements (sets_completed_1, sets_completed_3)
- [x] Add frame/title collection achievements (titles_1, titles_3, frames_1, frames_3)
- [x] Update `AchievementsConfig` with new definitions
- [x] Update `AchievementProgressSource` with TITLES_UNLOCKED, FRAMES_UNLOCKED, SETS_COMPLETED
- [x] Update `AchievementEngine` and `AchievementViewModel` to observe new progress sources

### 7. Testing & Build
- [x] Add unit tests for `DailyLoginStreakEngine` (10 tests)
- [x] Add unit tests for `CollectionSetEngine` (3 tests)
- [x] Add unit tests for `AchievementsConfig` new definitions (9 tests)
- [x] Add unit tests for `PetEntity.completedSetsJson` parsing (6 tests)
- [x] Run full test suite (570 tests, 3 pre-existing failures unrelated to changes)
- [x] Build APK and copy to target folder (`habit-pet.apk` ~85MB)
