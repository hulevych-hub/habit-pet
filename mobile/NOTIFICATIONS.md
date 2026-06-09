# NOTIFICATIONS

## Overview

The notification system in Habit Pet provides reminders to help users maintain their habit tracking streaks and remember to care for their pet. The system uses Android's AlarmManager to schedule recurring notifications and includes user-configurable preferences for each reminder type.

## Current Implementation

The notification system consists of:
- NotificationHelper: Main class for scheduling, canceling, and showing notifications
- NotificationPrefs: Helper for storing and retrieving user notification preferences
- NotificationPublisher: BroadcastReceiver that handles displaying notifications when alarms fire
- BootCompletedReceiver: BroadcastReceiver that reschedules notifications after device reboot
- Integration with HabitPetApp to initialize the notification system on app startup

## Rules

### Notification Types
The system supports three types of recurring reminders:
1. **Daily Reminder**: Prompts users to check their habits each day
2. **Streak Reminder**: Encourages users to maintain their habit completion streaks
3. **Pet Reminder**: Reminds users to care for their virtual pet

### Scheduling
- All reminders use `AlarmManager.setRepeating()` with `INTERVAL_DAY` (24 hours)
- Default times (configurable via parameters):
  * Daily Reminder: 9:00 AM
  * Streak Reminder: 8:00 PM
  * Pet Reminder: 12:00 PM (noon)
- If the scheduled time has already passed today, the reminder is set for tomorrow
- Reminders are canceled and rescheduled when updated to prevent duplicates

### User Preferences
Each reminder type has an associated user preference stored in SharedPreferences:
- Default state: All reminders enabled (true)
- Preferences persist across app sessions
- Users can enable/disable each reminder type independently via settings UI
- Preferences are initialized with default values on first app launch

### Notification Content
When a reminder triggers:
- NotificationPublisher receives the broadcast via AlarmManager
- Extracts title and message from Intent extras
- Shows notification via NotificationHelper.showNotification()
- Notification includes:
  * Small icon (app launcher icon)
  * Custom title and message
  * Tap-to-open MainActivity action
  * Auto-cancel when tapped

### Device Reboot Handling
- BootCompletedReceiver listens for `Intent.ACTION_BOOT_COMPLETED`
- On device boot, reschedules all three reminder types with default times
- Ensures reminders persist after device restart/power cycle

## Configuration

Notification system configuration values:
- **Channel ID**: "habit_pet_reminders"
- **Channel Name**: "Habit Pet Reminders"
- **Channel Description**: "Reminders for habits, streaks, and pet care"
- **Request Codes**:
  * DAILY_REMINDER_REQUEST_CODE: 1001
  * STREAK_REMINDER_REQUEST_CODE: 1002
  * PET_REMINDER_REQUEST_CODE: 1003
- **Default Times** (hours:minutes in 24-hour format):
  * Daily: 09:00
  * Streak: 20:00 (8:00 PM)
  * Pet: 12:00 (noon)
- **Default Preference State**: All reminders enabled (true)
- **Shared Preferences File**: "habit_pet_notification_prefs"

## Data Model

**Shared Preferences** (NotificationPrefs):
- File: `habit_pet_notification_prefs`
- Keys:
  * `daily_reminder_enabled`: Boolean
  * `streak_reminder_enabled`: Boolean
  * `pet_reminder_enabled`: Boolean

## Source Files

- app/src/main/java/com/example/mobile/util/NotificationHelper.kt
- app/src/main/java/com/example/mobile/util/NotificationPrefs.kt
- app/src/main/java/com/example/mobile/util/BootCompletedReceiver.kt
- app/src/main/java/com/example/mobile/HabitPetApp.kt (initialization)
- app/src/main/java/com/example/mobile/presentation/ui/screens/NotificationSettingsScreen.kt (UI)
- app/src/main/java/com/example/mobile/presentation/viewmodel/NotificationSettingsViewModel.kt (UI logic)

## Known Gaps

1. **Fixed Notification Content**: Reminder titles and messages are hardcoded in the scheduling methods; no customization available per reminder instance.
2. **Limited Scheduling Flexibility**: While times can be passed as parameters, the UI currently only uses default times; no time picker in settings.
3. **No Advanced Scheduling Options**: No support for weekday-specific reminders, interval customization, or complex recurrence patterns.
4. **Preference Initialization**: Default preferences are only set on first launch; no mechanism to update defaults in future app versions.
5. **Notification Importance**: Uses `IMPORTANCE_DEFAULT`; no user configuration for notification priority/bypass settings.
6. **Action Limitations**: Notifications only open the main app; no quick-action buttons for habit completion or pet care from the notification itself.