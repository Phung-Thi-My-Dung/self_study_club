# 02 — Roles & Permissions

## Roles
- `ADMIN`: club administrator
- `MEMBER`: standard club member

## Access Matrix (submission rules)

| Module | ADMIN | MEMBER |
|---|---:|---:|
| Login / Signup | ✅ | ✅ |
| Dashboard (global stats) | ✅ | ❌ |
| Member list / search / CRUD | (future) ✅ | ❌ |
| Courses | View all ✅ | View own ✅ |
| Create course | ✅ | ✅ (<= limit) |
| Tasks | ✅ | ✅ |
| Submit completion (streak event) | ✅ (if enrolled) | ✅ (if enrolled) |
| Leaderboard | ✅ | ✅ |
| Profile view | ✅ (self only in app) | ✅ |
| Profile edit | ❌ | ✅ (self only) |
| Notifications create | ✅ | ❌ |
| Notifications read / mark read | ✅ | ✅ |

## Enforcement Strategy
- **UI** hides admin-only screens for members
- **DB triggers** enforce hard rules:
  - Only ADMIN can create notifications
  - MEMBER course creation limit
  - Completion requires active enrollment in the course
