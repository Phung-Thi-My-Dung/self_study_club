package com.selfstudyclub.service;

import com.selfstudyclub.dao.LeaderboardDao;
import com.selfstudyclub.model.LeaderboardRow;

import java.util.List;

public class LeaderboardService {
    private final LeaderboardDao dao;
    public LeaderboardService(LeaderboardDao dao) { this.dao = dao; }

    public List<LeaderboardRow> topByTotalCompletions() { return dao.top50ByTotalCompletions(); }
    public List<LeaderboardRow> topByActiveDays() { return dao.top50ByActiveDays(); }
}
