# 01 — Project Overview

## Goal
Build a **desktop app** to manage a Self-Study Club where members create courses, complete tasks, and generate streak-based statistics.
The submission version focuses on a clean **database-centered** design and a maintainable Java Swing codebase.

## Key Concepts
- **2 roles only**: `ADMIN` and `MEMBER`
- **Streak event = task completion**
  - 1 completion = 1 streak event
  - 1 day can have **many** streak events (many tasks)
- **Notifications**
  - Only ADMIN can create
  - Members can read + mark as read
- **Database is the “source of truth”**
  - Important rules are enforced by **triggers**

## Main Modules
- Authentication (Login / Signup)
- Admin Dashboard (global statistics)
- Courses
- Tasks
- Task Completions (streak events)
- Leaderboard (Top 50)
- Profile (self edit)
- Notifications (admin create, member read)

## Non-Goals for the submission version
- Password reset flow (not required for teacher submission)
- Complex RBAC permissions tables (removed)
- Admin editing streak events (NOT allowed)
