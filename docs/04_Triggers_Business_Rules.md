# 04 — Triggers & Business Rules

The database enforces “hard” rules to match the assignment requirements.

## Trigger A — Only ADMIN can create notifications
Table: `notifications` (AFTER INSERT)

Rule:
- `created_by` must have role `ADMIN`.
- If not, the insert is rolled back with an error.

## Trigger B — Limit MEMBER course creation
Table: `courses` (AFTER INSERT)

Rule:
- If creator is MEMBER:
  - max courses = `user_course_limits.max_courses` if exists
  - else default = `app_settings.DEFAULT_MEMBER_COURSE_LIMIT` (seeded as 5)
- Count only `is_deleted = 0` courses.
- If count exceeds limit after insert => rollback.

Notes:
- ADMIN is not restricted.
- Recommended: keep admin accounts with ADMIN role only.

## Trigger C — Completion requires ACTIVE enrollment
Table: `task_completions` (AFTER INSERT)

Rule:
- For each inserted completion:
  - Find the course of the task
  - Require an ACTIVE enrollment row for (account_id, course_id)
- If not enrolled => rollback.

## Streak Clarification
- **Streak event** = completion record
- Leaderboards:
  - Total completions
  - Total active days (distinct completed_date)
