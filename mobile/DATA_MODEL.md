# DATA_MODEL

## Overview

Habit Pet uses a Room database to persist game state, player progress, and various entities. The data model consists of interconnected entities that track habits, completions, player statistics, pet evolution, inventory, achievements, and journal entries.

## Current Implementation

The data model includes 9 Room entities:
1. HabitEntity - Tracks habit definitions and streaks
2. HabitCompletionEntity - Records individual habit completions
3. HabitProgressEntity - Tracks timer habit progress
4. PetEntity - Stores pet state (level, XP, equipped items)
5. StatisticsEntity - Tracks player statistics and progress
6. InventoryItemEntity - Manages collectible accessories
7. AchievementEntity - Tracks achievement progress and rewards
8. JournalEntryEntity - Records game events and milestones
9. (Implicit) Room database schema with relationships

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
- `id: Long` - Primary key (auto-generated)
- `name: String` - Pet name (default: "Luna")
- `level: Int` - Current level (0+)
- `xp: Long` - Total experience points
- `coins: Int` - Total coins owned
- `evolutionStage: Int` - Evolution stage (0-4)
- `equippedHat: String?` - Currently equipped hat item ID
- `equippedGlasses: String?` - Currently equipped glasses item ID
- `equippedScarf: String?` - Currently equipped scarf item ID
- `equippedBackground: String?` - Currently equipped background item ID
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
- `rewardChestsAvailable: Int` - Unused chest reward counter
- `lastStreakDate: Long` - Date (days since epoch) of last streak counting

**InventoryItemEntity** (table: `inventory_items`)
- `id: Long` - Primary key (auto-generated)
- `name: String` - Item display name
- `type: String` - Category: "HAT", "GLASSES", "SCARF", or "BACKGROUND"
- `imageUrl: String` - Reference to image asset (currently unused)
- `isUnlocked: Boolean` - Whether item is available for purchase
- `isPurchased: Boolean` - Whether player has bought the item
- `isEquipped: Boolean` - Whether item is currently equipped
- `price: Int` - Cost in coins to purchase the item
    - `rarity: String` - Rarity of the item (NORMAL, RARE, EPIC, LEGENDARY)

**Chest Reward Integration**
The chest reward system interacts with InventoryItemEntity through the InventoryItemRepository:
- `grantItem(itemId: Long)`: Marks an item as purchased (isPurchased = true) when awarded from a chest
- `getUnownedItemsByType(type: String)`: Returns items not yet purchased, used to avoid duplicate chest rewards
- Chest rewards only grant accessories that are not already owned, preventing duplicates
- When all accessories of a rarity are owned, the chest reward falls back to coin and EXP rewards only

**AchievementEntity** (table: `achievements`)
- `id: Long` - Primary key (auto-generated)
- `name: String` - Achievement title
- `description: String` - Detailed requirement description
- `icon: String` - Icon identifier (currently unused in UI)
- `targetValue: Int` - Value needed to unlock achievement
- `rewardCoins: Int` - Coins awarded upon unlocking
- `isUnlocked: Boolean` - Current unlock status
- `unlockedDate: Long?` - Timestamp when achievement was unlocked

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
4. **Achievement Tracking**: AchievementEntity records are updated when milestones are reached
5. **Journal Generation**: JournalEntryEntity records are created for significant events
6. **Inventory Management**: InventoryItemEntity records track owned/purchased/equipped items

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
- app/src/main/java/com/example/mobile/data/local/entities/JournalEntryEntity.kt
- app/src/main/java/com/example/mobile/data/local/entities/PetEntity.kt
- app/src/main/java/com/example/mobile/data/local/entities/StatisticsEntity.kt
- app/src/main/java/com/example/mobile/data/local/dao/ (DAO interfaces)
- app/src/main/java/com/example/mobile/data/local/database/AppDatabase.kt (database definition)

## Known Gaps

1. **Underutilized Fields**: Several entity fields are tracked but not actively used:
   - StatisticsEntity.bestStreak (only updated when currentStreak exceeds it)
   - StatisticsEntity.globalStreak (appears redundant)
   - StatisticsEntity.petAgeDays (never updated or displayed)
   - StatisticsEntity.rewardChestsAvailable (tracked but never used)
   - AchievementEntity.icon (stored but not referenced in UI)
   - InventoryItemEntity.imageUrl (stored but not used in UI)
   - PetEntity.mood (tracked but not visually represented)

2. **Data Redundancy**: Some information is stored in multiple places:
   - Streak information in both HabitEntity (per habit) and StatisticsEntity (overall)
   - Level and evolution stage derived from XP rather than stored directly

3. **Missing Relationships**: While foreign keys exist conceptually, Room relationships aren't explicitly defined with `@ForeignKey` annotations.

4. **Sparse Data**: Many string fields (icon, imageUrl, mood) have default values but lack actual asset integration.

5. **Limited Query Optimization**: Basic indices exist, but complex query performance may degrade with large datasets.