# NOTIFICATIONS

## Overview

The notification system in Habit Pet provides soft, emotional re-entry nudges that help users return to their habits and feel connected to their dragon. The system uses Android's `AlarmManager` to schedule recurring notifications and stores user preferences in `SharedPreferences`.

Notification content is template-driven instead of purely functional. Messages are selected from the current player context, including streak strength and recent app activity, so reminders feel supportive rather than demanding. The same reinforcement message provider is also used by the activity timeline and reward screens so the app speaks with one supportive voice across re-entry, progress review, and reward moments.

## Current Implementation

The notification system consists of:
- `NotificationHelper`: schedules, cancels, and shows reminders with emotional templates
- `NotificationPrefs`: stores reminder preferences, last known streak, last active session, and notification frequency state
- `NotificationPublisher`: receives scheduled alarms and shows notifications when frequency rules allow
- `BootCompletedReceiver`: reschedules reminders after device reboot
- `MainActivity`: records the current streak and active session so scheduled reminders can use player context
- `ReinforcementMessageProvider`: selects contextual reinforcement messages from behavior-based pools and prevents immediate repeats
- Integration with `HabitPetApp` to create the notification channel and schedule reminders on startup

## Rules

### Notification Types

The system supports three recurring reminder types:
1. **Dragon Waiting**: a daily re-entry nudge
2. **Streak Encouragement**: a streak-focused reminder that adapts to low or high streak context
3. **Pet Bond Reminder**: a warm pet-care reminder that adapts to recent user activity

### Emotional Template System

Reminder titles and messages are selected from emotional templates in `ReinforcementMessageProvider`. The implemented behavior-based pools include:
- Consistent user: “You’re building something strong”
- Inactive user: “Your dragon is still waiting for you”
- Streak user: “Your consistency is rare”
- Fresh user: “Your dragon is ready when you are”

The previous harsh or commanding tone has been removed. Notifications no longer use messages like “Don’t break your streak” or “Your pet needs attention.” Instead, they use supportive language such as:
- “A small return is enough to wake the rhythm.”
- “Your dragon kept the hearth warm while you were away.”
- “This streak has weight. Your dragon notices.”

### Context-Based Selection

Reminder content is selected at schedule time using the latest context stored in `NotificationPrefs`.

Behavior context:
- `currentStreak >= 3`: streak reinforcement, including “Your consistency is rare”
- Inactive for more than 48 hours: patient re-entry language, including “Your dragon is still waiting for you”
- Recently active without a strong streak: consistent-user reinforcement, including “You’re building something strong”
- No activity context yet: fresh-start language, including “Your dragon is ready when you are”

Message rotation:
- Each behavior has its own message pool.
- `ReinforcementMessageProvider` stores the last used index per behavior in `reinforcement_message_prefs`.
- The next message in the current behavior pool is selected, wrapping back to the first message after the pool is exhausted.

Streak context:
- `currentStreak >= 7`: streak reinforcement
- `currentStreak > 0`: consistent or streak reinforcement depending on streak strength
- `currentStreak == 0`: fresh-start or inactive language depending on recent activity

Activity-frequency context:
- Pet reminders check the last active session timestamp.
- If the user has been inactive for more than 48 hours, the message becomes more patient and welcoming.
- If the user was recently active, the message reinforces existing progress instead of repeating a generic reminder.

### Cross-Surface Reinforcement

`ReinforcementMessageProvider` is shared beyond notifications:
- `ActivityTimelineScreen` appends a contextual reinforcement line to each timeline event.
- `RewardScreen` shows a contextual reinforcement line on every reward moment.
- Notification scheduling, timeline messages, and reward messages rotate within the same behavior pools so repeated surfaces do not feel robotic.

### Frequency Respect

The notification publisher checks `NotificationPrefs.shouldShowNotification()` before showing a scheduled reminder.

Default behavior:
- A reminder is skipped if the same notification was shown within the last 3 hours.
- Skipped reminders remain scheduled for the next daily alarm.
- The first notification after install or reset is always allowed.

This prevents repetitive reminders when the user has recently opened the app.

### Scheduling

- All reminders use `AlarmManager.setRepeating()` with `INTERVAL_DAY` (24 hours).
- Default times:
  * Dragon Waiting / Daily Reminder: 9:00 AM
  * Streak Encouragement / Streak Reminder: 8:00 PM
  * Pet Bond Reminder: 12:00 PM (noon)
- If the scheduled time has already passed today, the reminder is set for tomorrow.
- Reminders are canceled and rescheduled when updated to prevent duplicates.

### User Preferences

Each reminder type has an associated user preference stored in `SharedPreferences`:
- Default state: All reminders enabled (`true`)
- Preferences persist across app sessions
- Users can enable/disable each reminder type independently via the settings UI
- Preferences are initialized with default values on first app launch

## Configuration

Notification system configuration values:
- **Channel ID**: `habit_pet_reminders`
- **Channel Name**: `Habit Pet Reminders`
- **Channel Description**: `Reminders for habits, streaks, and pet care`
- **Request Codes**:
  * `DAILY_REMINDER_REQUEST_CODE`: 1001
  * `STREAK_REMINDER_REQUEST_CODE`: 1002
  * `PET_REMINDER_REQUEST_CODE`: 1003
- **Default Times** (hours:minutes in 24-hour format):
  * Daily / Dragon Waiting: 09:00
  * Streak Encouragement: 20:00 (8:00 PM)
  * Pet Bond Reminder: 12:00 (noon)
- **Default Preference State**: All reminders enabled (`true`)
- **Shared Preferences File**: `habit_pet_notification_prefs`
- **Default Minimum Notification Interval**: 3 hours

## Data Model

### Shared Preferences (`habit_pet_notification_prefs`)

Keys:
- `daily_reminder_enabled`: Boolean
- `streak_reminder_enabled`: Boolean
- `pet_reminder_enabled`: Boolean
- `last_streak`: Int
- `last_notification_at`: Long
- `last_active_session_timestamp`: Long

### Reinforcement Message Preferences (`reinforcement_message_prefs`)

Keys:
- `last_consistent_message_index`: Int
- `last_inactive_message_index`: Int
- `last_streak_message_index`: Int
- `last_fresh_message_index`: Int

### Fallback Activity Timestamp

`NotificationPrefs.getLastActiveSessionTimestamp()` also reads `last_active_session_timestamp` from `activity_timeline_engine` preferences when the notification preferences do not yet contain a local active-session timestamp.

## Source Files

- `app/src/main/java/com/example/mobile/util/NotificationHelper.kt`
- `app/src/main/java/com/example/mobile/util/NotificationPrefs.kt`
- `app/src/main/java/com/example/mobile/util/ReinforcementMessageProvider.kt`
- `app/src/main/java/com/example/mobile/util/BootCompletedReceiver.kt`
- `app/src/main/java/com/example/mobile/HabitPetApp.kt`
- `app/src/main/java/com/example/mobile/MainActivity.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/screens/ActivityTimelineScreen.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/reward/RewardScreen.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/screens/NotificationSettingsScreen.kt`
- `app/src/main/java/com/example/mobile/presentation/viewmodel/NotificationSettingsViewModel.kt`

## Known Gaps

1. Reminder content is selected when the alarm is scheduled. If streak changes after scheduling, the next scheduling cycle will use the updated streak.
2. The settings UI exposes reminder types, but not custom scheduling times.
3. Notifications open `MainActivity`; they do not include quick-action buttons for habit completion or pet care.
4. Notification importance remains `IMPORTANCE_DEFAULT`; users cannot configure bypass or priority behavior.
5. Activity-frequency suppression is local to the device and does not coordinate across multiple installed devices.
