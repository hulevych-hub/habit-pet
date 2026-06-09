# ACCESSORIES

## Overview

The accessories system in Habit Pet allows players to customize their pet's appearance with hats, glasses, scarves, and background items. Players can purchase accessories using coins earned from habit completions and equip them to change their pet's appearance.

## Current Implementation

The accessory system consists of:
- Inventory items stored in a Room database with properties for name, type, price, and ownership state
- Categorization system for four accessory types: HAT, GLASSES, SCARF, BACKGROUND
- Purchase mechanics using in-game currency (coins)
- Equip/unequip functionality that updates the pet's appearance
- UI for browsing, purchasing, and equipping items in the Rewards screen
- Visual display of equipped items on the pet in the Pet screen

## Rules

### Inventory Item Structure
Each accessory item has the following properties:
- `id: Long` - unique identifier
- `name: String` - display name of the accessory
- `type: String` - category: "HAT", "GLASSES", "SCARF", or "BACKGROUND"
- `imageUrl: String` - reference to image asset (currently unused in UI, uses placeholders)
- `isUnlocked: Boolean` - whether the item is available for purchase
- `isPurchased: Boolean` - whether the player has bought the item
- `isEquipped: Boolean` - whether the item is currently equipped on the pet
- `price: Int` - cost in coins to purchase the item

### Accessory Types
The system supports four accessory types, corresponding to fields in PetEntity:
- **HAT** - maps to `equippedHat` field in PetEntity
- **GLASSES** - maps to `equippedGlasses` field in PetEntity
- **SCARF** - maps to `equippedScarf` field in PetEntity
- **BACKGROUND** - maps to `equippedBackground` field in PetEntity

### Item Identification
When equipping an item, the system uses a derived item ID:
- `itemId = item.name.toLowerCase().replace(" ", "_")`
- This string value is stored in the PetEntity equipped* fields
- Example: "Top Hat" becomes "top_hat"

### Purchase Process
1. Player selects an item in the Rewards screen
2. System checks if player has enough coins (`statisticsRepository.getStatistics().firstOrNull()?.totalCoins >= item.price`)
3. If sufficient funds:
   - Deduct item price from total coins
   - Mark item as `isPurchased = true`
   - Item remains unequipped by default (`isEquipped = false`)

### Equipping Process
1. Player selects "Equip" on a purchased item
2. System updates PetEntity:
   - Sets appropriate equipped* field to the itemId (derived from name)
   - Marks inventory item as `isEquipped = true`
3. Only one item per type can be equipped at a time
4. Equipping a new item automatically unequips the previous item of that type (by setting its isEquipped to false)

### Unequipping Process
1. Player selects an equipped item (shows "Equipped" status)
2. System updates:
   - Sets the corresponding equipped* field in PetEntity to null
   - Marks inventory item as `isEquipped = false`

## Configuration

All accessory system values are managed through the inventory item database:
- Item names, types, prices, and initial lock/purchase states are stored per item
- No hardcoded values for specific items in the source code
- Default state: inventory database starts empty (no initializer implemented)

## Data Model

**InventoryItemEntity** (app/src/main/java/com/example/mobile/data/local/entities/InventoryItemEntity.kt):
- Table: `inventory_items`
- Columns: id, name, type, imageUrl, isUnlocked, isPurchased, isEquipped, price

**PetEntity** (app/src/main/java/com/example/mobile/data/local/entities/PetEntity.kt):
- Relevant columns: equippedHat, equippedGlasses, equippedScarf, equippedBackground (all String?)

## Source Files

- app/src/main/java/com/example/mobile/data/local/entities/InventoryItemEntity.kt
- app/src/main/java/com/example/mobile/data/local/dao/InventoryItemDao.kt
- app/src/main/java/com/example/mobile/data/local/database/AppDatabase.kt
- app/src/main/java/com/example/mobile/data/repository/InventoryItemRepositoryImpl.kt
- app/src/main/java/com/example/mobile/domain/repository/InventoryItemRepository.kt
- app/src/main/java/com/example/mobile/data/repository/PetRepositoryImpl.kt
- app/src/main/java/com/example/mobile/presentation/ui/screens/RewardsScreen.kt
- app/src/main/java/com/example/mobile/presentation/ui/screens/PetScreen.kt
- app/src/main/java/com/example/mobile/presentation/ui/components/AnimatedPet.kt

## Known Gaps

1. **No Default Inventory**: The system lacks an initializer to populate the database with default accessory items, meaning the inventory starts empty unless items are added through other means.

2. **Image URL Unused**: The `imageUrl` field is stored but not utilized in the current UI; items display as gray placeholders instead of actual images.

3. **Unlocked State Unused**: The `isUnlocked` field exists but doesn't appear to gate item availability in the current implementation (all items in database are shown regardless of this flag).

4. **No Item Source**: No mechanism exists in the current codebase to add new inventory items; the purchase system assumes items already exist in the database.

5. **Visual Integration**: While equipped items are saved to PetEntity, the AnimatedPet component appears to use placeholder logic for displaying equipped items rather than actual item images or animations.