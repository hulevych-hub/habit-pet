# NOTIFICATIONS

## Overview

The notification system in Habit Pet provides soft, emotional re-entry nudges that help users return to their habits and feel connected to their dragon. The system uses Android's `AlarmManager` to schedule recurring notifications and stores user preferences in `SharedPreferences`.

Notification content is template-driven instead of purely functional. Messages are selected from the current player context, including streak strength and recent app activity, so reminders feel supportive rather than demanding.

## Current Implementation

The notification system consists of:
- `NotificationHelper`: schedules, cancels, and shows reminders with emotional templates
- `NotificationPrefs`: stores reminder preferences, last known streak, last active session, and notification frequency state
- `NotificationPublisher`: receives scheduled alarms and shows notifications when frequency rules allow
- `BootCompletedReceiver`: reschedules reminders after device reboot
- `MainActivity`: records the current streak and active session so scheduled reminders can use player context
- Integration with `HabitPetApp` to create the notification channel and schedule reminders on startup

## Rules

### Notification Types

The system supports three recurring reminder types:
1. **Dragon Waiting**: a daily re-entry nudge
2. **Streak Encouragement**: a streak-focused reminder that adapts to low or high streak context
3. **Pet Bond Reminder**: a warm pet-care reminder that adapts to recent user activity

### Emotional Template System

Reminder titles and messages are selected from emotional templates. The implemented templates include:
- “Your dragon is waiting 🐉”
- “Something grew while you were away”
- “You’re close to a reward”

The previous harsh or commanding tone has been removed. Notifications no longer use messages like “Don’t break your streak” or “Your pet needs attention.” Instead, they use supportive language such as:
- “A small habit today is enough to keep your spark warm.”
- “Your dragon saved a little room for your next beginning.”
- “Your streak is shining. Your dragon is proud of the rhythm you’re building.”

### Context-Based Selection

Reminder content is selected at schedule time using the latest context stored in `NotificationPrefs`.

Streak context:
- `currentStreak >= 7`: reinforcement message, e.g. “You’re close to a reward”
- `currentStreak > 0`: gentle encouragement for maintaining momentum
- `currentStreak == 0`: low-streak encouragement and fresh-start language

Activity-frequency context:
- Pet reminders check the last active session timestamp.
- If the user has been inactive for more than 48 hours, the message becomes more patient and welcoming.
- If the user was recently active, the message reinforces existing progress instead of repeating a generic reminder.

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

### Fallback Activity Timestamp

`NotificationPrefs.getLastActiveSessionTimestamp()` also reads `last_active_session_timestamp` from `activity_timeline_engine` preferences when the notification preferences do not yet contain a local active-session timestamp.

## Source Files

- `app/src/main/java/com/example/mobile/util/NotificationHelper.kt`
- `app/src/main/java/com/example/mobile/util/NotificationPrefs.kt`
- `app/src/main/java/com/example/mobile/util/BootCompletedReceiver.kt`
- `app/src/main/java/com/example/mobile/HabitPetApp.kt`
- `app/src/main/java/com/example/mobile/MainActivity.kt`
- `app/src/main/java/com/example/mobile/presentation/ui/screens/NotificationSettingsScreen.kt`
- `app/src/main/java/com/example/mobile/presentation/viewmodel/NotificationSettingsViewModel.kt`

## Known Gaps

1. Reminder content is selected when the alarm is scheduled. If streak changes after scheduling, the next scheduling cycle will use the updated streak.
2. The settings UI exposes reminder types, but not custom scheduling times.
3. Notifications open `MainActivity`; they do not include quick-action buttons for habit completion or pet care.
4. Notification importance remains `IMPORTANCE_DEFAULT`; users cannot configure bypass or priority behavior.
5. Activity-frequency suppression is local to the device and does not coordinate across multiple installed devices.
