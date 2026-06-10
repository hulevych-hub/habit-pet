# ACCESSORIES

# 🧠 OVERVIEW

The accessories system allows players to customize their dragon using cosmetic items such as hats, glasses, scarves, and background items.

Accessories are:

- Cosmetic only (no gameplay effects)
- Unlockable via shop, chests, and future systems
- Equippable per slot
- Stored persistently in a local database

The system is designed to support future expansion (chests, rarity, achievements, events).

---

# 🧩 CURRENT IMPLEMENTATION

The accessory system consists of:

- Room database storage for inventory items
- Four accessory slots:
  - HAT
  - GLASSES
  - SCARF
  - BACKGROUND
- Coin-based purchase system
- Equip / unequip system
- Persistent storage via PetEntity
- Basic UI in RewardsScreen and PetScreen
- Placeholder rendering in AnimatedPet

---

# 📦 DATA MODEL

## InventoryItemEntity

Stored in `inventory_items` table:

- id: Long (primary key)
- name: String
- type: String (HAT | GLASSES | SCARF | BACKGROUND)
- imageUrl: String (currently unused)
- isUnlocked: Boolean
- isPurchased: Boolean
- isEquipped: Boolean
- price: Int
    - rarity: String (NORMAL | RARE | EPIC | LEGENDARY)

---

## PetEntity (equipped state)

- equippedHat: String?
- equippedGlasses: String?
- equippedScarf: String?
- equippedBackground: String?

Stores derived item IDs (see rules below)

---

# ⚠️ CORE SYSTEM RULES

---

## SLOT RULES

Each accessory belongs to exactly one slot:

- HAT → head accessories
- GLASSES → face accessories
- SCARF → neck accessories
- BACKGROUND → environment layer

Only one item per slot can be equipped at a time.

---

## ITEM IDENTIFICATION (IMPORTANT)

Currently, equipped items are stored using a derived ID:

itemId = item.name.toLowerCase().replace(" ", "_")

Example:
- "Top Hat" → top_hat

⚠️ LIMITATION:
This system is fragile:
- renaming breaks saves
- duplicate names cause collisions
- localization is not supported

Future improvement:
- replace with stable UUID-based item IDs

---

## RARITY SYSTEM (FUTURE)

Each accessory should support rarity:

- NORMAL
- RARE
- EPIC
- LEGENDARY

Rarity will be used for:
- chest drop probabilities
- economy balancing
- visual effects (future)

---

## ACQUISITION SOURCES

Accessories can be obtained from:

- Shop purchase (coins)
- Chest rewards
- Achievements (future systems)

Each accessory conceptually supports multiple acquisition sources.

---

## PURCHASE RULES

1. Player selects accessory
2. System checks coin balance:
   - totalCoins >= price
3. If sufficient:
   - deduct coins
   - set isPurchased = true
4. Item is NOT auto-equipped

---

## EQUIP RULES

1. Only purchased items can be equipped
2. Only one item per slot
3. Equipping replaces previous item in same slot
4. Equipped state is stored in PetEntity

---

## UNEQUIP RULES

1. Clearing slot sets PetEntity field to null
2. Updates inventory isEquipped = false

---

## CHEST INTEGRATION RULES (CRITICAL)

When accessories are granted from chests:

- Only grant accessories not already owned
- Prioritize unowned items
- If all accessories are owned:
  - fallback to coins or EXP
- Avoid duplicate rewards whenever possible

---

## ECONOMY ROLE

Accessories act as:

- Coin sink (shop purchases)
- Reward system items (chests)
- Long-term collection progression

Prices are currently static per item.

---

## ASSET SYSTEM RULES

Accessory images follow:

assets/accessories/{type}/{id}.png

Examples:
- assets/accessories/hat/top_hat.png
- assets/accessories/glasses/round_glasses.png
- assets/accessories/scarf/red_scarf.png

If asset is missing:
- use placeholder rendering
- never block UI
- never break flow

---

## UI BEHAVIOR

Current UI includes:

- RewardsScreen (purchase system)
- PetScreen (equipped display)

Limitations:

- No dedicated inventory separation (owned vs locked)
- No rarity filtering
- No slot filtering
- No sorting system

---

## CONFIGURATION RULES

- No accessory seed system exists yet
- Items must exist in database before use
- No global registry for accessories

---

# 📂 SOURCE FILES

- InventoryItemEntity.kt
- InventoryItemDao.kt
- AppDatabase.kt
- InventoryItemRepositoryImpl.kt
- InventoryItemRepository.kt
- PetRepositoryImpl.kt
- RewardsScreen.kt
- PetScreen.kt
- AnimatedPet.kt

---

# ⚠️ KNOWN LIMITATIONS

- No default inventory seeding system
- imageUrl field unused in UI
- isUnlocked not enforced in UI logic
- No rarity system implemented in data model
- No chest integration implemented in code
- Derived ID system is fragile
- No inventory UI separation (locked vs owned)
- No global accessory registry

---

# 🧭 DESIGN INTENT

The system is designed to evolve into:

- Chest-based reward system
- Rarity-driven economy
- Collection progression system
- Long-term cosmetic motivation loop

Current simplicity exists only to support MVP speed and iteration.