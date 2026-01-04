package com.selfstudyclub.dao;

import com.selfstudyclub.model.LeaderboardRow;
import java.util.List;

public interface LeaderboardDao {
    List<LeaderboardRow> top50ByTotalCompletions();
    List<LeaderboardRow> top50ByActiveDays();
}
