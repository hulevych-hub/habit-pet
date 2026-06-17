# UI Design

## Design System

Habit Pet uses a warm mobile-game visual language centered on the dragon as the emotional focal point.

### Color Palette

The app-level palettes are defined in `Color.kt` and `Theme.kt`. The default palette is `Autumn`, derived from the achievements milestone hall:

- Primary: Aurora Violet `#7C3AED`
- Secondary: Hall Gold `#FFD45A`
- Success: Mint Grass `#4EDB95`
- Background: Milestone Stone `#4A423D`
- Cards: Warm Stone `#5A4A42`
- Accent: Harvest Amber `#FFB84D`
- Gradients: Hall Gold Top `#FFE6A0` → Honey Gold `#F2B94A` → Burnt Amber `#C98224`
- Rarity colors: Normal `#6F6A8A`, Rare `#4EDB95`, Epic `#3B91FF`, Legendary `#B26CFF`

### Shape and Spacing

- Cards use rounded corners in the 24dp-30dp range.
- Pill controls and toggles use 999dp radius.
- Screen sections use 16dp horizontal padding and 10dp-16dp vertical spacing.
- Hero and settings sections use 18dp internal padding.
- Cards use light elevation to separate interactive surfaces without dense borders.

### Typography

- Screen titles use `headlineSmall`.
- Section titles use `titleLarge` or `titleMedium`.
- Metrics use `titleMedium`.
- Supporting text uses `bodySmall` or `bodyMedium`.
- Labels and badges use `labelMedium`.

### Motion

- Bottom navigation uses an animated active indicator that grows for the selected item.
- ProgressHeader pulses when streak or combo state changes.
- Habit completion buttons use animated state changes through existing completion state.
- Activity timeline uses colored nodes and milestone tinting instead of abrupt state changes.

## Screen Patterns

### Home

- The streak and coin pills appear in the top chrome on screens that use the shared gamified header.
- The Home screen keeps the dragon image and gradient background flush to the left and right edges, starting immediately below the shared header.
- The streak fire icon is muted until the global streak has been counted for the current day.
- The Home screen focuses on actionable daily progress: Next Unlock, Reset Game, Today's Quest, and today's habits.
- The Home content scrolls vertically so all of today's habits remain reachable.
- Today's habits are shown as wide rounded cards with completed-state emphasis.

### Habits

- The dense title header is removed so the habit list can use more vertical content space while bottom navigation remains available.
- Habit cards are premium cards with completion affordance, category badges, category labels, and swipe-revealed edit/delete actions.
- Long names are constrained to one line with ellipsis overflow to prevent vertical text wrapping.
- Swipe left reveals stacked white Edit and Remove action buttons on the right while the habit card stays visible; swipe right hides them.
- Repository-backed screens show a lightweight loading card while initial data is warming up.
- Recoverable failures show a lightweight error card with a clear retry/dismiss action.
- Settings toggle failures keep the previous toggle state and show the shared error card until the user dismisses it.
- Destructive actions require confirmation before they execute.
- Completion affordance is a prominent check button.
- Long press still opens actions for edit, delete, and session-level skip.

### Achievements

- Achievements render as a two-column collectible grid.
- Locked achievements use a mystery collectible style.
- Claimable achievements use stronger amber/violet visual distinction.
- Claimed achievements show a muted collected state.
- Achievement load or claim failures use the shared lightweight error card with a retry action.

### Inventory / Rewards

- Collection uses segmented tabs for Outfits, Backgrounds, and Auras with category icons.
- Inventory items render in a three-column grid.
- Rarity filters are segmented chips with stronger selected borders and readable muted unselected text.
- Rarity filters use their rarity color as a translucent unselected background and a fully saturated selected background.
- Owned and locked collections are toggled with a segmented control.
- The Rewards screen is entered from the shared header coin amount, opening directly to the locked collection tab.
- The Pet screen Attribute Card has an edit icon that opens the Rewards screen to the owned collection tab.
- Items display rarity color, source, price, equipped state, and immediate equip/unequip/purchase actions.

### Activity Timeline

- Timeline uses a central story path with alternating left/right event placement.
- Event cards use milestone tinting and colored nodes based on rarity.
- The screen emphasizes player journey over duplicated stats.

### Pet

- The pet screen uses a premium dark navy shell with a full-width dragon showcase immediately below the shared header.
- The showcase keeps the equipped background and animated dragon as the visual focus, with a gold frame and soft platform glow.
- The level/name medallion sits at the lower center of the showcase with gold connector lines on both sides, while the mood pill sits under the pet/background area to the right.
- Below the showcase, XP is shown as centered text above a gold progress bar.
- The pet bond control, attribute card, and level-up button use dark cards, gold borders, and warm amber gradients to match the reference layout.
- Attribute rows show outfit, scene, and aura with category icons and compact equipped-state text.

### Settings

- Notification settings use a premium header and rounded toggle cards.
- Toggles use the app primary color when enabled.
- Copy frames reminders as gentle, supportive nudges rather than pressure.
- Settings includes a subtle one-line copyright footer in muted grey.

### Bottom Navigation

- The persistent app shell uses five primary destinations: Home, Habits, Pet, Achievements, and Settings.
- Rewards opens from the shared header coin amount and the Pet Attribute Card edit icon instead of the bottom navigation.
- Navigation items use consistent icon and label spacing.
- The selected item has an animated rounded active indicator using theme colors.
- Achievement badge remains visible for claimable rewards.
- The bottom bar is hidden on detail, edit, and creation screens so focus stays on the current task.

## Asset Notes

- The launcher icon is now a custom full vector dragon icon in `drawable/ic_launcher_full.xml`.
- The currency icon is now a custom pile-of-coins drawable in `drawable/ic_coin.xml` and is reused through `CoinIcon` / `CoinPill`.
- The implementation uses existing `AnimatedPet` rendering, existing drawable assets for collection previews where available, Material icons, gradient surfaces, and rarity badges.
