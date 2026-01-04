package com.selfstudyclub.dao;

import java.time.LocalDate;
import java.util.Set;

public interface TaskCompletionDao {
    long createCompletion(long accountId, long taskId, LocalDate completedDate, String note);
    Set<LocalDate> activeDaysInMonth(long accountId, long courseId, int year, int month);
}
