package com.selfstudyclub.service;

import com.selfstudyclub.dao.TaskDao;
import com.selfstudyclub.model.Task;

import java.util.List;

public class TaskService {
    private final TaskDao dao;
    public TaskService(TaskDao dao) { this.dao = dao; }

    public long create(long courseId, String title, String description, long createdBy) {
        return dao.createTask(courseId, title, description, createdBy);
    }

    public List<Task> listByCourse(long courseId, int page, int pageSize) {
        return dao.listByCourse(courseId, page, pageSize);
    }
}
