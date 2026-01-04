package com.selfstudyclub.model;

import java.time.LocalDateTime;

public record Notification(long notificationId, String title, String body, String linkUrl,
                           long createdBy, LocalDateTime createdAt) {}
