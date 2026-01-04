package com.selfstudyclub.dao;

import com.selfstudyclub.model.Course;
import java.util.List;

public interface CourseDao {
    long createCourse(String title, String description, long createdBy);
    void softDeleteCourse(long courseId, long requesterId);
    List<Course> listAll(int page, int pageSize);
    List<Course> listByCreator(long createdBy, int page, int pageSize);
    long countAll();
    long countByCreator(long createdBy);
}
