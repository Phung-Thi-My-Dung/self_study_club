package com.selfstudyclub.dao;

public interface EnrollmentDao {
    void enrollActive(long accountId, long courseId);
    boolean isActiveEnrolled(long accountId, long courseId);
}
