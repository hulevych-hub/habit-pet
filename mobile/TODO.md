# TODO — UI/UX Audit Improvements

## Visual Design Consistency

### Spacing & Layout
- [ ] **Standardize horizontal padding across screens** — Most screens use `20.dp` horizontal padding, but `HabitDetailScreen` applies it twice (once in the outer `Column` and again in the inner `LazyColumn`), causing double padding on some content. Audit all screens for consistent `20.dp` horizontal padding.
- [ ] **Standardize content padding top/bottom** — `HomeScreen` uses `top = 16.dp, bottom = 32.dp`, `ActivityTimelineScreen` uses the same, but `HabitDetailScreen` uses `top = 16.dp, bottom = 32.dp` inside a nested structure. `StatisticsScreen` uses `top = 20.dp, bottom = 40.dp`. Unify to a single standard.
- [ ] **Standardize card internal padding** — Cards use `16.dp`, `18.dp`, `20.dp`, and `24.dp` internal padding inconsistently. `HabitCreationForm` uses `18.dp`, `HabitDetailScreen` completion status uses `20.dp`, `StatisticsScreen` stat cards use `18.dp`, hero card uses `24.dp`. Pick a standard (e.g., `16.dp` for compact, `20.dp` for hero/featured).
- [ ] **Standardize `Spacer` heights between sections** — Spacing between cards/sections varies: `12.dp`, `14.dp`, `16.dp`, `18.dp`. `HabitCreationForm` uses `16.dp` vertical arrangement, `NotificationSettingsScreen` uses `12.dp`. Define a spacing scale (e.g., `8.dp` tight, `12.dp` default, `16.dp` section, `24.dp` major).

### Card Design
- [ ] **Standardize card border radius** — Cards use `RoundedCornerShape(14.dp)`, `(16.dp)`, `(20.dp)`, `(22.dp)`, `(24.dp)`, and `(26.dp)`. `EmptyStateCard` uses `24.dp`, most content cards use `20.dp` or `22.dp`, `HeroStreakCard` uses `26.dp`. Define a consistent scale: small `(12.dp)`, medium `(16.dp)`, large `(20.dp)`, hero `(24.dp)`.
- [ ] **Standardize card border stroke width** — Borders use `1.dp`, `1.5.dp`, and conditional widths. Most cards use `1.dp` at `0.4f` alpha, but `TypeSelection` uses `1.5.dp`. Unify border styling.
- [ ] **Standardize card elevation** — Most cards use `0.5.dp`, some use `1.dp`, `TypeSelection` uses `0.dp`, `StatBentoCard` uses `1.dp`. Define a consistent elevation system.
- [ ] **Standardize card container colors** — Most cards use `AppTheme.current.card`, but `HabitCreationForm` type selection uses `AppTheme.current.surface` for unselected state. Ensure unselected/selected states are consistent across all card-based selectors.

### Typography
- [ ] **Standardize section header typography** — Section labels use various styles: `labelLarge` with `Bold` in `ActivityTimelineScreen`, `labelLarge` with `Bold` + `letterSpacing = 0.5.sp` in `HabitDetailScreen`, `labelLarge` with `Bold` + `letterSpacing = 0.5.sp` in `StatisticsScreen`. Create a consistent `SectionHeader` style.
- [ ] **Standardize card title typography** — Card titles use `bodyMedium` with `Bold`, `titleMedium` with `Bold`, and `labelLarge` with `Bold` interchangeably. Define a clear hierarchy.
- [ ] **Standardize body text color** — Body text uses `AppTheme.current.ink`, `AppTheme.current.muted`, and `AppTheme.current.ink.copy(alpha = 0.75f)` inconsistently. Define a clear text color hierarchy: primary (`ink`), secondary (`muted`), tertiary (`ink` at reduced alpha).
- [ ] **Audit font weight usage** — `FontWeight.Bold` is used heavily. Some places use `FontWeight.SemiBold`, `FontWeight.Medium`, `FontWeight.Black`. Ensure `Black` is reserved for hero numbers only, `Bold` for titles, `SemiBold` for subtitles, `Medium` for body emphasis.
- [ ] **Standardize letter spacing** — Labels use `letterSpacing = 0.5.sp` or `1.sp` inconsistently. Define a standard for uppercase labels vs. normal text.

### Color Usage
- [ ] **Standardize accent color for interactive elements** — `AppTheme.current.violet` is used for most interactive accents, but `HabitDetailScreen` uses `AppTheme.current.primary` for the back button and completion button. Ensure primary action buttons use a consistent color.
- [ ] **Standardize success color usage** — `AppTheme.current.success` is used for completed states, but the exact shade and alpha vary (`0.03f`, `0.04f`, `0.08f`, `0.1f`, `0.2f`). Define standard alpha values for background tints.
- [ ] **Standardize danger/error color usage** — Error states use `AppTheme.current.danger` with varying alpha values (`0.08f`, `0.2f`). Unify.
- [ ] **Audit `AppTheme.current.surface` vs `AppTheme.current.card`** — These are used interchangeably in some places (e.g., `HabitCreationForm` uses `surface` for unselected type cards, `outline` alpha backgrounds). Clarify when to use `surface` vs `card`.

## Navigation & Interaction Patterns

### Screen Headers
- [ ] **Standardize detail screen headers** — `HabitDetailScreen` uses `InlineScreenHeader` with back arrow + title, `HabitCreationScreen` uses the same pattern, `HabitEditScreen` uses the same. `StatisticsScreen` uses `CenterAlignedTopAppBar`. `HomeScreen`, `PetScreen`, `AchievementsScreen` use `GamifiedFixedHeader`. Ensure all detail screens follow the same header pattern and all main screens use `GamifiedFixedHeader`.
- [ ] **Standardize back button styling** — `InlineScreenHeader` uses `IconButton` with `ArrowBack` icon. The `tint` color varies: `AppTheme.current.primary` in `HabitDetailScreen`, `AppTheme.current.violet` in creation/edit screens. Unify.
- [ ] **Add consistent screen title styling** — Detail screen titles use `titleLarge` with `Bold`, but the color varies between `AppTheme.current.ink` and `accentColor`. Standardize.

### Empty States
- [ ] **Standardize empty state design** — `EmptyStateCard` is a reusable component, but `HabitDetailScreen` also has inline empty states with different copy patterns. Ensure all empty states use the `EmptyStateCard` component with consistent icon, title, message, and hint styling.
- [ ] **Standardize empty state icons** — `EmptyStateCard` always uses `Icons.Default.Star`. Some screens might benefit from contextual icons. Consider making the icon configurable or using contextual icons consistently.

### Loading States
- [ ] **Standardize loading state design** — `LoadingStateCard` is used consistently, but `HabitDetailScreen` has its own `CenteredProgressIndicator` with a full-screen background. Unify to use `LoadingStateCard` everywhere.
- [ ] **Standardize loading messages** — Loading messages vary in tone and style. Ensure they all follow the warm, playful brand voice consistently.

### Error States
- [ ] **Standardize error state design** — `ErrorStateCard` is used in some screens, but `HabitDetailScreen` has its own inline `ErrorMessage` composable with different styling. Unify to use `ErrorStateCard` everywhere.
- [ ] **Standardize error message copy** — Error messages vary in tone. Ensure they follow the supportive, non-harsh brand voice.

## Reward & Feedback Systems

### Reward Overlays
- [ ] **Standardize reward screen spacing** — `RewardScreen` uses `24.dp` padding inside the overlay box, but the `Spacer` heights between elements use negative values like `(-4).dp` and `(-8).dp`. Negative spacers are a code smell — restructure layouts to avoid them.
- [ ] **Standardize reinforcement message spacing** — `ReinforcementMessage` uses `Spacer(modifier = Modifier.height((-8).dp))` which is a negative spacer. Fix the layout to use proper positive spacing.
- [ ] **Standardize reward text colors** — Reward screens use `AppTheme.current.rewardText` and `AppTheme.current.rewardTextMuted`. Ensure these are defined in all theme variants and provide sufficient contrast.
- [ ] **Add haptic feedback for reward interactions** — Chest opening and reward completion lack haptic feedback. Add consistent haptic feedback for reward taps and completions.

### Micro Feedback
- [ ] **Standardize micro-feedback positioning** — `MicroFeedbackManager` overlay positioning should be consistent across all screens. Audit that it doesn't overlap with navigation elements or headers.
- [ ] **Standardize micro-feedback animation duration** — Ensure all micro-feedback animations have consistent duration and easing curves.

## Screen-Specific Improvements

### Home Screen
- [ ] **Standardize habit card spacing** — `HabitCard` internal spacing and the `verticalArrangement = Arrangement.spacedBy(10.dp)` in the habit list should match the global spacing scale.
- [ ] **Standardize progress header sizing** — `ProgressHeader` and `EvolutionTeaser` sizing should be consistent across different screen sizes.

### Habits Screen
- [ ] **Standardize FAB styling** — The floating action button for adding habits should use consistent styling (size, color, icon, elevation) with the design system.
- [ ] **Standardize habit list item spacing** — The `10.dp` spacing between habit cards should match the global spacing scale.

### Pet Screen
- [ ] **Standardize attribute card styling** — Attribute cards on the Pet screen should use the same card styling (border radius, elevation, padding) as cards on other screens.
- [ ] **Standardize customization grid spacing** — The customization item grid spacing should match the global spacing scale.

### Achievements Screen
- [ ] **Standardize achievement card styling** — Achievement cards should use consistent card styling with the rest of the app.
- [ ] **Standardize progress bar styling** — Progress bars on achievement cards should use consistent colors, heights, and corner radii.

### Activity Timeline Screen
- [ ] **Standardize timeline item spacing** — Timeline items use `padding(vertical = 2.dp)` and `padding(bottom = 12.dp)`. These should match the global spacing scale.
- [ ] **Standardize day header spacing** — Day headers use `padding(top = 20.dp, bottom = 4.dp)`. This should match the section header spacing standard.

### Statistics Screen
- [ ] **Standardize grid spacing** — `StatisticsScreen` uses `horizontalArrangement = Arrangement.spacedBy(14.dp)` and `verticalArrangement = Arrangement.spacedBy(14.dp)`. This should match the global spacing scale.
- [ ] **Standardize stat card sizing** — `StatBentoCard` internal padding of `18.dp` should match the card padding standard.

### Habit Creation/Edit Screens
- [ ] **Standardize form field styling** — `OutlinedTextField` uses `RoundedCornerShape(14.dp)` but cards use `RoundedCornerShape(22.dp)`. Consider unifying or clearly differentiating form elements from cards.
- [ ] **Standardize emoji picker bottom sheet** — The bottom sheet shape `RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)` is inconsistent with the app's corner radius scale. Use a standard large radius.
- [ ] **Standardize duration selector styling** — The duration selector's internal padding and button sizes should match the global scale.

### Habit Detail Screen
- [ ] **Remove duplicate horizontal padding** — The outer `Column` and inner `LazyColumn` both apply `padding(horizontal = 20.dp)`, causing double padding.
- [ ] **Standardize timer display sizing** — The timer uses `displayMedium` which may be too large. Ensure it's balanced with surrounding content.
- [ ] **Standardize completion history item spacing** — History items use custom spacing that should match the global scale.

### Notification Settings Screen
- [ ] **Standardize setting row styling** — `SettingRow` uses `RoundedCornerShape(20.dp)` while most cards use `22.dp`. Unify.
- [ ] **Standardize switch styling** — Switch colors are defined inline. Extract to a consistent switch style.
- [ ] **Standardize theme selection radio button styling** — Radio button sizing and spacing should match the global scale.

## Component-Level Improvements

### EmptyStateCard
- [ ] **Make icon configurable** — `EmptyStateCard` always uses `Icons.Default.Star`. Add an icon parameter for contextual empty states.
- [ ] **Standardize internal padding** — Uses `20.dp` padding which should match the card padding standard.

### LoadingStateCard
- [ ] **Standardize internal padding** — Uses `24.dp` padding which is larger than most cards. Align with card padding standard.

### ErrorStateCard
- [ ] **Standardize internal padding** — Uses `20.dp` padding. Align with card padding standard.
- [ ] **Add contextual icon** — Consider adding an error/warning icon for visual consistency with `EmptyStateCard`.

### AssetPainter / AssetPreview
- [ ] **Add consistent placeholder/fallback styling** — When assets fail to load, ensure the fallback UI is consistent across all screens.
- [ ] **Standardize asset preview sizing** — Asset previews use various sizes (`96.dp`, `120 * multiplier`). Define standard sizes.

### StreakCalendarOverlay
- [ ] **Standardize overlay padding and spacing** — Ensure the calendar overlay uses consistent spacing with the rest of the app.
- [ ] **Standardize day cell sizing** — Calendar day cells should have consistent sizing across different screen sizes.

## Theme & Design Tokens

### Color System
- [ ] **Document all theme color tokens** — Ensure every color in `Color.kt` has a clear purpose and usage guideline. Remove unused colors.
- [ ] **Ensure all theme variants have all required tokens** — Verify that `rewardBackdropStart`, `rewardBackdropCenter`, `rewardBackdropEnd`, `rewardText`, `rewardTextMuted`, `rewardAccent`, `headerSurface`, `surfaceVariant`, `onSurfaceVariant`, `dangerSoft`, `mint`, `gold`, `amberDark`, `violetMuted` are all defined in every `AppThemeOption`.
- [ ] **Standardize alpha values** — Define a set of standard alpha values (e.g., `0.04f` for subtle backgrounds, `0.08f` for hover states, `0.12f` for icon backgrounds, `0.2f` for borders, `0.4f` for dividers, `0.65f` for secondary text, `0.75f` for tertiary text).

### Spacing System
- [ ] **Define a spacing scale** — Create a formal spacing scale: `xs = 4.dp`, `sm = 8.dp`, `md = 12.dp`, `lg = 16.dp`, `xl = 20.dp`, `xxl = 24.dp`, `xxxl = 32.dp`. Apply consistently.
- [ ] **Define a corner radius scale** — Create a formal radius scale: `sm = 8.dp`, `md = 12.dp`, `lg = 16.dp`, `xl = 20.dp`, `xxl = 24.dp`. Apply consistently.

### Typography System
- [ ] **Define a formal typography scale** — Document the usage of each `MaterialTheme.typography` style: `displayMedium` for timer/hero numbers, `headlineLarge` for streak counts, `headlineMedium` for reward titles, `headlineSmall` for customization names, `titleLarge` for screen titles, `titleMedium` for card titles, `titleSmall` for stat values, `bodyLarge` for reinforcement messages, `bodyMedium` for card body text, `bodySmall` for descriptions, `labelLarge` for buttons, `labelMedium` for section headers, `labelSmall` for captions/tags.
- [ ] **Standardize text color per typography level** — Each typography level should have a default text color that's used consistently.

## Accessibility & Usability

### Touch Targets
- [ ] **Audit all touch targets** — Ensure all interactive elements meet the minimum `48.dp` touch target size. Some `IconButton` elements may be smaller (e.g., back buttons, duration +/- buttons).
- [ ] **Increase duration +/- button touch targets** — `HabitCreationForm` and `HabitEditForm` use `36.dp` buttons which are below the `48.dp` minimum.

### Contrast
- [ ] **Audit text contrast ratios** — Ensure all text meets WCAG AA contrast requirements, especially: `muted` text on `card` backgrounds, `onPrimary` text at `0.65f` alpha, `ink.copy(alpha = 0.75f)` on various backgrounds.
- [ ] **Audit reward screen contrast** — Reward overlay text colors (`rewardText`, `rewardTextMuted`) on gradient backgrounds need contrast verification.

### Content
- [ ] **Standardize copy tone** — Some screens use very elaborate/verbose copy ("Synchronized Journey Event", "Construct Habit Module", "Verification Method"). Consider simplifying for clarity while maintaining the playful tone.
- [ ] **Add content descriptions** — Ensure all icons and images have appropriate `contentDescription` values for screen readers. Some are set to `null`.
- [ ] **Standardize emoji usage in copy** — Some text uses emoji prefixes (e.g., "🎯 Target:", "💬 ") while others don't. Decide on a consistent approach.
