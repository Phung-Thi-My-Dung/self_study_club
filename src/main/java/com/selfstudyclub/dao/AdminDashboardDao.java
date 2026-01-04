package com.selfstudyclub.dao;

import com.selfstudyclub.model.DashboardStats;
import com.selfstudyclub.model.MemberRow;
import com.selfstudyclub.model.TopCourseRow;
import java.util.List;

public interface AdminDashboardDao {
    DashboardStats loadStats();
    List<TopCourseRow> top5CoursesToday();
    List<MemberRow> membersActiveToday(int page, int pageSize);
}
