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
- DRAGON_PHASES.md

Update:
- DRAGON_PHASES.md

## Tasks

- [ ] Audit all dragon phases and available assets
- [ ] Implement idle animation for Egg
- [ ] Implement idle animation for Hatchling
- [ ] Implement idle animation for Young Dragon
- [ ] Implement idle animation for Adult Dragon
- [ ] Implement idle animation for Ancient Dragon
- [ ] Verify animations loop correctly
- [ ] Verify performance on mobile
- [ ] Update DRAGON_PHASES.md

---

# 2. PHASE TRANSITION ANIMATIONS

## Documentation

Read:
- DRAGON_PHASES.md

Update:
- DRAGON_PHASES.md

## Tasks

- [ ] Audit available phase assets
- [ ] Implement Egg → Hatchling transition
- [ ] Implement Hatchling → Young Dragon transition
- [ ] Implement Young Dragon → Adult Dragon transition
- [ ] Implement Adult Dragon → Ancient Dragon transition
- [ ] Trigger transitions automatically on phase change
- [ ] Ensure transitions only play once per unlock
- [ ] Update DRAGON_PHASES.md

---

# 3. ACCESSORIES SYSTEM

## Documentation

Read:
- ACCESSORIES.md

Update:
- ACCESSORIES.md
- DATA_MODEL.md

## Tasks

### Data Model

- [x] Define accessory rarity system
- [x] Define accessory slot system
- [x] Add accessory ownership state
- [x] Add accessory equipped state
- [x] Add accessory unlock state
- [x] Persist accessory data

### Equipment System

- [x] Equip accessory
- [x] Unequip accessory
- [x] Prevent invalid slot combinations
- [x] Apply accessories to dragon rendering

### Inventory UI

- [x] Create Inventory tab
- [x] Create Locked tab
- [x] Group accessories by slot
- [x] Add rarity filter
- [x] Add slot filter
- [x] Display owned accessories
- [x] Display locked accessories

### Economy Integration

- [x] Define accessory coin costs
- [x] Link costs to accessory data

### Documentation

- [x] Update ACCESSORIES.md
- [x] Update DATA_MODEL.md

---

# 4. CHEST REWARD SYSTEM

## Documentation

Read:
- CHEST_REWARDS.md
- ACCESSORIES.md
- EXP.md
- ECONOMY.md

Update:
- CHEST_REWARDS.md
- DATA_MODEL.md

## Tasks

### Chest Types

- [x] Implement Normal Chest
- [x] Implement Rare Chest
- [x] Implement Epic Chest
- [x] Implement Legendary Chest

### Reward Types

- [x] Coin rewards
- [x] EXP rewards
- [x] Accessory rewards

### Accessory Logic

- [x] Only reward accessories not yet unlocked
- [x] Avoid duplicate accessory rewards when possible

### Reward Configuration

- [x] Make chest rewards configurable
- [x] Support variable EXP rewards per chest
- [x] Support variable coin rewards per chest
- [x] Support special chest configurations

### Opening Flow

- [x] Implement chest opening logic
- [x] Implement reward reveal flow
- [x] Persist granted rewards

### Documentation

- [x] Update CHEST_REWARDS.md
- [x] Update DATA_MODEL.md

---

# 5. ECONOMY + PROGRESSION BALANCING

## Documentation

Read:
- ECONOMY.md
- EXP.md
- CHEST_REWARDS.md
- DRAGON_PHASES.md

Update:
- ECONOMY.md
- EXP.md
- CHEST_REWARDS.md
- DRAGON_PHASES.md

## Tasks

### EXP SYSTEM

- [ ] Define normal habit EXP rewards
- [ ] Define timer habit EXP rewards
- [ ] Define chest EXP rewards
- [ ] Define level progression curve
- [ ] Define EXP required per level

### COINS

- [ ] Define coin rewards
- [ ] Define coin sinks
- [ ] Define accessory pricing balance

### CHEST BALANCE

- [ ] Define level-up chest probabilities
- [ ] Define chest rarity probabilities
- [ ] Define accessory reward probabilities
- [ ] Define coin reward probabilities
- [ ] Define EXP reward probabilities

### VALIDATION

- [ ] Verify progression speed
- [ ] Verify reward balance
- [ ] Verify economy stability

---

# 6. ACHIEVEMENTS

## Documentation

Read:
- ACHIEVEMENTS.md
- ECONOMY.md
- CHEST_REWARDS.md

Update:
- ACHIEVEMENTS.md

## Tasks

### System

- [x] Implement achievement tracking
- [x] Implement achievement completion logic
- [x] Implement achievement claiming flow

### Rewards

- [x] Coin rewards
- [x] EXP rewards
- [x] Chest rewards

### Content

- [x] Habit completion achievements
- [x] Streak achievements
- [x] Level achievements
- [x] Collection achievements

### UI

- [x] Create achievements screen
- [x] Show progress indicators
- [x] Show rewards clearly

---

# 7. GLOBAL STREAK EVENT

## Documentation

Read:
- ACHIEVEMENTS.md
- STATISTICS.md

Update:
- ACHIEVEMENTS.md
- STATISTICS.md

## Tasks

- [ ] Define streak milestone triggers
- [ ] Detect milestone streaks
- [ ] Create celebration screen
- [ ] Implement heart animation (Duolingo-style)
- [ ] Display reward summary
- [ ] Prevent duplicate triggers
- [ ] Update documentation

---

# 8. UI / LAYOUT POLISH

## Documentation

None required unless systems change.

## Tasks

- [ ] Review all major screens
- [ ] Improve spacing consistency
- [ ] Improve typography consistency
- [ ] Improve component consistency
- [ ] Improve animation flow
- [ ] Improve inventory UX
- [ ] Improve achievements UX
- [ ] Improve chest UX
- [ ] Improve dragon UX
- [ ] Ensure mobile responsiveness

---

# 9. PET RENAME

## Documentation

Read:
- DATA_MODEL.md

Update:
- DATA_MODEL.md

## Tasks

- [ ] Implement pet rename feature
- [ ] Persist pet name
- [ ] Validate input
- [ ] Display name across UI

---

# 10. NOTIFICATIONS

## Documentation

Read:
- NOTIFICATIONS.md

Update:
- NOTIFICATIONS.md

## Tasks

- [ ] Define notification types
- [ ] Habit reminder notifications
- [ ] Streak warning notifications
- [ ] Level-up notifications
- [ ] Chest notifications
- [ ] Notification preferences
- [ ] Persist settings
- [ ] Update documentation

---

# 🚧 FUTURE SYSTEMS (DO NOT IMPLEMENT)

## QUESTS

- [ ] Deferred until after MVP
- Documentation: QUESTS.md

## REBIRTH SYSTEM

- [ ] Deferred until after MVP
- Documentation: ENDGAME.md