package com.selfstudyclub.model;

import com.selfstudyclub.security.AccountStatus;

import java.time.LocalDateTime;

public record Account(long accountId, String email, String passwordHash,
                      AccountStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {}
