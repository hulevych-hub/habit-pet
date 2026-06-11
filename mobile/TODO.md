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

# 1. STATIC ANIMATIONS

## Documentation

Read:

* DRAGON_PHASES.md

Update:

* DRAGON_PHASES.md

## Tasks

* [x] Audit all dragon phases and available assets
* [x] Implement idle animation for Egg
* [x] Implement idle animation for Hatchling
* [x] Implement idle animation for Young Dragon
* [x] Implement idle animation for Adult Dragon
* [x] Implement idle animation for Ancient Dragon
* [x] Verify animations loop correctly
* [x] Verify performance on mobile
* [x] Update DRAGON_PHASES.md

---

# 2. PHASE TRANSITION ANIMATIONS

## Documentation

Read:

* DRAGON_PHASES.md

Update:

* DRAGON_PHASES.md

## Tasks

* [x] Audit available phase assets
* [x] Implement Egg → Hatchling transition
* [x] Implement Hatchling → Young Dragon transition
* [x] Implement Young Dragon → Adult Dragon transition
* [x] Implement Adult Dragon → Ancient Dragon transition
* [x] Trigger transitions automatically on phase change
* [x] Ensure transitions only play once per unlock
* [x] Update DRAGON_PHASES.md

---

# 3. CUSTOMIZATION SYSTEM (OUTFITS, BACKGROUNDS & AURAS)

## Documentation

Read:

* CUSTOMIZATION.md
* DATA_MODEL.md
* CHEST_REWARDS.md
* ECONOMY.md

Update:

* CUSTOMIZATION.md
* DATA_MODEL.md
* CHEST_REWARDS.md
* ECONOMY.md

## Tasks

### Migration

* [x] Audit current accessory implementation
* [x] Remove accessory slot system
* [x] Remove accessory-specific rendering logic
* [x] Remove accessory-specific UI
* [x] Remove accessory-specific filters
* [x] Migrate existing documentation

### Data Model

* [x] Define Outfit type
* [x] Define Background type
* [x] Define Aura type
* [x] Define rarity system
* [x] Define ownership state
* [x] Define unlock state
* [x] Define equipped state
* [x] Persist customization data

### Outfits

* [x] Implement outfit ownership
* [x] Implement outfit unlocks
* [x] Implement outfit equipping
* [x] Ensure only one outfit can be equipped
* [x] Apply outfits to dragon rendering

### Backgrounds

* [x] Implement background ownership
* [x] Implement background unlocks
* [x] Implement background equipping
* [x] Ensure only one background can be equipped
* [x] Apply backgrounds to dragon rendering

### Auras

* [x] Implement aura ownership
* [x] Implement aura unlocks
* [x] Implement aura equipping
* [x] Ensure only one aura can be equipped
* [x] Apply auras to dragon rendering

### Collection UI

* [x] Create Collection tab
* [x] Create Locked tab
* [x] Display owned items
* [x] Display locked items
* [x] Filter by type
* [x] Filter by rarity
* [x] Show equipped items
* [x] Show unlock source

### Economy Integration

* [x] Define outfit pricing
* [x] Define background pricing
* [x] Define aura pricing

### Chest Integration

* [x] Support outfit rewards
* [x] Support background rewards
* [x] Support aura rewards
* [x] Prevent duplicate rewards when possible
* [x] Prefer rewarding locked items

### Asset System

* [x] Define outfit asset naming convention
* [x] Define background asset naming convention
* [x] Define aura asset naming convention
* [x] Support placeholder assets

### Documentation

* [x] Update CUSTOMIZATION.md
* [x] Update DATA_MODEL.md
* [x] Update CHEST_REWARDS.md
* [x] Update ECONOMY.md

---

# 4. ECONOMY + PROGRESSION BALANCING

## Documentation

Read:

* ECONOMY.md
* EXP.md
* CHEST_REWARDS.md
* DRAGON_PHASES.md

Update:

* ECONOMY.md
* EXP.md
* CHEST_REWARDS.md
* DRAGON_PHASES.md

## Tasks

### Coins

* [x] Define coin rewards
* [x] Define outfit pricing balance
* [x] Define background pricing balance
* [x] Define aura pricing balance

### Chest Balance

* [x] Define outfit reward probabilities
* [x] Define background reward probabilities
* [x] Define aura reward probabilities

### Validation

* [x] Verify progression speed
* [x] Verify reward balance
* [x] Verify economy stability

### Documentation

* [x] Update ECONOMY.md
* [x] Update EXP.md
* [x] Update CHEST_REWARDS.md
* [x] Update DRAGON_PHASES.md