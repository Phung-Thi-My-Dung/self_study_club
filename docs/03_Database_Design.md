# 03 — Database Design (MS SQL Server)

Database: `club_test`

## Tables Overview

### Identity & Roles
- `roles(role_id, role_code, role_name)`
- `accounts(account_id, email, password_hash, status, created_at, updated_at)`
- `user_roles(account_id, role_id)`

### Profiles
- `user_profiles(account_id, full_name, date_of_birth, gender, phone, address, bio, joined_at, updated_at)`

### Settings
- `app_settings(setting_key, setting_value)`
- `user_course_limits(account_id, max_courses, updated_by, updated_at)`

### Courses / Learning
- `courses(course_id, title, description, created_by, is_deleted, created_at, updated_at)`
- `enrollments(enrollment_id, account_id, course_id, status, start_date, end_date, created_at)`
  - Unique index: 1 ACTIVE enrollment per (account_id, course_id)

### Tasks + Streak Events
- `tasks(task_id, course_id, title, description, created_by, is_deleted, created_at, updated_at)`
- `task_completions(completion_id, account_id, task_id, completed_date, note, created_at)`
  - Unique index prevents duplicate completion of same task in same day

### Notifications
- `notifications(notification_id, title, body, link_url, created_by, created_at)`
- `notification_reads(notification_id, account_id, read_at)`

## Key Relationships (ER-style)
- accounts 1—* user_roles *—1 roles
- accounts 1—1 user_profiles
- accounts 1—* courses (created_by)
- accounts 1—* enrollments
- courses 1—* tasks
- tasks 1—* task_completions
- notifications 1—* notification_reads

## Indexes (important ones)
- `UX_enr_active`: active enrollment per user/course
- `UX_tc_user_task_day`: unique completion per user/task/day
- `IX_tc_user_date`: quick calendar/active day queries
- `IX_notifications_created_at`: latest notifications

## Password Storage
- `accounts.password_hash` stores **BCrypt hash**, never the plain password.
