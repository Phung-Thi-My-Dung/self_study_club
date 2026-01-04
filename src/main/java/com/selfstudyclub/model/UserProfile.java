package com.selfstudyclub.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserProfile(long accountId, String fullName, LocalDate dateOfBirth, String gender,
                          String phone, String address, String bio, LocalDate joinedAt, LocalDateTime updatedAt) {}
