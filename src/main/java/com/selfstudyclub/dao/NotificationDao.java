package com.selfstudyclub.dao;

import com.selfstudyclub.model.Notification;
import java.util.List;

public interface NotificationDao {
    long createNotification(String title, String body, String linkUrl, long createdBy);
    List<Notification> listLatest(int limit);
}
