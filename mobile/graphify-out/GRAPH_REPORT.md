# Graph Report - mobile  (2026-06-08)

## Corpus Check
- 101 files · ~693,921 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 873 nodes · 1238 edges · 83 communities (66 shown, 17 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 27 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `201eae67`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 45|Community 45]]
- [[_COMMUNITY_Community 46|Community 46]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 48|Community 48]]
- [[_COMMUNITY_Community 49|Community 49]]
- [[_COMMUNITY_Community 50|Community 50]]
- [[_COMMUNITY_Community 51|Community 51]]
- [[_COMMUNITY_Community 52|Community 52]]
- [[_COMMUNITY_Community 53|Community 53]]
- [[_COMMUNITY_Community 54|Community 54]]
- [[_COMMUNITY_Community 55|Community 55]]
- [[_COMMUNITY_Community 56|Community 56]]
- [[_COMMUNITY_Community 57|Community 57]]
- [[_COMMUNITY_Community 58|Community 58]]
- [[_COMMUNITY_Community 59|Community 59]]
- [[_COMMUNITY_Community 60|Community 60]]
- [[_COMMUNITY_Community 61|Community 61]]
- [[_COMMUNITY_Community 62|Community 62]]
- [[_COMMUNITY_Community 63|Community 63]]
- [[_COMMUNITY_Community 64|Community 64]]
- [[_COMMUNITY_Community 65|Community 65]]
- [[_COMMUNITY_Community 66|Community 66]]
- [[_COMMUNITY_Community 67|Community 67]]
- [[_COMMUNITY_Community 68|Community 68]]
- [[_COMMUNITY_Community 69|Community 69]]

## God Nodes (most connected - your core abstractions)
1. `HabitDetailViewModel` - 27 edges
2. `HabitEditViewModel` - 20 edges
3. `DatabaseModule` - 18 edges
4. `HabitCompletionRepositoryImpl` - 16 edges
5. `HabitPetNavGraph()` - 16 edges
6. `HabitCompletionViewModel` - 16 edges
7. `HabitCreationViewModel` - 16 edges
8. `HomeScreenViewModel` - 13 edges
9. `Long` - 12 edges
10. `AppDatabase` - 11 edges

## Surprising Connections (you probably didn't know these)
- `HabitPetNavGraph()` --calls--> `HabitDetailScreen()`  [INFERRED]
  app/src/main/java/com/example/mobile/navigation/NavGraph.kt → app/src/main/java/com/example/mobile/presentation/ui/screens/HabitDetailScreen.kt
- `HabitPetNavGraph()` --calls--> `HabitEditScreen()`  [INFERRED]
  app/src/main/java/com/example/mobile/navigation/NavGraph.kt → app/src/main/java/com/example/mobile/presentation/ui/screens/HabitEditScreen.kt
- `HabitPetNavGraph()` --calls--> `HabitsScreen()`  [INFERRED]
  app/src/main/java/com/example/mobile/navigation/NavGraph.kt → app/src/main/java/com/example/mobile/presentation/ui/screens/HabitsScreen.kt
- `HabitPetNavGraph()` --calls--> `HomeScreen()`  [INFERRED]
  app/src/main/java/com/example/mobile/navigation/NavGraph.kt → app/src/main/java/com/example/mobile/presentation/ui/screens/HomeScreen.kt
- `HabitPetNavGraph()` --calls--> `NotificationSettingsScreen()`  [INFERRED]
  app/src/main/java/com/example/mobile/navigation/NavGraph.kt → app/src/main/java/com/example/mobile/presentation/ui/screens/NotificationSettingsScreen.kt

## Import Cycles
- None detected.

## Communities (83 total, 17 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.05
Nodes (40): AchievementViewModel, RewardManager, NavHostController, String, com, Boolean, Int, Modifier (+32 more)

### Community 1 - "Community 1"
Cohesion: 0.06
Nodes (25): Boolean, RewardUiEvent, StateFlow, HabitEntity, NavHostController, AchievementEntity, Boolean, List (+17 more)

### Community 2 - "Community 2"
Cohesion: 0.11
Nodes (16): AchievementDatabaseInitializer, AchievementEngine, StatisticsDatabaseInitializer, Context, Intent, Context, Int, Intent (+8 more)

### Community 3 - "Community 3"
Cohesion: 0.12
Nodes (12): com, Context, HabitCompletionRepository, HabitRepository, RewardManager, StatisticsDatabaseInitializer, StatisticsRepository, AppDatabase (+4 more)

### Community 4 - "Community 4"
Cohesion: 0.17
Nodes (13): Boolean, HabitCompletionEntity, HabitEntity, Int, List, Long, PetEntity, RewardUiEvent (+5 more)

### Community 5 - "Community 5"
Cohesion: 0.11
Nodes (22): Modifier, PetEntity, Boolean, HabitEntity, Int, Long, String, Int (+14 more)

### Community 6 - "Community 6"
Cohesion: 0.08
Nodes (17): AchievementRepositoryImpl, AchievementRepository, HabitCompletionRepository, HabitProgressRepository, HabitRepository, InventoryItemRepository, JournalEntryRepository, PetRepository (+9 more)

### Community 7 - "Community 7"
Cohesion: 0.17
Nodes (9): Boolean, Flow, HabitCompletionEntity, Int, List, Long, StatisticsEntity, HabitCompletionRepository (+1 more)

### Community 8 - "Community 8"
Cohesion: 0.17
Nodes (22): Boolean, HabitCompletionEntity, HabitEntity, Int, List, Long, PaddingValues, String (+14 more)

### Community 9 - "Community 9"
Cohesion: 0.13
Nodes (9): Boolean, HabitEntity, Int, Long, SharedFlow, StateFlow, String, Unit (+1 more)

### Community 10 - "Community 10"
Cohesion: 0.15
Nodes (9): Boolean, HabitEntity, Int, Long, SharedFlow, StateFlow, String, Unit (+1 more)

### Community 11 - "Community 11"
Cohesion: 0.23
Nodes (6): Flow, HabitCompletionEntity, Int, List, Long, HabitCompletionDao

### Community 12 - "Community 12"
Cohesion: 0.22
Nodes (8): Flow, Int, InventoryItemEntity, List, Long, String, InventoryItemRepository, InventoryItemRepositoryImpl

### Community 13 - "Community 13"
Cohesion: 0.22
Nodes (7): Boolean, Flow, HabitCompletionEntity, Int, List, Long, HabitCompletionRepository

### Community 14 - "Community 14"
Cohesion: 0.17
Nodes (7): Boolean, Int, SharedFlow, StateFlow, String, Unit, HabitCreationViewModel

### Community 15 - "Community 15"
Cohesion: 0.25
Nodes (6): Boolean, Flow, Int, StatisticsEntity, StatisticsRepositoryImpl, StatisticsRepository

### Community 16 - "Community 16"
Cohesion: 0.22
Nodes (7): Flow, Int, InventoryItemEntity, List, Long, String, InventoryItemRepository

### Community 17 - "Community 17"
Cohesion: 0.24
Nodes (13): RewardManager, Any, Int, RewardUiEvent, String, RewardOverlayHost(), AchievementRewardContent(), ChestRewardContent() (+5 more)

### Community 18 - "Community 18"
Cohesion: 0.22
Nodes (7): Flow, Int, InventoryItemEntity, List, Long, String, InventoryItemDao

### Community 19 - "Community 19"
Cohesion: 0.22
Nodes (7): Flow, HabitEntity, Int, List, Long, HabitRepository, HabitRepositoryImpl

### Community 20 - "Community 20"
Cohesion: 0.18
Nodes (5): Boolean, Flow, Int, StatisticsEntity, StatisticsRepository

### Community 21 - "Community 21"
Cohesion: 0.27
Nodes (13): Any, Int, RewardManager, RewardUiEvent, String, AchievementRewardContent(), ChestRewardContent(), CoinRewardContent() (+5 more)

### Community 22 - "Community 22"
Cohesion: 0.25
Nodes (13): Boolean, Int, Long, PaddingValues, String, HabitEditViewModel, DurationSelection(), HabitEditForm() (+5 more)

### Community 23 - "Community 23"
Cohesion: 0.15
Nodes (11): Boolean, HabitEntity, List, Long, PetEntity, StateFlow, StatisticsEntity, Map (+3 more)

### Community 24 - "Community 24"
Cohesion: 0.14
Nodes (13): Acknowledgments, Architecture Overview, Development Phases, Features, Getting Started, Habit Pet, Installation, Key Components (+5 more)

### Community 25 - "Community 25"
Cohesion: 0.22
Nodes (7): AchievementRepository, AchievementEntity, Flow, Int, List, Long, AchievementRepositoryImpl

### Community 26 - "Community 26"
Cohesion: 0.24
Nodes (6): AchievementEntity, Flow, Int, List, Long, AchievementDao

### Community 27 - "Community 27"
Cohesion: 0.24
Nodes (6): Flow, HabitEntity, Int, List, Long, HabitDao

### Community 28 - "Community 28"
Cohesion: 0.23
Nodes (4): com, AppDatabase, HabitProgressDao, RoomDatabase

### Community 29 - "Community 29"
Cohesion: 0.22
Nodes (7): Flow, Int, JournalEntryEntity, List, Long, JournalEntryRepository, JournalEntryRepositoryImpl

### Community 30 - "Community 30"
Cohesion: 0.24
Nodes (6): Flow, HabitEntity, Int, List, Long, HabitRepository

### Community 31 - "Community 31"
Cohesion: 0.24
Nodes (6): Flow, Int, JournalEntryEntity, List, Long, JournalEntryDao

### Community 32 - "Community 32"
Cohesion: 0.32
Nodes (6): Flow, Int, PetEntity, String, PetRepository, PetRepositoryImpl

### Community 33 - "Community 33"
Cohesion: 0.24
Nodes (6): AchievementEntity, Flow, Int, List, Long, AchievementRepository

### Community 34 - "Community 34"
Cohesion: 0.24
Nodes (6): Flow, Int, JournalEntryEntity, List, Long, JournalEntryRepository

### Community 35 - "Community 35"
Cohesion: 0.35
Nodes (3): Boolean, Context, NotificationPrefs

### Community 36 - "Community 36"
Cohesion: 0.17
Nodes (11): Phase 10: Production Polish, Phase 1: Foundation, Phase 2: Core Systems, Phase 3: Streak System (Current), Phase 4: Pet System, Phase 5: Pet Animation System, Phase 6: Rewards and Economy, Phase 7: Cosmetics (+3 more)

### Community 37 - "Community 37"
Cohesion: 0.24
Nodes (5): Flow, Int, Long, StatisticsEntity, StatisticsDao

### Community 38 - "Community 38"
Cohesion: 0.36
Nodes (3): RewardUiEvent, String, AchievementEngine

### Community 39 - "Community 39"
Cohesion: 0.25
Nodes (5): Flow, Int, PetEntity, String, PetRepository

### Community 40 - "Community 40"
Cohesion: 0.24
Nodes (5): Flow, Int, Long, PetEntity, PetDao

### Community 41 - "Community 41"
Cohesion: 0.38
Nodes (3): Int, Long, JournalEngine

### Community 42 - "Community 42"
Cohesion: 0.27
Nodes (9): Boolean, Context, List, String, NotificationSettingsViewModel, NotificationSettingsScreen(), SettingItem, SettingRow() (+1 more)

### Community 43 - "Community 43"
Cohesion: 0.38
Nodes (4): Context, Int, String, RewardPopupUtil

### Community 44 - "Community 44"
Cohesion: 0.28
Nodes (4): Flow, HabitProgressEntity, Long, HabitProgressDao

### Community 45 - "Community 45"
Cohesion: 0.25
Nodes (4): HabitProgressEntity, Long, HabitProgressRepository, HabitProgressRepositoryImpl

### Community 46 - "Community 46"
Cohesion: 0.28
Nodes (4): Flow, HabitProgressEntity, Long, HabitProgressRepository

### Community 47 - "Community 47"
Cohesion: 0.36
Nodes (3): Int, Long, StreakCalculatorTest

### Community 48 - "Community 48"
Cohesion: 0.46
Nodes (7): AchievementReward, ChestReward, CoinReward, DragonEvolutionReward, LevelUpReward, RewardUiEvent, StreakReward

### Community 49 - "Community 49"
Cohesion: 0.33
Nodes (4): Int, Long, Calendar, StreakCalculator

### Community 52 - "Community 52"
Cohesion: 0.40
Nodes (3): PetAnimations, String, Float

### Community 53 - "Community 53"
Cohesion: 0.50
Nodes (3): RewardUiEvent, SharedFlow, RewardEventBus

### Community 54 - "Community 54"
Cohesion: 0.50
Nodes (3): permissions, allow, deny

## Knowledge Gaps
- **199 isolated node(s):** `PreToolUse`, `deny`, `allow`, `AchievementDatabaseInitializer`, `StatisticsDatabaseInitializer` (+194 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **17 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `HabitPetNavGraph()` connect `Community 0` to `Community 1`, `Community 5`, `Community 8`, `Community 42`, `Community 22`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **Why does `HabitDetailViewModel` connect `Community 4` to `Community 1`?**
  _High betweenness centrality (0.018) - this node is a cross-community bridge._
- **Why does `PetScreen()` connect `Community 5` to `Community 0`?**
  _High betweenness centrality (0.016) - this node is a cross-community bridge._
- **Are the 13 inferred relationships involving `HabitPetNavGraph()` (e.g. with `.onCreate()` and `AchievementScreen()`) actually correct?**
  _`HabitPetNavGraph()` has 13 INFERRED edges - model-reasoned connections that need verification._
- **What connects `PreToolUse`, `deny`, `allow` to the rest of the system?**
  _199 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.05279034690799397 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.06342780026990553 - nodes in this community are weakly interconnected._