package com.selfstudyclub.dao;

import java.util.Set;

public interface NotificationReadDao {
    void markRead(long notificationId, long accountId);
    Set<Long> readNotificationIds(long accountId);
}
