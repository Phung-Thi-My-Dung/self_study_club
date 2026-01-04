-- =========================================================
-- 0) SCHEMA BASICS
-- =========================================================



SELECT 
  a.account_id,
  a.email,
  a.password_hash,   -- hashed, not plain password
  a.status,
  a.created_at
FROM accounts a
JOIN user_roles ur ON ur.account_id = a.account_id
JOIN roles r ON r.role_id = ur.role_id
WHERE r.role_code = 'ADMIN';



/* Ensure roles exist (ADMIN / MEMBER) */
IF NOT EXISTS (SELECT 1 FROM roles WHERE role_code = 'ADMIN')
    INSERT INTO roles(role_code, role_name) VALUES ('ADMIN', N'Administrator');

IF NOT EXISTS (SELECT 1 FROM roles WHERE role_code = 'MEMBER')
    INSERT INTO roles(role_code, role_name) VALUES ('MEMBER', N'Member');
GO

/* Create ADMIN account if not exists */
IF NOT EXISTS (SELECT 1 FROM accounts WHERE email = N'admin@gmail.com')
BEGIN
    INSERT INTO accounts(email, password_hash, status)
    VALUES (
        N'admin@gmail.com',
        -- BCrypt hash for plain password: "admin"
        N'$2a$12$uvK3934AedzbLxHA1EM5TONOkcwwQfB9i57XAiQRzqBUZ5xyvRLFO',
        'ACTIVE'
    );
END
GO

/* Assign ADMIN role (only ADMIN, to avoid MEMBER course-limit trigger) */
DECLARE @admin_id BIGINT = (SELECT account_id FROM accounts WHERE email = N'admin@gmail.com');
DECLARE @role_admin INT  = (SELECT role_id FROM roles WHERE role_code = 'ADMIN');
DECLARE @role_member INT = (SELECT role_id FROM roles WHERE role_code = 'MEMBER');

IF NOT EXISTS (
    SELECT 1 FROM user_roles
    WHERE account_id = @admin_id AND role_id = @role_admin
)
BEGIN
    INSERT INTO user_roles(account_id, role_id) VALUES (@admin_id, @role_admin);
END

-- Optional: remove MEMBER role if it was assigned before (avoid MEMBER limits)
IF EXISTS (
    SELECT 1 FROM user_roles
    WHERE account_id = @admin_id AND role_id = @role_member
)
BEGIN
    DELETE FROM user_roles
    WHERE account_id = @admin_id AND role_id = @role_member;
END
GO

/* Optional profile row */
IF NOT EXISTS (SELECT 1 FROM user_profiles WHERE account_id = @admin_id)
BEGIN
    INSERT INTO user_profiles(account_id, full_name, joined_at)
    VALUES (@admin_id, N'Admin', CAST(GETDATE() AS DATE));
END
GO




SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

-- =========================================================
-- 1) ROLES / PERMISSIONS (RBAC nhẹ, dễ mở rộng)
-- =========================================================
CREATE TABLE roles (
    role_id        INT IDENTITY(1,1) PRIMARY KEY,
    role_code      VARCHAR(50) NOT NULL UNIQUE,   -- 'ADMIN', 'MEMBER'
    role_name      NVARCHAR(100) NOT NULL
);

CREATE TABLE permissions (
    permission_id  INT IDENTITY(1,1) PRIMARY KEY,
    perm_code      VARCHAR(100) NOT NULL UNIQUE,
    perm_name      NVARCHAR(200) NOT NULL
);

CREATE TABLE role_permissions (
    role_id        INT NOT NULL,
    permission_id  INT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT FK_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(role_id),
    CONSTRAINT FK_role_permissions_perm FOREIGN KEY (permission_id) REFERENCES permissions(permission_id)
);

-- =========================================================
-- 2) USERS / AUTH
-- =========================================================
CREATE TABLE users (
    user_id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    name               NVARCHAR(320) NOT NULL,
    password_hash      NVARCHAR(255) NOT NULL,
    status             VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE/LOCKED/DELETED
    status_login       INT NOT NULL DEFAULT 0, -- Đăng nhập rồi hay chưa nếu rồi thì Login bằng Name
    created_at         DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at         DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    deleted_at         DATETIME2 NULL,

    CONSTRAINT UQ_users_name UNIQUE(name),
    CONSTRAINT CK_users_status CHECK (status IN ('ACTIVE','LOCKED','DELETED'))
);

CREATE TABLE user_roles (
    user_id  BIGINT NOT NULL,
    role_id  INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT FK_user_roles_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT FK_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

CREATE TABLE user_profiles (
    user_id      BIGINT PRIMARY KEY,
    full_name    NVARCHAR(150) NULL,
    date_of_birth DATE NULL,
    gender       VARCHAR(20) NULL, -- MALE/FEMALE/OTHER/NULL
    avatar_path  NVARCHAR(500) NULL,
    joined_at    DATE NULL,
    updated_at   DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_user_profiles_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT CK_user_profiles_gender CHECK (gender IS NULL OR gender IN ('MALE','FEMALE','OTHER'))
);


-- =========================================================
-- 3) SETTINGS / LIMITS
-- =========================================================
CREATE TABLE app_settings (
    setting_key   VARCHAR(100) PRIMARY KEY,
    setting_value NVARCHAR(500) NOT NULL,
    updated_at    DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);

--- example: ("Nhat": "100")
--- select * from app_settings where setting_key = 'Nhat';
--- example: ('PASSWORD_RESET_TOKEN_EXPIRY_MINUTES','30');
--- Example: ('DEFAULT_MEMBER_COURSE_LIMIT','5')

CREATE TABLE user_course_limits (
    user_id     BIGINT PRIMARY KEY,
    max_courses INT NOT NULL DEFAULT 5,
    updated_by  BIGINT NULL,   -- admin user_id
    updated_at  DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_ucl_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT FK_ucl_admin FOREIGN KEY (updated_by) REFERENCES users(user_id),
    CONSTRAINT CK_ucl_max CHECK (max_courses >= 0)
);

-- =========================================================
-- 4) COURSES / ENROLLMENTS
-- =========================================================
CREATE TABLE courses (
    course_id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    title           NVARCHAR(200) NOT NULL,
    description     NVARCHAR(2000) NULL,
    created_by      BIGINT NOT NULL,
    is_active       BIT NOT NULL DEFAULT 1,
    is_deleted      BIT NOT NULL DEFAULT 0,
    created_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    deleted_at      DATETIME2 NULL,
    CONSTRAINT FK_courses_creator FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE course_enrollments (
    enrollment_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    course_id     BIGINT NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE/LEFT
    start_date    DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    end_date      DATE NULL,
    created_at    DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_ce_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT FK_ce_course FOREIGN KEY (course_id) REFERENCES courses(course_id),
    CONSTRAINT CK_ce_status CHECK (status IN ('ACTIVE','LEFT'))
);

-- User cannot enroll same course twice as ACTIVE
CREATE UNIQUE INDEX UX_course_enrollments_active
ON course_enrollments(user_id, course_id)
WHERE status = 'ACTIVE';

-- =========================================================
-- 5) STREAKS
-- =========================================================
CREATE TABLE streak_submissions (
    streak_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    course_id     BIGINT NOT NULL,
    streak_date   DATE NOT NULL,
    note          NVARCHAR(500) NULL,
    evidence_link NVARCHAR(500) NULL,
    created_at    DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    created_by    BIGINT NOT NULL, -- who performed: usually = user_id, or admin if adjusted add
    source        VARCHAR(20) NOT NULL DEFAULT 'USER', -- USER/ADMIN
    CONSTRAINT FK_ss_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT FK_ss_course FOREIGN KEY (course_id) REFERENCES courses(course_id),
    CONSTRAINT FK_ss_actor FOREIGN KEY (created_by) REFERENCES users(user_id),
    CONSTRAINT CK_ss_source CHECK (source IN ('USER','ADMIN')),
    CONSTRAINT CK_ss_date CHECK (streak_date <= CAST(GETDATE() AS DATE))
);

-- 1 user - 1 course - 1 streak per day
CREATE UNIQUE INDEX UX_streak_unique_day
ON streak_submissions(user_id, course_id, streak_date);

-- Admin adjustments audit (không bắt buộc nhưng rất nên có)
CREATE TABLE streak_adjustments (
    adjustment_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    admin_id      BIGINT NOT NULL,
    user_id       BIGINT NOT NULL,
    course_id     BIGINT NOT NULL,
    streak_date   DATE NOT NULL,
    action        VARCHAR(20) NOT NULL, -- ADD/REMOVE
    reason        NVARCHAR(500) NOT NULL,
    created_at    DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_sa_admin FOREIGN KEY (admin_id) REFERENCES users(user_id),
    CONSTRAINT FK_sa_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT FK_sa_course FOREIGN KEY (course_id) REFERENCES courses(course_id),
    CONSTRAINT CK_sa_action CHECK (action IN ('ADD','REMOVE'))
);

-- =========================================================
-- 6) NOTIFICATIONS
-- =========================================================
CREATE TABLE notifications (
    notification_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title           NVARCHAR(200) NOT NULL,
    body            NVARCHAR(MAX) NOT NULL,
    link_url        NVARCHAR(500) NULL,
    created_by      BIGINT NOT NULL, -- admin
    created_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    is_active       BIT NOT NULL DEFAULT 1,
    CONSTRAINT FK_notif_admin FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE notification_reads (
    notification_id BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    read_at         DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (notification_id, user_id),
    CONSTRAINT FK_nr_notif FOREIGN KEY (notification_id) REFERENCES notifications(notification_id),
    CONSTRAINT FK_nr_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- =========================================================
-- 7) AUDIT LOGS
-- =========================================================
CREATE TABLE audit_logs (
    audit_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    actor_id     BIGINT NULL,
    action       VARCHAR(50) NOT NULL,        -- e.g. LOGIN, CREATE_COURSE, ADJUST_STREAK
    entity_type  VARCHAR(50) NOT NULL,        -- USERS/COURSES/STREAKS/NOTIFS
    entity_id    NVARCHAR(100) NULL,
    old_data     NVARCHAR(MAX) NULL,          -- JSON text
    new_data     NVARCHAR(MAX) NULL,          -- JSON text
    created_at   DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_audit_actor FOREIGN KEY (actor_id) REFERENCES users(user_id)
);

-- =========================================================
-- 8) INDEXES (Performance)
-- =========================================================
CREATE INDEX IX_users_status ON users(status);
CREATE INDEX IX_users_name ON users(name);

CREATE INDEX IX_courses_creator ON courses(created_by) INCLUDE (is_active, is_deleted);
CREATE INDEX IX_courses_active ON courses(is_active, is_deleted);

CREATE INDEX IX_ss_course_date ON streak_submissions(course_id, streak_date) INCLUDE (user_id);
CREATE INDEX IX_ss_user_date   ON streak_submissions(user_id, streak_date) INCLUDE (course_id);

CREATE INDEX IX_notif_created ON notifications(created_at);
GO
