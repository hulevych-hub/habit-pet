# Habit Pet

A habit tracking mobile application with gamification elements, built with modern Android technologies.

## Overview

Habit Pet is an Android application that helps users build and maintain healthy habits through gamification. Users care for a virtual pet that grows and evolves as they complete their habits, providing motivation and engagement.

## Features

- **Habit Tracking**: Create and track daily habits with streak counting
- **Virtual Pet**: Care for a pet that evolves based on your habit consistency
- **Achievement System**: Unlock achievements for various milestones
- **Activity Timeline**: Persistent gameplay moments for habit completions, rewards, streaks, and achievements
- **Notifications**: Customizable reminders for habits, streaks, and pet care
- **Rewards Economy**: Earn coins for completing habits and use them to purchase cosmetic items
- **Modern Architecture**: Built with Clean Architecture, Hilt for dependency injection, Jetpack Compose for UI

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: Clean Architecture with MVVM
- **Dependency Injection**: Hilt
- **Database**: Room
- **Asynchronous Programming**: Kotlin Coroutines and Flow
- **Navigation**: Jetpack Navigation Compose
- **Notifications**: Android AlarmManager and NotificationManager

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/mobile/
│   │   │   ├── data/           # Data layer (Room database, DAOs, repositories)
│   │   │   ├── domain/         # Domain layer (business logic, use cases)
│   │   │   ├── di/             # Dependency injection modules
│   │   │   ├── presentation/   # Presentation layer (ViewModels, UI screens)
│   │   │   ├── navigation/     # Navigation configuration
│   │   │   ├── HabitPetApp.kt  # Hilt application class and startup initializers
│   │   │   └── util/           # Utility classes (notifications, preferences)
│   │   └── res/                # Resources (strings, layouts, drawables)
```

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 11
- Android SDK 21+ (minSdkVersion 21)

### Installation

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle dependencies
4. Run the application on an emulator or physical device

## Architecture Overview

The application follows Clean Architecture principles with three main layers:

1. **Data Layer**: Handles data persistence using Room database and provides repositories
2. **Domain Layer**: Contains business logic, use cases, and domain models
3. **Presentation Layer**: Manages UI state and presentation logic using ViewModels and Jetpack Compose

### Key Components

- **AchievementEngine**: Monitors game events and unlocks achievements when conditions are met
- **ActivityTimelineEngine**: Persists gameplay moments into the activity timeline
- **NotificationHelper**: Manages scheduling and displaying notifications
- **NotificationPrefs**: Handles user preferences for notification settings
- **BootCompletedReceiver**: Reschedules notifications after device reboot

## Development Phases

The project is organized into development phases:

1. **Phase 1: Foundation** - Project setup, database, basic navigation
2. **Phase 2: Core Systems** - Habit creation, completion, XP system
3. **Phase 3: Streak System** - Streak tracking and calculations
4. **Phase 4: Pet System** - Pet entity, XP, level, and evolution systems
5. **Phase 5: Pet Animation System** - Mood-based animations for pet
6. **Phase 6: Rewards and Economy** - Coin system, rewards, inventory
7. **Phase 7: Cosmetics** - Pet customization and visual enhancements
8. **Phase 8: Achievements and Activity Timeline** - Achievement system and persistent gameplay moments
9. **Phase 9: Notifications** - Reminder system with user preferences
10. **Phase 10: Production Polish** - Performance optimization, accessibility, documentation

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Inspired by habit tracking and virtual pet games
- Built with modern Android development practices