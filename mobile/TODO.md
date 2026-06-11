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

## Implementation Queue

### 1. Combo / Momentum System

**Read**

- EXP.md
- ECONOMY.md

**Update**

- EXP.md

**Tasks**

- [x] Implement combo system for consecutive habit completions
- [x] Increase XP slightly based on streaked activity / short-term momentum
- [x] Reset combo after inactivity window using a configurable time threshold
- [x] Show subtle combo multiplier feedback in UI
- [x] Ensure combo is additive but not overpowered
- [x] Log combo milestones in Activity Timeline system

---

### 2. Positive Reinforcement Message System

**Read**

- NOTIFICATIONS.md

**Update**

- NOTIFICATIONS.md

**Tasks**

- [ ] Replace generic system messages with emotional reinforcement messages
- [ ] Add dynamic message pool based on user behavior:
- [ ] Consistent user: “You’re building something strong”
- [ ] Inactive user: “Your dragon is still waiting for you”
- [ ] Streak user: “Your consistency is rare”
- [ ] Ensure messages feel human and supportive, not robotic
- [ ] Integrate reinforcement messages into:
- [ ] Notifications
- [ ] Activity timeline
- [ ] Reward screens
- [ ] Ensure message selection is contextual and non-repetitive

---

### 3. Reward Moment Amplification System

**Read**

- ECONOMY.md
- CHEST_REWARDS.md

**Update**

- ECONOMY.md

**Tasks**

- [ ] Enhance all reward moments: XP, coins, chest, achievement
- [ ] Add visual emphasis states: scale, glow, pause effect logic only
- [ ] Ensure reward moments briefly interrupt flow positively
- [ ] Differentiate reward tiers:
- [ ] Small reward: subtle feedback
- [ ] Rare reward: stronger feedback
- [ ] Epic reward: full emphasis moment
- [ ] Ensure reward feedback never feels repetitive or annoying
- [ ] Integrate with existing reward pipelines without blocking logic

