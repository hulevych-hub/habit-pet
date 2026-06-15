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

# 17. UI REFINEMENT & UX IMPROVEMENTS

## Documentation

Read:

* UI_GUIDELINES.md (if exists)
* DRAGON_PHASES.md
* DATA_MODEL.md

Update:

* UI_GUIDELINES.md (if exists)

## Tasks

### Global Layout

* [x] Remove the top application title/header bar from all screens
* [x] Remove the black background app header entirely
* [x] Ensure screens use full vertical space for content
* [x] Verify no screen loses navigation functionality after removal

---

### Currency UI

* [x] Replace current coin icon with an actual coin symbol/icon across the app
* [x] Ensure coin icon remains consistent in:

  * ProgressHeader
  * Reward screens
  * Inventory
  * Statistics displays
  * Any future coin displays

---

### Home Screen Improvements

* [x] Move "Next unlock: ..." text below the "Level / Mood" section
* [x] Improve visual hierarchy of pet progression information
* [x] Keep Home screen focused on actionable daily progress

### Home Header & Habit List Refinements

* [x] Gray out the Home streak fire icon until the global streak has been counted for the current day
* [x] Replace the coin drawable with a pile of coins
* [x] Position the pet name on the left with level and mood badges on the right
* [x] Make the Home screen content vertically scrollable so all of today's habits are reachable

### Home Focus & Header Cleanup

* [x] Remove the dragon/progress detail block from the Home screen
* [x] Move "Next unlock: ..." above the Reset Game button
* [x] Remove the persistent Android action bar/header showing the app name

### Home Pet Summary & Rewards Filter Follow-up

* [x] Restore a full-width Home dragon image with name, level, and mood
* [x] Make Rewards rarity and collection filters visibly tinted when unselected
* [x] Make selected Rewards filters use the darker/saturated filter color
* [x] Fix Home streak fire state so it turns on after the global streak is counted for the current day
* [x] Apply the same streak fire grey/active behavior to other screens with gamified headers

---

### Habits Screen Layout Fixes

* [x] Fix habit row layout overflow issues
* [x] Ensure habit names display horizontally and never vertically
* [x] Move streak display below the habit name
* [x] Remove large completion icon from the habit row
* [x] Add a smaller checkbox icon aligned to the right side of the habit name
* [x] Ensure layout remains responsive for long habit names
* [x] Verify accessibility and readability on smaller screens

---

### Swipe Actions for Habits

* [x] Remove permanently visible Edit and Delete buttons
* [x] Implement swipe-to-reveal actions on habit rows
* [x] Swiping left should reveal:

  * Edit action (top)
  * Delete action (bottom)
* [x] Preserve existing edit and delete functionality
* [x] Add smooth swipe animations
* [x] Ensure accidental swipes are minimized
* [x] Follow Material Design swipe behavior where possible

---

### Theme & Input Improvements

* [x] Audit text colors across all screens
* [x] Fix unreadable text colors in habit creation/edit screens
* [x] Ensure text fields use proper contrast
* [x] Change white text on light backgrounds to black or appropriate contrast colors
* [x] Verify dark mode compatibility if supported

---

### Pet Screen Improvements

* [x] Increase dragon display area height
* [x] Increase background display height behind the dragon
* [x] Make the dragon the visual focus of the screen
* [x] Keep all important information visible without scrolling
* [x] Preserve responsiveness on smaller devices

---

### Rewards Screen Improvements

* [x] Add icon before each rewards tab title:

  * Outfits
  * Scenes
  * Auras
* [x] Select icons that visually communicate each category
* [x] Ensure icons match the overall design language

---

### Rarity Filter Styling

* [x] Update rarity filter colors:

  * Normal - Grey
  * Rare - Green
  * Epic - Blue
  * Legendary - Purple
  * Mythic/Future - Dark Orange (if applicable)
* [x] Ensure text remains readable on all filter colors
* [x] Apply colors consistently across all rarity displays

---

### Validation

* [x] Verify all layouts on small screens
* [x] Verify all layouts on large screens
* [x] Ensure no text clipping occurs
* [x] Ensure no UI overlap occurs
* [x] Ensure touch targets remain accessible
* [x] Verify animations remain smooth
* [x] Confirm all screens follow consistent spacing and typography

---

### App icon

* [x] Change the application icon when installing. It should be an icon that represents that application goal

### Build validation

* [x] Verify the app icon is wired through the launcher manifest using `@drawable/ic_launcher_full`
* [x] Generate the debug APK at `habit-pet.apk` with `./gradlew assembleDebug`

