package com.selfstudyclub.dao;

import com.selfstudyclub.model.UserProfile;
import java.util.Optional;

public interface UserProfileDao {
    Optional<UserProfile> findByAccountId(long accountId);
    void upsert(UserProfile profile);
}
