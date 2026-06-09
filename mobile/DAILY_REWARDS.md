# DAILY_REWARDS

## Overview

Habit Pet does not currently implement a standalone daily login bonus or daily reward system that provides rewards simply for opening the app each day. Daily progression is tied to activity-based systems rather than calendar-based rewards.

## Current Implementation

While there are several systems that operate on a daily basis, all reward mechanisms require player activity:
- Streak-based rewards require completing habits on consecutive days
- Achievement-based rewards require reaching specific milestones
- Progress-based rewards (XP, coins) require habit completion
- No rewards are granted solely based on calendar date or app opening

## Rules

### Activity-Based Daily Systems
All daily progression systems require player activity:
1. **Streak System**: Requires completing all habits each day to maintain/increase streak
2. **Journal System**: Logs activities based on actual habit completion
3. **Statistics Tracking**: daysActive counts days with at least one habit completion

### Absence of Calendar-Based Rewards
No systems exist that:
- Grant rewards for simply opening the app
- Provide login bonuses or daily gifts
- Reset daily to provide new reward opportunities
- Award based solely on date changes without activity requirements

## Configuration

Not applicable - no daily reward system exists to configure.

## Data Model

Not applicable - no daily reward system data model exists.

## Source Files

Not applicable - no daily reward system implementation exists.

## Known Gaps

1. **No Daily Login Bonus**: The game lacks a system to reward players for daily engagement regardless of activity level.
2. **Activity Dependency**: All progression requires active habit completion; passive engagement yields no rewards.
3. **No Daily Reset Mechanics**: Unlike many games, there are no daily reset cycles that refresh available rewards or challenges.
4. **Limited Daily Variation**: Daily experience does not change based on calendar date (no daily events, rotating bonuses, etc.).
5. **Missed Engagement Opportunity**: No mechanism to encourage daily app opens outside of habit completion motivation.