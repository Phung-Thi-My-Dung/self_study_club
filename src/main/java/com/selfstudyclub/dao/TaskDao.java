package com.selfstudyclub.dao;

import com.selfstudyclub.model.Task;
import java.util.List;

public interface TaskDao {
    long createTask(long courseId, String title, String description, long createdBy);
    List<Task> listByCourse(long courseId, int page, int pageSize);
    long countByCourse(long courseId);
}
