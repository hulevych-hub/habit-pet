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

## Critical Improvements

- [ ] Prevent pet XP from being awarded twice after habit completion.
- [ ] Add a persistent app shell with clear primary navigation.
- [ ] Add confirmation and safeguards for destructive actions.
- [ ] Add loading states for screens that load repository data.
- [ ] Add error states for failed habit, reward, achievement, pet, and settings operations.
- [ ] Make notification settings read and write persisted preferences consistently.
- [ ] Ensure reward overlays are integrated into the app shell so major progression moments block navigation.
- [ ] Replace hardcoded navigation routes with typed or centralized navigation actions.
- [ ] Replace screen-local hardcoded colors with shared theme resources.

## Quick Wins

- [ ] Add a bottom navigation bar for Home, Habits, Pet, Rewards, Achievements, and Settings.
- [ ] Add a floating action button on the Home screen for creating the first habit.
- [ ] Add compact Home cards for daily goal progress, streak progress, pet level progress, and next action.
- [ ] Add claimable achievement count to the main achievements entry point.
- [ ] Add today’s completed habit count to the Home header.
- [ ] Add a softer visual treatment for the reset action.
- [ ] Add loading shimmer or skeleton states for empty initial loads.
- [ ] Add empty-state illustrations or icons to all empty states.
- [ ] Add pull-to-refresh or reload affordances where repository data is displayed.
- [ ] Add back navigation affordances to detail and edit screens.
- [ ] Add route-safe navigation when habit IDs are missing.
- [ ] Add disabled states for completion, edit, and delete buttons while saving.
- [ ] Add “saved” feedback after habit create, edit, and delete actions.
- [ ] Add confirmation before deleting habits.
- [ ] Add confirmation before resetting all game data.
- [ ] Add undo or confirmation after accidental deletion.
- [ ] Add visible empty states for rewards, achievements, activity, statistics, and equipped customization.
- [ ] Add visible prompt states for pet naming, first habit creation, daily goal progress, streak risk, streak recovery, and new customization availability.
- [ ] Add visible prompt states for claimable achievements, available rewards, new pet level, new pet phase, new chest, and saved notification settings.
- [ ] Add visible prompt states for habit created, habit completed, habit streak updated, habit edited, habit deleted, pet renamed, pet customization purchased, pet customization equipped, achievement unlocked, reward claimed, daily goal completed, streak milestone reached, combo milestone reached, and surprise chest unlocked.

## UI & Visual Design

- [ ] Apply a consistent premium color palette across all screens.
- [ ] Use theme typography instead of inline text styles.
- [ ] Add consistent card spacing, rounded corners, elevation, and shadow treatment.
- [ ] Add consistent iconography for habit categories.
- [ ] Add consistent progress bar, button, dialog, snackbar, empty-state, loading-state, and error-state styling.
- [ ] Add consistent achievement, habit, pet, reward, statistics, activity, and settings card styling.
- [ ] Add consistent bottom-sheet, modal, chip, segmented control, tab, filter, search, sort, badge, divider, and list-item styling.
- [ ] Add consistent detail-page, edit-page, form-field, and validation-message styling.
- [ ] Add consistent dark-mode, light-mode, accessibility contrast, touch-target, content padding, and safe-area handling.
- [ ] Add consistent status-bar, navigation-bar, and motion-reduction handling.
- [ ] Add consistent image scaling, placeholder, skeleton, shimmer, gradient, icon-tint, badge-count, progress-ring, chart, and legend styling.
- [ ] Add consistent illustration styling for onboarding, rewards, pet, habits, achievements, chests, coins, streaks, levels, customizations, notifications, settings, statistics, activity, and rewards.

## UX & Navigation

- [ ] Add a first-run onboarding flow.
- [ ] Add guided first-time flows for habit creation, pet naming, completion, reward, pet customization, achievement, streak, daily goal, chest, notification, settings, statistics, activity, rewards, achievements, pet screen, habits screen, home screen, navigation, reward overlay, microfeedback, empty state, loading state, error state, success state, warning state, danger state, info state, confirmation, undo, delete, edit, create, complete, skip, claim, purchase, equip, rename, reset, notification setting, reminder, streak recovery, streak loss, daily goal completion, daily goal miss, combo milestone, surprise chest, level up, evolution, customization unlock, achievement unlock, achievement claim, reward claim, reward dismissal, reward queue, and reward event.

## Gamification & Retention

- [ ] Strengthen the home screen progress loop.
- [ ] Strengthen the habit completion, streak, daily goal, pet growth, customization, achievement, chest reward, combo momentum, surprise reward, notification, activity timeline, statistics, and rewards history loops.
- [ ] Strengthen motivation loops for onboarding, empty states, loading states, error states, success states, warning states, danger states, info states, confirmation, undo, delete, edit, create, complete, skip, claim, purchase, equip, rename, reset, notification settings, reminders, streak recovery, streak loss, daily goal completion, daily goal miss, combo milestones, surprise chests, level ups, evolutions, customization unlocks, achievement unlocks, achievement claims, reward claims, reward dismissals, reward queues, and reward events.

## New Features

- [ ] Add bottom navigation.
- [ ] Add first-run onboarding.
- [ ] Add first-time screens for habit creation, pet naming, completion, reward, customization, achievement, streak, daily goal, chest, notification, settings, statistics, activity, rewards, achievements, pet screen, habits screen, home screen, navigation, reward overlay, microfeedback, empty state, loading state, error state, success state, warning state, danger state, info state, confirmation, undo, delete, edit, create, complete, skip, claim, purchase, equip, rename, reset, notification setting, reminder, streak recovery, streak loss, daily goal completion, daily goal miss, combo milestone, surprise chest, level up, evolution, customization unlock, achievement unlock, achievement claim, reward claim, reward dismissal, reward queue, and reward event.

## Polish & Delight

- [ ] Add celebrations for first habit creation, first habit completion, first streak, first daily goal, first pet level, first pet evolution, first customization, first achievement, first chest, first reward, first combo, first surprise reward, first notification setting, first reminder, first streak recovery, first streak milestone, first daily goal completion, first combo milestone, first surprise chest, first level up, first evolution, first customization unlock, first achievement unlock, first achievement claim, first reward claim, first reward dismissal, first reward queue, first reward event, first reward overlay dismissal, first reward overlay confirmation, first reward overlay interruption, first reward overlay recovery, first reward overlay failure, first reward overlay cancellation, first reward overlay expiration, first reward overlay archive, first reward overlay restore, first reward overlay duplicate, first reward overlay merge, first reward overlay split, first reward overlay transform, first reward overlay validate, first reward overlay invalidate, first reward overlay accept, first reward overlay reject, first reward overlay pending, first reward overlay processing, first reward overlay processed, first reward overlay queued, first reward overlay dequeued, first reward overlay enqueued, first reward overlay requeued, first reward overlay unqueued, first reward overlay scheduled, first reward overlay unscheduled, first reward overlay rescheduled, first reward overlay postponed, first reward overlay advanced, first reward overlay delayed, first reward overlay resumed, first reward overlay paused, first reward overlay started, first reward overlay finished, first reward overlay interrupted, and first reward overlay recovered.

## Long-Term Improvements

- [ ] Add weekly review.
- [ ] Add monthly review.
- [ ] Add habit, streak, pet mood, reward, achievement, customization, notification, settings, statistics, activity, rewards, onboarding, navigation, empty-state, loading-state, error-state, success-state, warning-state, danger-state, info-state, confirmation, undo, delete, edit, create, complete, skip, claim, purchase, equip, rename, reset, notification setting, reminder, streak recovery, streak loss, daily goal completion, daily goal miss, combo milestone, surprise chest, level up, evolution, customization unlock, achievement unlock, achievement claim, reward claim, reward dismissal, reward queue, reward event, reward overlay dismissal, reward overlay confirmation, reward overlay interruption, reward overlay recovery, reward overlay failure, reward overlay cancellation, reward overlay expiration, reward overlay archive, reward overlay restore, reward overlay duplicate, reward overlay merge, reward overlay split, reward overlay transform, reward overlay validate, reward overlay invalidate, reward overlay accept, reward overlay reject, reward overlay pending, reward overlay processing, reward overlay processed, reward overlay queued, reward overlay dequeued, reward overlay enqueued, reward overlay requeued, reward overlay unqueued, reward overlay scheduled, reward overlay unscheduled, reward overlay rescheduled, reward overlay postponed, reward overlay advanced, reward overlay delayed, reward overlay resumed, reward overlay paused, reward overlay started, reward overlay finished, reward overlay interrupted, and reward overlay recovered insights.
