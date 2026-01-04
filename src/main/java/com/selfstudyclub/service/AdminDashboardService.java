package com.selfstudyclub.service;

import com.selfstudyclub.dao.AdminDashboardDao;
import com.selfstudyclub.model.DashboardStats;
import com.selfstudyclub.model.MemberRow;
import com.selfstudyclub.model.TopCourseRow;

import java.util.List;

public class AdminDashboardService {
    private final AdminDashboardDao dao;
    public AdminDashboardService(AdminDashboardDao dao) { this.dao = dao; }

    public DashboardStats stats() { return dao.loadStats(); }
    public List<TopCourseRow> top5CoursesToday() { return dao.top5CoursesToday(); }
    public List<MemberRow> membersActiveToday(int page, int pageSize) { return dao.membersActiveToday(page, pageSize); }
}
