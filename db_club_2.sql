/* =========================================================
   SELF-STUDY CLUB (MS SQL Server) - SIMPLE SUBMISSION VERSION
   - Only 2 roles: ADMIN / MEMBER
   - No PERMISSION tables (removed)
   - STREAK = TASK_COMPLETION (1 completion = 1 streak event)
   - One day can have many streaks (many tasks)
   - Only ADMIN can create notifications (trigger)
   - Completion requires ACTIVE enrollment in course (trigger)
   - Member course creation limit (trigger, default 5)
   ========================================================= */

--- Create database club test

CREATE DATABASE club_test;
GO

USE club_test;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================
    1) ROLE + USER_ROLE (simplified)
   ========================= */
CREATE TABLE roles (
    role_id    INT IDENTITY(1,1) PRIMARY KEY,
    role_code  VARCHAR(50) NOT NULL UNIQUE,     -- ADMIN / MEMBER
    role_name  NVARCHAR(100) NOT NULL
);
GO

CREATE TABLE accounts (
    account_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    email          NVARCHAR(320) NOT NULL UNIQUE,
    password_hash  NVARCHAR(255) NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE/LOCKED/DELETED
    created_at     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT CK_accounts_status CHECK (status IN ('ACTIVE','LOCKED','DELETED'))
);
GO

CREATE TABLE user_roles (
    account_id BIGINT NOT NULL,
    role_id    INT NOT NULL,
    PRIMARY KEY (account_id, role_id),
    CONSTRAINT FK_ur_account FOREIGN KEY (account_id) REFERENCES accounts(account_id),
    CONSTRAINT FK_ur_role    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);
GO

CREATE TABLE user_profiles (
    account_id    BIGINT PRIMARY KEY,
    full_name     NVARCHAR(150) NULL,
    date_of_birth DATE NULL,
    gender        VARCHAR(20) NULL,              -- MALE/FEMALE/OTHER
    phone         NVARCHAR(30) NULL,
    address       NVARCHAR(255) NULL,
    bio           NVARCHAR(1000) NULL,
    joined_at     DATE NULL,
    updated_at    DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_profile_account FOREIGN KEY (account_id) REFERENCES accounts(account_id),
    CONSTRAINT CK_profile_gender CHECK (gender IS NULL OR gender IN ('MALE','FEMALE','OTHER'))
);
GO

CREATE INDEX IX_accounts_status ON accounts(status);
GO

/* =========================
   2) SETTINGS (member course limit)
   ========================= */
CREATE TABLE app_settings (
    setting_key   VARCHAR(100) PRIMARY KEY,
    setting_value NVARCHAR(500) NOT NULL
);
GO

CREATE TABLE user_course_limits (
    account_id  BIGINT PRIMARY KEY,
    max_courses INT NOT NULL,
    updated_by  BIGINT NULL,   -- admin account_id (optional)
    updated_at  DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_ucl_account FOREIGN KEY (account_id) REFERENCES accounts(account_id),
    CONSTRAINT FK_ucl_updated_by FOREIGN KEY (updated_by) REFERENCES accounts(account_id),
    CONSTRAINT CK_ucl_max CHECK (max_courses >= 0)
);
GO

/* =========================
   3) COURSE / ENROLLMENT
   ========================= */
CREATE TABLE courses (
    course_id    BIGINT IDENTITY(1,1) PRIMARY KEY,
    title        NVARCHAR(200) NOT NULL,
    description  NVARCHAR(2000) NULL,
    created_by   BIGINT NOT NULL,
    is_deleted   BIT NOT NULL DEFAULT 0,
    created_at   DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at   DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_courses_creator FOREIGN KEY (created_by) REFERENCES accounts(account_id)
);
GO

CREATE INDEX IX_courses_creator ON courses(created_by) INCLUDE (is_deleted);
GO

CREATE TABLE enrollments (
    enrollment_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    account_id    BIGINT NOT NULL,
    course_id     BIGINT NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE/LEFT
    start_date    DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    end_date      DATE NULL,
    created_at    DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_enr_account FOREIGN KEY (account_id) REFERENCES accounts(account_id),
    CONSTRAINT FK_enr_course  FOREIGN KEY (course_id) REFERENCES courses(course_id),
    CONSTRAINT CK_enr_status CHECK (status IN ('ACTIVE','LEFT'))
);
GO

-- 1 user chỉ có 1 enrollment ACTIVE cho 1 course
CREATE UNIQUE INDEX UX_enr_active
ON enrollments(account_id, course_id)
WHERE status = 'ACTIVE';
GO

/* =========================
   4) TASK / TASK_COMPLETION (STREAK events)
   ========================= */
CREATE TABLE tasks (
    task_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    course_id   BIGINT NOT NULL,
    title       NVARCHAR(200) NOT NULL,
    description NVARCHAR(2000) NULL,
    created_by  BIGINT NOT NULL,
    is_deleted  BIT NOT NULL DEFAULT 0,
    created_at  DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at  DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_tasks_course FOREIGN KEY (course_id) REFERENCES courses(course_id),
    CONSTRAINT FK_tasks_creator FOREIGN KEY (created_by) REFERENCES accounts(account_id)
);
GO

CREATE INDEX IX_tasks_course ON tasks(course_id) INCLUDE (is_deleted);
GO

-- TASK_COMPLETION = STREAK EVENT
CREATE TABLE task_completions (
    completion_id  BIGINT IDENTITY(1,1) PRIMARY KEY,
    account_id     BIGINT NOT NULL,
    task_id        BIGINT NOT NULL,
    completed_date DATE NOT NULL, -- date for GitHub calendar + active days
    note           NVARCHAR(500) NULL,
    created_at     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_tc_account FOREIGN KEY (account_id) REFERENCES accounts(account_id),
    CONSTRAINT FK_tc_task    FOREIGN KEY (task_id) REFERENCES tasks(task_id),
    CONSTRAINT CK_tc_date CHECK (completed_date <= CAST(GETDATE() AS DATE))
);
GO

-- Prevent duplicate completion of same task in same day
CREATE UNIQUE INDEX UX_tc_user_task_day
ON task_completions(account_id, task_id, completed_date);
GO

CREATE INDEX IX_tc_user_date ON task_completions(account_id, completed_date) INCLUDE (task_id);
GO

/* =========================
   5) NOTIFICATION (admin create; member read)
   ========================= */
CREATE TABLE notifications (
    notification_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title           NVARCHAR(200) NOT NULL,
    body            NVARCHAR(MAX) NOT NULL,
    link_url        NVARCHAR(500) NULL,
    created_by      BIGINT NOT NULL, -- must be ADMIN (trigger)
    created_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_notif_creator FOREIGN KEY (created_by) REFERENCES accounts(account_id)
);
GO

CREATE TABLE notification_reads (
    notification_id BIGINT NOT NULL,
    account_id      BIGINT NOT NULL,
    read_at         DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (notification_id, account_id),
    CONSTRAINT FK_nr_notif   FOREIGN KEY (notification_id) REFERENCES notifications(notification_id),
    CONSTRAINT FK_nr_account FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);
GO

CREATE INDEX IX_notifications_created_at ON notifications(created_at);
GO



/* =========================
   6) SEED MINIMUM
   ========================= */
IF NOT EXISTS (SELECT 1 FROM roles WHERE role_code = 'ADMIN')
    INSERT INTO roles(role_code, role_name) VALUES ('ADMIN', N'Administrator');

IF NOT EXISTS (SELECT 1 FROM roles WHERE role_code = 'MEMBER')
    INSERT INTO roles(role_code, role_name) VALUES ('MEMBER', N'Member');

IF NOT EXISTS (SELECT 1 FROM app_settings WHERE setting_key = 'DEFAULT_MEMBER_COURSE_LIMIT')
    INSERT INTO app_settings(setting_key, setting_value) VALUES ('DEFAULT_MEMBER_COURSE_LIMIT', '5');
GO




/* =========================
   7) TRIGGERS (hard rules)
   ========================= */

-- Helper: check if an account is ADMIN
-- (Used inside triggers via EXISTS join roles)
GO

-- (A) Only ADMIN can create notifications
CREATE OR ALTER TRIGGER trg_notifications_admin_only
ON notifications
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        WHERE NOT EXISTS (
            SELECT 1
            FROM user_roles ur
            JOIN roles r ON r.role_id = ur.role_id
            WHERE ur.account_id = i.created_by
            AND r.role_code = 'ADMIN'
        )
    )
    BEGIN
        RAISERROR('Only ADMIN can create notifications.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
END
GO

-- (B) Limit MEMBER course creation (default 5, override in user_course_limits)
CREATE OR ALTER TRIGGER trg_courses_member_limit
ON courses
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- Default limit from app_settings (if missing => 5)
    DECLARE @default_limit INT =
        TRY_CAST((SELECT setting_value
                FROM app_settings
                WHERE setting_key = 'DEFAULT_MEMBER_COURSE_LIMIT') AS INT);
    IF @default_limit IS NULL SET @default_limit = 5;

    -- Cache MEMBER role_id to avoid repeated joins
    DECLARE @member_role_id INT = (SELECT role_id FROM roles WHERE role_code = 'MEMBER');
    IF @member_role_id IS NULL RETURN; -- safety (seed should ensure it's there)

    -- Check if any MEMBER creator exceeds their limit after this insert
    IF EXISTS (
        SELECT 1
        FROM (
            SELECT DISTINCT created_by AS account_id
            FROM inserted
        ) i
        JOIN user_roles ur
        ON ur.account_id = i.account_id
        AND ur.role_id = @member_role_id
        CROSS APPLY (
            -- limit_value: per-user override, else default
            SELECT limit_value =
                COALESCE(
                    (SELECT max_courses FROM user_course_limits WHERE account_id = i.account_id),
                    @default_limit
                )
        ) L
        CROSS APPLY (
            -- course_count: count non-deleted courses created by this user
            SELECT course_count =
                (SELECT COUNT(*) FROM courses c
                WHERE c.created_by = i.account_id AND c.is_deleted = 0)
        ) C
        WHERE C.course_count > L.limit_value
    )
    BEGIN
        RAISERROR('Course limit exceeded for MEMBER.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
END
GO


-- (C) Completion requires ACTIVE enrollment in the task's course
CREATE OR ALTER TRIGGER trg_task_completion_requires_enrollment
ON task_completions
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN tasks t ON t.task_id = i.task_id
        LEFT JOIN enrollments e
        ON e.account_id = i.account_id
            AND e.course_id  = t.course_id
            AND e.status     = 'ACTIVE'
        WHERE e.enrollment_id IS NULL
    )
    BEGIN
        RAISERROR('Must be ACTIVE enrolled in the course to submit completion.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
END
GO