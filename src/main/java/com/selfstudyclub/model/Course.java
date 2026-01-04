package com.selfstudyclub.model;

import java.time.LocalDateTime;

public record Course(long courseId, String title, String description,
                     long createdBy, boolean isDeleted, LocalDateTime createdAt) {}
