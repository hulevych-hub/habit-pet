# DATA_MODEL

## Overview

Habit Pet uses a Room database to persist game state, player progress, and various entities. The data model consists of interconnected entities that track habits, completions, player statistics, pet evolution, inventory, achievements, activity timeline events, and legacy journal compatibility.

## Current Implementation

The data model includes 10 Room entities:
1. HabitEntity - Tracks habit definitions and streaks
2. HabitCompletionEntity - Records individual habit completions
3. HabitProgressEntity - Tracks timer habit progress
4. PetEntity - Stores pet state (level, XP, equipped items)
5. StatisticsEntity - Tracks player statistics and progress
6. InventoryItemEntity - Manages collectible customization items synced from `EquipableConfig`
7. AchievementEntity - Tracks achievement progress and claim state
8. GameEventEntity - Records persistent activity timeline events
9. ChallengeEntity - Stores the single active rotating challenge
10. JournalEntryEntity - Legacy journal entry text retained for compatibility

## Rules

### Entity Relationships

**HabitEntity** (table: `habits`)
- `id: Long` - Primary key
- `name: String` - Habit name
- `icon: String` - Icon identifier
- `type: String` - "CHECKBOX" or "TIMER"
- `minimumDurationMinutes: Int` - Required time for timer habits
- `currentStreak: Int` - Current completion streak
- `bestStreak: Int` - Best streak achieved

**HabitCompletionEntity** (table: `habit_completions`)
- `id: Long` - Primary key (auto-generated)
- `habitId: Long` - Foreign key to HabitEntity
- `date: Long` - Completion timestamp (stored as milliseconds)
- `xpEarned: Long` - XP awarded for this completion
- Index: Unique constraint on (habitId, date) to prevent duplicate daily completions

**HabitProgressEntity** (table: `habit_progress`)
- `habitId: Long` - Primary key, foreign key to HabitEntity
- `date: Long` - Progress date
- `accumulatedMinutes: Int` - Time accumulated today for timer habits
- `lastUpdated: Long` - Last update timestamp

**PetEntity** (table: `pet`)
- `id: Long` - Primary key (auto-generated; missing pet rows are exposed as `id = 0` until first persisted)
- `name: String` - Pet name (default: "Luna", persisted and editable in the Pet screen)
- `level: Int` - Current level (0+)
- `xp: Long` - Total experience points
- `coins: Int` - Total coins owned
- `evolutionStage: Int` - Evolution stage (0-4)
- `equippedOutfit: String?` - Currently equipped outfit item ID
- `equippedBackground: String?` - Currently equipped background item ID
- `equippedAura: String?` - Currently equipped aura item ID
- `mood: String` - Pet's current mood
- `creationDate: Long` - Timestamp when pet was created

**StatisticsEntity** (table: `statistics`)
- `id: Long` - Primary key (constant value: 1)
- `currentStreak: Int` - Current overall habit streak
- `bestStreak: Int` - Best overall streak achieved
- `globalStreak: Int` - Duplicate/unused streak value
- `totalCompletions: Int` - Total habit completions across all habits
- `totalXp: Long` - Total accumulated experience points
- `daysActive: Int` - Number of days with at least one habit completion
- `totalHabitsCompleted: Int` - Total number of unique habits created
- `petAgeDays: Int` - Days since pet creation (currently unused)
- `totalCoins: Int` - Total accumulated in-game currency
- `lastStreakAwardedAt: Int` - Last streak value at which chest reward was given
- `lastUpdated: Long` - Timestamp of last statistics update
- `rewardChestsAvailable: Int` - Chest counter used by challenge availability checks
- `lastStreakDate: Long` - Date (days since epoch) of last streak counting
- `currentCombo: Int` - Current short-term combo hit count
- `bestCombo: Int` - Highest combo hit count achieved
- `lastHabitCompletionTimestamp: Long` - Timestamp used to calculate combo windows

**InventoryItemEntity** (table: `inventory_items`)
- `id: Long` - Primary key (auto-generated)
- `itemId: String` - Stable `EquipableConfig` identifier used by rendering, rewards, and migrations
- `name: String` - Item display name
- `type: String` - Category: `OUTFIT`, `BACKGROUND`, or `AURA`
- `imageUrl: String` - Expected asset path
- `isUnlocked: Boolean` - Catalog visibility/purchasability; configured from `EquipableConfig` during inventory synchronization
- `isPurchased: Boolean` - Whether player owns the item
- `isEquipped: Boolean` - Cached equipped state; Rewards screen also derives equipped state from `PetEntity`
- `price: Int` - Cost in coins to purchase the item; non-purchasable config items store `0` here
- `rarity: Rarity` - Rarity of the item (`NORMAL`, `RARE`, `EPIC`, `LEGENDARY`)
- `unlockSource: String` - `SHOP`, `CHEST`, or `ACHIEVEMENT`; `ACHIEVEMENT` items are excluded from chest customization rolls

**Chest Reward Integration**
The chest reward system interacts with InventoryItemEntity through the InventoryItemRepository:
- `grantItem(itemId: Long)`: Marks an item as purchased (`isPurchased = true`) when awarded from a chest
- `grantItemByItemId(itemId: String)`: Resolves a stable `EquipableConfig` item ID and marks it as purchased
- `getUnownedItemsByRarity(rarity: Rarity)`: Returns unpurchased items, preferring locked items first, to avoid duplicate chest rewards
- Chest rewards filter out `unlockSource = "ACHIEVEMENT"` items before selecting a random reward
- Chest rewards only grant customization items that are not already owned, preventing duplicates
- When all customization items of a rarity are owned, the chest reward falls back to coin and EXP rewards only

**AchievementEntity** (table: `achievements`)
- `id: String` - Stable achievement identifier from `AchievementsConfig`
- `progress: Int` - Latest persisted progress for the configured source
- `isUnlocked: Boolean` - Current unlock status
- `isClaimed: Boolean` - Whether the player has claimed the reward
- `unlockedDate: Long?` - Timestamp when achievement was unlocked

Achievement metadata and reward definitions are not stored in this table. They are loaded from `AchievementsConfig`.

**GameEventEntity** (table: `game_events`)
- `id: Long` - Primary key, auto-generated
- `type: String` - Extensible `GameEventType` name
- `timestamp: Long` - Event creation time in milliseconds
- `title: String` - Short event title for timeline cards
- `description: String` - Human-readable event description
- `icon: String` - Icon identifier used by UI mapping
- `rarity: String` - `COMMON`, `RARE`, `EPIC`, or `LEGENDARY`
- `payload: String?` - Optional JSON-style payload for future structured data

Game events are append-only. The timeline DAO exposes reverse-chronological queries with `LIMIT` and `OFFSET` for lazy loading.

**ChallengeEntity** (table: `challenges`)
- `id: Long` - Primary key, fixed to `1` for the single active challenge row
- `challengeId: String` - Stable definition ID from `ChallengeConfig`
- `title: String` - Challenge title
- `description: String` - Challenge description
- `icon: String` - Challenge icon key
- `type: String` - Challenge type such as `HABIT_COMPLETION`, `XP_EARNED`, or `STREAK`
- `targetValue: Int` - Target required to complete the challenge
- `progressValue: Int` - Current progress toward `targetValue`
- `rewardIdsJson: String` - Serialized reward definitions from `ChallengeConfig`
- `isCompleted: Boolean` - Whether the challenge target has been reached
- `isClaimed: Boolean` - Whether the player has claimed the completed challenge
- `previousChallengeId: String?` - Previous challenge ID used for randomization variety
- `createdAt: Long` - Challenge creation timestamp
- `completedAt: Long?` - Timestamp when the challenge was completed
- `claimedAt: Long?` - Timestamp when the challenge was claimed

The challenge row is replaced after the player claims a completed challenge so the next random challenge is immediately visible.

**JournalEntryEntity** (table: `journal_entries`)
- `id: Long` - Primary key
- `dayNumber: Int` - Days since pet creation
- `entryText: String` - Journal entry content
- `timestamp: Long` - When the entry was created (milliseconds)

### Data Flow

1. **Habit Creation**: HabitEntity records are created when users add new habits
2. **Habit Completion**: HabitCompletionEntity records are created when habits are completed
3. **Progress Updates**:
   - StatisticsEntity is updated with new totals (XP, completions, etc.)
   - PetEntity is updated with new XP, level, and evolution stage
   - HabitProgressEntity tracks timer habit accumulation
4. **Pet Rename**: PetEntity.name is updated through PetRepository.updatePet from the Pet screen and displayed on the Home and Pet screens
5. **Achievement Tracking**: AchievementEntity records are updated when milestones are reached
6. **Activity Timeline**: GameEventEntity records are appended for habit completions, challenge completions, achievements, level-ups, evolutions, chests, and streak milestones
7. **Challenge Rotation**: ChallengeEntity stores one active challenge and is replaced with a new random challenge after claim
8. **Legacy Journal Compatibility**: JournalEntryEntity remains in the schema for compatibility, but no active journal writer creates new rows
9. **Inventory Management**: InventoryItemEntity records track owned/purchased/equipped items and are synchronized from `EquipableConfig` without overwriting player-owned, purchased, or equipped state; catalog metadata is refreshed

## Configuration

All entities are configured with Room annotations:
- `@Entity(tableName = "...")` defines the table name
- `@PrimaryKey` identifies primary key fields
- `@ColumnInfo(name = "...")` specifies column names (when different from field name)
- `@Index` defines database indices for performance
- Data classes with auto-generated values use `autoGenerate = true`

## Source Files

- app/src/main/java/com/example/mobile/data/local/entities/AchievementEntity.kt
- app/src/main/java/com/example/mobile/data/local/entities/HabitCompletionEntity.kt
- app/src/main/java/com/example/mobile/data/local/entities/HabitEntity.kt
- app/src/main/java/com/example/mobile/data/local/entities/HabitProgressEntity.kt
- app/src/main/java/com/example/mobile/data/local/entities/InventoryItemEntity.kt
- app/src/main/java/com/example/mobile/data/local/database/InventoryItemDatabaseInitializer.kt
- app/src/main/java/com/example/mobile/domain/EquipableConfig.kt
- app/src/main/java/com/example/mobile/data/local/entities/GameEventEntity.kt
- app/src/main/java/com/example/mobile/data/local/entities/ChallengeEntity.kt
- app/src/main/java/com/example/mobile/data/local/entities/JournalEntryEntity.kt
- app/src/main/java/com/example/mobile/data/local/entities/PetEntity.kt
- app/src/main/java/com/example/mobile/data/local/entities/StatisticsEntity.kt
- app/src/main/java/com/example/mobile/data/local/dao/ChallengeDao.kt
- app/src/main/java/com/example/mobile/data/local/dao/ (DAO interfaces)
- app/src/main/java/com/example/mobile/data/local/database/AppDatabase.kt (database definition)

## Known Gaps

1. **Underutilized Fields**: Several entity fields are tracked but not actively used:
   - StatisticsEntity.bestStreak (only updated when currentStreak exceeds it)
   - StatisticsEntity.globalStreak (appears redundant)
   - StatisticsEntity.petAgeDays (never updated or displayed)
   - StatisticsEntity.rewardChestsAvailable (tracked but only used by challenge availability checks)
   - InventoryItemEntity.isEquipped is a cache; equipped state is authoritative in `PetEntity`
   - PetEntity.mood (tracked but not visually represented)

2. **Data Redundancy**: Some information is stored in multiple places:
   - Streak information in both HabitEntity (per habit) and StatisticsEntity (overall)
   - Level and evolution stage derived from XP rather than stored directly

3. **Missing Relationships**: While foreign keys exist conceptually, Room relationships aren't explicitly defined with `@ForeignKey` annotations.

4. **Sparse Data**: Many string fields (icon, imageUrl, mood) have default values but lack actual asset integration.

5. **Limited Query Optimization**: Basic indices exist, but complex query performance may degrade with large datasets.