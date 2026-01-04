package com.selfstudyclub.service;

import com.selfstudyclub.dao.TaskCompletionDao;

import java.time.LocalDate;
import java.util.Set;

public class StreakService {
    private final TaskCompletionDao dao;
    public StreakService(TaskCompletionDao dao) { this.dao = dao; }

    public void submitCompletionToday(long accountId, long taskId, String note) {
        dao.createCompletion(accountId, taskId, LocalDate.now(), note);
    }

    public Set<LocalDate> activeDaysInMonth(long accountId, long courseId, int year, int month) {
        return dao.activeDaysInMonth(accountId, courseId, year, month);
    }
}
