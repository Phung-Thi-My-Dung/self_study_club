package com.selfstudyclub.service;

import com.selfstudyclub.dao.NotificationDao;
import com.selfstudyclub.dao.NotificationReadDao;
import com.selfstudyclub.model.Notification;

import java.util.List;
import java.util.Set;

public class NotificationService {
    private final NotificationDao notifDao;
    private final NotificationReadDao readDao;

    public NotificationService(NotificationDao notifDao, NotificationReadDao readDao) {
        this.notifDao = notifDao;
        this.readDao = readDao;
    }

    public long create(String title, String body, String linkUrl, long createdBy) {
        return notifDao.createNotification(title, body, linkUrl, createdBy);
    }

    public List<Notification> latest(int limit) { return notifDao.listLatest(limit); }
    public Set<Long> readIds(long accountId) { return readDao.readNotificationIds(accountId); }
    public void markRead(long notificationId, long accountId) { readDao.markRead(notificationId, accountId); }
}
