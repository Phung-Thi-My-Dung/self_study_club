package com.selfstudyclub.service;

import com.selfstudyclub.dao.CourseDao;
import com.selfstudyclub.dao.EnrollmentDao;
import com.selfstudyclub.model.Course;

import java.util.List;

/**
 * - DB trigger enforces MEMBER course creation limit.
 * - We auto-enroll course creator so they can submit streak (completion trigger).
 */
public class CourseService {
    private final CourseDao courseDao;
    private final EnrollmentDao enrollmentDao;

    public CourseService(CourseDao courseDao, EnrollmentDao enrollmentDao) {
        this.courseDao = courseDao;
        this.enrollmentDao = enrollmentDao;
    }

    public long createCourseAndEnroll(String title, String description, long createdBy) {
        long courseId = courseDao.createCourse(title, description, createdBy);
        enrollmentDao.enrollActive(createdBy, courseId);
        return courseId;
    }

    public List<Course> listAll(int page, int pageSize) { return courseDao.listAll(page, pageSize); }
    public List<Course> listMine(long accountId, int page, int pageSize) { return courseDao.listByCreator(accountId, page, pageSize); }
}
