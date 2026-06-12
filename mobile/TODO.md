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

## Implementation Queue

### # 1. PREMIUM UI / UX REDESIGN (GAME-QUALITY POLISH PASS)

## Documentation

Read:

- DRAGON_PHASES.md
- ACHIEVEMENTS.md
- ACTIVITY_LOG.md
- ECONOMY.md
- EXP.md
- DATA_MODEL.md

Update:

- UI_DESIGN.md
- ACTIVITY_LOG.md (if timeline behavior changes)

---

## 🎯 GOAL

Transform the application into a premium mobile experience with:

- Modern mobile-game quality UI
- Strong emotional connection to the dragon
- High visual polish
- Clear progression visibility
- Excellent readability
- Smooth interactions
- Addictive reward feedback
- Consistent design language across all screens

The final experience should feel inspired by:

- Duolingo
- Finch
- Pokémon-style progression systems
- Modern premium mobile productivity apps

---

## 🎨 DESIGN SYSTEM FOUNDATION

### Color Palette

- [x] Define Primary Color:
    - Cosmic Lavender (#8A76F9)
- [x] Define Secondary Color:
    - Soft Amethyst (#A393EB)
- [x] Define Success Color:
    - Mint Grass (#4EDB95)
- [x] Define Background Colors:
    - Warm Alabaster (#FAFAFC)
    - Elevated Card White (#FFFFFF)
- [x] Define Accent Color:
    - Honey Amber (#FFB84D)

### Shape System

- [x] Standardize card radius to 24dp
- [x] Standardize component radius system
- [x] Standardize spacing system:
    - 16dp internal padding
    - 24dp section spacing

### Typography

- [x] Establish consistent typography hierarchy
- [x] Improve readability across all screens
- [x] Reduce visual noise
- [x] Ensure text density remains low

### Motion System

- [x] Define consistent animation timing
- [x] Define consistent easing curves
- [x] Ensure all transitions feel intentional
- [x] Remove abrupt UI state changes

---

## 🏠 HOME SCREEN REDESIGN

### Top Bar

- [x] Create compact streak indicator
- [x] Create wallet pill component
- [x] Remove dangerous actions from main UI
- [x] Move reset functionality into Settings

### Hero Area

- [x] Redesign dragon section as primary focal point
- [x] Allocate approximately 45% of viewport to dragon area
- [x] Add subtle gradient background behind dragon
- [x] Display:
    - pet name
    - level badge
    - dragon stage

### Unified Progress System

- [x] Replace separate progression cards with single progression module
- [x] Create Master Progress Track:
    - current stage
    - next stage
    - XP progress
    - evolution progress
- [x] Display progression visually instead of text-heavy cards

### Today's Nourishment Section

- [x] Redesign habits into wide rounded cards
- [x] Improve completion state visuals
- [x] Improve completed state transitions
- [x] Improve reward feedback when completing habits

### Future Reward Teasing

- [x] Add Next Unlock preview section
- [x] Surface upcoming rewards to increase anticipation

---

## 📋 HABITS SCREEN REDESIGN

### Header

- [x] Create motivational screen header
- [x] Display daily goal progress
- [x] Display daily XP progress

### Habit Cards

- [x] Convert habits into premium card layout
- [x] Add category icons
- [x] Display habit streak indicators
- [x] Improve completion affordance

### Interaction Improvements

- [x] Implement long-press actions:
    - edit
    - delete
    - skip
- [x] Improve touch feedback
- [x] Improve animation quality

### UX Goals

- [x] Reduce friction
- [x] Improve scanability
- [x] Improve daily usability

---

## 🏆 ACHIEVEMENTS SCREEN REDESIGN

### Layout

- [x] Convert achievements into collection-style grid
- [x] Implement two-column layout

### Locked State

- [x] Replace harsh locked styling
- [x] Create mystery collectible appearance
- [x] Show visible progress requirements

### Claimable State

- [x] Create premium claimable visual state
- [x] Add reward glow effect
- [x] Add strong visual distinction from locked items

### Reward Experience

- [x] Improve reward presentation flow
- [x] Improve reward reveal experience
- [x] Ensure achievements feel collectible

---

## 🎒 INVENTORY SCREEN REDESIGN

### Navigation

- [x] Create segmented tabs:
    - Outfits
    - Auras
    - Backgrounds

### Collection Layout

- [x] Create 3-column inventory grid
- [x] Improve browsing experience
- [x] Improve collection visibility

### Rarity System

- [x] Create rarity visual language:
    - Common
    - Rare
    - Epic
    - Legendary
    - Mythic
- [x] Ensure rarity is immediately recognizable

### Selection State

- [x] Create equipped item indicator
- [x] Improve selected item visibility
- [x] Add immediate visual feedback on equip

---

## 📖 ACTIVITY TIMELINE REDESIGN

### Structure

- [x] Remove duplicated stats sections
- [x] Focus screen entirely on player journey

### Timeline Layout

- [x] Create central timeline path
- [x] Alternate milestone placement
- [x] Improve event readability

### Event Types

- [x] Create distinct visual styles for:
    - habit completions
    - achievements
    - level ups
    - evolutions
    - rewards
    - streak milestones

### Storytelling

- [x] Make timeline feel like a personal journey
- [x] Make progression history enjoyable to browse
- [x] Improve emotional connection through event presentation

---

## ⚙️ SETTINGS SCREEN POLISH

### Controls

- [x] Improve toggle styling
- [x] Improve visual consistency
- [x] Align settings screen with overall design system

### Safety

- [x] Separate destructive actions
- [x] Reduce accidental interactions

---

## 🧭 BOTTOM NAVIGATION REDESIGN

### Layout

- [x] Improve spacing and alignment
- [x] Improve readability
- [x] Improve icon hierarchy

### Interaction

- [x] Add animated active-state indicator
- [x] Improve navigation transitions
- [x] Improve perceived responsiveness

---

## ✨ FINAL POLISH PASS

- [x] Review all screens for consistency
- [x] Verify spacing consistency
- [x] Verify typography consistency
- [x] Verify animation consistency
- [x] Verify component consistency
- [x] Verify responsive behavior
- [x] Verify accessibility and readability
- [x] Remove visual clutter
- [x] Ensure one focal point per screen
- [x] Ensure premium mobile-game quality across entire application

---

## SUCCESS CRITERIA

- [x] UI feels modern and premium
- [x] Progress is always visible
- [x] Rewards feel exciting
- [x] Navigation feels effortless
- [x] Dragon remains the emotional centerpiece
- [x] Application feels polished enough for daily long-term use

