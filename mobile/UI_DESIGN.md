# UI Design

## Design System

Habit Pet uses a warm mobile-game visual language centered on the dragon as the emotional focal point.

### Color Palette

The app-level palette is defined in `Color.kt` and `Theme.kt`:

- Primary: Cosmic Lavender `#8A76F9`
- Secondary: Soft Amethyst `#A393EB`
- Success: Mint Grass `#4EDB95`
- Background: Warm Alabaster `#FAFAFC`
- Cards: Elevated Card White `#FFFFFF`
- Accent: Honey Amber `#FFB84D`
- Rarity colors: Common `#6F6A8A`, Rare `#4BA3FF`, Epic `#B26CFF`, Legendary `#FFB84D`

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

- Top bar uses compact streak and wallet pills.
- The dragon hero area is the primary focal point with a gradient background.
- Hero displays pet name, level badge, evolution stage, and mood.
- ProgressHeader and EvolutionTeaser are grouped inside a single progression module.
- Today's habits are shown as wide rounded cards with completed-state emphasis.

### Habits

- Header is a lavender goal card with streak copy, daily XP progress, a segmented progress bar, and bonus chest emphasis.
- Habit cards are premium cards with completion affordance, category badges, category labels, and visible edit/delete actions.
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

- Collection uses segmented tabs for Outfits, Backgrounds, and Auras.
- Inventory items render in a three-column grid.
- Rarity filters are segmented chips.
- Owned and locked collections are toggled with a segmented control.
- Items display rarity color, source, price, equipped state, and immediate equip/unequip/purchase actions.

### Activity Timeline

- Timeline uses a central story path with alternating left/right event placement.
- Event cards use milestone tinting and colored nodes based on rarity.
- The screen emphasizes player journey over duplicated stats.

### Settings

- Notification settings use a premium header and rounded toggle cards.
- Toggles use the app primary color when enabled.
- Copy frames reminders as gentle, supportive nudges rather than pressure.

### Bottom Navigation

- The persistent app shell uses six primary destinations: Home, Habits, Pet, Rewards, Achievements, and Settings.
- Navigation items use consistent icon and label spacing.
- The selected item has an animated rounded active indicator using theme colors.
- Achievement badge remains visible for claimable rewards.
- The bottom bar is hidden on detail, edit, and creation screens so focus stays on the current task.

## Asset Notes

No final artwork assets were required for this pass. The implementation uses existing `AnimatedPet` rendering, existing drawable assets for collection previews where available, Material icons, gradient surfaces, and placeholder-style rarity badges.
