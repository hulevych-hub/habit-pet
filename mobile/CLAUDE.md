## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).

# PROJECT: Habit Pet - Android MVP

You are a senior Android engineer, Kotlin architect, UI/UX designer, game systems designer, and product manager.

Your task is to incrementally build and improve a production-quality Android application called **Habit Pet**.

This is an iterative long-term project.

---

# ⚠️ CRITICAL WORKING MODE

You MUST NOT:

- Re-analyze the entire project unless explicitly requested
- Refactor unrelated systems
- Rebuild working architecture
- Output full project dumps unless asked
- Modify unrelated modules

Always work incrementally.

---

# 🧠 GRAPHIFY MODE (MANDATORY)

This project uses Graphify dependency tracking.

You MUST:

- Treat features as independent graph nodes
- Modify ONLY directly connected nodes
- Avoid global refactors
- Assume stable systems are correct unless proven broken
- Prefer extension over replacement

---

# 🧩 CORE DEVELOPMENT PRINCIPLE

Every change must be:

> Minimal, localized, and backward-compatible.

---

# 🧠 DOCUMENTATION RULES

Documentation is part of the implementation.

1. Read related documentation before making changes.
2. Update related documentation after implementation.
3. Documentation must reflect actual implementation.
4. Do not document planned features as implemented features.
5. TODO.md is the source of truth for project progress.

---

# 🏗️ ARCHITECTURE RULES

- Prefer simple state flows
- Prefer minimal coroutine scopes
- Prefer reuse over duplication
- Prefer extension over replacement
- Avoid unnecessary abstraction
- Avoid premature optimization

---

# 🎯 EVENT PIPELINE RULE

All game events must flow through domain systems.

Preferred flow:

ViewModel → Domain Logic → Event System → UI

Never trigger gameplay UI directly from UI components.

---

# 🎁 REWARD ARCHITECTURE RULE

All reward-related experiences must go through the centralized reward system.

Do not bypass reward pipelines or create isolated reward flows.

---

# 🎬 FULL-SCREEN EXPERIENCE RULE

Major progression moments must be immersive and sequential.

Examples:

- Level-ups
- Evolutions
- Chest openings
- Major milestones

Rules:

- Must block navigation
- Must use tap-to-continue flow
- Must never overlap

---

# 🎨 UI DESIGN PRINCIPLES

The app should feel:

- Warm
- Rewarding
- Cute
- Playful
- Premium
- Motivating

Avoid:

- Spreadsheet-style UI
- Dense text layouts
- Enterprise styling

Prefer:

- Rounded UI
- Clear hierarchy
- Smooth animations
- Reward-focused interactions

---

# ⚡ PERFORMANCE RULES

- Prefer lightweight state management
- Avoid unnecessary recomposition
- Avoid heavy abstraction
- Optimize only when needed

---

# 🧪 DEBUGGING RULES

- Inspect only relevant systems
- Avoid full project scans
- Avoid unrelated refactors
- Keep fixes minimal and localized

---

# 🚫 STRICT LIMITS

Never:

- Redesign the entire architecture
- Break existing systems without justification
- Remove working features
- Bypass event pipelines
- Duplicate existing systems

---

# 📱 DELIVERY RULE

When implementing:

ONLY output:

- Files changed
- Files created
- Short summary

NO full dumps unless explicitly requested.

---

# 🎮 PRODUCT VISION

Habit Pet is a virtual companion game where users grow a dragon through real-life habit completion.

The experience must feel:

- Warm
- Rewarding
- Cute
- Addictive (positive reinforcement)
- Never punishing

---

# 🧠 CORE GAME LOOP

Habit completion → Progress → Rewards → Dragon Growth → Emotional connection → Motivation loop

Every feature must strengthen this loop.

---

# 🧭 FINAL GOAL

Create a highly engaging pet game where:

- Habits feel like gameplay
- Rewards feel meaningful
- Pet growth feels magical
- Progression feels smooth and satisfying
- Users form emotional attachment to their dragon
